package com.github.sirblobman.join.commands.spigot.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.github.sirblobman.join.commands.spigot.JoinCommandsSpigot;
import com.github.sirblobman.join.commands.spigot.object.ServerJoinCommand;
import com.github.sirblobman.join.commands.spigot.object.WorldJoinCommand;

public final class CommandManager {
    private final JoinCommandsSpigot plugin;
    private final List<ServerJoinCommand> serverJoinCommandList;
    private final List<WorldJoinCommand> worldJoinCommandList;

    public CommandManager(JoinCommandsSpigot plugin) {
        this.plugin = plugin;
        this.serverJoinCommandList = new ArrayList<>();
        this.worldJoinCommandList = new ArrayList<>();
    }

    public JoinCommandsSpigot getPlugin() {
        return this.plugin;
    }

    public List<ServerJoinCommand> getJoinCommandList() {
        return Collections.unmodifiableList(this.serverJoinCommandList);
    }

    public List<WorldJoinCommand> getWorldJoinCommandList() {
        return Collections.unmodifiableList(this.worldJoinCommandList);
    }

    public void loadServerJoinCommands() {
        this.serverJoinCommandList.clear();

        JoinCommandsSpigot plugin = getPlugin();
        Logger logger = plugin.getLogger();
        FileConfiguration config = plugin.getConfig();
        if (config == null) {
            return;
        }

        ConfigurationSection section = config.getConfigurationSection("server-join-commands");
        if (section == null) {
            logger.warning("Your config seems to be missing the 'server-join-commands' section.");
            logger.warning("This means that commands will not be executed when a player joins the server.");
            logger.warning("If you manually deleted it then please ignore this message.");
            return;
        }

        Set<String> commandIdSet = section.getKeys(false);
        for (String commandId : commandIdSet) {
            if (commandId == null || commandId.isEmpty()) {
                continue;
            }

            ConfigurationSection commandSection = section.getConfigurationSection(commandId);
            if (commandSection == null) {
                continue;
            }

            ServerJoinCommand serverJoinCommand = loadServerJoinCommand(commandSection);
            if (serverJoinCommand == null) {
                continue;
            }

            this.serverJoinCommandList.add(serverJoinCommand);
        }
    }

    private ServerJoinCommand loadServerJoinCommand(ConfigurationSection section) {
        if (section == null) {
            return null;
        }

        String commandId = section.getName();
        List<String> commandList = section.getStringList("command-list");
        String permission = section.getString("permission");
        boolean firstJoinOnly = section.getBoolean("first-join-only");
        long delay = section.getLong("delay");

        try {
            return new ServerJoinCommand(commandList, permission, firstJoinOnly, delay);
        } catch (Exception ex) {
            Logger logger = this.plugin.getLogger();
            logger.log(Level.WARNING, "An error occurred while loading the server join command with id '"
                    + commandId + "':", ex);
            return null;
        }
    }

    public void loadWorldJoinCommands() {
        this.worldJoinCommandList.clear();

        JoinCommandsSpigot plugin = getPlugin();
        Logger logger = plugin.getLogger();
        FileConfiguration config = plugin.getConfig();
        if (config == null) {
            return;
        }

        ConfigurationSection section = config.getConfigurationSection("world-join-commands");
        if (section == null) {
            logger.warning("Your config seems to be missing the 'world-join-commands' section.");
            logger.warning("This means that commands will not be executed when a player joins a world.");
            logger.warning("If you manually deleted it then please ignore this message.");
            return;
        }

        Set<String> commandIdSet = section.getKeys(false);
        for (String commandId : commandIdSet) {
            if (commandId == null || commandId.isEmpty()) {
                continue;
            }

            ConfigurationSection commandSection = section.getConfigurationSection(commandId);
            if (commandSection == null) {
                continue;
            }

            WorldJoinCommand worldJoinCommand = loadWorldJoinCommand(commandSection);
            if (worldJoinCommand == null) {
                continue;
            }

            this.worldJoinCommandList.add(worldJoinCommand);
        }
    }

    private WorldJoinCommand loadWorldJoinCommand(ConfigurationSection section) {
        if (section == null) {
            return null;
        }

        String commandId = section.getName();
        List<String> commandList = section.getStringList("command-list");
        List<String> worldList = section.getStringList("world-list");
        String permission = section.getString("permission");
        boolean firstJoinOnly = section.getBoolean("first-join-only");
        long delay = section.getLong("delay");

        try {
            return new WorldJoinCommand(worldList, commandList, permission, firstJoinOnly, delay);
        } catch (Exception ex) {
            Logger logger = this.plugin.getLogger();
            logger.log(Level.WARNING, "An error occurred while loading the world join command with id '"
                    + commandId + "':", ex);
            return null;
        }
    }
}
