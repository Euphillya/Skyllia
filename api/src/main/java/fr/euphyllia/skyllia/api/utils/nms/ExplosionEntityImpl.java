package fr.euphyllia.skyllia.api.utils.nms;

import org.bukkit.entity.EntityType;

import java.util.List;

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

