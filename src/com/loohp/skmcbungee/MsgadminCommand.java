package com.loohp.skmcbungee;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class MsgadminCommand extends Command {

	public MsgadminCommand() {
		super("msgadmin");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (sender instanceof ProxiedPlayer) {
			if (sender.hasPermission("skmc.message.admin")) {
				Main.mysqlSetup(false);
				if (args.length > 0) {			
					ProxiedPlayer player = (ProxiedPlayer) sender;
					if (!playerExists(player.getUniqueId())) {
						createPlayer(player.getUniqueId(), player);
					}
					if (args[0].toLowerCase().equals("intercept")) {
					    UUID togglePlayer = null;
						if (args.length < 2) {
							togglePlayer = player.getUniqueId();
						} else {
							togglePlayer = ProxyServer.getInstance().getPlayer(args[1]).getUniqueId();
						}
						if (updateSpy(togglePlayer) == true) {
							if (ProxyServer.getInstance().getPlayer(togglePlayer).equals(player) ) {
								sender.sendMessage(new ComponentBuilder("Enabled message interception").color(ChatColor.GREEN).create());
							} else {
								sender.sendMessage(new ComponentBuilder("Enabled message interception for " + ProxyServer.getInstance().getPlayer(togglePlayer).getName()).color(ChatColor.GREEN).create());
							}
							if (!ProxyServer.getInstance().getPlayer(togglePlayer).equals(player) && ProxyServer.getInstance().getPlayer(togglePlayer).hasPermission("skmc.message.admin")) {
								ProxyServer.getInstance().getPlayer(togglePlayer).sendMessage(new ComponentBuilder(player.getName() + " enabled message interception for you").color(ChatColor.GREEN).create());
							}
						} else {
							if (ProxyServer.getInstance().getPlayer(togglePlayer).equals(player) ) {
								sender.sendMessage(new ComponentBuilder("Disabled message interception").color(ChatColor.RED).create());
							} else {
								sender.sendMessage(new ComponentBuilder("Disabled message interception for " + ProxyServer.getInstance().getPlayer(togglePlayer).getName()).color(ChatColor.RED).create());
							}
							if (!ProxyServer.getInstance().getPlayer(togglePlayer).equals(player) && ProxyServer.getInstance().getPlayer(togglePlayer).hasPermission("skmc.message.admin")) {
								ProxyServer.getInstance().getPlayer(togglePlayer).sendMessage(new ComponentBuilder(player.getName() + " disabled message interception for you").color(ChatColor.RED).create());
							}
						}
					} else if (args[0].toLowerCase().equals("toggle") && args.length == 3) {
						if (ProxyServer.getInstance().getPlayer(args[1]) != null) {
							ProxiedPlayer togglePlayer = ProxyServer.getInstance().getPlayer(args[1]);
							if (args[1].toLowerCase().equals("all")) {
								if (updateBlocked(togglePlayer.getUniqueId(), 0, args[2]) == true) {
									sender.sendMessage(new ComponentBuilder(ChatColor.YELLOW + "Ignoring all private messages for " + togglePlayer.getName()).create());
								} else {
									sender.sendMessage(new ComponentBuilder(ChatColor.GREEN + "Un-ignored private messages that the player didn\'t block for " + togglePlayer.getName()).create());
								}
							} else if (args[2].toLowerCase().equals("clear")) {
								updateBlocked(togglePlayer.getUniqueId(), 2 , args[2]);
								sender.sendMessage(new ComponentBuilder(ChatColor.GREEN + "Cleared all blocked players for " + togglePlayer.getName()).create());
							} else if (args[2].toLowerCase().equals("list")) {
								List<String> list = listBlocked(togglePlayer.getUniqueId());
								List<String> compare = new ArrayList<String>();
								compare.add("");
								if (list.size() == 0 || list.equals(compare)) {
									if (list.contains("all") == true) {
										sender.sendMessage(new ComponentBuilder(ChatColor.AQUA + togglePlayer.getName() + " is currently blocking all private messages").create());
									} else {
										sender.sendMessage(new ComponentBuilder(ChatColor.GREEN + togglePlayer.getName() + " didn\'t block anyone").create());
									}
								} else {
									if (list.contains("all") == true) {
										sender.sendMessage(new ComponentBuilder(ChatColor.AQUA + togglePlayer.getName() + " is currently blocking all private messages").create());
									}
									sender.sendMessage(new ComponentBuilder(ChatColor.YELLOW + "Listing all blocked players by " + togglePlayer.getName() + ":").create());
								}
								if (list.size() != 0 && !list.equals(compare)) {
									for (String names : list) {
										if (!names.equals("all")) {
											sender.sendMessage(new ComponentBuilder(ChatColor.RED + names).create());
										}
									}
								}
							} else {
								if (!togglePlayer.getName().equals(args[2])) {
									if (updateBlocked(togglePlayer.getUniqueId(), 1 , args[2]) == true) {
										sender.sendMessage(new ComponentBuilder(ChatColor.YELLOW + "Blocked player " + args[2] + " from messaging " + togglePlayer.getName()).create());
									} else {
										sender.sendMessage(new ComponentBuilder(ChatColor.GREEN + "Player " + args[2] + " can now message " + togglePlayer.getName()).create());
									}
								} else {
									sender.sendMessage(new ComponentBuilder("You cannot block " + togglePlayer.getName() + " from himself/herself").color(ChatColor.YELLOW).create());
								}
							}
						} else {
							sender.sendMessage(new ComponentBuilder("The player isn\'t online").color(ChatColor.RED).create());
						}
					} else {
						sender.sendMessage(new ComponentBuilder("Usage: /msgadmin <intercept/toggle> [intercept: [player]/toggle: <player>] [toggle: <all/clear/player/list>]").color(ChatColor.RED).create());
					}
				} else {
					sender.sendMessage(new ComponentBuilder("Usage: /msgadmin <intercept/toggle> [intercept: [player]/toggle: <player>] [toggle: <all/clear/player/list>]").color(ChatColor.RED).create());
				}
				
				try {
					Main.getConnection().close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
			} else {
				sender.sendMessage(new ComponentBuilder("You do not have permission to use that command!").color(ChatColor.RED).create());
			}
		} else {
			sender.sendMessage(new ComponentBuilder("This command can only be used as a player!").color(ChatColor.RED).create());
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
	
	public List<String> listBlocked(UUID uuid) {
		List<String> list = new ArrayList<String>();
		try {
			PreparedStatement statement = Main.getConnection().prepareStatement("SELECT * FROM " + Main.table + " WHERE UUID=?");
			statement.setString(1, uuid.toString());
			ResultSet results = statement.executeQuery();
			results.next();
			String[] string = results.getString("BLOCKED").split(",");
			list = Arrays.asList(string);
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean updateSpy(UUID uuid) {
		boolean toggle = false;
		try {
			PreparedStatement statement = Main.getConnection().prepareStatement("SELECT * FROM " + Main.table + " WHERE UUID=?");
			statement.setString(1, uuid.toString());
			ResultSet results = statement.executeQuery();
			results.next();
			
			toggle = results.getBoolean("SOCIALSPY");
		} catch (SQLException e) {
			e.printStackTrace();
		}
			
		if (toggle == true) {
			toggle = false;
		} else {
			toggle = true;
		} 
		
		try {
			PreparedStatement statement = Main.getConnection().prepareStatement("UPDATE " + Main.table + " SET SOCIALSPY=? WHERE UUID=?");
			statement.setBoolean(1, toggle);
			statement.setString(2, uuid.toString());
			statement.executeUpdate();
			
			Main.msgSpy.put(ProxyServer.getInstance().getPlayer(uuid), toggle);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return toggle;
	}	
	
	public boolean updateBlocked(UUID uuid, int Operation, String arg) {
		boolean mode = false;
		List<String> blocked = new ArrayList<String>();
		String raw = null;
		try {
			PreparedStatement statement = Main.getConnection().prepareStatement("SELECT * FROM " + Main.table + " WHERE UUID=?");
			statement.setString(1, uuid.toString());
			ResultSet results = statement.executeQuery();
			results.next();
			
			raw = results.getString("BLOCKED");
			String[] string = results.getString("BLOCKED").split(",");
			blocked = Arrays.asList(string);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		//Operation modes
		//0 == toggle all
		//1 == toggle player
		//2 == clear
		
		//10 == add all
		//11 == remove all
		//12 == add player
		//13 == remove player
		//14 == clear
		
		if (Operation == 0) {
			if (!blocked.contains(arg)) {
				Operation = 10;
				mode = true;
			} else {
				Operation = 11;
				mode = false;
			}
		} else if (Operation == 1) {
			if (!blocked.contains(arg)) {
				Operation = 12;
				mode = true;
			} else {
				Operation = 13;
				mode = false;
			}
		} else if (Operation == 2) {
			Operation = 14;
			mode = false;
		}
		
		String newBlocked = raw;
		if (Operation == 10) {
			newBlocked = newBlocked + "," + arg;
			if (newBlocked.substring(0, 1).equals(",")) {
				newBlocked = newBlocked.substring(1, newBlocked.length());
			}
		} else if (Operation == 11) {
			newBlocked = "";
			for (String string : blocked) {
				if (!string.toLowerCase().trim().equals(arg.toLowerCase().trim())) {
					newBlocked = newBlocked + "," + string;
				}
			}
			if (newBlocked.substring(0, 1).equals(",")) {
				newBlocked = newBlocked.substring(1, newBlocked.length());
			}
		} else if (Operation == 12) {
			newBlocked = newBlocked + "," + arg;
			if (newBlocked.length() > 0) {
				if (newBlocked.substring(0, 1).equals(",")) {
					newBlocked = newBlocked.substring(1, newBlocked.length());
				}
			}
		} else if (Operation == 13) {
			newBlocked = "";
			for (String string : blocked) {
				if (!string.toLowerCase().trim().equals(arg.toLowerCase().trim())) {
					newBlocked = newBlocked + "," + string;
				}
			}
			if (newBlocked.length() > 0) {
				if (newBlocked.substring(0, 1).equals(",")) {
					newBlocked = newBlocked.substring(1, newBlocked.length());
				}
			}
			ProxyServer.getInstance().getLogger().info(newBlocked);
		} else if (Operation == 14) {
			newBlocked = "";
		}
			
		try {
			PreparedStatement statement = Main.getConnection().prepareStatement("UPDATE " + Main.table + " SET BLOCKED=? WHERE UUID=?");
			statement.setString(1, newBlocked);
			statement.setString(2, uuid.toString());
			statement.executeUpdate();
			
			Main.msgBlock.put(ProxyServer.getInstance().getPlayer(uuid), Arrays.asList(newBlocked.split(",")));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return mode;
	}	
}