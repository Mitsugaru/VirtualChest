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
import org.bukkit.inventory.ItemStack;

import com.zone.vchest.VirtualChestWorker;
import com.zone.vchest.objects.VirtualChest;
import com.zone.vchest.utils.MaterialContainer;

/**
 * @author Balor (aka Antoine Aflalo)
 * 
 */
public class GiveItem implements GPCommand {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.Balor.commands.GPCommand#execute(com.Balor.bukkit.GiftPost.GiftPostWorker
	 * , org.bukkit.command.CommandSender, java.lang.String[])
	 */
	public void execute(VirtualChestWorker gpw, CommandSender sender, String[] args) {
		if (args.length < 2) {
			sender.sendMessage(getHelp());
			return;
		}
		Player p = (Player) sender;
		VirtualChest v;
		if ((v = gpw.getSendChest(p.getName())) != null) {
			MaterialContainer mc = checkMaterial(args[1], p);
			if (mc.isNull()) {
				p.sendMessage(chestKeeper() + ChatColor.RED + "This material don't exist : "
						+ ChatColor.WHITE + args[1]);
				return;
			}

			int nb;
			if (args.length == 2)
				nb = 1;
			else
				try {
					nb = Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {
					p.sendMessage(chestKeeper() + ChatColor.RED + args[2] + " is not a number.");
					return;
				}
			v.addItem(new ItemStack(mc.material, nb, mc.dmg));
			p.sendMessage(chestKeeper() + ChatColor.WHITE + "Successfuly added " + ChatColor.GOLD
					+ nb + " " + mc.display() + ChatColor.WHITE + " to your send chest ("
					+ ChatColor.GREEN + v.getName() + ChatColor.WHITE + ")");
		} else
			p.sendMessage(chestKeeper() + ChatColor.RED
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
		return ((gpw.hasFlag(args, "i") || gpw.hasFlag(args, "item")))
				&& gpw.hasPerm((Player) sender, getPermName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.Balor.commands.GPCommand#getPermName()
	 */
	public String getPermName() {
		return "giftpost.admin.item";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.Balor.commands.GPCommand#getHelp()
	 */
	public String getHelp() {
		return ChatColor.GOLD + "/gp i (id|name) numberOfItems" + ChatColor.WHITE
				+ ": add the item to your send chest\n";
	}

	/**
	 * Translate the id or name to a material
	 * 
	 * @param mat
	 * @return Material
	 */
	private MaterialContainer checkMaterial(String mat, Player player) {

		String[] info = new String[2];
		if (mat.contains(":"))
			info = mat.split(":");
		else {
			info[0] = mat;
			info[1] = "0";
		}
		MaterialContainer mc = new MaterialContainer(info[0], info[1]);
		return mc;

	}

}
