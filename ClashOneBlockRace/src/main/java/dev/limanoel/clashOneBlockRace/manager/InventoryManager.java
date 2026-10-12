package dev.limanoel.clashOneBlockRace.manager;

import dev.limanoel.clashOneBlockRace.ClashOneBlockRace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class InventoryManager {

    private final ClashOneBlockRace plugin;
    private final File saveFile;
    private FileConfiguration saveConfig;

    public InventoryManager(ClashOneBlockRace plugin) {
        this.plugin = plugin;
        this.saveFile = new File(plugin.getDataFolder(), "save.yml");
        loadConfig();
    }

    private void loadConfig() {
        if (!saveFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                saveFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Konnte save.yml nicht erstellen: " + e.getMessage());
            }
        }
        saveConfig = YamlConfiguration.loadConfiguration(saveFile);
    }

    private void saveConfig() {
        try {
            saveConfig.save(saveFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Konnte save.yml nicht speichern: " + e.getMessage());
        }
    }

    /**
     * Speichert das komplette Inventar eines Spielers (Inhalt, Rüstung, Offhand)
     * in der save.yml und leert danach sein Inventar.
     */
    public void saveAndClearInventory(Player player) {
        UUID uuid = player.getUniqueId();
        String path = "inventories." + uuid;

        PlayerInventory inv = player.getInventory();

        // Inventar-Inhalt (Slots 0-35)
        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                saveConfig.set(path + ".slot." + i, contents[i]);
            }
        }

        // Rüstung (Helm, Brustplatte, Hose, Schuhe)
        ItemStack[] armor = inv.getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            if (armor[i] != null) {
                saveConfig.set(path + ".armor." + i, armor[i]);
            }
        }

        // Offhand
        ItemStack offhand = inv.getItemInOffHand();
        if (offhand.getType() != org.bukkit.Material.AIR) {
            saveConfig.set(path + ".offhand", offhand);
        }

        // Markiere, dass der Spieler gespeicherte Daten hat
        saveConfig.set(path + ".saved", true);

        saveConfig();

        // Inventar leeren
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().setItemInOffHand(new ItemStack(org.bukkit.Material.AIR));
    }

    /**
     * Stellt das gespeicherte Inventar eines Spielers wieder her
     * und entfernt die gespeicherten Daten aus der save.yml.
     */
    public void restoreInventory(Player player) {
        UUID uuid = player.getUniqueId();
        String path = "inventories." + uuid;

        if (!saveConfig.getBoolean(path + ".saved", false)) {
            return; // Nichts gespeichert
        }

        PlayerInventory inv = player.getInventory();

        // Inventar leeren bevor wir wiederherstellen
        inv.clear();
        inv.setArmorContents(new ItemStack[4]);
        inv.setItemInOffHand(new ItemStack(org.bukkit.Material.AIR));

        // Inventar-Inhalt wiederherstellen
        if (saveConfig.contains(path + ".slot")) {
            for (String key : saveConfig.getConfigurationSection(path + ".slot").getKeys(false)) {
                int slot = Integer.parseInt(key);
                ItemStack item = saveConfig.getItemStack(path + ".slot." + key);
                if (item != null) {
                    inv.setItem(slot, item);
                }
            }
        }

        // Rüstung wiederherstellen
        if (saveConfig.contains(path + ".armor")) {
            ItemStack[] armor = new ItemStack[4];
            for (String key : saveConfig.getConfigurationSection(path + ".armor").getKeys(false)) {
                int slot = Integer.parseInt(key);
                ItemStack item = saveConfig.getItemStack(path + ".armor." + key);
                if (item != null) {
                    armor[slot] = item;
                }
            }
            inv.setArmorContents(armor);
        }

        // Offhand wiederherstellen
        if (saveConfig.contains(path + ".offhand")) {
            ItemStack offhand = saveConfig.getItemStack(path + ".offhand");
            if (offhand != null) {
                inv.setItemInOffHand(offhand);
            }
        }

        // Gespeicherte Daten löschen
        saveConfig.set(path, null);
        saveConfig();
    }

    /**
     * Prüft, ob ein Spieler gespeicherte Inventar-Daten hat.
     */
    public boolean hasSavedInventory(Player player) {
        return saveConfig.getBoolean("inventories." + player.getUniqueId() + ".saved", false);
    }

    /**
     * Config neu laden (z.B. nach Server-Neustart).
     */
    public void reloadConfig() {
        loadConfig();
    }
}
