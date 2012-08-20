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

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.zone.vchest.VirtualChestWorker;

/**
 * @author Balor (aka Antoine Aflalo)
 * 
 */
public class Rename implements GPCommand {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.Balor.commands.GPCommand#execute(com.Balor.bukkit.GiftPost.GiftPostWorker
	 * , org.bukkit.command.CommandSender, java.lang.String[])
	 */
	public void execute(VirtualChestWorker gpw, CommandSender sender, String[] args) {
		if(args.length < 2)
		{
			sender.sendMessage(getHelp());
			return;
		}
		String oldName = args[1].toLowerCase();
		Player player = (Player) sender;
		String newName;
		if (args.length == 3)
			newName = args[2].toLowerCase();
		else {
			Random generator = new Random();
			newName = ("c" + generator.nextInt(20)).toLowerCase();
		}
		if (gpw.chestExists(player, oldName)) {
			if (!gpw.chestExists(player, newName)) {
				gpw.renameChest(player, oldName, newName);
				sender.sendMessage(chestKeeper() + "Chest " + ChatColor.BLUE + oldName
						+ ChatColor.WHITE + " is now named " + ChatColor.GOLD + newName);

			} else
				sender.sendMessage(chestKeeper() + ChatColor.RED
						+ "You have already a chest named : " + ChatColor.WHITE + newName);
		} else
			sender.sendMessage(chestKeeper() + ChatColor.RED
					+ "You don't have a chest. To buy one type " + ChatColor.GOLD
					+ "/gp buy (large|normal) nameOfTheChest");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.Balor.commands.GPCommand#validate(com.Balor.bukkit.GiftPost.
	 * GiftPostWorker, org.bukkit.command.CommandSender, java.lang.String[])
	 */
	public boolean validate(VirtualChestWorker gpw, CommandSender sender, String[] args) {
		return ((gpw.hasFlag(args, "r") || gpw.hasFlag(args, "rename")))
				&& gpw.hasPerm((Player) sender, getPermName());
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
		return ChatColor.GOLD + "/gp r oldName newName" + ChatColor.WHITE + ": rename the chest.";
	}

}
