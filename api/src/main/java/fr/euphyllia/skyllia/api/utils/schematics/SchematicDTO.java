package fr.euphyllia.skyllia.api.utils.schematics;

import java.util.List;

public class SchematicDTO {
    public int version = 1;
    public Vec3i origin;
    public Size3i size;
    public List<String> palette;
    public List<int[]> blocks;
    public List<BlockEntityDTO> blockEntities;
    public List<EntityDTO> entities;
}
