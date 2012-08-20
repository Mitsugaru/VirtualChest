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
package com.zone.vchest.manager.permissions.plugins;

import in.mDev.MiracleM4n.mChatSuite.mChatSuite;
import in.mDev.MiracleM4n.mChatSuite.api.Reader;
import in.mDev.MiracleM4n.mChatSuite.types.InfoType;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;

import com.zone.vchest.manager.permissions.NoPermissionsPlugin;


/** @author Lathanael (aka Philippe Leipold) */
public class SuperPermissions implements IPermissionPlugin {
	
	protected static Reader mChatInfo = null;
	
	/**
	 *
	 */
	public SuperPermissions() {
	}
	
	/** @param mChatSuite
	 *            the mChatAPI to set */
	@SuppressWarnings("deprecation")
	public static void setmChatapi(mChatSuite mChatSuite) {
		if (SuperPermissions.mChatInfo == null && mChatSuite != null)
			mChatInfo = mChatSuite.getReader();
	}
	
	/** @return the mChatAPI */
	public static boolean isApiSet() {
		return mChatInfo != null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * be.Balor.Manager.Permissions.AbstractPermission#hasPerm(org.bukkit.command
	 * .CommandSender, java.lang.String, boolean)
	 */
	@Override
	public boolean hasPerm(CommandSender player, String perm, boolean errorMsg) {
		if (!(player instanceof Player))
			return true;
		if (player.hasPermission(perm))
			return true;
		else {
			if (errorMsg)
				player.sendMessage(ChatColor.RED + "You don't have the permission to do that " + ChatColor.BLUE + "(" + perm + ")");
			return false;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * be.Balor.Manager.Permissions.AbstractPermission#hasPerm(org.bukkit.command
	 * .CommandSender, org.bukkit.permissions.Permission, boolean)
	 */
	@Override
	public boolean hasPerm(CommandSender player, Permission perm, boolean errorMsg) {
		if (!(player instanceof Player))
			return true;
		if (player.hasPermission(perm))
			return true;
		else {
			if (errorMsg)
				player.sendMessage(ChatColor.RED + "You don't have the permission to do that " + ChatColor.BLUE + "(" + perm.getName() + ")");
			return false;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * be.Balor.Manager.Permissions.AbstractPermission#isInGroup(org.java.lang
	 * .String, org.bukkit.entity.Player)
	 */
	@Override
	public boolean isInGroup(String group, Player player) throws NoPermissionsPlugin {
		throw new NoPermissionsPlugin("To use this functionality you need a Permission Plugin");
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * be.Balor.Manager.Permissions.AbstractPermission#getUsers(org.java.lang
	 * .String)
	 */
	@Override
	public Set<Player> getUsers(String groupName) throws NoPermissionsPlugin {
		throw new NoPermissionsPlugin("To use this functionality you need a Permission Plugin");
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * be.Balor.Manager.Permissions.AbstractPermission#getPermissionLimit(org
	 * .bukkit.entity.Player, java.lang.String)
	 */
	@Override
	public String getPermissionLimit(final Player p, String limit) {
		String result = null;
		if (mChatInfo != null)
			result = mChatInfo.getInfo(p.getName(), InfoType.USER, p.getWorld().getName(), "admincmd." + limit);
		if (result == null || (result != null && result.isEmpty())) {
			Pattern regex = Pattern.compile("admincmd\\." + limit.toLowerCase() + "\\.[0-9]+");
			final Set<PermissionAttachmentInfo> perms = p.getEffectivePermissions();
			for (PermissionAttachmentInfo info : perms) {
				Matcher regexMatcher = regex.matcher(info.getPermission());
				if (regexMatcher.find())
					return info.getPermission().split("\\.")[2];
			}
		} else
			return result;
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * be.Balor.Manager.Permissions.AbstractPermission#getPrefix(java.lang.String
	 * , java.lang.String)
	 */
	@Override
	public String getPrefix(Player player) {
		if (mChatInfo != null)
			return mChatInfo.getPrefix(player.getName(), InfoType.USER, player.getWorld().getName());
		else
			return "";
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * be.Balor.Manager.Permissions.IPermissionPlugin#getSuffix(org.bukkit.entity
	 * .Player)
	 */
	@Override
	public String getSuffix(Player player) {
		if (mChatInfo != null)
			return mChatInfo.getSuffix(player.getName(), InfoType.USER, player.getWorld().getName());
		else
			return "";
	}
}
