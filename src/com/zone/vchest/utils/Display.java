/*
 * This file is part of GiftPost .
 * GiftPost is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * GiftPost is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with GiftPost. If not, see <http://www.gnu.org/licenses/>.
 */
package com.zone.vchest.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.zone.vchest.VirtualChestWorker;
import com.zone.vchest.commands.Buy;
import com.zone.vchest.commands.Chest;
import com.zone.vchest.commands.ChestList;
import com.zone.vchest.commands.EmptyChest;
import com.zone.vchest.commands.GPCommand;
import com.zone.vchest.commands.GiveItem;
import com.zone.vchest.commands.Help;
import com.zone.vchest.commands.RemoveChest;
import com.zone.vchest.commands.Rename;
import com.zone.vchest.commands.Send;
import com.zone.vchest.commands.SetChest;
import com.zone.vchest.commands.SetChestLimit;
import com.zone.vchest.commands.Upgrade;

/** @author Balor */
public class Display {
	
	public static void sendHelp(CommandSender sender, Class<?> command) {
		GPCommand gpCom = VirtualChestWorker.getInstance().getCommand(command);
		if (gpCom != null)
			sender.sendMessage(gpCom.getHelp());
	}
	
	public static void sendHelp(CommandSender sender, int page) {
		Player player = (Player) sender;
		sender.sendMessage(ChatColor.AQUA + "Virtual Chest (Gift Post) \n");
		sendHelp(sender, Help.class);
		boolean onlySign = VirtualChestWorker.getInstance().getConfig().getString("only-sign", "false").equals("true");
		if (page == 1) {
			if (!onlySign && VirtualChestWorker.getInstance().hasPerm(player, "giftpost.chest.everywhere", false))
				sendHelp(sender, Chest.class);
			if (VirtualChestWorker.getInstance().hasPerm(player, "giftpost.chest.open", false)) {
				if (!onlySign)
					sendHelp(sender, Buy.class);
				sendHelp(sender, SetChest.class);
				sendHelp(sender, ChestList.class);
				if (!onlySign)
					sendHelp(sender, Upgrade.class);
				sendHelp(sender, Rename.class);
				sendHelp(sender, RemoveChest.class);
				sendHelp(sender, EmptyChest.class);
			}
			if (VirtualChestWorker.getInstance().hasPerm(player, "giftpost.chest.send", false))
				sendHelp(sender, Send.class);
		} else if (page == 2) {
			if (VirtualChestWorker.getInstance().hasPerm(player, "giftpost.admin.limit", false))
				sendHelp(sender, SetChestLimit.class);
			if (VirtualChestWorker.getInstance().hasPerm(player, "giftpost.admin.item", false))
				sendHelp(sender, GiveItem.class);
		}
	}
	
	public static String chestKeeper() {
		return "[" + ChatColor.GOLD + "Chest Keeper" + ChatColor.WHITE + "] ";
	}
}