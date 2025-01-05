package fr.euphyllia.skyllia.api.event.players;

import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event triggered when a player interacts with a portal to change worlds within the Skyblock environment.
 * <p>
 * This event is called asynchronously when a player touches a portal, initiating a world change process.
 * It allows plugins to perform actions before or after the world transition occurs, such as validating the
 * transition, modifying world configurations, or enforcing additional restrictions. Additionally, this event
 * can be cancelled by other plugins to prevent the world change from taking place.
 * </p>
 * <p>
 * Note: This event is not triggered if the warp name is "home", as "home" warps are handled differently.
 * </p>
 * <p>
 * To handle this event, plugins must register an event listener and implement the appropriate handler.
 * </p>
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * import fr.euphyllia.skyllia.api.event.players.PlayerPrepareChangeWorldSkyblockEvent;
 * import fr.euphyllia.skyllia.api.configuration.WorldConfig;
 * import fr.euphyllia.skyllia.api.skyblock.Island;
 * import org.bukkit.entity.Player;
 * import org.bukkit.event.EventHandler;
 * import org.bukkit.event.Listener;
 *
 * public class PortalInteractionListener implements Listener {
 *
 *     @EventHandler
 *     public void onPlayerPrepareChangeWorld(PlayerPrepareChangeWorldSkyblockEvent event) {
 *         Player player = event.getPlayer();
 *         PlayerPrepareChangeWorldSkyblockEvent.PortalType portalType = event.getPortalType();
 *         WorldConfig worldConfig = event.getWorldConfig();
 *
 *         // Example 1: Logging the portal interaction
 *         player.sendMessage("You have touched a " + portalType + " portal.");
 *         System.out.println("Player " + player.getName() + " is attempting to change world via " + portalType + " portal.");
 *
 *         // Example 2: Modifying world configuration before the transition
 *         if (portalType == PlayerPrepareChangeWorldSkyblockEvent.PortalType.NETHER) {
 *             worldConfig.setDifficulty("Hard");
 *             player.sendMessage("Nether world difficulty set to Hard.");
 *         }
 *
 *         // Example 3: Preventing the world change under certain conditions
 *         if (player.hasPermission("skyblock.portal.override")) {
 *             player.sendMessage("You do not have permission to change to this world.");
 *             event.setCancelled(true);
 *         }
 *     }
 * }
 * }</pre>
 *
 * @see Island
 * @see WorldConfig
 */
public class PlayerPrepareChangeWorldSkyblockEvent extends Event implements Cancellable {

    /**
     * The handler list for this event.
     */
    private static final HandlerList handlerList = new HandlerList();

    /**
     * The player who is interacting with the portal.
     */
    private final Player player;

    /**
     * The type of portal being used for the world change.
     */
    private final PortalType portalType;

    /**
     * The world configuration associated with the world change.
     */
    private final WorldConfig worldConfig;

    /**
     * Indicates whether this event has been cancelled.
     */
    private boolean cancel = false;

    /**
     * Constructs a new {@code PlayerPrepareChangeWorldSkyblockEvent}.
     *
     * @param player The player who touched the portal.
     * @param wc     The world configuration for the target world.
     * @param pt     The type of the portal being used.
     */
    public PlayerPrepareChangeWorldSkyblockEvent(Player player, WorldConfig wc, PortalType pt) {
        super(false);
        this.player = player;
        this.worldConfig = wc;
        this.portalType = pt;
    }

    /**
     * Retrieves the handler list for this event.
     *
     * @return The handler list.
     */
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    /**
     * Retrieves the handlers associated with this event.
     *
     * @return The handler list.
     */
    @Override
    public @NotNull HandlerList getHandlers() {
        return getHandlerList();
    }

    /**
     * Retrieves the player who is interacting with the portal.
     *
     * @return The player involved in the portal interaction.
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Retrieves the type of portal being used for the world change.
     *
     * @return The portal type.
     */
    public PortalType getPortalType() {
        return this.portalType;
    }

    /**
     * Retrieves the world configuration associated with the world change.
     *
     * @return The world configuration.
     */
    public WorldConfig getWorldConfig() {
        return this.worldConfig;
    }

    /**
     * Checks whether this event has been cancelled. A cancelled event will prevent the world change
     * from occurring, but the event will still pass to other plugins.
     *
     * @return {@code true} if this event is cancelled, {@code false} otherwise.
     */
    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    /**
     * Sets the cancellation state of this event. A cancelled event will prevent the world change
     * from occurring, but the event will still pass to other plugins.
     *
     * @param cancel {@code true} to cancel this event, {@code false} to allow it to proceed.
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    /**
     * Represents the types of portals that a player can interact with to change worlds.
     */
    public enum PortalType {
        /**
         * Represents a Nether portal.
         */
        NETHER,

        /**
         * Represents an End portal.
         */
        END
    }
}
