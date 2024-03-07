package fr.euphyllia.skyllia.utils.nms.v1_18_R1;

import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.world.WorldFeedback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;


public class WorldNMS extends fr.euphyllia.skyllia.api.utils.nms.WorldNMS {

    static GameType getGameType(GameMode gameMode) {
        return switch (gameMode) {
            case SURVIVAL -> GameType.SURVIVAL;
            case CREATIVE -> GameType.CREATIVE;
            case ADVENTURE -> GameType.ADVENTURE;
            case SPECTATOR -> GameType.SPECTATOR;
        };
    }

    @Override
    public WorldFeedback.FeedbackWorld createWorld(WorldCreator creator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetChunk(World craftWorld, Position position) {
        final ServerLevel serverLevel = ((CraftWorld) craftWorld).getHandle();
        io.papermc.paper.util.TickThread.ensureTickThread("Cannot regenerate chunk asynchronously");
        final net.minecraft.server.level.ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
        final ChunkPos chunkPos = new ChunkPos(position.x(), position.z());
        final net.minecraft.world.level.chunk.LevelChunk levelChunk = serverChunkCache.getChunk(chunkPos.x, chunkPos.z, true);
        final Iterable<BlockPos> blockPosIterable = BlockPos.betweenClosed(chunkPos.getMinBlockX(), serverLevel.getMinBuildHeight(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), serverLevel.getMaxBuildHeight() - 1, chunkPos.getMaxBlockZ());
        for (Entity entity : serverLevel.getChunkEntities(position.x(), position.z())) {
            if (entity instanceof Player) continue;
            entity.remove();
        }
        for (final BlockPos blockPos : blockPosIterable) {
            levelChunk.removeBlockEntity(blockPos);
            serverLevel.setBlock(blockPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 16);
        }
        for (final BlockPos blockPos : blockPosIterable) { // Fix memory issue client
            serverChunkCache.blockChanged(blockPos);
        }
    }
}
