/*This file is part of GiftPost .

GiftPost is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

GiftPost is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with GiftPost.  If not, see <http://www.gnu.org/licenses/>.*/
package com.zone.vchest.commands;

import static com.zone.vchest.utils.Display.chestKeeper;

import com.zone.vchest.VirtualChestWorker;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 
 * @author Balor
 */
public class Send implements GPCommand {

	public void execute(VirtualChestWorker gpw, CommandSender sender, String[] args) {
		if (args.length != 2) {
			sender.sendMessage(getHelp());
			return;
		}
		String targetName = args[1];
		Player target = sender.getServer().getPlayer(targetName);
		Player player = (Player) sender;
		if (targetName.equals("allusers") && gpw.hasPerm(player, "giftpost.admin.sendallusers")) {
			sendToAll(player);
			return;
		}
		if (gpw.getSendChest(player.getName()) == null)
			sender.sendMessage(chestKeeper() + ChatColor.RED
					+ "You don't have a chest. To buy one type " + ChatColor.GOLD
					+ "/gp buy (large|normal)");
		else if (gpw.getSendChest(targetName) == null)
			sender.sendMessage(chestKeeper() + ChatColor.RED + targetName + " don't have a chest.");
		else if (player.getName().equals(targetName))
			sender.sendMessage(chestKeeper() + ChatColor.RED
					+ "You can't send a gift to yourself !");
		else if (gpw.getSendChest(player.getName()).isEmpty())
			sender.sendMessage(chestKeeper() + ChatColor.DARK_GRAY
					+ "Your chest is empty, nothing to send");
		else if (gpw.getSendChest(targetName).isFull())
			sender.sendMessage(chestKeeper() + ChatColor.RED + "The chest of " + ChatColor.BLUE
					+ targetName + ChatColor.RED + " is full !");
		else if (gpw.getSendChest(targetName).leftCases() < gpw.getSendChest(player.getName())
				.usedCases())
			sender.sendMessage(chestKeeper() + ChatColor.RED + "There isn't enough place in the "
					+ ChatColor.BLUE + targetName + ChatColor.RED + "'s chest !");
		else {
			if (target != null) {
				if (checkMaxRange(gpw, player, target) && inSameWorld(gpw, player, target)) {
					if (gpw.economyCheck(player, "iConomy-send-price")) {
						gpw.getSendChest(targetName).addItemStack(
								gpw.getSendChest(player.getName()).getMcContents());
						gpw.getSendChest(player.getName()).emptyChest();
						target.sendMessage(chestKeeper() + ChatColor.GREEN + player.getName()
								+ ChatColor.GRAY
								+ " send you a gift, look in your send chest (using command "
								+ ChatColor.GOLD + "/gp c "
								+ gpw.getSendChest(targetName).getName() + ChatColor.GRAY + ").");
						sender.sendMessage(chestKeeper() + ChatColor.BLUE
								+ "Successfully send your gift to " + ChatColor.GREEN + targetName);
					}

				} else
					sender.sendMessage(chestKeeper() + ChatColor.GRAY + targetName + ChatColor.RED
							+ " is to far away from you to send him your gift !");
			} else {
				if (gpw.getConfig().getString("allow-offline", "false").matches("true")) {
					if (inSameWorld(gpw, player.getWorld().getName(), gpw.getFileManager()
							.openWorldFile(targetName))) {
						if (gpw.economyCheck(player, "iConomy-send-price")) {
							sender.sendMessage(chestKeeper() + ChatColor.BLUE
									+ "Successfully send your gift to " + ChatColor.GREEN
									+ targetName + ChatColor.RED
									+ " but he's offline, he'll receve it when he'll connect.");
							gpw.getSendChest(targetName).addItemStack(
									gpw.getSendChest(player.getName()).getMcContents());
							gpw.getFileManager().createOfflineFile(targetName,
									gpw.getSendChest(player.getName()).getMcContents(),
									player.getName());
							gpw.getSendChest(player.getName()).emptyChest();
							gpw.unloadPlayerChests(targetName);
						}
					} else
						sender.sendMessage(chestKeeper() + targetName + ChatColor.RED
								+ " is offline, and he was in an another world when he quit.");
				} else
					sender.sendMessage(targetName + ChatColor.RED
							+ " is offline, you can't send him your gift.");
			}

		}

	}

	/**
	 * Send the content of the chest to all player who have a chest.
	 * 
	 * @param sender
	 */
	private void sendToAll(Player sender) {
		VirtualChestWorker gpw = VirtualChestWorker.getInstance();
		String senderName = sender.getName();
		if (gpw.getSendChest(senderName).isEmpty())
			sender.sendMessage(chestKeeper() + ChatColor.DARK_GRAY
					+ "Your chest is empty, nothing to send");
		for (String player : gpw.getAllOwner()) {
			if (!player.equals(senderName)) {
				Player target;
				if ((target = sender.getServer().getPlayer(player)) != null) {
					if (!gpw.getSendChest(player).isFull()
							&& gpw.getSendChest(player).leftCases() >= gpw.getSendChest(senderName)
									.usedCases()) {
						if (checkMaxRange(gpw, sender, target) && inSameWorld(gpw, sender, target)) {
							gpw.getSendChest(player).addItemStack(
									gpw.getSendChest(senderName).getMcContents(), true);
							target.sendMessage(chestKeeper() + ChatColor.GREEN + senderName
									+ ChatColor.GRAY
									+ " send you a gift, look in your send chest (using command "
									+ ChatColor.GOLD + "/gp c "
									+ gpw.getSendChest(player).getName() + ChatColor.GRAY + ").");
						}
					}
				} else {
					if (gpw.getConfig().getString("allow-offline", "false").matches("true")) {
						if (inSameWorld(gpw, sender.getWorld().getName(), gpw.getFileManager()
								.openWorldFile(player))) {
							gpw.getSendChest(player).addItemStack(
									gpw.getSendChest(senderName).getMcContents(), true);
							gpw.getFileManager().createOfflineFile(player,
									gpw.getSendChest(senderName).getMcContents(), senderName);
							gpw.unloadPlayerChests(player);
						}
					}
				}
			}

		}
		gpw.getSendChest(senderName).emptyChest();
		String msg = chestKeeper() + ChatColor.BLUE + "Successfully send your gift to all users";
		if (gpw.getConfig().getString("use-max-range", "false ").matches("true"))
			msg += " in your range.";
		sender.sendMessage(msg);
	}

	/**
	 * Check if the player are in the same world.
	 * 
	 * @param gpw
	 * @param player
	 * @param target
	 * @return
	 */
	private boolean inSameWorld(VirtualChestWorker gpw, Player player, Player target) {
		if (gpw.getConfig().getString("world-check", "false ").matches("true")) {
			return player.getWorld().getName().equals(target.getWorld().getName());
		}
		return true;
	}

	private boolean inSameWorld(VirtualChestWorker gpw, String worldFrom, String worldTo) {
		if (gpw.getConfig().getString("world-check", "false ").matches("true")) {
			return worldFrom.equals(worldTo);
		}
		return true;
	}

	/**
	 * Function to check if the player don't exceed the max-range
	 * 
	 * @param gpw
	 * @param player
	 * @param target
	 * @return
	 */
	private boolean checkMaxRange(VirtualChestWorker gpw, Player player, Player target) {
		if (gpw.getConfig().getString("use-max-range", "false ").matches("true")) {
			int maxRadius = gpw.getConfig().getInt("max-range", 100);
			int totaldistance = 0;

			int x1 = player.getLocation().getBlockX();
			int y1 = player.getLocation().getBlockY();
			int z1 = player.getLocation().getBlockZ();
			int x2 = target.getLocation().getBlockX();
			int y2 = target.getLocation().getBlockY();
			int z2 = target.getLocation().getBlockZ();

			totaldistance = ((x1 - x2) ^ 2 + (y1 - y2) ^ 2 + (z1 - z2) ^ 2);
			if (!(totaldistance < (maxRadius ^ 2))) {
				return false;
			}
		}
		return true;
	}

	public boolean validate(VirtualChestWorker gpw, CommandSender sender, String[] args) {
		return ((gpw.hasFlag(args, "s") || gpw.hasFlag(args, "send")))
				&& gpw.hasPerm((Player) sender, getPermName());
	}

	public String getPermName() {
		return "giftpost.chest.send";
	}

	public String getHelp() {
		return ChatColor.GOLD + "/gp s player " + ChatColor.WHITE
				+ ": send the content of your send chest to the player (case sensitive)";
	}

}
