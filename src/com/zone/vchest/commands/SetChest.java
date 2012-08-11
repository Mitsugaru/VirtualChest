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

/**
 * @author Antoine
 * 
 */
public class SetChest implements GPCommand {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.Balor.commands.GPCommand#execute(com.Balor.bukkit.GiftPost.GiftPostWorker
	 * , org.bukkit.command.CommandSender, java.lang.String[])
	 */
	public void execute(VirtualChestWorker gpw, CommandSender sender, String[] args) {
		if (args.length != 3) {
			sender.sendMessage(getHelp());
			return;
		}
		String chestName = args[1].toLowerCase();
		String type = args[2].toLowerCase();
		if (type.equals("default") || type.equals("send")) {
			if (type.equals("default")) {
				if (gpw.setDefaultChest(((Player) sender).getName(), chestName))
					sender.sendMessage(chestKeeper() + chestName + ChatColor.GREEN
							+ " is now your default chest.");
				else
					sender.sendMessage(chestKeeper() + ChatColor.RED + "You don't have this chest.");
			} else {
				assert type.equals("send");
				if (gpw.setSendChest(((Player) sender).getName(), chestName))
					sender.sendMessage(chestKeeper() + chestName + ChatColor.GREEN
							+ " is now your send chest.");
				else
					sender.sendMessage(chestKeeper() + ChatColor.RED + "You don't have this chest.");
			}
		} else
			sender.sendMessage(chestKeeper() + ChatColor.RED
					+ " Only 2 choose possible : default and send");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.Balor.commands.GPCommand#validate(com.Balor.bukkit.GiftPost.
	 * GiftPostWorker, org.bukkit.command.CommandSender, java.lang.String[])
	 */
	public boolean validate(VirtualChestWorker gpw, CommandSender sender, String[] args) {
		return (gpw.hasFlag(args, "set") && gpw.hasPerm((Player) sender, getPermName()));
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
		return ChatColor.GOLD
				+ "/gp set ChestName (default|send)"
				+ ChatColor.WHITE
				+ ": set the ChestName as your default chest (open when using a chest) or send chest (for gifts)";
	}

}
