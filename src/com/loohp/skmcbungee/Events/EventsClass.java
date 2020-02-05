package com.loohp.skmcbungee.Events;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.UUID;

import com.loohp.skmcbungee.Main;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ProxyReloadEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class EventsClass implements Listener {
	
	@EventHandler
	public void onReloadBungee(ProxyReloadEvent event) {
		Main.loadConfig();
		event.getSender().sendMessage(new TextComponent(ChatColor.GREEN + "SKMCBungee reloaded"));
	}

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
    	ProxyServer.getInstance().getConsole().sendMessage(new TextComponent(ChatColor.GREEN + event.getPlayer().getName() + " triggered PostLoginEvent"));
    	
    	long unixTime = System.currentTimeMillis();
    	Main.playerConnect.put(event.getPlayer(), unixTime);
    	
    	Main.playerServer.put(event.getPlayer(), "NEW");
    	
    	Main.mysqlSetup(false);
    	
    	if (!playerExists(event.getPlayer().getUniqueId())) {
			createPlayer(event.getPlayer().getUniqueId(), event.getPlayer());
		}
    	
    	try {
			PreparedStatement statement = Main.getConnection().prepareStatement("SELECT * FROM " + Main.table + " WHERE UUID=?");
			statement.setString(1, event.getPlayer().getUniqueId().toString());
			ResultSet results = statement.executeQuery();
			results.next();
			String[] string = results.getString("BLOCKED").split(",");
			Main.msgBlock.put(event.getPlayer(), Arrays.asList(string));
			boolean spy = results.getBoolean("BLOCKED");
			Main.msgSpy.put(event.getPlayer(), spy);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	try {
			Main.getConnection().close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    }
    
    public boolean playerExists(UUID uuid) {
		try {
			PreparedStatement statement = Main.getConnection().prepareStatement("SELECT * FROM " + Main.table + " WHERE UUID=?");
			statement.setString(1, uuid.toString());

			ResultSet results = statement.executeQuery();
			if (results.next()) {
				return true;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void createPlayer(UUID uuid, ProxiedPlayer player) {
		try {
			PreparedStatement statement = Main.getConnection().prepareStatement("SELECT * FROM " + Main.table + " WHERE UUID=?");
			statement.setString(1, uuid.toString());
			ResultSet results = statement.executeQuery();
			results.next();
			System.out.print(1);
			if (playerExists(uuid) != true) {
				PreparedStatement insert = Main.getConnection().prepareStatement("INSERT INTO " + Main.table + " (UUID,NAME,BLOCKED,SOCIALSPY) VALUES (?,?,?,?)");
				insert.setString(1, uuid.toString());
				insert.setString(2, player.getName());
				insert.setString(3, "");
				insert.setBoolean(4, false);
				insert.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
    
    @EventHandler
    public void onPostDisconnect(PlayerDisconnectEvent event) {
    	ProxyServer.getInstance().getConsole().sendMessage(new TextComponent(ChatColor.RED + event.getPlayer().getName() + " triggered PlayerDisconnectEvent"));
    	Main.msgBlock.remove(event.getPlayer());
    	Main.msgSpy.remove(event.getPlayer());
    	if (!Main.playerConnect.containsKey(event.getPlayer())) {
    		String message = Main.configuration.getString("NETWORK.Leave");
	    	if (!message.equals("")) {
	    		message = message.replace("%Player%", event.getPlayer().getDisplayName());
	    		message = message.replace("&", "§");
	    		for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
	    			player.sendMessage(new TextComponent(message));
	    		}
	    	}
    	}
    	if (Main.playerConnect.containsKey(event.getPlayer())) {
    		Main.playerConnect.remove(event.getPlayer());
    	}
    	if (Main.playerServer.containsKey(event.getPlayer())) {
    		Main.playerServer.remove(event.getPlayer());
    	}
    }
    
    @EventHandler
    public void onConnected(ServerSwitchEvent event) {
    	ProxyServer.getInstance().getConsole().sendMessage(new TextComponent(ChatColor.YELLOW + event.getPlayer().getName() + " --> " + event.getPlayer().getServer().getInfo().getName()));
    	if (Main.playerConnect.containsKey(event.getPlayer())) {
    		String message = Main.configuration.getString("NETWORK.Join");
	    	if (!message.equals("")) {
	    		message = message.replace("%SwitchTo%", Main.serverNickname.get(event.getPlayer().getServer().getInfo().getName()));
	    		message = message.replace("%Player%", event.getPlayer().getDisplayName());
	    		message = message.replace("&", "§");
	    		for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
	    			player.sendMessage(new TextComponent(message));
	    		}
	    	}
	    	Main.playerConnect.remove(event.getPlayer());
    	}
    	if (Main.playerServer.containsKey(event.getPlayer())) {
    		if (!Main.serverGrouping.get(Main.playerServer.get(event.getPlayer())).equals(Main.serverGrouping.get(event.getPlayer().getServer().getInfo().getName()))) {
    			String message = "";
    			if (Main.configuration.contains(Main.serverGrouping.get(event.getPlayer().getServer().getInfo().getName()) + ".Join")) {
    				message = Main.configuration.getString(Main.serverGrouping.get(event.getPlayer().getServer().getInfo().getName()) + ".Join");
    			} else {
    				message = Main.configuration.getString("DEFAULT.Join");
    			}
		    	if (!message.equals("")) {
		    		message = message.replace("%SwitchTo%", Main.serverNickname.get(event.getPlayer().getServer().getInfo().getName()));
		    		message = message.replace("%Player%", event.getPlayer().getDisplayName());
		    		message = message.replace("&", "§");
		    		for (Entry<String, String> entry : Main.serverGrouping.entrySet()) {
		    			if (entry.getValue().equals(Main.serverGrouping.get(event.getPlayer().getServer().getInfo().getName()))) {
		    				for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
		    					if (player.getServer() != null) {
		    						if (Main.serverGrouping.get(player.getServer().getInfo().getName()).equals(Main.serverGrouping.get(event.getPlayer().getServer().getInfo().getName()))) {
		    							player.sendMessage(new TextComponent(message));
		    						}
		    					}
		    				}
		    				break;
		    			}
		    		}
		    	}
    		} else {
    			String message = "";
    			if (Main.configuration.contains(Main.serverGrouping.get(event.getPlayer().getServer().getInfo().getName()) + ".Switch")) {
    				message = Main.configuration.getString(Main.serverGrouping.get(event.getPlayer().getServer().getInfo().getName()) + ".Switch");
    			} else {
    				message = Main.configuration.getString("DEFAULT.Switch");
    			}
    			if (!message.equals("")) {
    				message = message.replace("%SwitchTo%", Main.serverNickname.get(event.getPlayer().getServer().getInfo().getName()));
    	    		message = message.replace("%Player%", event.getPlayer().getDisplayName());
    	    		message = message.replace("&", "§");
    	    		for (Entry<String, String> entry : Main.serverGrouping.entrySet()) {
		    			if (entry.getValue().equals(Main.serverGrouping.get(event.getPlayer().getServer().getInfo().getName()))) {
		    				for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
		    					if (player.getServer() != null) {
		    						if (Main.serverGrouping.get(player.getServer().getInfo().getName()).equals(Main.serverGrouping.get(event.getPlayer().getServer().getInfo().getName()))) {
		    							player.sendMessage(new TextComponent(message));
		    						}
		    					}
		    				}
		    				break;
		    			}
		    		}
    			}
    		}
    	}
    	if (Main.playerServer.containsKey(event.getPlayer())) {
    		if (!Main.serverGrouping.get(Main.playerServer.get(event.getPlayer())).equals(Main.serverGrouping.get(event.getPlayer().getServer().getInfo().getName()))) {
    			String message = "";
    			if (Main.configuration.contains(Main.serverGrouping.get(Main.playerServer.get(event.getPlayer())) + ".Leave")) {
    				message = Main.configuration.getString(Main.serverGrouping.get(Main.playerServer.get(event.getPlayer())) + ".Leave");
    			} else {
    				message = Main.configuration.getString("DEFAULT.Leave");
    			}
		    	if (!message.equals("")) {
		    		message = message.replace("%SwitchTo%", Main.serverNickname.get(event.getPlayer().getServer().getInfo().getName()));
		    		message = message.replace("%Player%", event.getPlayer().getDisplayName());
		    		message = message.replace("&", "§");
		    		for (Entry<String, String> entry : Main.serverGrouping.entrySet()) {
		    			if (entry.getValue().equals(Main.serverGrouping.get(Main.playerServer.get(event.getPlayer())))) {
		    				for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
		    					if (player.getServer() != null) {
		    						if (Main.serverGrouping.get(player.getServer().getInfo().getName()).equals(Main.serverGrouping.get(Main.playerServer.get(event.getPlayer())))) {
		    							player.sendMessage(new TextComponent(message));
		    						}
		    					}
		    				}
		    				break;
		    			}
		    		}
		    	}
    		}
    	}
    	Main.playerServer.put(event.getPlayer(), event.getPlayer().getServer().getInfo().getName());
    }
    
    @EventHandler
    public void onPlayerChat(ChatEvent event) {
    	if (event.getMessage().toLowerCase().startsWith("/pm") || event.getMessage().toLowerCase().startsWith("/tell")) {
    		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
    		event.setCancelled(true);
    		player.sendMessage(new TextComponent(ChatColor.GRAY + "Please use /msg instead!"));
    	}
    	
    	if (event.getMessage().toLowerCase().startsWith("/end")) {
    		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
    		event.setCancelled(true);
    		player.sendMessage(new TextComponent(ChatColor.RED + "This command has been DISABLED!"));
    	}
    	
    	if (event.getMessage().toLowerCase().startsWith("/")) {
    		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
    		if (!ProxyServer.getInstance().getPlayer(player.getUniqueId()).hasPermission("skmc.admin.interceptcommands.exempt")) {
    			String command = event.getMessage();
    			String[] label = command.split(" ");
    			if (label[0].startsWith("/login") || label[0].startsWith("/register") || label[0].startsWith("/changepassword") || label[0].equalsIgnoreCase("/msg") || label[0].equalsIgnoreCase("/pm") || label[0].equalsIgnoreCase("/r")) {
    				command = label[0];
    			}
    			ProxyServer.getInstance().getConsole().sendMessage(new TextComponent(ChatColor.GREEN + player.getName() + ChatColor.AQUA + " executed command at " + ChatColor.GREEN + player.getServer().getInfo().getName() + ChatColor.AQUA + " --> " + ChatColor.YELLOW + command));
    			for (ProxiedPlayer onlinePlayer : ProxyServer.getInstance().getPlayers()) {
    				if (onlinePlayer.hasPermission("skmc.admin.interceptcommands") && onlinePlayer != player) {
    					onlinePlayer.sendMessage(new TextComponent(ChatColor.GREEN + player.getName() + ChatColor.AQUA + " executed command at " + ChatColor.GREEN + player.getServer().getInfo().getName() + ChatColor.AQUA + " --> \n" + ChatColor.YELLOW + command));
    					}
    				}
    			}
    	} else {
			ProxiedPlayer player = (ProxiedPlayer) event.getSender();
    		if (!ProxyServer.getInstance().getPlayer(player.getUniqueId()).hasPermission("skmc.admin.interceptchat.exempt")) {
    			String chat = event.getMessage();
    			ProxyServer.getInstance().getConsole().sendMessage(new TextComponent("[Chat] " + player.getName() + " @ " + player.getServer().getInfo().getName() + " --> " + chat));
    			for (ProxiedPlayer onlinePlayer : ProxyServer.getInstance().getPlayers()) {
    				if (onlinePlayer.hasPermission("skmc.admin.interceptchat") && !onlinePlayer.getServer().getInfo().getName().startsWith(player.getServer().getInfo().getName())) {
    						onlinePlayer.sendMessage(new TextComponent(ChatColor.GRAY + "[Chat Monitor] " + player.getName() + " @ " + player.getServer().getInfo().getName() + " --> " + chat));
    				}
    			}
    		}
    	}
    }
}