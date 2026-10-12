package dev.limanoel.clashOneBlockRace.commands;

import dev.limanoel.clashOneBlockRace.ClashOneBlockRace;
import dev.limanoel.clashOneBlockRace.manager.GameManager;
import dev.limanoel.clashOneBlockRace.manager.InventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LeaveCommand implements CommandExecutor {

    private final ClashOneBlockRace plugin;
    private final GameManager gameManager;
    private final InventoryManager inventoryManager;

    public LeaveCommand(ClashOneBlockRace plugin, GameManager gameManager, InventoryManager inventoryManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.inventoryManager = inventoryManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl ausführen!");
            return true;
        }

        World currentWorld = player.getWorld();
        String worldName = currentWorld.getName();

        if (!worldName.toLowerCase().contains("oneblock") && !inventoryManager.hasSavedInventory(player)) {
            player.sendMessage("Du bist in keinem Match.");
            return true;
        }

        gameManager.removePlayerBedrock(player);

        Location targetLocation = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0).getSpawnLocation();

        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SURVIVAL);
        }

        player.setFallDistance(0);
        player.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
        player.setFireTicks(0);

        for (Player other : currentWorld.getPlayers()) {
            if (!other.equals(player)) {
            }
        }

        if (targetLocation != null) {
            player.teleport(targetLocation);
        }
        player.sendMessage("Du hast das Match verlassen.");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                inventoryManager.restoreInventory(player);
            }
        }, 1L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            gameManager.checkWorldAfterPlayerLeave(currentWorld);
            gameManager.checkWinner(worldName);
        }, 2L);

        return true;
    }
}
