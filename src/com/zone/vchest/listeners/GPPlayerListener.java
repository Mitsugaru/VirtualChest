/************************************************************************ This file is part of GiftPost.
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

import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.zone.vchest.VirtualChestWorker;
import com.zone.vchest.commands.Buy;
import com.zone.vchest.commands.Chest;
import com.zone.vchest.commands.Upgrade;

/** @author Balor (aka Antoine Aflalo) */
public class GPPlayerListener implements Listener, Runnable {
	
	private final VirtualChestWorker worker;
	Set<Player> block = new HashSet<Player>();
	
	public GPPlayerListener() {
		worker = VirtualChestWorker.getInstance();
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent event) {
		onSign(event);
		if (worker.getConfig().getString("use-wand", "true").matches("true") && worker.hasPerm(event.getPlayer(), "giftpost.chest.everywhere", false) && event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getType() != null && (event.getPlayer().getItemInHand().getType().getId() == worker.getConfig().getInt("wand-item-id", Material.CHEST.getId())) && (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))) {
			new Chest().execute(worker, event.getPlayer(), null);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event) {
		VirtualChestWorker.workerLog.info(event.getPlayer().getName() + " connected");
		if (worker.getConfig().getString("message-of-the-day", "true").matches("true"))
			event.getPlayer().sendMessage("Virtual Chest is installed : /gp help to see all commands");
		if (worker.numberOfChest(event.getPlayer()) > 0 && worker.getFileManager().hasOfflineItems(event.getPlayer())) {
			worker.getFileManager().openOfflineFile(event.getPlayer());
			if (worker.getConfig().getString("message-of-the-day", "true").matches("true"))
				event.getPlayer().sendMessage(ChatColor.GOLD + "(command" + ChatColor.RED + " /gp c" + ChatColor.GOLD + " to see your chest.)");
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerQuit(PlayerQuitEvent event) {
		final Player p = event.getPlayer();
		final String pName = p.getName();
		if (worker.getConfig().getString("allow-offline", "true").matches("true") && worker.haveAChestInMemory(pName))
			worker.getFileManager().createWorldFile(p);
		if (worker.haveAChestInMemory(pName))
			worker.unloadPlayerChests(pName);
	}
	
	@EventHandler
	public void onSign(PlayerInteractEvent event) {
		if (block.contains(event.getPlayer())) {
			return;
		}
		block.add(event.getPlayer());
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			final Block block = event.getClickedBlock();
			if (block.getType().equals(Material.WALL_SIGN) || block.getType().equals(Material.SIGN_POST)) {
				final Sign sign = (Sign) block.getState();
				if (sign.getLine(0).indexOf("[Chest Keeper]") == 0 && sign.getLine(0).indexOf("]") != -1 && worker.hasPerm(event.getPlayer(), "giftpost.chest.open")) {
					new Chest().execute(worker, event.getPlayer(), null);
				} else if (sign.getLine(0).indexOf("[Buy Chest]") == 0 && sign.getLine(0).indexOf("]") != -1 && worker.hasPerm(event.getPlayer(), "giftpost.chest.open")) {
					new Buy().execute(worker, event.getPlayer(), null);
				} else if (sign.getLine(0).indexOf("[Up Chest]") == 0 && sign.getLine(0).indexOf("]") != -1 && worker.hasPerm(event.getPlayer(), "giftpost.chest.open")) {
					new Upgrade().execute(worker, event.getPlayer(), null);
				}
			}
		}
	}
	
	@Override
	public void run() {
		block.clear();
	}
}
