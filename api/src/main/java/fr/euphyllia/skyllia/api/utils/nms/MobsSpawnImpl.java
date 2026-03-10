package fr.euphyllia.skyllia.api.utils.nms;

import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Map;
import java.util.Set;

public interface MobsSpawnImpl {

    Set<CreatureSpawnEvent.SpawnReason> ignoredReasons();

    Map<EntityType, String> supportedBossMobs();

    Map<EntityType, String> supportedHostileAdjacentMobs();

    Map<EntityType, String> supportedHostileMobs();

    Map<EntityType, String> supportedNeutralMobs();

    Map<EntityType, String> supportedOtherMobs();

    Map<EntityType, String> supportedPassiveMobs();
}
