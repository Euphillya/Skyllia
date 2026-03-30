package fr.euphyllia.skylliagenerator.registry;

import fr.euphyllia.skylliagenerator.api.GeneratorDefinition;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GeneratorRegistry {

    private final Map<NamespacedKey, GeneratorDefinition> definitions = new ConcurrentHashMap<>();
    private NamespacedKey defaultKey = new NamespacedKey("skylliagenerator", "default");

    public void registerGenerator(GeneratorDefinition generatorDefinition) {
        definitions.put(generatorDefinition.id(), generatorDefinition);
    }

    public void setDefaultKey(NamespacedKey defaultKey) {
        this.defaultKey = defaultKey;
    }

    @Nullable
    public GeneratorDefinition getGenerator(NamespacedKey key) {
        return definitions.get(key);
    }

    public GeneratorDefinition getDefault() {
        GeneratorDefinition def = definitions.get(defaultKey);
        if (def != null) return def;
        if (!definitions.isEmpty()) return definitions.values().iterator().next();
        return GeneratorDefinition.COBBLESTONE_FALLBACK;
    }

    public Set<NamespacedKey> getGenerators() {
        return definitions.keySet();
    }

    public void unregisterGenerator(NamespacedKey key) {
        definitions.remove(key);
    }

    public void unregisterGenerators() {
        definitions.clear();
    }

    public int size() {
        return definitions.size();
    }
}
