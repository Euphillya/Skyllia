package fr.euphyllia.skyllia.utils.nms.v1_21_R2;

import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import org.bukkit.World;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;

@SuppressWarnings("unchecked")
public final class WorldHeightUtil {

    private static final MappedRegistry<DimensionType> REGISTRY =
            (MappedRegistry<DimensionType>) MinecraftServer.getServer()
                    .registryAccess().lookup(Registries.DIMENSION_TYPE).orElseThrow();

    private WorldHeightUtil() {
    }

    public static Holder<DimensionType> registerCustomDimension(String worldName, WorldConfig config) {
        int minY = config.getWorldMinY() != null ? config.getWorldMinY() : defaultMinY(config.getEnvironment());
        int height = config.getWorldHeight() != null ? config.getWorldHeight() : defaultHeight(config.getEnvironment());
        int logicalHeight = config.getWorldLogicalHeight() != null ? config.getWorldLogicalHeight() : height;

        Holder<DimensionType> baseHolder = getBaseHolder(config.getEnvironment());
        DimensionType old = baseHolder.value();

        DimensionType newDimension = new DimensionType(
                old.fixedTime(), old.hasSkyLight(), old.hasCeiling(),
                old.ultraWarm(), old.natural(), old.coordinateScale(),
                old.bedWorks(), old.respawnAnchorWorks(),
                minY, height, logicalHeight,
                old.infiniburn(), old.effectsLocation(),
                old.ambientLight(), old.monsterSettings()
        );

        ResourceKey<DimensionType> newKey = ResourceKey.create(
                Registries.DIMENSION_TYPE,
                ResourceLocation.fromNamespaceAndPath("skyllia", worldName.toLowerCase().replace(" ", "_"))
        );

        set(MappedRegistry.class, "frozen", REGISTRY, false);
        set(MappedRegistry.class, "unregisteredIntrusiveHolders", REGISTRY, new IdentityHashMap<>());
        REGISTRY.createIntrusiveHolder(newDimension);
        Holder<DimensionType> holder = REGISTRY.register(newKey, newDimension, RegistrationInfo.BUILT_IN);
        set(MappedRegistry.class, "unregisteredIntrusiveHolders", REGISTRY, null);
        set(MappedRegistry.class, "frozen", REGISTRY, true);
        return holder;
    }

    private static Holder<DimensionType> getBaseHolder(World.Environment env) {
        return switch (env) {
            case NETHER -> REGISTRY.getOrThrow(BuiltinDimensionTypes.NETHER);
            case THE_END -> REGISTRY.getOrThrow(BuiltinDimensionTypes.END);
            default -> REGISTRY.getOrThrow(BuiltinDimensionTypes.OVERWORLD);
        };
    }

    private static int defaultMinY(World.Environment env) {
        return switch (env) {
            case NETHER, THE_END -> 0;
            default -> -64;
        };
    }

    private static int defaultHeight(World.Environment env) {
        return switch (env) {
            case NETHER, THE_END -> 256;
            default -> 384;
        };
    }

    private static void set(Class<?> clazz, String fieldName, Object instance, Object value) {
        try {
            Field f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(instance, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("WorldHeightUtil: cannot set field " + clazz.getSimpleName() + "." + fieldName, e);
        }
    }
}
