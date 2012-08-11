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

/**
 * @author Balor (aka Antoine Aflalo)
 * 
 */
public class EmptyChest implements GPCommand {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.Balor.commands.GPCommand#execute(com.Balor.bukkit.GiftPost.GiftPostWorker
	 * , org.bukkit.command.CommandSender, java.lang.String[])
	 */
	public void execute(VirtualChestWorker gpw, CommandSender sender, String[] args) {
		if (args.length <= 1) {
			sender.sendMessage(getHelp());
			return;
		}
		Player p = (Player) sender;
		VirtualChest v;
		if ((v = gpw.getChest(p.getName(), args[1])) != null)
		{
			v.emptyChest();
			sender.sendMessage(chestKeeper() + ChatColor.GREEN + "Chest emptied succefuly");
		}
		else
			p.sendMessage(chestKeeper() + ChatColor.RED
					+ "You don't have this chest. To buy one type " + ChatColor.GOLD
					+ "/gp buy (large|normal) " + args[1].toLowerCase());

		

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.Balor.commands.GPCommand#validate(com.Balor.bukkit.GiftPost.
	 * GiftPostWorker, org.bukkit.command.CommandSender, java.lang.String[])
	 */
	public boolean validate(VirtualChestWorker gpw, CommandSender sender, String[] args) {
		return (gpw.hasFlag(args, "e") || gpw.hasFlag(args, "empty"))
				&& (gpw.hasPerm((Player) sender, getPermName()) || gpw.hasPerm((Player) sender,
						"giftpost.admin.empty"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.Balor.commands.GPCommand#getPermName()
	 */
	public String getPermName() {
		return "giftpost.chest.empty";
	}

	public String getHelp() {
		return ChatColor.GOLD + "/gp e <chest>" + ChatColor.WHITE + ": empty the <chest>.";
	}

}
