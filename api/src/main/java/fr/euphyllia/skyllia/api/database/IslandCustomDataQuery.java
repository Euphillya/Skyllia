package fr.euphyllia.skyllia.api.database;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Set;

public abstract class IslandCustomDataQuery {

    public abstract <P, C> boolean set(
            @NotNull NamespacedKey namespace,
            @NotNull Island island,
            @NotNull String dataKey,
            @NotNull PersistentDataType<P, C> type,
            @NotNull C value
    );

    @Nullable
    public abstract <P, C> C get(
            @NotNull NamespacedKey namespace,
            @NotNull Island island,
            @NotNull String dataKey,
            @NotNull PersistentDataType<P, C> type
    );

    @NotNull
    public abstract <P, C> C getOrDefault(
            @NotNull NamespacedKey namespace,
            @NotNull Island island,
            @NotNull String dataKey,
            @NotNull PersistentDataType<P, C> type,
            @NotNull C defaultValue
    );

    public abstract boolean has(
            @NotNull NamespacedKey namespace,
            @NotNull Island island,
            @NotNull String dataKey
    );

    public abstract <P, C> boolean has(
            @NotNull NamespacedKey namespace,
            @NotNull Island island,
            @NotNull String dataKey,
            @NotNull PersistentDataType<P, C> type
    );

    @NotNull
    public abstract Set<String> getKeys(
            @NotNull NamespacedKey namespace,
            @NotNull Island island
    );

    public abstract boolean remove(
            @NotNull NamespacedKey namespace,
            @NotNull Island island,
            @NotNull String dataKey
    );

    public abstract boolean clear(
            @NotNull NamespacedKey namespace,
            @NotNull Island island
    );

    public abstract int size(
            @NotNull NamespacedKey namespace,
            @NotNull Island island
    );

    public abstract boolean isEmpty(
            @NotNull NamespacedKey namespace,
            @NotNull Island island
    );

    public byte[] serializePrimitive(Object primitive) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(primitive);
        oos.close();
        return baos.toByteArray();
    }

    @SuppressWarnings("unchecked")
    public <P> P deserializePrimitive(byte[] data, Class<P> primitiveType) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bais);
        P result = (P) ois.readObject();
        ois.close();
        return result;
    }

    public String getTableName(NamespacedKey namespace) {
        return "island_custom_data_" + namespace.getNamespace() + "_" + namespace.getKey().replace('-', '_');
    }

    public static class EmptyAdapterContext implements PersistentDataAdapterContext {
        @Override
        public @NotNull PersistentDataContainer newPersistentDataContainer() {
            throw new UnsupportedOperationException("Nested containers not supported");
        }
    }
}
