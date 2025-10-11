package fr.euphyllia.skylliachallenge.gui;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GuiSettings {

    public final int rows;
    public final int pageSize;
    public final NavItem previous;
    public final NavItem next;
    public final int maxPageSize;

    private GuiSettings(int rows, int pageSize, int maxPageSize, NavItem previous, NavItem next) {
        this.rows = rows;
        this.pageSize = pageSize;
        this.maxPageSize = maxPageSize;
        this.previous = previous;
        this.next = next;
    }

    public static GuiSettings load(FileConfiguration cfg) {
        int rows = Math.max(1, cfg.getInt("gui.rows", 6));
        int pageSize = Math.max(1, cfg.getInt("gui.page-size", 45));
        int mPageSize = Math.max(1, cfg.getInt("gui.max-pages", 100));

        NavItem prev = NavItem.fromConfig(
                cfg.getString("gui.navigation.previous.slot", "6,3"),
                cfg.getString("gui.navigation.previous.material", "ARROW"),
                cfg.getString("gui.navigation.previous.model_name", ""),
                cfg.getInt("gui.navigation.previous.custom_model_data", 0)
        );

        NavItem next = NavItem.fromConfig(
                cfg.getString("gui.navigation.next.slot", "6,7"),
                cfg.getString("gui.navigation.next.material", "ARROW"),
                cfg.getString("gui.navigation.next.model_name", ""),
                cfg.getInt("gui.navigation.next.custom_model_data", 0)
        );

        return new GuiSettings(rows, pageSize, mPageSize, prev, next);
    }


    public record NavItem(int row, int column, Material material, String modelName, int customModelData) {

        public static NavItem fromConfig(String slot, String matStr, String modelName, int cmd) {
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
            return new NavItem(r, c, m, modelName, cmd);
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
                        // < 1.21.4 : ignore silencieusement
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
