package dev.limanoel.clashOneBlockRace;

import dev.limanoel.clashOneBlockRace.commands.LeaveCommand;
import dev.limanoel.clashOneBlockRace.commands.OneBlockRaceCommand;
import dev.limanoel.clashOneBlockRace.commands.privateGamesCommand;
import dev.limanoel.clashOneBlockRace.listener.GameStartListener;
import dev.limanoel.clashOneBlockRace.manager.GameManager;
import dev.limanoel.clashOneBlockRace.manager.InventoryManager;
import dev.limanoel.clashOneBlockRace.manager.PrivateGamesManager;
import dev.limanoel.clashOneBlockRace.manager.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class ClashOneBlockRace extends JavaPlugin {

    private WorldManager worldManager;
    private GameManager gameManager;
    private InventoryManager inventoryManager;
    private PrivateGamesManager privateGamesListener;

    @Override
    public void onEnable() {
        this.inventoryManager = new InventoryManager(this);
        this.worldManager = new WorldManager(this);
        this.gameManager = new GameManager(this);
        this.privateGamesListener = new PrivateGamesManager();

        this.worldManager.setInventoryManager(inventoryManager);
        this.worldManager.setGameManager(gameManager);

        OneBlockRaceCommand raceCommand = new OneBlockRaceCommand(this, worldManager, gameManager);
        LeaveCommand leaveCommand = new LeaveCommand(this, gameManager, inventoryManager);
        privateGamesCommand privateGamesCommand = new privateGamesCommand(this, worldManager, gameManager);

        this.getServer().getPluginManager().registerEvents(new GameStartListener(this, gameManager, inventoryManager), this);
        this.getServer().getPluginManager().registerEvents(raceCommand, this);
        this.getServer().getPluginManager().registerEvents(privateGamesCommand, this);

        if (this.getCommand("oneblockrace") != null) {
            this.getCommand("oneblockrace").setExecutor(raceCommand);
        } else if (this.getCommand("oneblock") != null) {
            this.getCommand("oneblock").setExecutor(raceCommand);
        }

        if (this.getCommand("privategames") != null) {
            this.getCommand("privategames").setExecutor(privateGamesCommand);
        }

        if (this.getCommand("leave") != null) {
            this.getCommand("leave").setExecutor(leaveCommand);
        }

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            gameManager.cleanEmptyWorlds();
        }, 200L, 200L);
    }

    @Override
    public void onDisable() {
        if (gameManager != null) {
            gameManager.deleteAllOneBlockWorlds();
        }
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }


    public PrivateGamesManager getPrivateGamesListener() {
        return privateGamesListener;
    }
}