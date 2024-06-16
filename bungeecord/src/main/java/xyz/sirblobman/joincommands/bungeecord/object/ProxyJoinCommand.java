package xyz.sirblobman.joincommands.bungeecord.object;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;

import xyz.sirblobman.joincommands.bungeecord.JoinCommandsPlugin;
import xyz.sirblobman.joincommands.common.utility.Validate;

public final class ProxyJoinCommand {
    private final List<String> commandList;
    private final String permission;
    private final boolean firstJoinOnly;
    private final long delay;

    public ProxyJoinCommand(@NotNull List<String> commandList, @NotNull String permission, boolean firstJoinOnly,
                            long delay) {
        this.commandList = Validate.notEmpty(commandList, "commandList must not be empty!");
        this.permission = permission;

        this.firstJoinOnly = firstJoinOnly;
        this.delay = delay;
    }

    public @NotNull List<String> getCommands() {
        return Collections.unmodifiableList(this.commandList);
    }

    public @NotNull String getPermission() {
        return this.permission;
    }

    public boolean isFirstJoinOnly() {
        return this.firstJoinOnly;
    }

    public long getDelay() {
        return this.delay;
    }

    public boolean canExecute(@NotNull JoinCommandsPlugin plugin, @NotNull ProxiedPlayer player) {
        if (isFirstJoinOnly() && hasJoinedBefore(plugin, player)) {
            return false;
        }

        String permission = getPermission();
        if (!permission.isEmpty()) {
            return player.hasPermission(this.permission);
        }

        return true;
    }

    public void execute(@NotNull JoinCommandsPlugin plugin, @NotNull ProxiedPlayer player) {
        String playerName = player.getName();
        List<String> commandList = getCommands();
        for (String command : commandList) {
            String replaced = command.replace("{player}", playerName);
            if (replaced.startsWith("[PLAYER]")) {
                String playerCommand = replaced.substring(8);
                runAsPlayer(plugin, player, playerCommand);
            } else {
                runAsConsole(plugin, replaced);
            }
        }
    }

    private boolean hasJoinedBefore(@NotNull JoinCommandsPlugin plugin, @NotNull ProxiedPlayer player) {
        Configuration configuration = plugin.getConfig();
        if (configuration.getBoolean("disable-player-data", false)) {
            return false;
        }

        UUID playerId = player.getUniqueId();
        String playerIdString = playerId.toString();
        String path = ("joined-before." + playerIdString);
        return configuration.getBoolean(path, false);
    }

    private void runAsPlayer(@NotNull JoinCommandsPlugin plugin, @NotNull ProxiedPlayer player,
                             @NotNull String command) {
        if (command.isEmpty()) {
            return;
        }

        try {
            ProxyServer proxy = plugin.getProxy();
            PluginManager manager = proxy.getPluginManager();
            manager.dispatchCommand(player, command);
        } catch (Exception ex) {
            Logger logger = plugin.getLogger();
            String errorMessage = "An error occurred while executing command '/" + command + "' as a player:";
            logger.log(Level.WARNING, errorMessage, ex);
        }
    }

    private void runAsConsole(@NotNull JoinCommandsPlugin plugin, @NotNull String command) {
        if (command.isEmpty()) {
            return;
        }

        try {
            ProxyServer proxy = plugin.getProxy();
            CommandSender console = proxy.getConsole();
            PluginManager manager = proxy.getPluginManager();
            manager.dispatchCommand(console, command);
        } catch (Exception ex) {
            Logger logger = plugin.getLogger();
            String errorMessage = "An error occurred while executing command '/" + command + "' in console:";
            logger.log(Level.WARNING, errorMessage, ex);
        }
    }
}
