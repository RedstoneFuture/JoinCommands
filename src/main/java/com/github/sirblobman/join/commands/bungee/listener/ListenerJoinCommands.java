package com.github.sirblobman.join.commands.bungee.listener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;


import com.github.sirblobman.join.commands.utility.Validate;
import com.github.sirblobman.join.commands.bungee.JoinCommandsBungee;
import com.github.sirblobman.join.commands.bungee.manager.CommandManager;
import com.github.sirblobman.join.commands.bungee.object.ProxyJoinCommand;

public final class ListenerJoinCommands implements Listener {
    private final JoinCommandsBungee plugin;

    public ListenerJoinCommands(JoinCommandsBungee plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMessage(PluginMessageEvent e) {
        Connection connection = e.getReceiver();
        if (!(connection instanceof ProxiedPlayer)) {
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) connection;
        String channel = e.getTag();
        byte[] data = e.getData();

        if (channel.equals("jc:player")) {
            e.setCancelled(true);
            runPlayerCommand(player, data);
            return;
        }

        if (channel.equals("jc:console")) {
            e.setCancelled(true);
            runConsoleCommand(data);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PostLoginEvent e) {
        ProxiedPlayer player = e.getPlayer();
        runProxyJoinCommands(player);
        setJoinedProxyBefore(player);
    }

    private JoinCommandsBungee getPlugin() {
        return this.plugin;
    }

    private void runPlayerCommand(ProxiedPlayer player, byte[] data) {
        Validate.notNull(player, "player must not be null!");
        Validate.notNull(data, "data must not be null!");
        JoinCommandsBungee plugin = getPlugin();

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            DataInputStream dataStream = new DataInputStream(inputStream);
            String command = dataStream.readUTF();

            ProxyServer proxy = plugin.getProxy();
            PluginManager manager = proxy.getPluginManager();
            manager.dispatchCommand(player, command);
        } catch (IOException ex) {
            Logger logger = plugin.getLogger();
            logger.log(Level.WARNING, "An error occurred while parsing a jc:player command:", ex);
        }
    }

    private void runConsoleCommand(byte[] data) {
        Validate.notNull(data, "data must not be null!");
        JoinCommandsBungee plugin = getPlugin();

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            DataInputStream dataStream = new DataInputStream(inputStream);
            String command = dataStream.readUTF();

            ProxyServer proxy = plugin.getProxy();
            CommandSender console = proxy.getConsole();

            PluginManager manager = proxy.getPluginManager();
            manager.dispatchCommand(console, command);
        } catch (IOException ex) {
            Logger logger = plugin.getLogger();
            logger.log(Level.WARNING, "An error occurred while parsing a jc:player command:", ex);
        }
    }

    private void runProxyJoinCommands(ProxiedPlayer player) {
        JoinCommandsBungee plugin = getPlugin();
        CommandManager commandManager = plugin.getCommandManager();
        List<ProxyJoinCommand> commandList = commandManager.getProxyJoinCommandList();

        ProxyServer proxy = plugin.getProxy();
        TaskScheduler scheduler = proxy.getScheduler();

        for (ProxyJoinCommand command : commandList) {
            if (!command.shouldBeExecutedFor(plugin, player)) {
                continue;
            }

            long delay = command.getDelay();
            Runnable task = () -> command.executeFor(plugin, player);
            scheduler.schedule(plugin, task, delay, TimeUnit.SECONDS);
        }
    }

    private void setJoinedProxyBefore(ProxiedPlayer player) {
        JoinCommandsBungee plugin = getPlugin();
        Configuration configuration = plugin.getConfig();
        if(configuration.getBoolean("disable-player-data", false)) {
            return;
        }

        UUID playerId = player.getUniqueId();
        String playerIdString = playerId.toString();
        String path = ("joined-before." + playerIdString);

        configuration.set(path, true);
        plugin.saveConfig();
    }
}
