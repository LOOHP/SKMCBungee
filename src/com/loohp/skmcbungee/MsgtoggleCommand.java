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

public class MsgtoggleCommand extends Command {

	public MsgtoggleCommand() {
		super("msgtoggle");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (sender instanceof ProxiedPlayer) {
			if (sender.hasPermission("skmc.message.msg")) {
				Main.mysqlSetup(false);
				if (args.length > 0) {			
					ProxiedPlayer player = (ProxiedPlayer) sender;
					if (!playerExists(player.getUniqueId())) {
						createPlayer(player.getUniqueId(), player);
					}
					//Operation modes
					//0 == toggle all
					//1 == toggle player
					//2 == clear
					if (args[0].toLowerCase().equals("all")) {
						if (updateBlocked(player.getUniqueId(), 0, args[0]) == true) {
							sender.sendMessage(new ComponentBuilder(ChatColor.YELLOW + "Ignoring all private messages..").create());
						} else {
							sender.sendMessage(new ComponentBuilder(ChatColor.GREEN + "Listening to all private messages that you haven't blocked individually!").create());
						}
					} else if (args[0].toLowerCase().equals("clear")) {
						updateBlocked(player.getUniqueId(), 2 , args[0]);
						sender.sendMessage(new ComponentBuilder(ChatColor.GREEN + "Cleared all blocked players!").create());
					} else if (args[0].toLowerCase().equals("list")) {
						List<String> list = listBlocked(player.getUniqueId());
						List<String> compare = new ArrayList<String>();
						compare.add("");
						if (list.size() == 0 || list.equals(compare)) {
							if (list.contains("all") == true) {
								sender.sendMessage(new ComponentBuilder(ChatColor.AQUA + "***You are currently blocking all private messages!").create());
							} else {
								sender.sendMessage(new ComponentBuilder(ChatColor.GREEN + "You are such a friendly person! You didn\'t block anyone!").create());
							}
						} else {
							if (list.contains("all") == true) {
								sender.sendMessage(new ComponentBuilder(ChatColor.AQUA + "***You are currently blocking all private messages!").create());
							}
							sender.sendMessage(new ComponentBuilder(ChatColor.YELLOW + "Listing all blocked players:").create());
						}
						if (list.size() != 0 && !list.equals(compare)) {
							for (String names : list) {
								if (!names.equals("all")) {
									sender.sendMessage(new ComponentBuilder(ChatColor.RED + names).create());
								}
							}
						}
					} else {
						if (!player.getName().equals(args[0])) {
							if (updateBlocked(player.getUniqueId(), 1 , args[0]) == true) {
								sender.sendMessage(new ComponentBuilder(ChatColor.YELLOW + "Blocking player " + args[0] + " from messaging you..").create());
							} else {
								sender.sendMessage(new ComponentBuilder(ChatColor.GREEN + "Player " + args[0] + " can now message you again!").create());
							}
						} else {
							sender.sendMessage(new ComponentBuilder("You cannot shut yourself up \\_/").color(ChatColor.YELLOW).create());
						}
					}
				} else {
					sender.sendMessage(new ComponentBuilder("Usage: /msgtoggle <all/clear/player/list>").color(ChatColor.RED).create());
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
			if (newBlocked.length() > 0) {
				if (newBlocked.substring(0, 1).equals(",")) {
					newBlocked = newBlocked.substring(1, newBlocked.length());
				}
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