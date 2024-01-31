package fr.euphyllia.skyllia.utils;

import org.bukkit.entity.*;

public class EntityUtils {

    public static boolean isMonster(EntityType entityType) {
        Class<? extends Entity> entityClass = entityType.getEntityClass();
        return entityClass != null && Monster.class.isAssignableFrom(entityClass);
    }

    public static boolean isPassif(EntityType entityType) {
        Class<? extends Entity> entityClass = entityType.getEntityClass();
        return entityClass != null && (Animals.class.isAssignableFrom(entityType.getEntityClass()) ||
                Ambient.class.isAssignableFrom(entityType.getEntityClass()));
    }
}
