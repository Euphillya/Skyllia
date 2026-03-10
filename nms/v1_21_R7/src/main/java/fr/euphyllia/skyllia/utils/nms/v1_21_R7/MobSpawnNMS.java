package fr.euphyllia.skyllia.utils.nms.v1_21_R7;

import fr.euphyllia.skyllia.api.utils.nms.MobsSpawnImpl;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Map;
import java.util.Set;

public class MobSpawnNMS implements MobsSpawnImpl {

    private static final Set<CreatureSpawnEvent.SpawnReason> IGNORED_REASONS;
    private static final Map<EntityType, String> HOSTILE_MOBS;
    private static final Map<EntityType, String> BOSS_MOBS;
    private static final Map<EntityType, String> HOSTILE_ADJACENT_MOBS;
    private static final Map<EntityType, String> NEUTRAL_MOBS;
    private static final Map<EntityType, String> OTHER_MOBS;
    private static final Map<EntityType, String> PASSIVE_MOBS;

    static {
        IGNORED_REASONS = Set.of(
                CreatureSpawnEvent.SpawnReason.SPAWNER,
                CreatureSpawnEvent.SpawnReason.SPAWNER_EGG,
                CreatureSpawnEvent.SpawnReason.CUSTOM,
                CreatureSpawnEvent.SpawnReason.COMMAND,
                CreatureSpawnEvent.SpawnReason.TRIAL_SPAWNER
        );

        BOSS_MOBS = Map.of(
                EntityType.ENDER_DRAGON, "ender_dragon",
                EntityType.WITHER, "wither"
        );

        HOSTILE_ADJACENT_MOBS = Map.of(
                EntityType.SKELETON_HORSE, "skeleton_horse",
                EntityType.ZOMBIE_HORSE, "zombie_horse"
        );

        HOSTILE_MOBS = Map.ofEntries(
                Map.entry(EntityType.BLAZE, "blaze"),
                Map.entry(EntityType.CREEPER, "creeper"),
                Map.entry(EntityType.ELDER_GUARDIAN, "elder_guardian"),
                Map.entry(EntityType.ENDERMITE, "endermite"),
                Map.entry(EntityType.EVOKER, "evoker"),
                Map.entry(EntityType.GHAST, "ghast"),
                Map.entry(EntityType.GUARDIAN, "guardian"),
                Map.entry(EntityType.HOGLIN, "hoglin"),
                Map.entry(EntityType.HUSK, "husk"),
                Map.entry(EntityType.MAGMA_CUBE, "magma_cube"),
                Map.entry(EntityType.PHANTOM, "phantom"),
                Map.entry(EntityType.PIGLIN_BRUTE, "piglin_brute"),
                Map.entry(EntityType.PILLAGER, "pillager"),
                Map.entry(EntityType.RAVAGER, "ravager"),
                Map.entry(EntityType.SHULKER, "shulker"),
                Map.entry(EntityType.SILVERFISH, "silverfish"),
                Map.entry(EntityType.SKELETON, "skeleton"),
                Map.entry(EntityType.SLIME, "slime"),
                Map.entry(EntityType.STRAY, "stray"),
                Map.entry(EntityType.VEX, "vex"),
                Map.entry(EntityType.VINDICATOR, "vindicator"),
                Map.entry(EntityType.WARDEN, "warden"),
                Map.entry(EntityType.WITCH, "witch"),
                Map.entry(EntityType.WITHER_SKELETON, "wither_skeleton"),
                Map.entry(EntityType.ZOGLIN, "zoglin"),
                Map.entry(EntityType.ZOMBIE, "zombie"),
                Map.entry(EntityType.ZOMBIE_VILLAGER, "zombie_villager"),
                Map.entry(EntityType.BOGGED, "bogged"),
                Map.entry(EntityType.BREEZE, "breeze"),
                Map.entry(EntityType.CREAKING, "creaking"),
                Map.entry(EntityType.PARCHED, "parched")
        );

        NEUTRAL_MOBS = Map.ofEntries(
                Map.entry(EntityType.BEE, "bee"),
                Map.entry(EntityType.CAVE_SPIDER, "cave_spider"),
                Map.entry(EntityType.DOLPHIN, "dolphin"),
                Map.entry(EntityType.DROWNED, "drowned"),
                Map.entry(EntityType.ENDERMAN, "enderman"),
                Map.entry(EntityType.FOX, "fox"),
                Map.entry(EntityType.GOAT, "goat"),
                Map.entry(EntityType.IRON_GOLEM, "iron_golem"),
                Map.entry(EntityType.LLAMA, "llama"),
                Map.entry(EntityType.NAUTILUS, "nautilus"),
                Map.entry(EntityType.PANDA, "panda"),
                Map.entry(EntityType.PIGLIN, "piglin"),
                Map.entry(EntityType.POLAR_BEAR, "polar_bear"),
                Map.entry(EntityType.PUFFERFISH, "pufferfish"),
                Map.entry(EntityType.SPIDER, "spider"),
                Map.entry(EntityType.TRADER_LLAMA, "trader_llama"),
                Map.entry(EntityType.WOLF, "wolf"),
                Map.entry(EntityType.ZOMBIE_NAUTILUS, "zombie_nautilus"),
                Map.entry(EntityType.ZOMBIFIED_PIGLIN, "zombified_piglin")
        );

        OTHER_MOBS = Map.of(
                EntityType.GIANT, "giant",
                EntityType.ILLUSIONER, "illusioner",
                EntityType.ARMOR_STAND, "armor_stand",
                EntityType.MANNEQUIN, "mannequin"
        );

        PASSIVE_MOBS = Map.ofEntries(
                Map.entry(EntityType.ALLAY, "allay"),
                Map.entry(EntityType.AXOLOTL, "axolotl"),
                Map.entry(EntityType.BAT, "bat"),
                Map.entry(EntityType.CAT, "cat"),
                Map.entry(EntityType.CHICKEN, "chicken"),
                Map.entry(EntityType.COD, "cod"),
                Map.entry(EntityType.COW, "cow"),
                Map.entry(EntityType.DONKEY, "donkey"),
                Map.entry(EntityType.FROG, "frog"),
                Map.entry(EntityType.GLOW_SQUID, "glow_squid"),
                Map.entry(EntityType.HORSE, "horse"),
                Map.entry(EntityType.MOOSHROOM, "mooshroom"),
                Map.entry(EntityType.MULE, "mule"),
                Map.entry(EntityType.OCELOT, "ocelot"),
                Map.entry(EntityType.PARROT, "parrot"),
                Map.entry(EntityType.PIG, "pig"),
                Map.entry(EntityType.RABBIT, "rabbit"),
                Map.entry(EntityType.SALMON, "salmon"),
                Map.entry(EntityType.SHEEP, "sheep"),
                Map.entry(EntityType.SNIFFER, "sniffer"),
                Map.entry(EntityType.SNOW_GOLEM, "snow_golem"),
                Map.entry(EntityType.SQUID, "squid"),
                Map.entry(EntityType.STRIDER, "strider"),
                Map.entry(EntityType.TADPOLE, "tadpole"),
                Map.entry(EntityType.TROPICAL_FISH, "tropical_fish"),
                Map.entry(EntityType.TURTLE, "turtle"),
                Map.entry(EntityType.VILLAGER, "villager"),
                Map.entry(EntityType.WANDERING_TRADER, "wandering_villager"),
                Map.entry(EntityType.ARMADILLO, "armadillo"),
                Map.entry(EntityType.CAMEL, "camel"),
                Map.entry(EntityType.HAPPY_GHAST, "happy_ghast"),
                Map.entry(EntityType.COPPER_GOLEM, "copper_golem")
        );
    }

    @Override
    public Set<CreatureSpawnEvent.SpawnReason> ignoredReasons() {
        return IGNORED_REASONS;
    }

    @Override
    public Map<EntityType, String> supportedBossMobs() {
        return BOSS_MOBS;
    }

    @Override
    public Map<EntityType, String> supportedHostileAdjacentMobs() {
        return HOSTILE_ADJACENT_MOBS;
    }

    @Override
    public Map<EntityType, String> supportedHostileMobs() {
        return HOSTILE_MOBS;
    }


    @Override
    public Map<EntityType, String> supportedNeutralMobs() {
        return NEUTRAL_MOBS;
    }

    @Override
    public Map<EntityType, String> supportedOtherMobs() {
        return OTHER_MOBS;
    }

    @Override
    public Map<EntityType, String> supportedPassiveMobs() {
        return PASSIVE_MOBS;
    }
}