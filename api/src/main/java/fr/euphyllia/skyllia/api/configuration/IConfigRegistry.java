package fr.euphyllia.skyllia.api.configuration;

/**
 * Allows external addons to register their own {@link IConfigurationProvider}
 * into Skyllia's global configuration system.
 *
 * <p>Registered providers will be reloaded automatically when
 * {@code ConfigLoader#reloadConfigs()} is called (e.g. via /skyllia reload).</p>
 *
 * <p>The implementation is held by {@code ConfigLoader} and exposed
 * through {@link fr.euphyllia.skyllia.api.SkylliaAPI#getConfigRegistry()}.</p>
 */
public interface IConfigRegistry {

    /**
     * Register a configuration provider so it participates in global reloads.
     *
     * @param provider the provider to register
     */
    void registerConfig(IConfigurationProvider provider);

    /**
     * Unregister a previously registered provider.
     * Should be called in the addon's {@code onDisable()}.
     *
     * @param provider the provider to remove
     */
    void unregisterConfig(IConfigurationProvider provider);
}
