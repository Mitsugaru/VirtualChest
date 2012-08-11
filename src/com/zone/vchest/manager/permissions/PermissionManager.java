/************************************************************************ This file is part of AdminCmd.
 * AdminCmd is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * AdminCmd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with AdminCmd. If not, see <http://www.gnu.org/licenses/>. ************************************************************************/
package com.zone.vchest.manager.permissions;

import java.lang.ref.WeakReference;
import java.util.Hashtable;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;


import com.platymuus.bukkit.permissions.PermissionsPlugin;
import com.zone.vchest.VirtualChestWorker;
import com.zone.vchest.manager.permissions.plugins.BukkitPermissions;
import com.zone.vchest.manager.permissions.plugins.IPermissionPlugin;
import com.zone.vchest.manager.permissions.plugins.PermissionsEx;
import com.zone.vchest.manager.permissions.plugins.SuperPermissions;
import com.zone.vchest.manager.permissions.plugins.bPermissions;

/** @author Balor (aka Antoine Aflalo) */
public class PermissionManager {
	
	private static PermissionManager instance = null;
	private static boolean permissionsEx = false;
	private static boolean yetiPermissions = false;
	private static boolean bPermissions = false;
	private static boolean permissionsBukkit = false;
	private static IPermissionPlugin permissionHandler;
	private static boolean warningSend = false;
	
	/** @return the instance */
	public static PermissionManager getInstance() {
		if (instance == null)
			instance = new PermissionManager();
		return instance;
	}
	
	public static String getPermissionLimit(Player p, String limit) {
		return permissionHandler.getPermissionLimit(p, limit);
	}
	
	public static String getPrefix(Player player) {
		return permissionHandler.getPrefix(player);
	}
	
	public static String getSuffix(Player player) {
		return permissionHandler.getSuffix(player);
	}
	
	public static boolean hasPerm(CommandSender player, Permission perm) throws NullPointerException {
		return hasPerm(player, perm, true);
	}
	
	public static boolean hasPerm(CommandSender player, Permission perm, boolean errorMsg) throws NullPointerException {
		if (perm == null)
			throw new NullPointerException("The Permission Node can't be NULL");
		if (player == null)
			throw new NullPointerException("The CommandSender can't be NULL");
		return permissionHandler.hasPerm(player, perm, errorMsg);
	}
	
	/** Check the permission with an error message if the user don't have the
	 * Permission
	 * 
	 * @param player
	 *            player to check the permission
	 * @param perm
	 *            permission node
	 * @return if the user have or not the permission
	 * @throws NullPointerException
	 *             when the permission node is null */
	public static boolean hasPerm(CommandSender player, String perm) throws NullPointerException {
		return hasPerm(player, perm, true);
	}
	
	/** Check the permission with the possibility to disable the error msg
	 * 
	 * @param player
	 *            player to check the permission
	 * @param perm
	 *            permission node
	 * @param errorMsg
	 *            send or not an error message to the user if he don't have the
	 *            permission
	 * @return if the user have or not the permission
	 * @throws NullPointerException
	 *             when the permission node is null */
	public static boolean hasPerm(CommandSender player, String perm, boolean errorMsg) throws NullPointerException {
		if (perm == null)
			throw new NullPointerException("The Permission Node can't be NULL");
		if (player == null)
			throw new NullPointerException("The CommandSender can't be NULL");
		return permissionHandler.hasPerm(player, perm, errorMsg);
	}
	
	/** @return the bPermissions */
	public static boolean isbPermissionsSet() {
		return bPermissions;
	}
	
	public static boolean isInGroup(String groupName, Player player) throws NoPermissionsPlugin {
		return permissionHandler.isInGroup(groupName, player);
	}
	
	/** @return the PermissionsBukkit */
	public static boolean isPermissionsBukkitSet() {
		return permissionsBukkit;
	}
	
	/** @return the permissionsEx */
	public static boolean isPermissionsExSet() {
		return permissionsEx;
	}
	
	/** @return the yetiPermissions */
	public static boolean isYetiPermissionsSet() {
		return yetiPermissions;
	}
	
	/** Set bPermission Plugin
	 * 
	 * @param plugin
	 * @param infoReader
	 * @return */
	public static boolean setbPermissions() {
		if (!bPermissions && !permissionsEx) {
			bPermissions = true;
			permissionHandler = new bPermissions();
			if (!yetiPermissions)
				VirtualChestWorker.log.info("Successfully linked with bPermissions.");
			else
				VirtualChestWorker.log.info("Successfully linked with bPermissions overpassing the Permission Bridge.");
		} else {
			return false;
		}
		return true;
	}
	
	/** Set PermissionsBukkit Plugin
	 * 
	 * @param plugin
	 * @return */
	public static boolean setPermissionsBukkit(PermissionsPlugin plugin) {
		if (!permissionsBukkit && !bPermissions && !permissionsEx) {
			permissionsBukkit = true;
			permissionHandler = new BukkitPermissions(plugin);
			if (!yetiPermissions)
				VirtualChestWorker.log.info("Successfully linked with PermissionsBukkit.");
			else
				VirtualChestWorker.log.info("Successfully linked with PermissionsBukkit overpassing the Permission Bridge.");
		} else {
			return false;
		}
		return true;
	}
	
	/** @param pEX
	 *            the pEX to set */
	public static boolean setPEX(ru.tehkode.permissions.PermissionManager pEX) {
		if (!permissionsEx) {
			if (!VirtualChestWorker.getInstance().getConfig().getBoolean("forceOfficialBukkitPerm")) {
				permissionsEx = true;
				permissionHandler = new PermissionsEx(pEX);
				if (!yetiPermissions)
					VirtualChestWorker.log.info("Successfully linked with PermissionsEX");
				else
					VirtualChestWorker.log.info("Use PermissionsEX instead of Yeti's Permissions.");
			} else if (!warningSend) {
				VirtualChestWorker.log.info("Plugin Forced to use Offical Bukkit Permission System");
				warningSend = true;
			}
			return true;
		} else
			return false;
	}
	/** Set Permission Plugin
	 * 
	 * @param plugin
	 * @return */
	private final Hashtable<String, WeakReference<PermissionLinker>> permissionLinkers = new Hashtable<String, WeakReference<PermissionLinker>>();
	
	/**
	 *
	 */
	private PermissionManager() {
		if (permissionHandler == null)
			permissionHandler = new SuperPermissions();
	}
	
	public synchronized boolean addPermissionLinker(PermissionLinker perm) {
		final String name = perm.getName();
		if (name == null) {
			throw new NullPointerException();
		}
		final WeakReference<PermissionLinker> ref = permissionLinkers.get(name);
		if (ref != null) {
			if (ref.get() == null) {
				// Hashtable holds stale weak reference
				// to a logger which has been GC-ed.
				// Allow to register new one.
				permissionLinkers.remove(name);
			} else {
				// We already have a registered logger with the given name.
				return false;
			}
		}
		// We're adding a new logger.
		// Note that we are creating a weak reference here.
		permissionLinkers.put(name, new WeakReference<PermissionLinker>(perm));
		return true;
	}
	
	PermissionLinker demandPermissionLinker(String name) {
		PermissionLinker result = getPermissionLinker(name);
		if (result == null) {
			result = new PermissionLinker(name);
			addPermissionLinker(result);
			result = getPermissionLinker(name);
		}
		return result;
	}
	
	public synchronized PermissionLinker getPermissionLinker(String name) {
		final WeakReference<PermissionLinker> ref = permissionLinkers.get(name);
		if (ref == null) {
			return null;
		}
		final PermissionLinker perm = ref.get();
		if (perm == null) {
			// Hashtable holds stale weak reference
			// to a logger which has been GC-ed.
			permissionLinkers.remove(name);
		}
		return perm;
	}
}
