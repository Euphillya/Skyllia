package fr.euphyllia.skyllia.api.utils.nms;

import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
public abstract class ExplosionEntityImpl {

    /**
     * Returns a list of EntityTypes that can cause an explosion as a human entity.
     *
     * @return List of EntityTypes that can cause an explosion as a human entity.
     */
    public abstract List<EntityType> causeHumanEntity();

    /**
     * Returns a list of EntityTypes that can cause an explosion as a mob entity.
     *
     * @return List of EntityTypes that can cause an explosion as a mob entity.
     */
    public abstract List<EntityType> causeMobEntity();
}

