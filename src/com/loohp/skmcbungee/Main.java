package com.loohp.skmcbungee;

import com.loohp.skmcbungee.Events.EventsClass;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.loohp.skmcbungee.SendCommand;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class Main extends Plugin {
	
	public static java.sql.Connection connection;
    public static String host, database, username, password, table;
    public static int port;
	
	public static Map<ProxiedPlayer, ProxiedPlayer> msgPlayer = new HashMap<ProxiedPlayer, ProxiedPlayer>();
	public static PrintWriter writer = null;
	
	public static Map<ProxiedPlayer, List<String>> msgBlock = new HashMap<ProxiedPlayer, List<String>>();
	public static Map<ProxiedPlayer, Boolean> msgSpy = new HashMap<ProxiedPlayer, Boolean>();
	
	public static Map<ProxiedPlayer, Long> playerConnect = new HashMap<ProxiedPlayer, Long>();
	public static Map<ProxiedPlayer, String> playerServer = new HashMap<ProxiedPlayer, String>();
	public static Map<String, String> serverGrouping = new HashMap<String, String>();
	public static Map<String, String> serverNickname = new HashMap<String, String>();
	
	public static Plugin skmcbungee = null;
	
	public static net.md_5.bungee.config.Configuration configuration = null;
	public static ConfigurationProvider config = null;
			
	@Override
    public void onEnable() {
		
		skmcbungee = ProxyServer.getInstance().getPluginManager().getPlugin("SKMCBungee");
		config = ConfigurationProvider.getProvider(YamlConfiguration.class);
		
		getProxy().getPluginManager().registerCommand(this, new ReloadCommand());
		getProxy().getPluginManager().registerCommand(this, new SendCommand());
		getProxy().getPluginManager().registerCommand(this, new SudoCommand());
		getProxy().getPluginManager().registerCommand(this, new MsgCommand());
		getProxy().getPluginManager().registerCommand(this, new ReplyCommand());
		getProxy().getPluginManager().registerCommand(this, new MsgtoggleCommand());
		getProxy().getPluginManager().registerCommand(this, new MsgadminCommand());
		getProxy().getPluginManager().registerListener(this, new EventsClass());
        getLogger().info(ChatColor.GREEN + "SKMCBungee has been enabled!");  
        
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = new File(getDataFolder(), "config.yml");

        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        loadConfig();
        
        String fileName = new SimpleDateFormat("yyyy'-'MM'-'dd'_'HH'-'mm'-'ss'_'zzz'.txt'").format(new Date());
        File dir = new File("plugins/SKMCBungee/logs/messages");
        dir.mkdirs();
        File file1 = new File(dir, fileName);
        getLogger().info(ChatColor.GREEN + "Creating logging file " + file1.toString());
        try {
			file1.createNewFile();
		} catch (IOException e1) {
			//e1.printStackTrace();
			getLogger().info(ChatColor.RED + "Failed to create logging file! (IOException)");
		}
        try {
			writer = new PrintWriter(file1, "UTF-8");
			getLogger().info(ChatColor.GREEN + "Logger started!");
			String time = new SimpleDateFormat("yyyy'-'MM'-'dd' 'HH':'mm':'ss'('zzz')'").format(new Date());
			Main.writer.println("[Message Logger] " + time + " > Logging Session Started!");
		} catch (FileNotFoundException e) {
			getLogger().info(ChatColor.RED + "Failed to start logging session! (FileNotFoundException)");
		} catch (UnsupportedEncodingException e) {
			getLogger().info(ChatColor.RED + "Failed to start logging session! (UnsupportedEncodingException)");
		}
    } 
	
	@Override
    public void onDisable() {
        getLogger().info(ChatColor.RED + "SKMCBungee has been disabled!");
        String time = new SimpleDateFormat("yyyy'-'MM'-'dd' 'HH':'mm':'ss'('zzz')'").format(new Date());
        Main.writer.println("[Message Logger] " + time + " > Logging Session Closed!");
        writer.close();
    } 
	
	public static void loadConfig() {
		try {
			configuration = config.load(new File(skmcbungee.getDataFolder(), "config.yml"));
			Main.saveConfig();
			
			for (Entry<String, ServerInfo> entry : ProxyServer.getInstance().getServers().entrySet()) {
				String server = entry.getValue().getName();
				if (Main.configuration.contains("ServerGrouping." + server)) {
					Main.serverGrouping.put(server, Main.configuration.getString("ServerGrouping." + server));
				} else {
					Main.serverGrouping.put(server, server.toUpperCase() + "_SELF_GROUP");
				}
			}
			Main.serverGrouping.put("NEW", "NEWJOIN");
			for (Entry<String, ServerInfo> entry : ProxyServer.getInstance().getServers().entrySet()) {
				String server = entry.getValue().getName();
				if (Main.configuration.contains("ServerNicknames." + server)) {
					Main.serverNickname.put(server, Main.configuration.getString("ServerNicknames." + server));
				} else {
					Main.serverNickname.put(server, server);
				}
			}
			
		} catch (IOException e4) {
			e4.printStackTrace();
		}
		
		mysqlSetup(true);   
        
        try {
			if (getConnection() != null && !getConnection().isClosed()) {
			}
		} catch (SQLException e3) {
			e3.printStackTrace();
		}
        
        try {
			getConnection().close();
		} catch (SQLException e2) {
			e2.printStackTrace();
		}
	}
	
	public static void saveConfig() {
		try {
			config.save(configuration, new File(skmcbungee.getDataFolder(), "config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void mysqlSetup(boolean echo) {
        host = configuration.getString("MYSQL.host");
        port =  configuration.getInt("MYSQL.port");
        database = configuration.getString("MYSQL.database");
        username = configuration.getString("MYSQL.username");
        password = configuration.getString("MYSQL.password");
		table = "msgtoggle";

		try {
			synchronized (Main.class) {
				if (getConnection() != null && !getConnection().isClosed()) {
					ProxyServer.getInstance().getLogger().info(ChatColor.RED + "MYSQL Failed to connect! [getConnection() != null && !getConnection().isClosed()]");
					return;
				}
				Class.forName("com.mysql.jdbc.Driver");
				setConnection(DriverManager.getConnection("jdbc:mysql://" + Main.host + ":" + Main.port + "/" + Main.database, Main.username, Main.password));
				
				if (echo == true) {
					ProxyServer.getInstance().getLogger().info(ChatColor.GREEN + "MYSQL CONNECTED");
				}
			}
		} catch (SQLException e) {
			ProxyServer.getInstance().getLogger().info(ChatColor.RED + "MYSQL Failed to connect! (SQLException)");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			ProxyServer.getInstance().getLogger().info(ChatColor.RED + "MYSQL Failed to connect! (ClassNotFoundException)");
			e.printStackTrace();
		}
	}

	public static Connection getConnection() {
		return Main.connection;
	}

	public static void setConnection(Connection connection) {
		Main.connection = connection;
	}
		  
}
