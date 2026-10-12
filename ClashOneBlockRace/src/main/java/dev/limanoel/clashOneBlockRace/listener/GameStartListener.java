package dev.limanoel.clashOneBlockRace.listener;

import dev.limanoel.clashOneBlockRace.manager.GameManager;
import dev.limanoel.clashOneBlockRace.manager.InventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class GameStartListener implements Listener {

    private static final int MIN_PLAYERS = 2;
    private static final int COUNTDOWN_SECONDS = 30;
    private static final List<Integer> ANNOUNCEMENTS = List.of(10, 5, 4, 3, 2, 1);

    private final JavaPlugin plugin;
    private final GameManager gameManager;
    private final InventoryManager inventoryManager;

    public GameStartListener(JavaPlugin plugin, GameManager gameManager, InventoryManager inventoryManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.inventoryManager = inventoryManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        World currentWorld = player.getWorld();
        String worldName = currentWorld.getName();

        if (!worldName.toLowerCase().contains("oneblock")) {
            return;
        }

        if (gameManager.isState(worldName, "STARTING") || gameManager.isState(worldName, "WAITING")) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (to != null && (from.getX() != to.getX() || from.getZ() != to.getZ() || from.getY() != to.getY())) {
                event.setTo(new Location(from.getWorld(), from.getX(), from.getY(), from.getZ(), to.getYaw(), to.getPitch()));
                return;
            }
        }

        if (player.getLocation().getY() < 0) {
            handleVoidFall(player, currentWorld, worldName);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        World world = player.getWorld();
        String worldName = world.getName();

        if (!worldName.toLowerCase().contains("oneblock")) {
            return;
        }

        if (gameManager.isState(worldName, "WAITING") || gameManager.isState(worldName, "STARTING")) {
            event.setCancelled(true);
            if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                handleVoidFall(player, world, worldName);
            }
            return;
        }

        if (gameManager.isState(worldName, "ACTIVE") && event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            event.setCancelled(true);
            handleVoidFall(player, world, worldName);
        }
    }

    private void handleVoidFall(Player player, World world, String worldName) {
        if (gameManager.isState(worldName, "ACTIVE")) {
            if (player.getGameMode() != GameMode.SPECTATOR) {
                player.setGameMode(GameMode.SPECTATOR);
                player.setFallDistance(0);
                player.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
                player.setFireTicks(0);

                Location specLoc = new Location(world, 0, 60 + 5, player.getLocation().getZ());
                player.teleport(specLoc);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    gameManager.checkWinner(worldName);
                }, 1L);
            }
        } else {
            player.setFallDistance(0);
            player.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
            player.teleport(new Location(world, 0, 60, player.getLocation().getZ()));
        }
    }

    @EventHandler
    public void onSpectatorTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld().getName().toLowerCase().contains("oneblock")) {
            if (event.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
                event.setCancelled(true);
                player.sendMessage("§cDu kannst dich in diesem Match nicht als Zuschauer teleportieren.");
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player.getWorld().getName().toLowerCase().contains("oneblock")) {
            event.getDrops().clear();
            event.setDroppedExp(0);
            gameManager.removePlayerBedrock(player);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && !player.getWorld().getName().toLowerCase().contains("oneblock")) {
                if (inventoryManager.hasSavedInventory(player)) {
                    inventoryManager.restoreInventory(player);
                }
            }
        }, 1L);
    }

    @EventHandler
    public void onPlayerJoinWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World currentWorld = player.getWorld();
        String worldName = currentWorld.getName();

        World leavedWorld = event.getFrom();

        if (leavedWorld.getName().toLowerCase().contains("oneblock")) {
            gameManager.removePlayerBedrock(player);

            if (!currentWorld.getName().toLowerCase().contains("oneblock")) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        inventoryManager.restoreInventory(player);
                    }
                }, 1L);
            }

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                gameManager.checkWorldAfterPlayerLeave(leavedWorld);
                gameManager.checkWinner(leavedWorld.getName());
            }, 2L);
        }

        if (worldName.toLowerCase().contains("oneblock")) {
            int playerCount = currentWorld.getPlayers().size();

            if (playerCount >= MIN_PLAYERS && gameManager.isState(worldName, "WAITING")) {
                gameManager.setGameState(worldName, "STARTING");
                startCountdown(worldName, "WAITING");
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        String worldName = world.getName();

        if (worldName.toLowerCase().contains("oneblock")) {
            gameManager.removePlayerBedrock(player);
            inventoryManager.restoreInventory(player);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                gameManager.checkWorldAfterPlayerLeave(world);
                gameManager.checkWinner(worldName);
            }, 2L);
        }
    }

    @EventHandler
    public void onPlayerSwitchGamemodeEvent(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();

        if (worldName.toLowerCase().contains("oneblock") && gameManager.isState(worldName, "ACTIVE")) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                gameManager.checkWinner(worldName);
            }, 1L);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline() && inventoryManager.hasSavedInventory(player)) {
                    inventoryManager.restoreInventory(player);
                }
            }
        }.runTaskLater(plugin, 20L);
    }

    public void startCountdown(String worldName, String status) {
        if (!worldName.toLowerCase().contains("oneblock")) {
            return;
        }

        final int countdownDuration = COUNTDOWN_SECONDS;
        final List<Integer> announcements = ANNOUNCEMENTS;
        final int minPlayers = MIN_PLAYERS;

        BukkitTask task = new BukkitRunnable() {
            int secondsLeft = countdownDuration;

            @Override
            public void run() {
                World bukkitWorld = Bukkit.getWorld(worldName);

                if (bukkitWorld == null) {
                    this.cancel();
                    return;
                }

                int playerCount = bukkitWorld.getPlayers().size();
                if (playerCount < minPlayers) {
                    gameManager.cancelCountdown(worldName);
                    gameManager.setGameState(worldName, "WAITING");
                    this.cancel();
                    return;
                }

                if (announcements.contains(secondsLeft)) {
                    for (Player player : bukkitWorld.getPlayers()) {
                        player.sendMessage("§eMatch startet in §6" + secondsLeft + "§e Sekunden!");
                    }
                }

                if (secondsLeft <= 0) {
                    gameManager.setGameState(worldName, "ACTIVE");
                    gameManager.startActiveGameLoop(worldName);

                    for (Player player : bukkitWorld.getPlayers()) {
                        player.sendMessage("§aDas Match hat begonnen!");
                    }

                    this.cancel();
                    return;
                }

                secondsLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        gameManager.setCountdownTask(worldName, task);
    }
}
