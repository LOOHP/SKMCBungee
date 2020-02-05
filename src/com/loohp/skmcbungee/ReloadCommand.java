package com.loohp.skmcbungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class ReloadCommand extends Command {

	public ReloadCommand() {
		super("skmcbungee");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (sender.hasPermission("skmc.admin.reload")) {
			Main.loadConfig();
			sender.sendMessage(new TextComponent(ChatColor.GREEN + "SKMCBungee reloaded"));
		} else {
			sender.sendMessage(new ComponentBuilder("You do not have permission to use that command!").color(ChatColor.RED).create());
		}
	}
}