package fr.euphyllia.skyllia.utils.nms.v26_3;

import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
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

    public static Holder<DimensionType> registerCustomDimension(final String worldName, final WorldConfig config) {
        final int minY = config.getWorldMinY() != null ? config.getWorldMinY() : defaultMinY(config.getEnvironment());
        final int height = config.getWorldHeight() != null ? config.getWorldHeight() : defaultHeight(config.getEnvironment());
        final int logicalHeight = config.getWorldLogicalHeight() != null ? config.getWorldLogicalHeight() : height;

        final Holder<DimensionType> baseHolder = getBaseHolder(config.getEnvironment());
        final DimensionType old = baseHolder.value();

        final EnvironmentAttributeMap.Entry<Float, ?> entry = old.attributes().get(EnvironmentAttributes.CLOUD_HEIGHT);
        final Float originalCloudHeight = entry == null ? null : entry.applyModifier(0f);
        final EnvironmentAttributeMap.Builder newAttributesBuilder = EnvironmentAttributeMap.builder()
                .putAll(old.attributes());
        if (originalCloudHeight != null) {
            newAttributesBuilder.set(EnvironmentAttributes.CLOUD_HEIGHT, originalCloudHeight);
        }

        final DimensionType newDimension = new DimensionType(
                old.hasFixedTime(), old.hasSkyLight(), old.hasCeiling(), old.hasEnderDragonFight(), old.coordinateScale(),
                minY, height, logicalHeight,
                old.infiniburn(), old.ambientLight(), old.monsterSettings(),
                old.skybox(), old.cardinalLightType(), newAttributesBuilder.build(), old.timelines(), old.defaultClock()
        );

        final ResourceKey<DimensionType> newKey = ResourceKey.create(
                Registries.DIMENSION_TYPE,
                Identifier.fromNamespaceAndPath("skyllia", worldName.toLowerCase().replace(" ", "_"))
        );

        set(MappedRegistry.class, "frozen", REGISTRY, false);
        set(MappedRegistry.class, "unregisteredIntrusiveHolders", REGISTRY, new IdentityHashMap<>());
        REGISTRY.createIntrusiveHolder(newDimension);
        final Holder<DimensionType> holder = REGISTRY.register(newKey, newDimension, RegistrationInfo.BUILT_IN);
        set(MappedRegistry.class, "unregisteredIntrusiveHolders", REGISTRY, null);
        set(MappedRegistry.class, "frozen", REGISTRY, true);
        return holder;
    }

    private static Holder<DimensionType> getBaseHolder(final World.Environment env) {
        return switch (env) {
            case NETHER -> REGISTRY.getOrThrow(BuiltinDimensionTypes.NETHER);
            case THE_END -> REGISTRY.getOrThrow(BuiltinDimensionTypes.END);
            default -> REGISTRY.getOrThrow(BuiltinDimensionTypes.OVERWORLD);
        };
    }

    private static int defaultMinY(final World.Environment env) {
        return switch (env) {
            case NETHER, THE_END -> 0;
            default -> -64;
        };
    }

    private static int defaultHeight(final World.Environment env) {
        return switch (env) {
            case NETHER, THE_END -> 256;
            default -> 384;
        };
    }

    private static void set(final Class<?> clazz, final String fieldName, final Object instance, final Object value) {
        try {
            final Field f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(instance, value);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("WorldHeightUtil: cannot set field " + clazz.getSimpleName() + "." + fieldName, e);
        }
    }
}
