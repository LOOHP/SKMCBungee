package com.loohp.skmcbungee;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CustomSendCommand extends Command implements TabExecutor{

	public CustomSendCommand() {
		super("send");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (sender.hasPermission("bungeecord.command.send")) {
			if (args.length == 2) {
				if (ProxyServer.getInstance().getServerInfo(args[1]) == null) {
					if (ProxyServer.getInstance().getPlayer(args[1]) != null) {
						args[1] = ProxyServer.getInstance().getPlayer(args[1]).getServer().getInfo().getName();
					}
				}
				if (ProxyServer.getInstance().getServerInfo(args[1]) != null) {
					if (ProxyServer.getInstance().getPlayer(args[0]) != null) {
						ProxyServer.getInstance().getPlayer(args[0]).connect(ProxyServer.getInstance().getServerInfo(args[1]));
						sender.sendMessage(new ComponentBuilder(ChatColor.AQUA + "ServerConnector > " + ChatColor.GREEN + "Sent " + ProxyServer.getInstance().getPlayer(args[0]).getName() + " to " + ProxyServer.getInstance().getServerInfo(args[1]).getName() + "!").create());
					} else if (args[0].equalsIgnoreCase("all")) {
						for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
							player.connect(ProxyServer.getInstance().getServerInfo(args[1]));
							sender.sendMessage(new ComponentBuilder(ChatColor.AQUA + "ServerConnector > " + ChatColor.GREEN + "Sent " + player.getName() + " to " + ProxyServer.getInstance().getServerInfo(args[1]).getName() + "!").color(ChatColor.GREEN).create());
						}
					} else if (ProxyServer.getInstance().getServerInfo(args[0]) != null) {
						for (ProxiedPlayer player : ProxyServer.getInstance().getServerInfo(args[0]).getPlayers()) {
							player.connect(ProxyServer.getInstance().getServerInfo(args[1]));
							sender.sendMessage(new ComponentBuilder(ChatColor.AQUA + "ServerConnector > " + ChatColor.GREEN + "Sent " + player.getName() + " to " + ProxyServer.getInstance().getServerInfo(args[1]).getName() + "!").color(ChatColor.GREEN).create());
						}
					} else {
						sender.sendMessage(new ComponentBuilder(ChatColor.RED + "Error: Player \"" + ChatColor.YELLOW + args[0] + ChatColor.RED + "\" does not exists!").create());
					}
				} else {
					sender.sendMessage(new ComponentBuilder(ChatColor.RED + "Error: Server \"" + ChatColor.YELLOW + args[1] + ChatColor.RED + "\" does not exists!").create());
				}
			} else {
			sender.sendMessage(new ComponentBuilder("Usage: /send <player/server/all> <server>").color(ChatColor.RED).create());
			}
		} else {
			sender.sendMessage(new ComponentBuilder("You do not have permission to use that command!").color(ChatColor.RED).create());
		}
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		if (sender.hasPermission("bungeecord.command.send")) {
			List<String> results = new ArrayList<>();
			if (args.length == 0) {
				return Collections.emptyList();
		    } else if (args.length == 1) {
			    for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
			    	results.add(player.getName());
			    }
			    for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
			    	results.add(server.getName());
			    }		    
			    results.add("all");
		    } else if (args.length == 2) {
			    for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
			    	results.add(server.getName());
			    }		    
		    }
			return results;
		}
		return new ArrayList<>();
	}
}