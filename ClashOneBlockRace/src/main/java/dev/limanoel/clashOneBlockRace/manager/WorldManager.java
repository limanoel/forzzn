package dev.limanoel.clashOneBlockRace.manager;

import dev.limanoel.clashOneBlockRace.ClashOneBlockRace;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

public class WorldManager {

    private static final int SPAWN_Y = 60;
    private static final int BEDROCK_Y = 59;
    private static final int SPAWN_Z_SPACING = 15;
    private static final int DIRECTION_OFFSET_X = 4;
    private static final int DIRECTION_OFFSET_Y = 2;

    private final ClashOneBlockRace plugin;
    private InventoryManager inventoryManager;
    private GameManager gameManager;

    public WorldManager(ClashOneBlockRace plugin) {
        this.plugin = plugin;
    }

    public void setInventoryManager(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public World createOneBlockWorldMultiplayer(String oneblock_multiplayer) {
        World existWorld = Bukkit.getWorld(oneblock_multiplayer);
        if (existWorld != null) {
            return existWorld;
        }

        WorldCreator creator = new WorldCreator(oneblock_multiplayer);
        creator.type(WorldType.FLAT);
        creator.generateStructures(false);
        creator.generatorSettings("{\"layers\": [], \"structures\": {\"structures\": {}}}");

        World multiplayer = Bukkit.createWorld(creator);
        if (multiplayer != null) {
            multiplayer.setGameRule(GameRules.SPAWN_MOBS, false);
        }

        return multiplayer;
    }

    public World createOneBlockWorldPrivateGame(String oneblock_multiplayer) {
        World existWorld = Bukkit.getWorld(oneblock_multiplayer);
        if (existWorld != null) {
            return existWorld;
        }

        WorldCreator creator = new WorldCreator(oneblock_multiplayer);
        creator.type(WorldType.FLAT);
        creator.generateStructures(false);
        creator.generatorSettings("{\"layers\": [], \"structures\": {\"structures\": {}}}");

        World multiplayer = Bukkit.createWorld(creator);
        if (multiplayer != null) {
            multiplayer.setGameRule(GameRules.SPAWN_MOBS, false);
        }

        return multiplayer;
    }

    public void setupPrivateGame(Player player, String oneblock_multiplayer) {
        World multiplayerWorld = createOneBlockWorldMultiplayer(oneblock_multiplayer);
        if (multiplayerWorld == null) {
            return;
        }

        if (inventoryManager != null) {
            inventoryManager.saveAndClearInventory(player);
        }

        int playerCount = multiplayerWorld.getPlayers().size();
        int zOffset = playerCount * SPAWN_Z_SPACING;

        float buildDirectionYaw = -90f;
        Location spawnLocation = new Location(multiplayerWorld, 0.5, SPAWN_Y, zOffset + 0.5, buildDirectionYaw, 0f);
        Location startBlock = new Location(multiplayerWorld, 0, BEDROCK_Y, zOffset);
        startBlock.getBlock().setType(Material.BEDROCK);

        if (gameManager != null) {
            gameManager.registerPlayerBedrock(player.getUniqueId(), startBlock);
        }

        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.setFallDistance(0);
        player.teleport(spawnLocation);

        spawnDirectionIndicator(multiplayerWorld, zOffset, player);
    }

    public void setupMultiplayer(Player player, String oneblock_multiplayer) {
        World multiplayerWorld = createOneBlockWorldMultiplayer(oneblock_multiplayer);
        if (multiplayerWorld == null) {
            return;
        }

        if (inventoryManager != null) {
            inventoryManager.saveAndClearInventory(player);
        }

        int playerCount = multiplayerWorld.getPlayers().size();
        int zOffset = playerCount * SPAWN_Z_SPACING;

        float buildDirectionYaw = -90f;
        Location spawnLocation = new Location(multiplayerWorld, 0.5, SPAWN_Y, zOffset + 0.5, buildDirectionYaw, 0f);
        Location startBlock = new Location(multiplayerWorld, 0, BEDROCK_Y, zOffset);
        startBlock.getBlock().setType(Material.BEDROCK);

        if (gameManager != null) {
            gameManager.registerPlayerBedrock(player.getUniqueId(), startBlock);
        }

        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.setFallDistance(0);
        player.teleport(spawnLocation);

        spawnDirectionIndicator(multiplayerWorld, zOffset, player);
    }

    private void spawnDirectionIndicator(World world, int zOffset, Player player) {
        Location indicatorLoc = new Location(world, DIRECTION_OFFSET_X, SPAWN_Y + DIRECTION_OFFSET_Y, zOffset + 0.5);

        TextDisplay display = world.spawn(indicatorLoc, TextDisplay.class, entity -> {
            Component text = Component.text("➡ BAUE HIER LANG ➡");
            entity.text(text);
            entity.setBillboard(Display.Billboard.CENTER);
            entity.setShadowed(true);
            entity.setSeeThrough(false);
            entity.setDefaultBackground(false);
            entity.setPersistent(true);
        });

        if (gameManager != null) {
            gameManager.registerDirectionIndicator(player.getUniqueId(), display);
        }
    }
}
