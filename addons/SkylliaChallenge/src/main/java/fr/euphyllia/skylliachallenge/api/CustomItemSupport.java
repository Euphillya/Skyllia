package fr.euphyllia.skylliachallenge.api;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

/**
 * Bridge to a custom-item provider (for example Nexo or Oraxen).
 * <p>
 * Implementations let the challenge system recognize and resolve items that do not exist in vanilla
 * Minecraft. Each provider owns a {@link #getNamespace() namespace} (such as {@code "nexo"} or
 * {@code "oraxen"}); a custom item is then referenced in configuration as {@code namespace:id}, and
 * the matching {@code CustomItemSupport} is responsible for translating that reference into a real
 * {@link ItemStack} and for testing whether a given item or block corresponds to a custom id.
 * </p>
 *
 * <p>
 * Requirements that accept custom items (for example {@code ITEM:nexo:custom_wheat} or
 * {@code BLOCKBREAK:oraxen:custom_ore}) rely on these methods to validate progress against the
 * provider's items rather than vanilla {@code Material}s.
 * </p>
 */
public interface CustomItemSupport {

    /**
     * Returns the namespace this provider is responsible for.
     * <p>
     * It is the prefix used in configuration references of the form {@code namespace:id}
     * (e.g. {@code "nexo"} for {@code nexo:custom_wheat}).
     * </p>
     *
     * @return the provider namespace, never {@code null}
     */
    String getNamespace();

    /**
     * Checks whether the given item stack is the custom item identified by {@code id}.
     *
     * @param item the item stack to test (may be {@code null} or empty)
     * @param id   the custom item id within this provider's namespace
     * @return {@code true} if {@code item} is the custom item {@code id}, {@code false} otherwise
     */
    boolean matches(ItemStack item, String id);

    /**
     * Checks whether the given block is the custom block identified by {@code id}.
     *
     * @param block the block to test (may be {@code null})
     * @param id    the custom block id within this provider's namespace
     * @return {@code true} if {@code block} is the custom block {@code id}, {@code false} otherwise
     */
    boolean matchesBlock(Block block, String id);

    /**
     * Builds a fresh {@link ItemStack} for the custom item identified by {@code id}.
     *
     * @param id the custom item id within this provider's namespace
     * @return the corresponding {@link ItemStack}, or {@code null} if the provider has no item with
     * that id
     */
    ItemStack getItemFromId(String id);
}