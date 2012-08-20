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

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


import com.zone.vchest.VirtualChestWorker;
import com.zone.vchest.manager.permissions.PermissionManager;
import com.zone.vchest.objects.VirtualChest;
import com.zone.vchest.objects.VirtualLargeChest;

import static com.zone.vchest.utils.Display.chestKeeper;

/**
 * @author Antoine
 * 
 */
public class Buy implements GPCommand {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.Balor.commands.GPCommand#execute(com.Balor.bukkit.GiftPost.GiftPostWorker
	 * , org.bukkit.command.CommandSender, java.lang.String[])
	 */
	public void execute(VirtualChestWorker gpw, CommandSender sender, String[] args) {
		String type;
		Player player = (Player) sender;
		if (VirtualChestWorker.getInstance().getConfig().getString("only-normal", "false")
				.equals("true"))
			type = "normal";
		else {
			if (args != null && args.length >= 2)
				type = args[1].toLowerCase();
			else
				type = gpw.getDefaultType(player);
		}

		String chestName;
		if (args != null && args.length == 3)
			chestName = args[2].toLowerCase();
		else {
			chestName = (type + (gpw.numberOfChest(player) + 1)).toLowerCase();
			Random rand = new Random();
			while (gpw.chestExists(player, chestName)) {
				chestName = (type + (gpw.numberOfChest(player) + 1) + rand.nextInt(10))
						.toLowerCase();
			}
		}

		if (gpw.numberOfChest(player) > 0 && gpw.chestExists(player, chestName))
			sender.sendMessage(chestKeeper() + ChatColor.RED
					+ "You have have already a chest named : " + ChatColor.AQUA + chestName);
		else if (type.matches("normal") || type.matches("large")) {
			int limit = getLimit(player);
			if (gpw.numberOfChest(player) + 1 <= limit || limit == 0) {
				if (gpw.economyCheck(player, "iConomy-" + type + "Chest-price")) {
					if (type.matches("normal"))
						gpw.addChest(player, new VirtualChest(chestName));
					if (type.matches("large"))
						gpw.addChest(player, new VirtualLargeChest(chestName));
					player.sendMessage(chestKeeper() + type + " Chest successfully bought. "
							+ ChatColor.GOLD + "(command /gp c " + chestName
							+ " OR use a chest with left click to open it)");
				}
			} else
				sender.sendMessage(chestKeeper() + ChatColor.RED
						+ "You have reach your limit of chest." + ChatColor.DARK_RED + "(" + limit
						+ ")");
		} else
			sender.sendMessage(chestKeeper() + ChatColor.RED
					+ "There is only 2 type of Chests : large and normal");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.Balor.commands.GPCommand#validate(com.Balor.bukkit.GiftPost.
	 * GiftPostWorker, org.bukkit.command.CommandSender, java.lang.String[])
	 */
	public boolean validate(VirtualChestWorker gpw, CommandSender sender, String[] args) {
		return ((gpw.hasFlag(args, "b") || gpw.hasFlag(args, "buy")))
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
		return "giftpost.chest.open";
	}

	public String getHelp() {
		if (VirtualChestWorker.getInstance().getConfig().getString("only-normal", "false")
				.equals("true"))
			return ChatColor.GOLD + "/gp b" + ChatColor.WHITE + ": to buy a normal chest \n";
		return ChatColor.GOLD + "/gp b (large OR normal) ChestName" + ChatColor.WHITE
				+ ": to buy a large or normal chest \n";
	}

	private int getLimit(Player player) {
		Integer limit = VirtualChestWorker.getInstance().getFileManager().openChestLimitFile(player);
		if (limit == -1)
			try {
				limit = Integer.parseInt(PermissionManager.getPermissionLimit(player, "maxchests"));
			} catch (NumberFormatException e) {
			}

		if (limit == null || limit == -1)
			limit = VirtualChestWorker.getInstance().getConfig().getInt("max-number-chest", 10);
		return limit;
	}

}
