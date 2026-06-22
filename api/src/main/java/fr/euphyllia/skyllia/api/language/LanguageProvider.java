package fr.euphyllia.skyllia.api.language;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;

/**
 * Provides translation and messaging capabilities for Skyllia.
 * <p>
 * Implementations resolve translation keys against loaded language files,
 * apply placeholders, and optionally prepend the configured message prefix.
 * Falls back to the default locale ({@code en_GB}) when no translation is
 * found for the requested locale.
 */
public interface LanguageProvider {

    /**
     * Translates a key for the given locale, applying placeholders and the message prefix.
     *
     * @param locale       the target locale
     * @param key          the translation key (e.g. {@code "island.permission.quickshop.shop.create.name"})
     * @param placeholders a map of placeholder tokens to their replacement values
     * @return the translated {@link Component}
     */
    Component translate(Locale locale, String key, Map<String, String> placeholders);

    /**
     * Translates a key for the given locale, applying placeholders, with optional prefix.
     *
     * @param locale       the target locale
     * @param key          the translation key
     * @param placeholders a map of placeholder tokens to their replacement values
     * @param usePrefix    whether to prepend the configured message prefix
     * @return the translated {@link Component}
     */
    Component translate(Locale locale, String key, Map<String, String> placeholders, boolean usePrefix);

    /**
     * Translates a key using the default locale ({@code en_GB}), applying placeholders.
     *
     * @param key          the translation key
     * @param placeholders a map of placeholder tokens to their replacement values
     * @return the translated {@link Component}
     */
    Component translate(String key, Map<String, String> placeholders);

    /**
     * Translates a key using the player's own locale, with no placeholders.
     *
     * @param player the player whose locale is used
     * @param key    the translation key
     * @return the translated {@link Component}
     */
    Component translate(Player player, String key);

    /**
     * Translates a key using the player's own locale, applying placeholders.
     *
     * @param player       the player whose locale is used
     * @param key          the translation key
     * @param placeholders a map of placeholder tokens to their replacement values
     * @return the translated {@link Component}
     */
    Component translate(Player player, String key, Map<String, String> placeholders);

    /**
     * Sends a translated message to a player using their locale, applying placeholders.
     *
     * @param player       the recipient
     * @param key          the translation key
     * @param placeholders a map of placeholder tokens to their replacement values
     */
    void sendMessage(Player player, String key, Map<String, String> placeholders);

    /**
     * Sends a translated message to a player using their locale, applying placeholders,
     * with optional prefix.
     *
     * @param player       the recipient
     * @param key          the translation key
     * @param placeholders a map of placeholder tokens to their replacement values
     * @param usePrefix    whether to prepend the configured message prefix
     */
    void sendMessage(Player player, String key, Map<String, String> placeholders, boolean usePrefix);

    /**
     * Sends a translated message to a {@link CommandSender} using the default locale,
     * applying placeholders.
     *
     * @param sender       the recipient (player, console, or command block)
     * @param key          the translation key
     * @param placeholders a map of placeholder tokens to their replacement values
     */
    void sendMessage(CommandSender sender, String key, Map<String, String> placeholders);

    /**
     * Sends a translated message to a player using their locale, with no placeholders.
     *
     * @param player the recipient
     * @param key    the translation key
     */
    void sendMessage(Player player, String key);

    /**
     * Sends a translated message to a {@link CommandSender} using the default locale,
     * with no placeholders.
     *
     * @param sender the recipient (player, console, or command block)
     * @param key    the translation key
     */
    void sendMessage(CommandSender sender, String key);

    /**
     * Returns the raw translated string for the given locale and key, applying placeholders,
     * without deserializing it into a {@link Component}.
     * <p>
     * Useful when the translated value must be passed as a plain string to a third-party API
     * (e.g. as a cancellation reason).
     *
     * @param locale       the target locale
     * @param key          the translation key
     * @param placeholders a map of placeholder tokens to their replacement values
     * @return the raw translated string, or a {@code <red>Missing translation: key</red>} fallback
     */
    @Nullable String translateRaw(@NotNull Locale locale, String key, @NotNull Map<String, String> placeholders);
}