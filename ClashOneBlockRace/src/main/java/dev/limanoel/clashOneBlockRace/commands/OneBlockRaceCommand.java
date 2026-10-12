package dev.limanoel.clashOneBlockRace.commands;

import dev.limanoel.clashOneBlockRace.manager.GameManager;
import dev.limanoel.clashOneBlockRace.manager.WorldManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OneBlockRaceCommand implements CommandExecutor, Listener {

    private static final int GUI_SIZE = 27;
    private static final int JOIN_SLOT = 13;
    private static final int CANCEL_SLOT = 26;

    private final JavaPlugin plugin;
    private final WorldManager worldManager;
    private final GameManager gameManager;
    private final NamespacedKey guiKey;

    public OneBlockRaceCommand(JavaPlugin plugin, WorldManager worldManager, GameManager gameManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
        this.gameManager = gameManager;
        this.guiKey = new NamespacedKey(plugin, "gui_item");
    }

    public Inventory createJoinGUI() {
        Inventory gui = Bukkit.createInventory(null, GUI_SIZE, Component.text("OneBlockRace"));

        ItemStack joinItem = createGuiItem(Material.NETHERITE_SWORD, Component.text("Match beitreten"), List.of(Component.text("Klicke hier, um einem Match beizutreten.")));
        gui.setItem(JOIN_SLOT, joinItem);

        ItemStack barrier = createGuiItem(Material.BARRIER, Component.text("Abbrechen"), List.of(Component.text("Schließt dieses Menü.")));
        gui.setItem(CANCEL_SLOT, barrier);

        return gui;
    }

    private ItemStack createGuiItem(Material material, Component displayName, List<Component> loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(displayName);
            if (loreLines != null && !loreLines.isEmpty()) {
                meta.lore(loreLines);
            }
            meta.getPersistentDataContainer().set(guiKey, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack currentItem = event.getCurrentItem();

        if (currentItem != null && currentItem.hasItemMeta()) {
            ItemMeta meta = currentItem.getItemMeta();
            if (meta != null && meta.getPersistentDataContainer().has(guiKey, PersistentDataType.BYTE)) {
                event.setCancelled(true);

                if (!(event.getWhoClicked() instanceof Player player)) {
                    return;
                }

                Material type = currentItem.getType();

                if (type == Material.BARRIER) {
                    player.closeInventory();
                } else if (type == Material.NETHERITE_SWORD) {
                    player.closeInventory();

                    org.bukkit.World targetWorld = null;

                    for (org.bukkit.World world : org.bukkit.Bukkit.getWorlds()) {
                        if (world.getName().startsWith("oneblockmultiplayer_") && gameManager.isState(world.getName(), "WAITING")) {
                            targetWorld = world;
                            break;
                        }
                    }

                    String finalWorldName;

                    if (targetWorld == null) {
                        int nextNumber = 1;

                        while (org.bukkit.Bukkit.getWorld("oneblockmultiplayer_" + nextNumber) != null) {
                            nextNumber++;
                        }

                        finalWorldName = "oneblockmultiplayer_" + nextNumber;
                        worldManager.createOneBlockWorldMultiplayer(finalWorldName);
                        gameManager.setGameState(finalWorldName, "WAITING");
                    } else {
                        finalWorldName = targetWorld.getName();
                    }

                    worldManager.setupMultiplayer(player, finalWorldName);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        for (ItemStack item : event.getNewItems().values()) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.getPersistentDataContainer().has(guiKey, PersistentDataType.BYTE)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl ausführen!");
            return true;
        }

        player.openInventory(createJoinGUI());
        return true;
    }
}
