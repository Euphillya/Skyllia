package fr.euphyllia.skyllia.utils.nms.v1_21_R6;

import org.bukkit.entity.EntityType;

import java.util.List;

public class ExplosionEntityImpl extends fr.euphyllia.skyllia.api.utils.nms.ExplosionEntityImpl {

    @Override
    public List<EntityType> causeHumanEntity() {
        return List.of(EntityType.TNT, EntityType.TNT_MINECART);
    }

    @Override
    public List<EntityType> causeMobEntity() {
        return List.of(EntityType.CREEPER, EntityType.GHAST, EntityType.ENDER_DRAGON, EntityType.WITHER, EntityType.BLAZE,
                EntityType.WITHER_SKULL, EntityType.FIREBALL, EntityType.SMALL_FIREBALL);
    }
}
