package fr.euphyllia.skyllia.utils.nms.v1_20_R1;

import org.bukkit.entity.EntityType;

import java.util.List;

public class ExplosionEntityImpl extends fr.euphyllia.skyllia.api.utils.nms.ExplosionEntityImpl {

    @Override
    public List<EntityType> causeHumanEntity() {
        return List.of(EntityType.PRIMED_TNT, EntityType.MINECART_TNT);
    }

    @Override
    public List<EntityType> causeMobEntity() {
        return List.of(EntityType.CREEPER, EntityType.GHAST, EntityType.ENDER_DRAGON, EntityType.WITHER, EntityType.BLAZE,
                EntityType.WITHER_SKULL, EntityType.FIREBALL, EntityType.SMALL_FIREBALL);
    }
}
