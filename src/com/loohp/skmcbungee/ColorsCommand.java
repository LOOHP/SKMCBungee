package com.loohp.skmcbungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;

public class ColorsCommand extends Command {

	public ColorsCommand() {
		super("colors");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (sender.hasPermission("skmc.colors")) {
			CommandSender player = sender;
			if (args.length > 0) {
				if (ProxyServer.getInstance().getPlayer(args[0]) != null) {
					player = ProxyServer.getInstance().getPlayer(args[0]);
				} else {
					sender.sendMessage(new ComponentBuilder(ChatColor.RED + "That player is not online!").create());
					return;
				}
			}
			player.sendMessage(new ComponentBuilder("§c=========================").create());
			player.sendMessage(new ComponentBuilder("§7Color Codes").create());
			player.sendMessage(new ComponentBuilder("§c=========================").create());
			player.sendMessage(new ComponentBuilder("§f  &0 = §0Black").create());
			player.sendMessage(new ComponentBuilder("§f  &1 = §1Dark Blue").create());
			player.sendMessage(new ComponentBuilder("§f  &2 = §2Dark Green").create());
			player.sendMessage(new ComponentBuilder("§f  &3 = §3Dark Aqua").create());
			player.sendMessage(new ComponentBuilder("§f  &4 = §4Dark Red").create());
			player.sendMessage(new ComponentBuilder("§f  &5 = §5Dark Purple").create());
			player.sendMessage(new ComponentBuilder("§f  &6 = §6Gold").create());
			player.sendMessage(new ComponentBuilder("§f  &7 = §7Gray").create());
			player.sendMessage(new ComponentBuilder("§f  &8 = §8Dark Gray").create());
			player.sendMessage(new ComponentBuilder("§f  &9 = §9Blue").create());
			player.sendMessage(new ComponentBuilder("§f  &a = §aGreen").create());
			player.sendMessage(new ComponentBuilder("§f  &b = §bAqua").create());
			player.sendMessage(new ComponentBuilder("§f  &c = §cRed").create());
			player.sendMessage(new ComponentBuilder("§f  &d = §dLight Purple").create());
			player.sendMessage(new ComponentBuilder("§f  &e = §eYellow").create());
			player.sendMessage(new ComponentBuilder("§f  &f = §fWhite").create());
			player.sendMessage(new ComponentBuilder("§7Use /formats to view text formatting codes").create());
			player.sendMessage(new ComponentBuilder("§c=========================").create());
		} else {
			sender.sendMessage(new ComponentBuilder(ChatColor.RED + "You do not have permission to use that command!").create());
		}
	}
}
