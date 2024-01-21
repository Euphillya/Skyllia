package fr.euphyllia.skyllia.api.utils.nms;

import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.world.WorldFeedback;
import org.bukkit.World;
import org.bukkit.WorldCreator;

public abstract class WorldNMS {

    public abstract WorldFeedback.FeedbackWorld createWorld(WorldCreator creator);

    public abstract void resetChunk(World craftWorld, Position position);

}
