package fr.euphyllia.skyllia.api.addons.skylliaacidrain.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link LivingEntity} is about to receive acid damage
 * from water or rain in a Skyblock world.
 *
 * <p>This event is fired by the <b>SkylliaAcidRain</b> addon before any
 * damage or potion effects are applied to the entity.</p>
 *
 * <p>Cancelling this event prevents damage and potion effects from being applied.
 * Particles, sounds, and the ActionBar message are unaffected by cancellation.</p>
 *
 * <p>The damage amount can also be modified via {@link #setDamage(double)}
 * without cancelling the event entirely.</p>
 *
 */
public class EntityDamageAcidEvent extends EntityEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private double damage;
    private final boolean inWater;
    private boolean cancelled;

    public EntityDamageAcidEvent(@NotNull LivingEntity entity, double damage, boolean inWater) {
        super(entity);
        this.damage = damage;
        this.inWater = inWater;
        this.cancelled = false;
    }

    /**
     * Returns the living entity receiving acid damage.
     */
    @Override
    public @NotNull LivingEntity getEntity() {
        return (LivingEntity) super.getEntity();
    }

    /**
     * Gets the amount of damage that will be dealt.
     */
    public double getDamage() {
        return damage;
    }

    /**
     * Sets the amount of damage to deal. Set to 0 or below to cancel damage only.
     */
    public void setDamage(double damage) {
        this.damage = damage;
    }

    /**
     * Returns true if the entity is in water, false if exposed to acid rain.
     */
    public boolean isInWater() {
        return inWater;
    }

    /**
     * Returns true if the entity is exposed to acid rain (not water).
     */
    public boolean isInRain() {
        return !inWater;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Cancels the event — no damage, no potion effects will be applied.
     */
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
