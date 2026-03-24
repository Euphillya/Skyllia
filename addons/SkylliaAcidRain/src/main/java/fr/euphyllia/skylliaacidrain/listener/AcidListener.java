package fr.euphyllia.skylliaacidrain.listener;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.addons.skylliaacidrain.event.EntityDamageAcidEvent;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliaacidrain.SkylliaAcidRain;
import fr.euphyllia.skylliaacidrain.configuration.AcidConfigLoader;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AcidListener implements Listener {

    private final SkylliaAcidRain plugin;

    private final Map<UUID, LivingEntity> trackedEntities = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledTask> acidTasks = new ConcurrentHashMap<>();

    public AcidListener(SkylliaAcidRain plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityAddWorldEvent(final EntityAddToWorldEvent event) {
        final World world = event.getWorld();
        if (!SkylliaAPI.isWorldSkyblock(world)) {
            return;
        }

        final Entity entity = event.getEntity();
        if (AcidConfigLoader.config.isOnlyPlayer() && !(entity instanceof Player)) {
            return;
        }

        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        startTask(livingEntity);
    }

    public void restartAllTasks() {
        for (Map.Entry<UUID, ScheduledTask> entry : acidTasks.entrySet()) {
            ScheduledTask task = entry.getValue();
            task.cancel();
        }
        acidTasks.clear();

        for (Map.Entry<UUID, LivingEntity> entry : trackedEntities.entrySet()) {
            UUID uuid = entry.getKey();
            LivingEntity entity = entry.getValue();
            if (entity == null) {
                trackedEntities.remove(uuid);
                return;
            }
            entity.getScheduler().run(plugin, scheduledTask -> {
                if (!entity.isDead() && entity.isValid()) {
                    startTask(entity);
                } else {
                    trackedEntities.remove(uuid);
                }
            }, () -> trackedEntities.remove(uuid));
        }
    }

    @EventHandler
    public void onEntityRemoveWorldEvent(final EntityRemoveFromWorldEvent event) {
        final World world = event.getWorld();
        if (!SkylliaAPI.isWorldSkyblock(world)) {
            return;
        }
        final Entity entity = event.getEntity();
        if (AcidConfigLoader.config.isOnlyPlayer() && !(entity instanceof Player)) {
            return;
        }
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        stopTask(livingEntity.getUniqueId());
    }

    private void startTask(LivingEntity entity) {
        UUID uuid = entity.getUniqueId();
        long intervalTick = AcidConfigLoader.config.getDamageIntervalMs();

        ScheduledTask task = entity.getScheduler().runAtFixedRate(
                plugin,
                scheduledTask -> handleAcidTick(entity),
                null,
                intervalTick,
                intervalTick
        );

        if (task != null) {
            trackedEntities.put(uuid, entity);
            acidTasks.put(uuid, task);
        }
    }

    private void stopTask(UUID uuid) {
        ScheduledTask task = acidTasks.remove(uuid);
        if (task != null) task.cancel();
        trackedEntities.remove(uuid);
    }


    private void handleAcidTick(LivingEntity entity) {
        if (entity.isDead()) {
            return;
        }

        if (entity instanceof Player player) {
            if (player.getGameMode() == GameMode.CREATIVE
                    || player.getGameMode() == GameMode.SPECTATOR) return;
        }

        boolean inWater = entity.isInWater();
        boolean inRain = entity.isInRain();

        if (!inWater && !inRain) return;

        applyAcidEffects(entity, inWater);
    }

    private void applyAcidEffects(LivingEntity entity, boolean inWater) {
        double damage = AcidConfigLoader.config.getDamage();

        EntityDamageAcidEvent acidEvent = new EntityDamageAcidEvent(entity, damage, inWater);
        acidEvent.callEvent();
        if (acidEvent.isCancelled()) return;

        entity.damage(acidEvent.getDamage());

        if (AcidConfigLoader.config.isPoisonEnabled()) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.POISON,
                    AcidConfigLoader.config.getPoisonDuration(),
                    AcidConfigLoader.config.getPoisonAmplifier(),
                    false, true, true));
        }
        if (AcidConfigLoader.config.isSlownessEnabled()) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
                    AcidConfigLoader.config.getSlownessDuration(),
                    AcidConfigLoader.config.getSlownessAmplifier(),
                    false, true, true));
        }
        if (AcidConfigLoader.config.isNauseaEnabled()) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA,
                    AcidConfigLoader.config.getNauseaDuration(),
                    AcidConfigLoader.config.getNauseaAmplifier(),
                    false, true, true));
        }

        if (AcidConfigLoader.config.isParticles()) {
            entity.getWorld().spawnParticle(Particle.SPLASH, entity.getLocation().add(0, 1.0, 0), 20, 0.3, 0.5, 0.3, 0.05);
            entity.getWorld().spawnParticle(Particle.DRIPPING_WATER, entity.getLocation().add(0, 1.8, 0), 8, 0.2, 0.1, 0.2, 0.0);
        }

        if (AcidConfigLoader.config.isSound()) {
            try {
                final NamespacedKey key = NamespacedKey.fromString(AcidConfigLoader.config.getSoundName().toLowerCase(Locale.ROOT));
                Sound sound;

                if (key != null) {
                    sound = Bukkit.getUnsafe().get(RegistryKey.SOUND_EVENT, key);
                    if (sound != null) {
                        entity.getWorld().playSound(entity.getLocation(), sound, SoundCategory.HOSTILE, 0.6f, 0.8f);
                    }
                }
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (entity instanceof Player player) {
            Component msg = inWater
                    ? ConfigLoader.language.translate(player.locale(), "addons.acid-island.water-contact", Map.of(), false)
                    : ConfigLoader.language.translate(player.locale(), "addons.acid-island.rain-contact", Map.of(), false);
            player.sendActionBar(msg);
        }
    }
}
