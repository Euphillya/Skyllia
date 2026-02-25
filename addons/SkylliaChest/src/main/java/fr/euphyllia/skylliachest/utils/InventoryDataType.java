package fr.euphyllia.skylliachest.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class InventoryDataType implements PersistentDataType<byte[], ItemStack[]> {

    public static final InventoryDataType INSTANCE = new InventoryDataType();
    private static final Logger log = LoggerFactory.getLogger(InventoryDataType.class);

    @Override
    public byte @NotNull [] toPrimitive(@NotNull ItemStack @NotNull [] complex, @NotNull PersistentDataAdapterContext context) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            dos.writeInt(complex.length);

            for (ItemStack item : complex) {
                if (item == null || item.getType().isAir()) {
                    dos.writeInt(-1);
                } else {
                    byte[] serializedItem = item.serializeAsBytes();
                    dos.writeInt(serializedItem.length);
                    dos.write(serializedItem);
                }
            }
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Failed to serialize inventory data", e);
            return new byte[0];
        }
    }

    @Override
    public @NotNull ItemStack @NotNull [] fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
        if (primitive.length == 0) {
            return new ItemStack[0];
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(primitive);
             DataInputStream dis = new DataInputStream(bais)) {
            int arrayLength = dis.readInt();
            ItemStack[] items = new ItemStack[arrayLength];

            for (int i = 0; i < arrayLength; i++) {
                int itemSize = dis.readInt();
                if (itemSize == -1) {
                    items[i] = null;
                } else {
                    byte[] itemData = new byte[itemSize];
                    dis.readFully(itemData);
                    items[i] = ItemStack.deserializeBytes(itemData);
                }
            }
            return items;
        } catch (IOException e) {
            log.error("Failed to deserialize inventory data", e);
            return new ItemStack[0];
        }
    }

    @Override
    public @NotNull Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public @NotNull Class<ItemStack[]> getComplexType() {
        return ItemStack[].class;
    }
}
