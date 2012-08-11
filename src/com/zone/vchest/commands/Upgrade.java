/************************************************************************
 * This file is part of GiftPost.									
 *																		
 * GiftPost is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by	
 * the Free Software Foundation, either version 3 of the License, or		
 * (at your option) any later version.									
 *																		
 * GiftPost is distributed in the hope that it will be useful,	
 * but WITHOUT ANY WARRANTY; without even the implied warranty of		
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the			
 * GNU General Public License for more details.							
 *																		
 * You should have received a copy of the GNU General Public License
 * along with GiftPost.  If not, see <http://www.gnu.org/licenses/>.
 ************************************************************************/
package com.zone.vchest.commands;

import static com.zone.vchest.utils.Display.chestKeeper;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.zone.vchest.VirtualChestWorker;
import com.zone.vchest.objects.VirtualChest;
import com.zone.vchest.objects.VirtualLargeChest;

/**
 * @author Antoine
 * 
 */
public class Upgrade implements GPCommand {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.Balor.commands.GPCommand#execute(com.Balor.bukkit.GiftPost.GiftPostWorker
	 * , org.bukkit.command.CommandSender, java.lang.String[])
	 */
	public void execute(VirtualChestWorker gpw, CommandSender sender, String[] args) {
		Player player = (Player) sender;
		VirtualChest v;
		if (args !=null && args.length == 2)
			v = gpw.getChest(player.getName(), args[1].toLowerCase());
		else
			v = gpw.getDefaultChest(player.getName());
		if (v != null) {
			if ((v instanceof VirtualChest) && !(v instanceof VirtualLargeChest)) {
				if (gpw.economyUpgradeCheck(player)) {
					if (gpw.upgradeChest(player, v)) {
						sender.sendMessage(chestKeeper() + v.getName() + " is now a Large Chest.");
					} else
						sender.sendMessage(chestKeeper()
								+ "A problem happen when you tried to upgrade your chest .");
				}
			} else
				sender.sendMessage(chestKeeper() + ChatColor.RED
						+ "You can't upgrade a Large chest !");
		} else
			sender.sendMessage(chestKeeper() + ChatColor.RED + "You don't have a chest to upgrade.");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.Balor.commands.GPCommand#validate(com.Balor.bukkit.GiftPost.
	 * GiftPostWorker, org.bukkit.command.CommandSender, java.lang.String[])
	 */
	public boolean validate(VirtualChestWorker gpw, CommandSender sender, String[] args) {
		return ((gpw.hasFlag(args, "u") || gpw.hasFlag(args, "upgrade")))
				&& gpw.hasPerm((Player) sender, getPermName())
				&& VirtualChestWorker.getInstance().getConfig().getString("only-sign", "false")
						.equals("false");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.Balor.commands.GPCommand#getPermName()
	 */
	public String getPermName() {
		return "giftpost.chest.upgrade";
	}

	public String getHelp() {
		return ChatColor.GOLD + "/gp u (ChestName OR nothing)" + ChatColor.WHITE
				+ ": if you have a normal chest, upgrade to a large chest.";
	}

}
