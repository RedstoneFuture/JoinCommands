package com.SirBlobman.join.commands.bungee;

import com.SirBlobman.join.commands.bungee.listener.ListenerJoinCommands;
import com.SirBlobman.join.commands.bungee.manager.CommandManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JoinCommandsBungee extends Plugin {
    private final CommandManager commandManager = new CommandManager(this);
    
    @Override
    public void onEnable() {
        loadConfig();
        
        CommandManager commandManager = getCommandManager();
        commandManager.loadProxyJoinCommands();
        
        registerChannels();
        registerListener();
    }
    
    public CommandManager getCommandManager() {
        return this.commandManager;
    }
    
    private Configuration config;
    public Configuration getConfig() {
        if(this.config == null) loadConfig();
        
        return this.config;
    }
    
    public void saveConfig() {
        File pluginFolder = getDataFolder();
        if(!pluginFolder.exists()) pluginFolder.mkdirs();
        
        try {
            ConfigurationProvider provider = ConfigurationProvider.getProvider(YamlConfiguration.class);
            File file = new File(pluginFolder, "config.yml");
            provider.save(this.config, file);
        } catch(IOException ex) {
            Logger logger = getLogger();
            logger.log(Level.WARNING, "An error occurred while saving the 'config.yml' file:", ex);
        }
    }
    
    private void registerChannels() {
        ProxyServer proxy = getProxy();
        proxy.registerChannel("jc:player");
        proxy.registerChannel("jc:console");
    }
    
    private void registerListener() {
        ProxyServer proxy = getProxy();
        PluginManager manager = proxy.getPluginManager();
        
        Listener listener = new ListenerJoinCommands(this);
        manager.registerListener(this, listener);
    }
    
    private void loadConfig() {
        File pluginFolder = getDataFolder();
        if(!pluginFolder.exists()) pluginFolder.mkdirs();
        
        File file = new File(pluginFolder, "config.yml");
        if(!file.exists()) saveDefaultConfig();
        
        try {
            ConfigurationProvider provider = ConfigurationProvider.getProvider(YamlConfiguration.class);
            this.config = provider.load(file);
        } catch(IOException ex) {
            Logger logger = getLogger();
            logger.log(Level.WARNING, "An error occurred while loading the 'config.yml' file:", ex);
            this.config = null;
        }
    }
    
    private void saveDefaultConfig() {
        File pluginFolder = getDataFolder();
        if(!pluginFolder.exists()) pluginFolder.mkdirs();
        
        File file = new File(pluginFolder, "config.yml");
        Path path = file.toPath();
        
        try {
            InputStream configStream = getResourceAsStream("config.yml");
            Files.copy(configStream, path, StandardCopyOption.REPLACE_EXISTING);
        } catch(IOException ex) {
            Logger logger = getLogger();
            logger.log(Level.WARNING, "An error occurred while creating the default 'config.yml' file:", ex);
        }
    }
}