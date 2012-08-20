/*
 * This file is part of GiftPost .
 * GiftPost is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * GiftPost is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with GiftPost. If not, see <http://www.gnu.org/licenses/>.
 */
package com.zone.vchest;

// GiftPost
import static com.zone.vchest.utils.Display.chestKeeper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.google.common.collect.MapMaker;
import com.zone.vchest.commands.GPCommand;
import com.zone.vchest.manager.permissions.PermissionManager;
import com.zone.vchest.objects.VirtualChest;
import com.zone.vchest.objects.VirtualLargeChest;
import com.zone.vchest.tools.config.file.ExtendedConfiguration;
import com.zone.vchest.utils.FilesManager;
import com.zone.vchest.utils.LogFormatter;
import com.zone.vchest.utils.PlayerChests;

/** @author Balor */
public class VirtualChestWorker {
	
	private HashMap<String, HashMap<String, VirtualChest>> chests = new HashMap<String, HashMap<String, VirtualChest>>();
	private ConcurrentMap<String, VirtualChest> defaultChests = new MapMaker().makeMap();
	private ConcurrentMap<String, VirtualChest> sendReceiveChests = new MapMaker().makeMap();
	private List<GPCommand> commands = new ArrayList<GPCommand>();
	private ExtendedConfiguration config;
	private FilesManager fManager;
	public static final Logger log = Logger.getLogger("Minecraft");
	public static final Logger workerLog = Logger.getLogger("VirtualChest");
	private static Economy economy = null;
	private static VirtualChestWorker instance;
	private ConcurrentMap<String, PlayerChests> allChests = new MapMaker().concurrencyLevel(8).makeMap();
	private static boolean disable = false;
	
	private VirtualChestWorker() {
	}
	
	public static VirtualChestWorker getInstance() {
		if (instance == null)
			instance = new VirtualChestWorker();
		return instance;
	}
	
	public static void killInstance() {
		workerLog.info("Worker instance destroyed");
		for (Handler h : workerLog.getHandlers()) {
			h.close();
			workerLog.removeHandler(h);
		}
		instance = null;
	}
	
	public void setConfig(ExtendedConfiguration config, Plugin plugin) {
		this.config = config;
		if (config.getString("iConomy").equals("true")) {
			RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
			if (economyProvider != null) {
				economy = economyProvider.getProvider();
			} else {
				this.config.set("iConomy", "false");
				try {
					this.config.save();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				log.severe("[VirtualChest] To work with Economy system you need to have the VAULT API. Value set to false in the config file.");
			}
		}
	}
	
	public void setfManager(String path) {
		fManager = new FilesManager(path);
		FileHandler fh;
		try {
			// This block configure the logger with handler and formatter
			File logger = new File(path + File.separator + "log.txt");
			if (logger.exists())
				logger.delete();
			fh = new FileHandler(logger.getPath(), true);
			workerLog.addHandler(fh);
			workerLog.setUseParentHandlers(false);
			workerLog.setLevel(Level.ALL);
			LogFormatter formatter = new LogFormatter();
			fh.setFormatter(formatter);
			// the following statement is used to log any messages
			workerLog.info("Logger created");
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public FilesManager getFileManager() {
		return fManager;
	}
	
	public ExtendedConfiguration getConfig() {
		return config;
	}
	
	/** @param disable
	 *            the disable to set */
	public static void setDisable(boolean disable2) {
		disable = disable2;
	}
	
	/** @return the disable */
	public static boolean isDisable() {
		return disable;
	}
	
	/** Return the chest, create it if not exist
	 * 
	 * @param playerName
	 * @return */
	public VirtualChest getChest(String playerName, String chestName) {
		if (playerName == null) {
			workerLog.severe("PlayerName == null");
			return null;
		}
		if (chestName == null) {
			workerLog.severe("chestName == null");
			return null;
		}
		workerLog.info("Opening chest :" + chestName + " of player " + playerName);
		if (chests.containsKey(playerName) && chests.get(playerName).containsKey(chestName))
			return chests.get(playerName).get(chestName);
		else {
			if (chestExists(playerName, chestName)) {
				HashMap<String, VirtualChest> loadedChests = fManager.getPlayerChests(playerName);
				chests.put(playerName, loadedChests);
				workerLog.info("Chests owned by " + playerName + " loaded from file (" + loadedChests.size() + ")");
				return loadedChests.get(chestName);
			} else {
				workerLog.warning("Tried to load " + chestName + " of player " + playerName + " that don't exist");
				return null;
			}
		}
	}
	
	/** Save the chests of the player and remove it from memory.
	 * 
	 * @param player */
	public void unloadPlayerChests(String player) {
		fManager.savePlayerChest(player, chests.get(player));
		defaultChests.remove(player);
		sendReceiveChests.remove(player);
		HashMap<String, VirtualChest> playerChests = chests.get(player);
		for (String name : playerChests.keySet())
			playerChests.get(name).emptyChest();
		playerChests.clear();
		playerChests = null;
		chests.remove(player);
		workerLog.info("Chests of " + player + " unloaded from memory.");
	}
	
	/** check if the given chest already exists.
	 * 
	 * @param playerName
	 * @param chestName
	 * @return */
	public boolean chestExists(Player player, String chestName) {
		return chestExists(player.getName(), chestName);
	}
	
	public boolean chestExists(String player, String chestName) {
		return (allChests.containsKey(player) && allChests.get(player).hasChest(chestName));
	}
	
	/** Return the number of owned chest
	 * 
	 * @param p
	 * @return */
	public int numberOfChest(Player p) {
		if (allChests.containsKey(p.getName()))
			return allChests.get(p.getName()).names.size();
		else
			return 0;
	}
	
	/** Return all the chest of the selected player
	 * 
	 * @param p
	 * @return */
	public HashMap<String, VirtualChest> listOfChest(Player p) {
		return chests.get(p.getName());
	}
	
	public ArrayList<String> chestList(String playerName) {
		if (allChests.containsKey(playerName)) {
			return (ArrayList<String>) allChests.get(playerName).names;
		} else
			return null;
	}
	
	/** Return the default chest.
	 * 
	 * @param playerName
	 * @return */
	public VirtualChest getDefaultChest(String playerName) {
		if (defaultChests.containsKey(playerName))
			return defaultChests.get(playerName);
		else {
			String defaultChest = fManager.openDefaultChest(playerName);
			if (defaultChest == null && allChests.containsKey(playerName))
				defaultChest = allChests.get(playerName).names.get(0);
			VirtualChest v = getChest(playerName, defaultChest);
			if (v != null)
				setDefaultChest(playerName, v);
			else
				workerLog.severe("Opening default chest of " + playerName + " returned NULL for chest : " + defaultChest);
			return v;
		}
	}
	
	/** Return the send chest.
	 * 
	 * @param playerName
	 * @return */
	public VirtualChest getSendChest(String playerName) {
		if (sendReceiveChests.containsKey(playerName))
			return sendReceiveChests.get(playerName);
		else {
			VirtualChest v = getChest(playerName, fManager.openSendChest(playerName));
			if (v == null)
				return getDefaultChest(playerName);
			else {
				sendReceiveChests.put(playerName, v);
				return v;
			}
		}
	}
	
	/** Set default chest for the player.
	 * 
	 * @param playerName
	 * @param v
	 * @return */
	public boolean setDefaultChest(String playerName, String vChest) {
		VirtualChest v = getChest(playerName, vChest);
		return setDefaultChest(playerName, v);
	}
	
	public boolean setDefaultChest(String playerName, VirtualChest v) {
		if (v != null && chests.get(playerName).containsValue(v)) {
			defaultChests.put(playerName, v);
			fManager.createDefaultChest(playerName, v.getName());
			return true;
		}
		return false;
	}
	
	/** Set send chest for the player.
	 * 
	 * @param playerName
	 * @param v
	 * @return */
	public boolean setSendChest(String playerName, String vChest) {
		VirtualChest v = getChest(playerName, vChest);
		if (v != null)
			return setSendChest(playerName, v);
		return false;
	}
	
	public boolean setSendChest(String playerName, VirtualChest v) {
		if (chests.containsKey(playerName) && chests.get(playerName).containsValue(v)) {
			sendReceiveChests.put(playerName, v);
			fManager.createSendReceiveChest(playerName, v.getName());
			return true;
		}
		return false;
	}
	
	/** add the new chest in the list of existing chests.
	 * 
	 * @param playerName
	 * @param type
	 * @param vChestName */
	private void addInAllChests(String playerName, String type, String vChestName) {
		if (allChests.containsKey(playerName)) {
			allChests.get(playerName).names.add(vChestName);
			allChests.get(playerName).types.add(type);
		} else {
			PlayerChests pChest = new PlayerChests();
			pChest.names.add(vChestName);
			pChest.types.add("type");
			allChests.put(playerName, pChest);
		}
		workerLog.info("Created new " + type + " chest (" + vChestName + ") for " + playerName);
	}
	
	/** Add a new chest
	 * 
	 * @param player
	 * @param vChest
	 *            VirtualChest to add */
	public void addChest(Player player, VirtualChest vChest) {
		if (chests.containsKey(player.getName()))
			chests.get(player.getName()).put(vChest.getName(), vChest);
		else {
			HashMap<String, VirtualChest> tmp = new HashMap<String, VirtualChest>();
			tmp.put(vChest.getName(), vChest);
			chests.put(player.getName(), tmp);
		}
		if (vChest instanceof VirtualLargeChest)
			fManager.createChestFile(player, vChest.getName(), "large");
		else
			fManager.createChestFile(player, vChest.getName(), "normal");
		if (vChest instanceof VirtualLargeChest)
			addInAllChests(player.getName(), "large", vChest.getName());
		else
			addInAllChests(player.getName(), "normal", vChest.getName());
		if (numberOfChest(player) == 1)
			setDefaultChest(player.getName(), vChest);
	}
	
	public boolean upgradeChest(Player player, VirtualChest vChest) {
		if (chests.containsKey(player.getName())) {
			VirtualLargeChest newChest = new VirtualLargeChest(vChest);
			chests.get(player.getName()).put(vChest.getName(), newChest);
			fManager.upgradeChest(player, vChest.getName());
			allChests.get(player.getName()).types.set(allChests.get(player.getName()).names.indexOf(vChest.getName()), "large");
			if (defaultChests.containsValue(vChest))
				defaultChests.put(player.getName(), newChest);
			if (sendReceiveChests.containsValue(vChest))
				sendReceiveChests.put(player.getName(), newChest);
			return true;
		}
		return false;
	}
	
	/** @param player
	 * @return if the player have a chest loaded. */
	public boolean haveAChestInMemory(String player) {
		return chests.containsKey(player);
	}
	
	/** Delete the chest from memory and save.
	 * 
	 * @param player
	 * @param vChest
	 * @return */
	public boolean removeChest(Player player, VirtualChest vChest) {
		String pName = player.getName();
		if (chests.containsKey(pName)) {
			HashMap<String, VirtualChest> playerChests = chests.get(pName);
			if (playerChests.remove(vChest.getName()) != null) {
				fManager.deleteChestFile(pName, vChest.getName());
				PlayerChests pChests = allChests.get(pName);
				int index = pChests.names.indexOf(vChest.getName());
				pChests.names.remove(index);
				pChests.types.remove(index);
				workerLog.info(pName + " removed his chest : " + vChest.getName());
				if (pChests.names.size() != 0) {
					String newDefaultChest = pChests.names.get(0);
					if (defaultChests.containsValue(vChest)) {
						defaultChests.put(pName, getChest(player.getName(), newDefaultChest));
						fManager.createDefaultChest(pName, newDefaultChest);
					}
					if (sendReceiveChests.containsValue(vChest)) {
						sendReceiveChests.put(pName, defaultChests.get(pName));
						fManager.createSendReceiveChest(pName, newDefaultChest);
					}
				} else {
					defaultChests.remove(pName);
					sendReceiveChests.remove(pName);
					chests.remove(pName);
					allChests.remove(pName);
					workerLog.info(pName + " has no more chest.");
					fManager.removePlayer(pName);
				}
				vChest = null;
				return true;
			}
			return false;
		}
		return false;
	}
	
	/** Rename a chest
	 * 
	 * @param player
	 * @param oldName
	 * @param newName */
	public void renameChest(Player player, String oldName, String newName) {
		String playerName = player.getName();
		VirtualChest v = getChest(playerName, oldName);
		if (v != null) {
			v.setName(newName);
			chests.get(playerName).remove(oldName);
			chests.get(playerName).put(newName, v);
			fManager.renameChestFile(playerName, oldName, newName);
			PlayerChests pChest = allChests.get(playerName);
			int index = pChest.names.indexOf(oldName);
			pChest.names.remove(index);
			String type = pChest.types.get(index);
			pChest.types.remove(index);
			pChest.names.add(newName);
			pChest.types.add(type);
			if (defaultChests.containsValue(v))
				fManager.createDefaultChest(playerName, newName);
			if (sendReceiveChests.containsValue(v))
				fManager.createSendReceiveChest(playerName, newName);
			workerLog.info(playerName + " renamed his chest [" + oldName + "] to {" + newName + "}");
		}
	}
	
	/** Get a command represented by a specific class
	 * 
	 * @param clazz
	 * @return */
	public GPCommand getCommand(Class<?> clazz) {
		for (GPCommand command : commands)
			if (command.getClass() == clazz)
				return command;
		return null;
	}
	
	/** @return the list of commands */
	public List<GPCommand> getCommands() {
		return commands;
	}
	
	/** Save all the chests. */
	public synchronized void save() {
		fManager.savePerPlayer(chests);
		workerLog.info("Chests Saved !");
	}
	
	/** load all the chests */
	public synchronized void oldLoad() {
		try {
			config.reload();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		fManager.loadChests("chests.dat", chests);
	}
	
	/** load all the chests */
	public synchronized void newLoad() {
		try {
			config.reload();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		allChests = fManager.getAllPlayerChestType();
		if (allChests == null) {
			allChests = new MapMaker().concurrencyLevel(8).makeMap();
			workerLog.info("No player files found");
		} else {
			for (Player p : VirtualChestPlugin.getBukkitServer().getOnlinePlayers()) {
				String playerName = p.getName();
				if (allChests.containsKey(playerName)) {
					chests.put(playerName, fManager.getPlayerChests(playerName));
					workerLog.info("Chests owned by " + playerName + " loaded from file");
				}
			}
		}
	}
	
	/** Transfer from an old save
	 * 
	 * @deprecated */
	@Deprecated
	public synchronized void transfer() {
		HashMap<String, HashMap<String, VirtualChest>> loaded = fManager.transfer("chest.dat");
		if (loaded != null) {
			chests = loaded;
			TreeMap<String, String> tmp = fManager.getAllPlayerDefaultChest();
			if (tmp != null)
				for (String player : tmp.keySet())
					defaultChests.put(player, getChest(player, tmp.get(player)));
		}
	}
	
	/** Check the permissions
	 * 
	 * @param player
	 * @param perm
	 * @return boolean */
	public boolean hasPerm(Player player, String perm) {
		return hasPerm(player, perm, true);
	}
	
	/** Check the permission with the possibility to disable the error msg
	 * 
	 * @param player
	 * @param perm
	 * @param errorMsg
	 * @return */
	public boolean hasPerm(Player player, String perm, boolean errorMsg) {
		return PermissionManager.hasPerm(player, perm, errorMsg);
	}
	
	/** Check if the command contain the flag
	 * 
	 * @param args
	 * @param checkFlag
	 * @return */
	public boolean hasFlag(String[] args, String checkFlag) {
		if (args.length >= 1) {
			String flag = args[0].toLowerCase();
			return flag.equals(checkFlag) || flag.equals("-" + checkFlag);
		}
		return false;
	}
	
	/** @return a keyset with all the name of the player owning a chest. */
	public Set<String> getAllOwner() {
		return chests.keySet();
	}
	
	/** Convert the save to the new format */
	public void convertSave() {
		oldLoad();
		fManager.savePerPlayer(chests);
		chests.clear();
		sendReceiveChests.clear();
		defaultChests.clear();
		allChests = fManager.getAllPlayerChestType();
		workerLog.info("Saves converted.");
	}
	
	/** Check if the plugin iConomy is present and if the player have enough
	 * money. After checked, substract the money.
	 * 
	 * @param gpw
	 * @param player
	 * @return */
	public boolean economyCheck(Player player, String configParam) {
		if (economy != null && getConfig().getString("iConomy", "false").matches("true") && !this.hasPerm(player, "giftpost.admin.free", false)) {
			if (!economy.has(player.getName(), getConfig().getDouble(configParam, 1.0))) {
				player.sendMessage(chestKeeper() + ChatColor.RED + "You don't have " + economy.format(getConfig().getDouble(configParam, 10.0)) + " to pay the Chests Keeper !");
				return false;
			} else {
				if (getConfig().getDouble(configParam, 1.0) != 0) {
					economy.withdrawPlayer(player.getName(), getConfig().getDouble(configParam, 1.0));
					player.sendMessage(chestKeeper() + " " + economy.format(getConfig().getDouble(configParam, 1.0)) + ChatColor.DARK_GRAY + " used to pay the Chests Keeper.");
				}
				return true;
			}
		}
		return true;
	}
	
	public boolean economyUpgradeCheck(Player player) {
		if (economy != null && getConfig().getString("iConomy", "false").matches("true") && !this.hasPerm(player, "giftpost.admin.free", false)) {
			double amount = getConfig().getDouble("iConomy-largeChest-price", 500.0) - getConfig().getDouble("iConomy-normalChest-price", 250.0);
			if (!economy.has(player.getName(), amount)) {
				player.sendMessage(chestKeeper() + ChatColor.RED + "You don't have " + economy.format(amount) + " to pay the Chests Keeper !");
				return false;
			} else {
				if (amount != 0) {
					economy.withdrawPlayer(player.getName(), amount);
					player.sendMessage(chestKeeper() + " " + economy.format(amount) + ChatColor.DARK_GRAY + " used to pay the Chests Keeper.");
				}
				return true;
			}
		}
		return true;
	}
	
	public String getDefaultType(Player player) {
		String limit;
		limit = PermissionManager.getPermissionLimit(player, "chestType");
		if (limit == null || limit.isEmpty())
			limit = VirtualChestWorker.getInstance().getConfig().getString("chest-default", "normal");
		return limit;
	}
}
