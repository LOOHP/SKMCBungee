package com.loohp.skmcbungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;

public class FormatsCommand extends Command {

	public FormatsCommand() {
		super("formats");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (sender.hasPermission("skmc.formats")) {
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
			player.sendMessage(new ComponentBuilder("§7Format Codes").create());
			player.sendMessage(new ComponentBuilder("§c=========================").create());
			player.sendMessage(new ComponentBuilder("§f  &k = §kMagic §r§f(Magic)").create());
			player.sendMessage(new ComponentBuilder("§f  &l = §lBold").create());
			player.sendMessage(new ComponentBuilder("§f  &m = §mStrikethrough").create());
			player.sendMessage(new ComponentBuilder("§f  &n = §nUnderline").create());
			player.sendMessage(new ComponentBuilder("§f  &o = §oItalic").create());
			player.sendMessage(new ComponentBuilder("§f  &r = §rReset").create());
			player.sendMessage(new ComponentBuilder("§7Use /colors to view text coloring codes").create());
			player.sendMessage(new ComponentBuilder("§c=========================").create());
		} else {
			sender.sendMessage(new ComponentBuilder(ChatColor.RED + "You do not have permission to use that command!").create());
		}
	}
}