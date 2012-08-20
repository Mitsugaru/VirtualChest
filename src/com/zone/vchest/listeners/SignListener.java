/************************************************************************ f * This file is part of GiftPost.
 * GiftPost is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * GiftPost is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with GiftPost. If not, see <http://www.gnu.org/licenses/>. ************************************************************************/
package com.zone.vchest.listeners;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

import com.zone.vchest.VirtualChestWorker;

/** @author Balor (aka Antoine Aflalo) */
public class SignListener implements Listener {
	
	private final VirtualChestWorker worker;
	
	public SignListener() {
		worker = VirtualChestWorker.getInstance();
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getBlock().getState() instanceof Sign) {
			final Sign sign = (Sign) event.getBlock().getState();
			if ((sign.getLine(0).indexOf("[Chest Keeper]") == 0 || sign.getLine(0).indexOf("[Buy Chest]") == 0 || sign.getLine(0).indexOf("[Up Chest]") == 0) && sign.getLine(0).indexOf("]") != -1 && !worker.hasPerm(event.getPlayer(), "giftpost.admin.sign"))
				event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent e) {
		final String line0 = e.getLine(0);
		if (line0.indexOf("[Chest Keeper]") == 0 && line0.indexOf("]") != -1) {
			if (!worker.hasPerm(e.getPlayer(), "giftpost.admin.sign"))
				e.setLine(0, "\u00A74[No Perm]");
			else {
				if (e.getLine(1).isEmpty()) {
					e.setLine(1, ChatColor.DARK_RED + "Click me to");
					e.setLine(2, ChatColor.DARK_RED + "open your");
					e.setLine(3, ChatColor.DARK_RED + "chest !");
				}
			}
		} else if (line0.indexOf("[Buy Chest]") == 0 && line0.indexOf("]") != -1) {
			if (!worker.hasPerm(e.getPlayer(), "giftpost.admin.sign"))
				e.setLine(0, "\u00A74[No Perm]");
			else {
				if (e.getLine(1).isEmpty()) {
					e.setLine(1, ChatColor.DARK_RED + "Click me to");
					e.setLine(2, ChatColor.DARK_RED + "buy a");
					e.setLine(3, ChatColor.DARK_RED + "chest !");
				}
			}
		} else if (line0.indexOf("[Up Chest]") == 0 && line0.indexOf("]") != -1) {
			if (!worker.hasPerm(e.getPlayer(), "giftpost.admin.sign"))
				e.setLine(0, "\u00A74[No Perm]");
			else {
				if (e.getLine(1).isEmpty()) {
					e.setLine(1, ChatColor.DARK_RED + "Click me to");
					e.setLine(2, ChatColor.DARK_RED + "upgrade a");
					e.setLine(3, ChatColor.DARK_RED + "chest !");
				}
			}
		}
	}
}
