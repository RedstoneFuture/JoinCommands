package com.github.sirblobman.join.commands.spigot;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;

import com.github.sirblobman.join.commands.spigot.command.CommandJoinCommands;
import com.github.sirblobman.join.commands.spigot.listener.ListenerJoinCommands;
import com.github.sirblobman.join.commands.spigot.manager.CommandManager;
import com.github.sirblobman.join.commands.spigot.manager.PlayerDataManager;

public final class JoinCommandsSpigot extends JavaPlugin {
    private final PlayerDataManager playerDataManager;
    private final CommandManager commandManager;

    public JoinCommandsSpigot() {
        this.playerDataManager = new PlayerDataManager(this);
        this.commandManager = new CommandManager(this);
    }

    @Override
    public void onLoad() {
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        reloadConfig();

        registerBungeeCordChannels();
        registerListener();

        new CommandJoinCommands(this).register();
    }

    @Override
    public void onDisable() {
        // Do Nothing
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        CommandManager commandManager = getCommandManager();
        commandManager.loadServerJoinCommands();
        commandManager.loadWorldJoinCommands();
    }

    public PlayerDataManager getPlayerDataManager() {
        return this.playerDataManager;
    }

    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    public boolean usePlaceholderAPIHook() {
        FileConfiguration config = getConfig();
        boolean useHook = config.getBoolean("spigot-options.placeholderapi-hook", true);
        PluginManager pluginManager = Bukkit.getPluginManager();
        return (useHook && pluginManager.isPluginEnabled("PlaceholderAPI"));
    }

    private void registerBungeeCordChannels() {
        FileConfiguration config = getConfig();
        if (!config.getBoolean("spigot-options.bungeecord-hook")) {
            return;
        }

        Messenger messenger = Bukkit.getMessenger();
        messenger.registerOutgoingPluginChannel(this, "jc:console");
        messenger.registerOutgoingPluginChannel(this, "jc:player");
    }

    private void registerListener() {
        PluginManager manager = Bukkit.getPluginManager();
        Listener listener = new ListenerJoinCommands(this);
        manager.registerEvents(listener, this);
    }
}
