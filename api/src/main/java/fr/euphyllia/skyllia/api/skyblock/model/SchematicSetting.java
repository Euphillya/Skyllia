package fr.euphyllia.skyllia.api.skyblock.model;

import org.jetbrains.annotations.Nullable;

public record SchematicSetting(double height, String schematicFile, boolean ignoreAirBlocks, boolean copyEntities,
                               String plugin, @Nullable Integer minBuildHeight, @Nullable Integer maxBuildHeight) {
}
