package com.loohp.skmcbungee;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map.Entry;

import de.myzelyam.api.vanish.BungeeVanishAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class ReplyCommand extends Command {

	public ReplyCommand() {
		super("r");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (sender instanceof ProxiedPlayer) {
			if (sender.hasPermission("skmc.message.msg")) {
				if (args.length > 0) {			
					ProxiedPlayer player = (ProxiedPlayer) sender;
					if (Main.msgPlayer.containsKey(player)) {
						ProxiedPlayer target = Main.msgPlayer.get(player);
						if (ProxyServer.getInstance().getPlayer(target.getUniqueId()) != null) {
							if (!BungeeVanishAPI.isInvisible(ProxyServer.getInstance().getPlayer(target.getUniqueId())) || sender.hasPermission("skmc.message.admin")) {
								String message = "";
								for (String arg : Arrays.copyOfRange(args, 0, args.length)) {
									message = message + arg + " ";
								}
								message = message.substring(0, message.length() - 1);
								Main.msgPlayer.put(player, target);
								player.sendMessage(new ComponentBuilder("§f[§c" + player.getDisplayName() + " §f➸ §a " + target.getDisplayName() + "§b §c(Outgoing)§f] §b" + message).create());
								if ((!Main.msgBlock.get(target).contains("all") && !Main.msgBlock.get(target).contains(player.getName())) || player.hasPermission("skmc.message.admin")) {
									if (Main.msgBlock.get(target).contains("all") || Main.msgBlock.get(target).contains(player.getName())) {
										sender.sendMessage(new ComponentBuilder("Bypassing private message block from the recieving end..").color(ChatColor.GRAY).create());
									}
									Main.msgPlayer.put(target, player);
									target.sendMessage(new ComponentBuilder("§f[§a" + player.getDisplayName() + " §f➸ §c " + target.getDisplayName() + "§b §a(Incomming)§f] §b" + message).create());
									target.sendMessage(new ComponentBuilder("§7Use §6/r <message>§7 to reply!").create());
									String time = new SimpleDateFormat("yyyy'-'MM'-'dd' 'HH':'mm':'ss'('zzz')'").format(new Date());
									Main.writer.println("[Message Logger] " + time + " [" + player.getServer().getInfo().getName() + "]" + player.getName() + " >[" + target.getServer().getInfo().getName() + "]" + target.getName() + " > " + message);
								} else {
									String time = new SimpleDateFormat("yyyy'-'MM'-'dd' 'HH':'mm':'ss'('zzz')'").format(new Date());
									Main.writer.println("[Message Logger] " + time + " [" + player.getServer().getInfo().getName() + "]" + player.getName() + " >[" + target.getServer().getInfo().getName() + "]" + target.getName() + " > " + message + " (Blocked by reciever)");
								}
								for (Entry<ProxiedPlayer, Boolean> entry : Main.msgSpy.entrySet()) {
									if (entry.getValue() == true) {
										if (!entry.getKey().equals(player) || !entry.getKey().equals(target)) {
											entry.getKey().sendMessage(new ComponentBuilder("§c§lAdmin §8| §f[§c" + player.getDisplayName() + " §f➸ §a " + target.getDisplayName() + "§f] §b" + message).create());
										}
									}
								}
							} else {
								sender.sendMessage(new ComponentBuilder("The player you tried to messsage isn\'t online!").color(ChatColor.YELLOW).create());
								Main.msgPlayer.remove(player);
							}
						} else {
							sender.sendMessage(new ComponentBuilder("The player you tried to messsage isn\'t online!").color(ChatColor.YELLOW).create());
							Main.msgPlayer.remove(player);
						}
					} else {
						sender.sendMessage(new ComponentBuilder("You have nobody to reply to! Use /msg instead!").color(ChatColor.YELLOW).create());
					}
				} else {
					ProxiedPlayer player = (ProxiedPlayer) sender;
					if (Main.msgPlayer.containsKey(player)) {
						ProxiedPlayer target = Main.msgPlayer.get(player);
						sender.sendMessage(new ComponentBuilder("You are currently in a converstation with " + target.getName()).color(ChatColor.AQUA).create());
					} else {
						sender.sendMessage(new ComponentBuilder("You have nobody to reply to! Use /msg instead!").color(ChatColor.YELLOW).create());
					}
				}
			} else {
				sender.sendMessage(new ComponentBuilder("You do not have permission to use that command!").color(ChatColor.RED).create());
			}
		} else {
			sender.sendMessage(new ComponentBuilder("This command can only be used as a player!").color(ChatColor.RED).create());
		}
	}
}