package com.loohp.skmcbungee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class SudoCommand extends Command implements TabExecutor{

	public SudoCommand() {
		super("bsudo");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (sender instanceof ProxiedPlayer) {
			if (sender.hasPermission("skmc.admin.bsudo")) {
				if (args.length > 1) {		
					String command = "";
					for (String arg : Arrays.copyOfRange(args, 1, args.length)) {
						command = command + arg + " ";
					}
					command = command.substring(0, command.length() - 1);
					ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command);
					sender.sendMessage(new ComponentBuilder("Command Sent as PROXY console").color(ChatColor.GREEN).create());
				} else {
					sender.sendMessage(new ComponentBuilder("Usage: /bsudo console <command>").color(ChatColor.RED).create());
				}
			} else {
				sender.sendMessage(new ComponentBuilder("You do not have permission to use that command!").color(ChatColor.RED).create());
			}
		} else {
			sender.sendMessage(new ComponentBuilder("This command can only be used as a player!").color(ChatColor.RED).create());
		}
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		List<String> results = new ArrayList<>();
		if (args.length == 0) {
			return Collections.emptyList();
	    } else if (args.length == 1) {
	    	results.add("console");
	    }		    
		return results;
	}
}