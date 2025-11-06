package fr.euphyllia.skylliachallenge.gui;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GuiSettings {

    public final int rows;
    public final int pageSize;
    public final NavItem previous;
    public final NavItem next;
    public final NavItem other;
    public final int maxPageSize;

    private GuiSettings(int rows, int pageSize, int maxPageSize, NavItem previous, NavItem next, NavItem other) {
        this.rows = rows;
        this.pageSize = pageSize;
        this.maxPageSize = maxPageSize;
        this.previous = previous;
        this.next = next;
        this.other = other;
    }

    public static GuiSettings load(FileConfiguration cfg) {
        int rows = Math.max(1, cfg.getInt("gui.rows", 6));
        int pageSize = Math.max(1, cfg.getInt("gui.page-size", 45));
        int mPageSize = Math.max(1, cfg.getInt("gui.max-pages", 100));

        NavItem prev = NavItem.fromConfig(
                cfg.getString("gui.navigation.previous.slot", "6,3"),
                cfg.getString("gui.navigation.previous.material", "ARROW"),
                cfg.getString("gui.navigation.previous.model_name", ""),
                cfg.getInt("gui.navigation.previous.custom_model_data", 0),
                List.of(),
                true
        );

        NavItem next = NavItem.fromConfig(
                cfg.getString("gui.navigation.next.slot", "6,7"),
                cfg.getString("gui.navigation.next.material", "ARROW"),
                cfg.getString("gui.navigation.next.model_name", ""),
                cfg.getInt("gui.navigation.next.custom_model_data", 0),
                List.of(),
                true
        );

        NavItem other = NavItem.fromConfig(
                cfg.getString("gui.navigation.other.slot", "6,5"),
                cfg.getString("gui.navigation.other.material", "ARROW"),
                cfg.getString("gui.navigation.other.model_name", ""),
                cfg.getInt("gui.navigation.other.custom_model_data", 0),
                List.of(),
                false
        );

        return new GuiSettings(rows, pageSize, mPageSize, prev, next, other);
    }


    public record NavItem(int row, int column, Material material, String modelName, int customModelData, List<String> commands, boolean enabled) {

        public static NavItem fromConfig(String slot, String matStr, String modelName, int customModelData, List<String> commands, boolean enabled) {
            int r = 6, c = 3;
            if (slot != null && slot.contains(",")) {
                String[] sp = slot.split(",");
                try {
                    r = Integer.parseInt(sp[0].trim());
                    c = Integer.parseInt(sp[1].trim());
                } catch (NumberFormatException ignored) {
                }
            }
            Material m = Material.matchMaterial(matStr != null ? matStr : "ARROW");
            return new NavItem(r, c, m, modelName, customModelData, commands, enabled);
        }

        public ItemStack toItemStack() {
            ItemStack it = new ItemStack(material);
            ItemMeta meta = it.getItemMeta();
            if (meta != null) {
                // model_name non vide => setItemModel (si dispo)
                if (!modelName.isEmpty()) {
                    try {
                        NamespacedKey key = NamespacedKey.fromString(modelName);
                        if (key != null) {
                            meta.setItemModel(key);
                        }
                    } catch (NoSuchMethodError ignored) {
                        // < 1.21.4: ignore silently
                    }
                }
                // custom_model_data > 0 => set
                if (customModelData > 0) {
                    meta.setCustomModelData(customModelData);
                }
                it.setItemMeta(meta);
            }
            return it;
        }
    }
}
