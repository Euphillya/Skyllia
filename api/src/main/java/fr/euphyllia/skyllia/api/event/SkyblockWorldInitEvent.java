package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicSetting;
import org.bukkit.Location;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired (async) after a schematic has been pasted for one world of a newly
 * created island.
 *
 * <p>Since an island can span multiple worlds (overworld, nether, end…),
 * this event is fired <em>once per world</em> rather than once per island.
 * Each firing carries the exact center location for that world, including
 * the Y coordinate set by the schematic configuration.</p>
 *
 * <h3>Event order during island creation</h3>
 * <ol>
 *   <li>{@link SkyblockCreateEvent} — island registered in DB, no worlds yet</li>
 *   <li>{@link SkyblockWorldInitEvent} × N — one per world, after each paste</li>
 *   <li>{@link SkyblockLoadEvent} — fired after the primary world is ready</li>
 * </ol>
 */
public class SkyblockWorldInitEvent extends IslandEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final String worldName;
    private final Location centerLocation;
    private final SchematicSetting schematicSetting;
    private final boolean primaryWorld;

    public SkyblockWorldInitEvent(@NotNull Island island,
                                  @NotNull String worldName,
                                  @NotNull Location centerLocation,
                                  @NotNull SchematicSetting schematicSetting,
                                  boolean primaryWorld) {
        super(island, true);
        this.worldName = worldName;
        this.centerLocation = centerLocation;
        this.schematicSetting = schematicSetting;
        this.primaryWorld = primaryWorld;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public String getWorldName() {
        return worldName;
    }

    public Location getCenterLocation() {
        return centerLocation;
    }

    public SchematicSetting getSchematicSetting() {
        return schematicSetting;
    }

    public boolean isPrimaryWorld() {
        return primaryWorld;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
