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
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class privateGamesCommand implements CommandExecutor, Listener {
    private final JavaPlugin plugin;
    private final WorldManager worldManager;
    private final GameManager gameManager;
    private final NamespacedKey guiKey;

    public privateGamesCommand(JavaPlugin plugin, WorldManager worldManager, GameManager gameManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
        this.gameManager = gameManager;
        this.guiKey = new NamespacedKey(plugin, "gui_item");
    }

    public Inventory createPrivateGamesGUI() {
        Inventory gui = Bukkit.createInventory(null, 27, Component.text("Private Games"));

        ItemStack joinItem = new ItemStack(Material.EMERALD);
        ItemMeta joinMeta = joinItem.getItemMeta();
        if (joinMeta != null) {
            joinMeta.displayName(Component.text("Privates Match erstellen"));
            joinItem.setItemMeta(joinMeta);
        }
        gui.setItem(13, joinItem);

        return gui;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView() != null && "Private Games".equals(event.getView().getTitle().toString())) {
            event.setCancelled(true);
        }

        if (event.getClickedInventory() != null && event.getClickedInventory().equals(event.getView().getTopInventory())) {
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.EMERALD) {
                Player player = (Player) event.getWhoClicked();
                if (player.hasPermission("obr.privategames")) {
                    player.sendMessage("§aPrivates Match wird vorbereitet...");
                } else {
                    player.sendMessage("§cDu hast keine Berechtigung für private Matches.");
                }
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl ausführen!");
            return true;
        }

        player.openInventory(createPrivateGamesGUI());
        return true;
    }
}
