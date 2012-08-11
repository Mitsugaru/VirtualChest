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

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.zone.vchest.VirtualChestWorker;
/**
 * @author Antoine
 * 
 */
public class ChestList implements GPCommand {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.Balor.commands.GPCommand#execute(com.Balor.bukkit.GiftPost.GiftPostWorker
	 * , org.bukkit.command.CommandSender, java.lang.String[])
	 */
	public void execute(VirtualChestWorker gpw, CommandSender sender, String[] args) {
		Player player = (Player) sender;
		String playerName = player.getName();
		if(args.length > 1 && gpw.hasPerm((Player) sender, "giftpost.admin.open"))
			playerName = args[1];
		ArrayList<String> chestList = gpw.chestList(playerName);
		if (chestList == null)
			sender.sendMessage("[" + ChatColor.GOLD + "Chest Keeper" + ChatColor.WHITE + "] "
					+ ChatColor.RED + "You don't have any chest.");
		else {
			String msg = chestKeeper() + ChatColor.GREEN + "List of all "+playerName+"'s chest ("
					+ chestList.size() + "): " + ChatColor.DARK_RED;
			for (String chestName : chestList)
				msg += chestName + ", ";
			msg = msg.substring(0, msg.length() - 2);
			sender.sendMessage(msg);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.Balor.commands.GPCommand#validate(com.Balor.bukkit.GiftPost.
	 * GiftPostWorker, org.bukkit.command.CommandSender, java.lang.String[])
	 */
	public boolean validate(VirtualChestWorker gpw, CommandSender sender, String[] args) {
		return (gpw.hasFlag(args, "list") || gpw.hasFlag(args, "l"))
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
		return ChatColor.GOLD + "/gp l " + ChatColor.WHITE + ": list all your chests";
	}

}
