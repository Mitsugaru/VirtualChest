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
package com.zone.vchest.listeners;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.zone.vchest.VirtualChestWorker;
import com.zone.vchest.objects.VirtualChest;

/**
 * @author Balor (aka Antoine Aflalo)
 * 
 */
public class DeathEntityListener implements Listener {
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		final Player p = (Player) event.getEntity();
		if (VirtualChestWorker.getInstance().numberOfChest(p) != 0
				&& VirtualChestWorker.getInstance().getConfig().getString("drop-on-death", "false")
						.matches("true")) {
			final Location deathLoc = p.getLocation();
			final HashMap<String, VirtualChest> listOfChest = VirtualChestWorker.getInstance()
					.listOfChest(p);
			for (final String chestName : listOfChest.keySet()) {
				final VirtualChest v = listOfChest.get(chestName);
				for (final ItemStack item : v.getContents())
					if (item != null)
						p.getWorld().dropItem(deathLoc, item);

				v.emptyChest();
			}
		}
	}

}
