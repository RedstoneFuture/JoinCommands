package com.github.sirblobman.join.commands.spigot.listener;

import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitScheduler;

import com.github.sirblobman.join.commands.spigot.JoinCommandsSpigot;
import com.github.sirblobman.join.commands.spigot.manager.CommandManager;
import com.github.sirblobman.join.commands.spigot.manager.PlayerDataManager;
import com.github.sirblobman.join.commands.spigot.object.ServerJoinCommand;
import com.github.sirblobman.join.commands.spigot.object.WorldJoinCommand;
import com.github.sirblobman.join.commands.utility.Validate;

public final class ListenerJoinCommands implements Listener {
    private final JoinCommandsSpigot plugin;

    public ListenerJoinCommands(JoinCommandsSpigot plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        sendDebug("", "Detected PlayerJoinEvent...");

        Player player = e.getPlayer();
        sendDebug("Player Name: " + player.getName());

        sendDebug("Running server join commands for player...");
        runServerJoinComands(player);
        sendDebug("Finished running server join commands.");

        sendDebug("Setting player as previously joined if not already set.");
        setJoinedServerBefore(player);

        World world = player.getWorld();
        sendDebug("Detected world join for world " + world.getName());

        sendDebug("Running world join commands for player...");
        runWorldJoinCommands(player, world);
        sendDebug("Finished running world join commands.");

        setJoinedWorldBefore(player, world);
        sendDebug("Setting player as previously joined world if not already set.");

        sendDebug("Finished PlayerJoinEvent checks.");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChangeWorld(PlayerChangedWorldEvent e) {
        sendDebug("", "Detected PlayerChangedWorldEvent...");
        Player player = e.getPlayer();
        sendDebug("Player Name: " + player.getName());
        World world = player.getWorld();
        sendDebug("World Name: " + world.getName());

        sendDebug("Running world join commands for player...");
        runWorldJoinCommands(player, world);
        sendDebug("Finished running world join commands.");

        setJoinedWorldBefore(player, world);
        sendDebug("Setting player as previously joined world if not already set.");

        sendDebug("Finished PlayerChangedWorldEvent checks.");
    }

    private JoinCommandsSpigot getPlugin() {
        return this.plugin;
    }

    private FileConfiguration getConfiguration() {
        JoinCommandsSpigot plugin = getPlugin();
        return plugin.getConfig();
    }

    private void runServerJoinComands(Player player) {
        if (player == null) {
            return;
        }

        BukkitScheduler scheduler = Bukkit.getScheduler();
        CommandManager commandManager = this.plugin.getCommandManager();
        List<ServerJoinCommand> commandList = commandManager.getJoinCommandList();

        for (ServerJoinCommand command : commandList) {
            if (!command.shouldBeExecutedFor(this.plugin, player)) {
                continue;
            }

            long delay = command.getDelay();
            Runnable task = () -> command.executeFor(this.plugin, player);
            scheduler.scheduleSyncDelayedTask(this.plugin, task, delay);
        }
    }

    private void runWorldJoinCommands(Player player, World world) {
        if (player == null || world == null) {
            return;
        }

        BukkitScheduler scheduler = Bukkit.getScheduler();
        CommandManager commandManager = this.plugin.getCommandManager();
        List<WorldJoinCommand> commandList = commandManager.getWorldJoinCommandList();

        for (WorldJoinCommand command : commandList) {
            if (!command.shouldBeExecutedFor(this.plugin, player, world)) {
                continue;
            }

            long delay = command.getDelay();
            Runnable task = () -> command.executeFor(this.plugin, player, world);
            scheduler.scheduleSyncDelayedTask(this.plugin, task, delay);
        }
    }

    private void setJoinedServerBefore(Player player) {
        Validate.notNull(player, "player must not be null!");

        FileConfiguration configuration = getConfiguration();
        if(configuration.getBoolean("disable-player-data", false)) {
            return;
        }

        JoinCommandsSpigot plugin = getPlugin();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        YamlConfiguration playerData = playerDataManager.get(player);
        if (playerData.getBoolean("join-commands.played-before", false)) {
            return;
        }

        playerData.set("join-commands.played-before", true);
        playerDataManager.save(player);
    }

    private void setJoinedWorldBefore(Player player, World world) {
        Validate.notNull(player, "player must not be null!");
        Validate.notNull(world, "world must not be null!");

        FileConfiguration configuration = getConfiguration();
        if(configuration.getBoolean("disable-player-data", false)) {
            return;
        }

        String worldName = world.getName();
        PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
        YamlConfiguration playerData = playerDataManager.get(player);

        String path = "join-commands.played-before-world-list";
        List<String> worldList = playerData.getStringList(path);
        if (worldList.contains(worldName)) {
            return;
        }

        worldList.add(worldName);
        playerData.set(path, worldList);
        playerDataManager.save(player);
    }

    private void sendDebug(String... messageArray) {
        FileConfiguration configuration = getConfiguration();
        if (!configuration.getBoolean("debug-mode", false)) {
            return;
        }

        JoinCommandsSpigot plugin = getPlugin();
        Logger logger = plugin.getLogger();
        for (String message : messageArray) {
            String logMessage = String.format(Locale.US, "[Debug] %s", message);
            logger.info(logMessage);
        }
    }
}
