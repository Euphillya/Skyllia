package fr.euphyllia.skylliabackup.manager;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.coordinate.RegionCoordinate;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skylliabackup.SkylliaBackup;
import fr.euphyllia.skylliabackup.configuration.BackupConfigManager;
import org.bukkit.Bukkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupManager {

    private static final Logger log = LoggerFactory.getLogger(BackupManager.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private final SkylliaBackup plugin;
    private final BackupConfigManager config;
    private final Map<UUID, Long> playerCooldowns = new HashMap<>();

    public BackupManager(SkylliaBackup plugin, BackupConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public long getRemainingCooldown(UUID playerId) {
        long cooldown = config.getPlayerCooldownSeconds();
        if (cooldown <= 0) return 0;
        Long last = playerCooldowns.get(playerId);
        if (last == null) return 0;
        long elapsed = (System.currentTimeMillis() / 1000L) - last;
        return Math.max(0, cooldown - elapsed);
    }

    public void stampCooldown(UUID playerId) {
        playerCooldowns.put(playerId, System.currentTimeMillis() / 1000L);
    }

    public File backupIsland(Island island, String trigger) {
        UUID islandId = island.getId();
        RegionCoordinate pos = island.getRegionCoordinate();

        List<WorldConfig> worlds = SkylliaAPI.getRegisteredWorlds();
        if (worlds == null || worlds.isEmpty()) {
            log.warn("No worlds registered in Skyllia — cannot backup island {}", islandId);
            return null;
        }

        List<File> regionFiles = new ArrayList<>();
        for (WorldConfig worldCfg : worlds) {
            String worldName = worldCfg.getWorldName();
            List<RegionCoordinate> regions = RegionHelper.getRegionsWithinBlockRange(pos, (int) island.getSize());
            for (RegionCoordinate region : regions) {
                File mca = getRegionFile(worldName, region);
                if (mca != null && mca.exists()) {
                    regionFiles.add(mca);
                }
            }
        }

        if (regionFiles.isEmpty()) {
            log.warn("No region files found for island {}", islandId);
            return null;
        }

        String ownerName = islandId.toString().substring(0, 8);
        for (Players p : island.getMembers()) {
            if (p.getRoleType() == RoleType.OWNER) {
                ownerName = p.getLastKnowName();
                break;
            }
        }

        File backupRoot = new File(config.getBackupFolder());
        File islandDir = new File(backupRoot, islandId.toString());
        islandDir.mkdirs();

        String timestamp = DATE_FMT.format(LocalDateTime.now());
        String zipName = String.format("backup_%s_%s_%s.zip", ownerName, trigger, timestamp);
        File zipFile = new File(islandDir, zipName);

        try {
            createZip(zipFile, regionFiles, islandId.toString());
        } catch (IOException e) {
            log.error("Failed to create backup ZIP for island {}", islandId, e);
            return null;
        }

        rotateBackups(islandDir);

        if (config.isUploadEnabled()) {
            final UUID finalIslandId = islandId;
            Bukkit.getAsyncScheduler().runNow(plugin, task -> uploadBackup(zipFile, finalIslandId));
        }

        log.info("Backup created: {}", zipFile.getAbsolutePath());
        return zipFile;
    }

    public int backupAllIslands() {
        List<Island> islands = SkylliaAPI.getAllIslandsValid();
        if (islands == null || islands.isEmpty()) return 0;
        int count = 0;
        for (Island island : islands) {
            if (!island.isDisable()) {
                if (backupIsland(island, "admin-all") != null) count++;
            }
        }
        return count;
    }

    private File getRegionFile(String worldName, RegionCoordinate region) {
        org.bukkit.World world = Bukkit.getWorld(worldName);
        if (world == null) {
            log.warn("World '{}' is not loaded, skipping", worldName);
            return null;
        }
        File regionDir = new File(world.getWorldFolder(), "region");
        return new File(regionDir, "r." + region.x() + "." + region.z() + ".mca");
    }

    private void createZip(File zipFile, List<File> regionFiles, String islandId) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            ZipEntry meta = new ZipEntry(islandId + "/backup-info.txt");
            zos.putNextEntry(meta);
            String info = "Island ID: " + islandId + "\nCreated at: " + LocalDateTime.now() + "\nRegion files: " + regionFiles.size() + "\n";
            zos.write(info.getBytes());
            zos.closeEntry();

            for (File regionFile : regionFiles) {
                // Structure dans le ZIP : <islandId>/<worldName>/region/r.X.Z.mca
                String relativePath = islandId + "/" + relativize(Bukkit.getWorldContainer(), regionFile);
                ZipEntry entry = new ZipEntry(relativePath.replace(File.separatorChar, '/'));
                zos.putNextEntry(entry);
                try (FileInputStream fis = new FileInputStream(regionFile)) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = fis.read(buf)) > 0) zos.write(buf, 0, len);
                }
                zos.closeEntry();
            }
        }
    }

    private String relativize(File base, File target) {
        return base.toURI().relativize(target.toURI()).getPath();
    }

    private void rotateBackups(File islandDir) {
        int max = config.getMaxBackupsPerIsland();
        if (max <= 0) return;
        File[] zips = islandDir.listFiles((dir, name) -> name.endsWith(".zip"));
        if (zips == null || zips.length <= max) return;
        Arrays.sort(zips, Comparator.comparingLong(File::lastModified));
        int toDelete = zips.length - max;
        for (int i = 0; i < toDelete; i++) {
            if (zips[i].delete()) log.debug("Rotated old backup: {}", zips[i].getName());
        }
    }

    private void uploadBackup(File zipFile, UUID islandId) {
        String boundary = "----SkylliaBackup" + System.currentTimeMillis();
        try {
            URL url = new URL(config.getUploadUrl());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setRequestProperty("User-Agent", "SkylliaBackup/" + plugin.getPluginMeta().getVersion());
            if (!config.getUploadToken().isBlank()) {
                conn.setRequestProperty("Authorization", "Bearer " + config.getUploadToken());
            }

            try (OutputStream out = conn.getOutputStream()) {
                writeField(out, boundary, "island_id", islandId.toString());
                writeFilePart(out, boundary, "file", zipFile);
                out.write(("\r\n--" + boundary + "--\r\n").getBytes());
            }

            int status = conn.getResponseCode();
            if (status == 200 || status == 201) {
                log.info("Backup uploaded for island {}: HTTP {}", islandId, status);
            } else {
                log.warn("Upload returned HTTP {} for island {}", status, islandId);
            }
            conn.disconnect();
        } catch (Exception e) {
            log.error("Failed to upload backup for island {}", islandId, e);
        }
    }

    private void writeField(OutputStream out, String boundary, String name, String value) throws IOException {
        out.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + name + "\"\r\n\r\n" + value + "\r\n").getBytes());
    }

    private void writeFilePart(OutputStream out, String boundary, String fieldName, File file) throws IOException {
        out.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + file.getName() + "\"\r\nContent-Type: application/zip\r\n\r\n").getBytes());
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = fis.read(buf)) > 0) out.write(buf, 0, len);
        }
        out.write("\r\n".getBytes());
    }
}