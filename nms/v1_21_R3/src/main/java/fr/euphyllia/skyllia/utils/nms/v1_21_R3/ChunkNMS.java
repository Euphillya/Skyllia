package fr.euphyllia.skyllia.utils.nms.v1_21_R3;

import fr.euphyllia.skyllia.api.utils.nms.ChunkImpl;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.jetbrains.annotations.NotNull;

public class ChunkNMS extends ChunkImpl {

    public void forEachNonAirBlockInChunk(@NotNull World world, int chunkX, int chunkZ, @NotNull ChunkBlockConsumer consumer) {
        final ServerLevel nms = ((CraftWorld) world).getHandle();
        final LevelChunk chunk = nms.getChunkSource().getChunkNow(chunkX, chunkZ);
        if (chunk == null) {
            return;
        }
        final LevelChunkSection[] sections = chunk.getSections();
        if (sections == null || sections.length == 0) {
            return;
        }
        final int baseX = chunkX << 4;
        final int baseZ = chunkZ << 4;
        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            final LevelChunkSection section = sections[sectionIndex];
            if (section == null || section.hasOnlyAir()) {
                continue;
            }
            final int sectionY = chunk.getSectionYFromSectionIndex(sectionIndex);
            for (int ly = 0; ly < 16; ly++) {
                final int worldY = (sectionY << 4) | ly;
                for (int lz = 0; lz < 16; lz++) {
                    final int worldZ = baseZ | lz;
                    for (int lx = 0; lx < 16; lx++) {
                        final int worldX = baseX | lx;
                        final BlockState state = section.getBlockState(lx, ly, lz);
                        if (state.isAir() || state.getBlock() == Blocks.AIR) {
                            continue;
                        }

                        consumer.accept(worldX, worldY, worldZ, state.getBukkitMaterial());
                    }
                }
            }
        }
    }
}
