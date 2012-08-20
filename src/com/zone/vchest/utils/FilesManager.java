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
package com.zone.vchest.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.aranai.virtualchest.ItemStackSave;
import com.google.common.collect.MapMaker;
import com.google.common.io.Files;
import com.zone.vchest.VirtualChestWorker;
import com.zone.vchest.objects.VirtualChest;
import com.zone.vchest.objects.VirtualLargeChest;
import com.zone.vchest.tools.config.file.ExtendedConfiguration;

/** @author Balor (aka Antoine Aflalo) */
public class FilesManager {
	
	protected String path;
	private ExtendedConfiguration cacheConfiguration;
	private String lastDir = "";
	private String lastFilename = "";
	
	/** Constructor
	 * 
	 * @param path */
	public FilesManager(String path) {
		this.path = path;
		if (!new File(this.path).exists()) {
			new File(this.path).mkdir();
		}
	}
	
	/** Open the file and return the Configuration object
	 * 
	 * @param directory
	 * @param filename
	 * @return the configuration file */
	private ExtendedConfiguration getYml(String directory, String filename) {
		if (cacheConfiguration != null && lastDir.equals(directory == null ? "" : directory) && lastFilename.equals(filename))
			return cacheConfiguration;
		if (directory == null)
			lastDir = "";
		lastFilename = filename;
		cacheConfiguration = ExtendedConfiguration.loadConfiguration(getFile(directory, filename));
		return cacheConfiguration;
	}
	
	/** Open the file and return the File object
	 * 
	 * @param directory
	 * @param fileName
	 * @return the configuration file */
	private File getFile(String directory, String fileName) {
		return getFile(directory, fileName, true);
	}
	
	private File getFile(String directory, String fileName, boolean create) {
		File file = new File(path + (directory != null ? File.separator + directory + File.separator : File.separator) + fileName);
		try {
			Files.createParentDirs(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!file.exists() && create) {
			try {
				file.createNewFile();
			} catch (IOException ex) {
				System.out.println("cannot create file " + file.getPath());
			}
		}
		return file;
	}
	
	/** Save the player chest.
	 * 
	 * @param pName
	 * @param chests */
	public void savePlayerChest(String pName, HashMap<String, VirtualChest> chests) {
		ExtendedConfiguration pChests = getYml("Chests", pName + ".chestYml");
		for (String chestName : chests.keySet()) {
			VirtualChest v = chests.get(chestName);
			if (v instanceof VirtualLargeChest) {
				createChestFile(pName, chestName, "large");
				pChests.set(chestName + ".type", "large");
			} else {
				createChestFile(pName, chestName, "normal");
				pChests.set(chestName + ".type", "normal");
			}
			List<ItemStackSave> toBeSave = new ArrayList<ItemStackSave>();
			for (org.bukkit.inventory.ItemStack is : v.getContents())
				if (is != null) {
					toBeSave.add(new ItemStackSave(is));
				}
			pChests.set(chestName + ".eitems", toBeSave);
			pChests.remove(chestName + ".items");
		}
		try {
			pChests.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
		VirtualChestWorker.workerLog.fine("Chests of " + pName + " Saved.");
	}
	
	/** Convert the old save to the new save format
	 * 
	 * @param chest */
	public void savePerPlayer(HashMap<String, HashMap<String, VirtualChest>> chest) {
		for (String pNames : chest.keySet()) {
			savePlayerChest(pNames, chest.get(pNames));
		}
	}
	
	/** Create the offline file for the player
	 * 
	 * @param to
	 * @param items
	 * @param from */
	public void createOfflineFile(String to, ItemStack[] items, String from) {
		ExtendedConfiguration conf = getYml("Players", to + ".yml");
		List<String> itemsNames = new ArrayList<String>();
		List<Integer> itemsAmount = new ArrayList<Integer>();
		List<String> playerNames = new ArrayList<String>();
		for (ItemStack is : items)
			if (is != null) {
				itemsNames.add(Material.getMaterial(is.getTypeId()).toString().toLowerCase().replace('_', ' '));
				itemsAmount.add(is.getAmount());
			}
		if (conf.get("From." + from) == null) {
			playerNames.add(from);
			if (conf.get("Players") == null)
				conf.set("Players", playerNames);
			else {
				playerNames.addAll(conf.getStringList("Players", null));
				conf.set("Players", playerNames);
			}
			conf.set("From." + from, from);
			conf.set("From." + from + ".Items", itemsNames);
			conf.set("From." + from + ".Amount", itemsAmount);
		} else {
			List<String> list = new ArrayList<String>();
			List<Integer> list2 = new ArrayList<Integer>();
			list = conf.getStringList("From." + from + ".Items", list);
			list2 = conf.getIntList("From." + from + ".Amount", list2);
			itemsNames.addAll(list);
			itemsAmount.addAll(list2);
			conf.set("From." + from + ".Items", itemsNames);
			conf.set("From." + from + ".Amount", itemsAmount);
		}
		try {
			conf.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Empty offline information from the file.
	 * 
	 * @param player */
	public void emptyOfflineFile(Player player) {
		ExtendedConfiguration conf = getYml("Players", player.getName() + ".yml");
		conf.set("Players", null);
		conf.set("From", null);
		try {
			conf.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Accessor to deleteFile()
	 * 
	 * @param p */
	public void deletePlayerFile(Player p) {
		deleteFile("Players", p.getName() + ".yml");
	}
	
	/** Delete the given file
	 * 
	 * @param directory
	 * @param fileName */
	private void deleteFile(String directory, String fileName) {
		File toDel = new File(path + File.separator + directory + File.separator + fileName);
		if (toDel.exists())
			try {
				toDel.delete();
			} catch (SecurityException ex) {
				System.out.println("cannot delete file " + toDel.getPath());
			}
	}
	
	/** Save the name of the world were the player is.
	 * 
	 * @param p */
	public void createWorldFile(Player p) {
		ExtendedConfiguration conf = getYml("Players", p.getName() + ".yml");
		conf.set("World", p.getWorld().getName());
		try {
			conf.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** The world were the player is.
	 * 
	 * @param name
	 * @return */
	public String openWorldFile(String name) {
		return getYml("Players", name + ".yml").getString("World", "world");
	}
	
	/** Add the type of the chest for the player
	 * 
	 * @param p
	 * @param type */
	public void createChestFile(Player p, String chestName, String type) {
		createChestFile(p.getName(), chestName, type);
	}
	
	private void createChestFile(String pName, String chestName, String type) {
		ExtendedConfiguration chest = getYml("Chests", pName + ".chestYml");
		chest.set(chestName + ".type", type);
		try {
			chest.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Rename a chestFile
	 * 
	 * @param pName
	 * @param oldName
	 * @param newName */
	public void renameChestFile(String pName, String oldName, String newName) {
		File newChestSave;
		if ((newChestSave = getFile("Chests", pName + ".chestYml", false)).exists()) {
			ExtendedConfiguration chest = ExtendedConfiguration.loadConfiguration(newChestSave);
			chest.createSection(newName, chest.getConfigurationSection(oldName).getValues(true));
			chest.remove(oldName);
			try {
				chest.save();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			ExtendedConfiguration conf = getYml("Players", pName + ".yml");
			List<String> chests = conf.getStringList("ChestsNames", null);
			List<String> chestsTypes = conf.getStringList("ChestsTypes", null);
			if (chests.contains(oldName)) {
				int i = chests.indexOf(oldName);
				chests.remove(i);
				String type = chestsTypes.get(i);
				chestsTypes.remove(i);
				chests.add(newName);
				chestsTypes.add(type);
				conf.set("ChestsNames", chests);
				conf.set("ChestsTypes", chestsTypes);
				try {
					conf.save();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/** Remove the chest.
	 * 
	 * @param pName
	 * @param chestName */
	public void deleteChestFile(String pName, String chestName) {
		File newChestSave;
		if ((newChestSave = getFile("Chests", pName + ".chestYml", false)).exists()) {
			ExtendedConfiguration chest = ExtendedConfiguration.loadConfiguration(newChestSave);
			chest.remove(chestName);
			try {
				chest.save();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			ExtendedConfiguration conf = getYml("Players", pName + ".yml");
			List<String> chests = conf.getStringList("ChestsNames", null);
			List<String> chestsTypes = conf.getStringList("ChestsTypes", null);
			if (chests.contains(chestName)) {
				int i = chests.indexOf(chestName);
				chests.remove(i);
				chestsTypes.remove(i);
				conf.set("ChestsNames", chests);
				conf.set("ChestsTypes", chestsTypes);
				try {
					conf.save();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/** Upgrade the selected chest.
	 * 
	 * @param p
	 * @param chestName
	 * @return */
	public boolean upgradeChest(Player p, String chestName) {
		File newChestSave;
		if ((newChestSave = getFile("Chests", p.getName() + ".chestYml", false)).exists()) {
			ExtendedConfiguration chest = ExtendedConfiguration.loadConfiguration(newChestSave);
			if (chest.get(chestName) == null)
				return false;
			chest.set(chestName + ".type", "large");
			try {
				chest.save();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		} else {
			ExtendedConfiguration conf = getYml("Players", p.getName() + ".yml");
			List<String> chests = conf.getStringList("ChestsNames", null);
			List<String> chestsTypes = conf.getStringList("ChestsTypes", null);
			if (chests.contains(chestName)) {
				chestsTypes.set(chests.indexOf(chestName), "large");
				conf.set("ChestsTypes", chestsTypes);
				try {
					conf.save();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
			}
		}
		return false;
	}
	
	public void createChestLimitFile(String player, int limit) {
		ExtendedConfiguration conf = getYml("Players", player + ".yml");
		conf.set("ChestLimit", limit);
		try {
			conf.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int openChestLimitFile(Player p) {
		return getYml("Players", p.getName() + ".yml").getInt("ChestLimit", -1);
	}
	
	/** Check if the player have the chest and then set it as default.
	 * 
	 * @param player
	 * @param chest
	 * @return */
	public boolean createDefaultChest(String player, String chest) {
		ExtendedConfiguration conf = getYml("Players", player + ".yml");
		if (openChestTypeFile(player).concat().containsKey(chest)) {
			conf.set("DefaultChest", chest);
			try {
				conf.save();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}
	
	/** Check if the player have the chest and then set it as send/receive.
	 * 
	 * @param player
	 * @param chest
	 * @return */
	public boolean createSendReceiveChest(String player, String chest) {
		ExtendedConfiguration conf = getYml("Players", player + ".yml");
		if (openChestTypeFile(player).concat().containsKey(chest)) {
			conf.set("SendChest", chest);
			try {
				conf.save();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}
	
	/** Return the value of DefaultChest
	 * 
	 * @param player
	 * @return */
	public String openDefaultChest(String player) {
		return getYml("Players", player + ".yml").getString("DefaultChest", null);
	}
	
	/** Return the value of SendChest
	 * 
	 * @param player
	 * @return */
	public String openSendChest(String player) {
		return getYml("Players", player + ".yml").getString("SendChest", null);
	}
	
	/** Get all the Chests with their type for the given player
	 * 
	 * @param name
	 * @return */
	public PlayerChests openChestTypeFile(String name) {
		if (getFile("Chests", name + ".chestYml", false).exists()) {
			ExtendedConfiguration conf = getYml("Chests", name + ".chestYml");
			List<String> names = new ArrayList<String>();
			List<String> types = new ArrayList<String>();
			for (String nameString : conf.getKeys(false)) {
				names.add(nameString);
				types.add(conf.getString(nameString + ".type"));
			}
			VirtualChestWorker.workerLog.info("Loaded chest of " + name + " using NEW saveFile");
			return new PlayerChests(types, names);
		}
		ExtendedConfiguration conf = getYml("Players", name + ".yml");
		VirtualChestWorker.workerLog.info("Loaded chest of " + name + " using OLD saveFile");
		return new PlayerChests(conf.getStringList("ChestsTypes", new ArrayList<String>()), conf.getStringList("ChestsNames", new ArrayList<String>()));
	}
	
	/** Open the file and say to the player what was send and delete it after
	 * read.
	 * 
	 * @param p */
	public void openOfflineFile(Player p) {
		ExtendedConfiguration conf = getYml("Players", p.getName() + ".yml");
		List<String> playerNames = new ArrayList<String>();
		List<String> itemsNames = new ArrayList<String>();
		List<Integer> itemsAmount = new ArrayList<Integer>();
		playerNames = conf.getStringList("Players", null);
		if (playerNames != null) {
			for (String name : playerNames) {
				itemsNames = conf.getStringList("From." + name + ".Items", itemsNames);
				itemsAmount = conf.getIntList("From." + name + ".Amount", itemsAmount);
				String msg = ChatColor.GREEN + name + ChatColor.WHITE + " send you : " + ChatColor.GOLD;
				for (int i = 0; i < itemsNames.size(); i++)
					msg += itemsAmount.toArray()[i] + " " + itemsNames.toArray()[i] + ", ";
				p.sendMessage(msg.subSequence(0, msg.length() - 2).toString());
			}
		}
		emptyOfflineFile(p);
	}
	
	/** Check if the player have received some item when he was offline
	 * 
	 * @param p
	 * @return */
	public boolean hasOfflineItems(Player p) {
		ExtendedConfiguration conf = getYml("Players", p.getName() + ".yml");
		List<String> playerNames = new ArrayList<String>();
		playerNames = conf.getStringList("Players", null);
		return playerNames != null;
	}
	
	/** Save all the chest.
	 * 
	 * @param chest */
	public void saveChests(HashMap<String, HashMap<String, VirtualChest>> chest, String fileName) {
		String filename = path + File.separator + fileName;
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		ArrayList<SerializedItemStack> itemstacks = new ArrayList<SerializedItemStack>();
		HashMap<String, HashMap<String, ArrayList<SerializedItemStack>>> saved = new HashMap<String, HashMap<String, ArrayList<SerializedItemStack>>>();
		HashMap<String, ArrayList<SerializedItemStack>> tmp = new HashMap<String, ArrayList<SerializedItemStack>>();
		for (String pNames : chest.keySet()) {
			HashMap<String, VirtualChest> hMap = chest.get(pNames);
			for (String chestName : hMap.keySet()) {
				VirtualChest v = hMap.get(chestName);
				if (v instanceof VirtualLargeChest)
					createChestFile(pNames, chestName, "large");
				else
					createChestFile(pNames, chestName, "normal");
				for (org.bukkit.inventory.ItemStack is : v.getContents()) {
					if (is != null)
						itemstacks.add(new SerializedItemStack(is.getTypeId(), is.getAmount(), is.getDurability()));
				}
				tmp.put(chestName, new ArrayList<SerializedItemStack>(itemstacks));
				itemstacks = new ArrayList<SerializedItemStack>();
			}
			saved.put(pNames, new HashMap<String, ArrayList<SerializedItemStack>>(tmp));
			tmp = new HashMap<String, ArrayList<SerializedItemStack>>();
		}
		try {
			fos = new FileOutputStream(filename);
			out = new ObjectOutputStream(fos);
			out.writeObject(saved);
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	/** Get all the type of the parties chests.
	 * 
	 * @return */
	public HashMap<String, String> openAllParties() {
		ExtendedConfiguration conf = getYml("Parties", "parties.yml");
		HashMap<String, String> result = new HashMap<String, String>();
		List<String> names = conf.getStringList("Names", null);
		List<String> types = conf.getStringList("ChestTypes", null);
		if (names == null || types == null)
			return null;
		int i = 0;
		for (String n : names) {
			result.put(n, types.get(i));
			i++;
		}
		return result;
	}
	
	/** Create the file containing the type of the parties chests.
	 * 
	 * @param names
	 * @param types */
	public void createPartyFile(List<String> names, List<String> types) {
		ExtendedConfiguration conf = getYml("Parties", "parties.yml");
		conf.set("Names", names);
		conf.set("ChestTypes", types);
		try {
			conf.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Get the list of all registered player who own a chest.
	 * 
	 * @return */
	public ConcurrentMap<String, PlayerChests> getAllPlayerChestType() {
		ConcurrentMap<String, PlayerChests> result = new MapMaker().concurrencyLevel(5).makeMap();
		File dir = new File(path + File.separator + "Chests");
		if (dir.exists()) {
			List<File> chests = new ArrayList<File>();
			chests.addAll(Arrays.asList(ChestFileFilter.listRecursively(dir)));
			chests.addAll(Arrays.asList(OldChestFileFilter.listRecursively(dir)));
			for (File chest : chests) {
				String s = chest.getName();
				s = s.subSequence(0, s.indexOf('.')).toString();
				PlayerChests typesNames = openChestTypeFile(s);
				if (!typesNames.isEmpty()) {
					result.put(s, typesNames.clone());
					VirtualChestWorker.workerLog.info("Chest of " + s + " : " + typesNames);
				}
			}
			return result;
		}
		return null;
	}
	
	/** open every player file to get the default chest.
	 * 
	 * @return */
	public TreeMap<String, String> getAllPlayerDefaultChest() {
		TreeMap<String, String> result = new TreeMap<String, String>();
		File dir = new File(path + File.separator + "Players");
		if (dir.exists()) {
			for (String s : dir.list()) {
				s = s.subSequence(0, s.length() - 4).toString();
				result.put(s, openDefaultChest(s));
			}
			return result;
		}
		return null;
	}
	
	/** open every player file to get the send chest.
	 * 
	 * @return */
	public TreeMap<String, String> getAllPlayerSendChest() {
		TreeMap<String, String> result = new TreeMap<String, String>();
		File dir = new File(path + File.separator + "Players");
		if (dir.exists()) {
			for (String s : dir.list()) {
				s = s.subSequence(0, s.length() - 4).toString();
				result.put(s, openSendChest(s));
			}
			return result;
		}
		return null;
	}
	
	/** Save all parties chest
	 * 
	 * @param chest
	 * @param fileName */
	public void saveParties(HashMap<String, VirtualChest> chest, String fileName) {
		String filename = path + File.separator + fileName;
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		ArrayList<SerializedItemStack> itemstacks = new ArrayList<SerializedItemStack>();
		HashMap<String, ArrayList<SerializedItemStack>> saved = new HashMap<String, ArrayList<SerializedItemStack>>();
		for (String partyName : chest.keySet()) {
			VirtualChest v = chest.get(partyName);
			for (org.bukkit.inventory.ItemStack is : v.getContents()) {
				if (is != null)
					itemstacks.add(new SerializedItemStack(is.getTypeId(), is.getAmount(), is.getDurability()));
			}
			saved.put(partyName, new ArrayList<SerializedItemStack>(itemstacks));
			itemstacks = new ArrayList<SerializedItemStack>();
		}
		try {
			fos = new FileOutputStream(filename);
			out = new ObjectOutputStream(fos);
			out.writeObject(saved);
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	/** Load all parties
	 * 
	 * @param fileName
	 * @return */
	public HashMap<String, VirtualChest> loadParties(String fileName) {
		String filename = path + File.separator + fileName;
		HashMap<String, VirtualChest> partiesAndChests = new HashMap<String, VirtualChest>();
		HashMap<String, ArrayList<SerializedItemStack>> saved = null;
		HashMap<String, String> partiesChestType = openAllParties();
		if (new File(filename).exists()) {
			FileInputStream fis = null;
			ObjectInputStream in = null;
			try {
				fis = new FileInputStream(filename);
				in = new ObjectInputStream(fis);
				saved = (HashMap<String, ArrayList<SerializedItemStack>>) in.readObject();
				in.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
			}
			if (saved != null) {
				// Chest
				for (String partyName : saved.keySet()) {
					ArrayList<SerializedItemStack> al = saved.get(partyName);
					VirtualChest v;
					if (!partiesChestType.containsKey(partyName) || partiesChestType.get(partyName).matches("normal"))
						v = new VirtualChest(partyName);
					else
						v = new VirtualLargeChest(partyName);
					// ItemStack
					for (SerializedItemStack sis : al) {
						v.addItemStack(new ItemStack(sis.id, sis.count, sis.damage));
					}
					partiesAndChests.put(partyName, v.clone());
				}
				return partiesAndChests;
			}
			return null;
		} else
			return null;
	}
	
	public void removePlayer(String playerName) {
		getFile("Players", playerName + ".yml").delete();
		getFile("Chests", playerName + ".chest").delete();
		VirtualChestWorker.workerLog.info(playerName + " files deleted.");
	}
	
	/** Get all the chests of the player
	 * 
	 * @param player
	 * @return */
	public HashMap<String, VirtualChest> getPlayerChests(String player) {
		HashMap<String, VirtualChest> result = new HashMap<String, VirtualChest>();
		HashMap<String, ArrayList<SerializedItemStack>> saved = new HashMap<String, ArrayList<SerializedItemStack>>();
		if (getFile("Chests", player + ".chestYml", false).exists()) {
			ExtendedConfiguration chestSave = getYml("Chests", player + ".chestYml");
			String chestType = null;
			for (String chestName : chestSave.getKeys(false)) {
				chestType = chestSave.getString(chestName + ".type", "large");
				VirtualChest v;
				if (chestType.equals("normal"))
					v = new VirtualChest(chestName);
				else
					v = new VirtualLargeChest(chestName);
				if (chestSave.contains(chestName + ".items"))
					for (String toParse : chestSave.getStringList(chestName + ".items", new ArrayList<String>())) {
						v.addItem(new ItemStackSave(toParse).getItemStack());
					}
				else if (chestSave.contains(chestName + ".eitems")) {
					for (Object iss : chestSave.getList(chestName + ".eitems"))
						v.addItem(((ItemStackSave) iss).getItemStack());
				}
				result.put(chestName, v.clone());
			}
			File oldChest = getFile("Chests", player + ".chest", false);
			if (oldChest.exists())
				oldChest.delete();
			VirtualChestWorker.workerLog.fine("[NEW SAVE] Chests of " + player + " loaded");
		} else {
			File playerChests = getFile("Chests", player + ".chest", false);
			if (playerChests.exists()) {
				FileInputStream fis = null;
				ObjectInputStream in = null;
				TreeMap<String, String> chests = openChestTypeFile(player).concat();
				try {
					fis = new FileInputStream(playerChests);
					in = new ObjectInputStream(fis);
					saved = (HashMap<String, ArrayList<SerializedItemStack>>) in.readObject();
					in.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				} catch (ClassNotFoundException ex) {
					ex.printStackTrace();
				}
				for (String chestName : saved.keySet()) {
					VirtualChest v;
					if (!chests.containsKey(chestName) || chests.get(chestName).matches("normal"))
						v = new VirtualChest(chestName);
					else
						v = new VirtualLargeChest(chestName);
					for (SerializedItemStack sis : saved.get(chestName)) {
						v.addItemStack(new ItemStack(sis.id, sis.count, sis.damage));
					}
					result.put(chestName, v.clone());
				}
				VirtualChestWorker.workerLog.fine("[OLD SAVE] Chests of " + player + " loaded");
			} else
				VirtualChestWorker.workerLog.warning(player + " don't have chests, but you tried to load it.");
		}
		return result;
	}
	
	/** Load the saved chest file.
	 * 
	 * @param fileName
	 * @return an HashMap with all the chests */
	public void loadChests(String fileName, HashMap<String, HashMap<String, VirtualChest>> playerAndChest) {
		String filename = path + File.separator + fileName;
		HashMap<String, HashMap<String, ArrayList<SerializedItemStack>>> saved = null;
		ConcurrentMap<String, PlayerChests> playerChestType = getAllPlayerChestType();
		if (new File(filename).exists()) {
			FileInputStream fis = null;
			ObjectInputStream in = null;
			try {
				fis = new FileInputStream(filename);
				in = new ObjectInputStream(fis);
				saved = (HashMap<String, HashMap<String, ArrayList<SerializedItemStack>>>) in.readObject();
				in.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
			}
			if (saved != null) {
				// Player
				for (String playerName : saved.keySet()) {
					if (playerChestType.containsKey(playerName)) {
						HashMap<String, VirtualChest> tmp = new HashMap<String, VirtualChest>();
						playerAndChest.put(playerName, tmp);
						TreeMap<String, String> chestsTypes = playerChestType.get(playerName).concat();
						HashMap<String, ArrayList<SerializedItemStack>> hMap = saved.get(playerName);
						// Chest
						for (String chestName : hMap.keySet()) {
							ArrayList<SerializedItemStack> al = hMap.get(chestName);
							VirtualChest v;
							if (!chestsTypes.containsKey(chestName) || chestsTypes.get(chestName).matches("normal"))
								v = new VirtualChest(chestName);
							else
								v = new VirtualLargeChest(chestName);
							// ItemStack
							for (SerializedItemStack sis : al) {
								v.addItemStack(new ItemStack(sis.id, sis.count, sis.damage));
							}
							playerAndChest.get(playerName).put(chestName, v);
						}
					}
				}
			}
		}
	}
	
	/** Used ONE time to transfer old save format to new.
	 * 
	 * @deprecated use public HashMap<String, HashMap<String, VirtualChest>>
	 *             loadChests(String fileName) instead
	 * @param fileName
	 * @return */
	@Deprecated
	public HashMap<String, HashMap<String, VirtualChest>> transfer(String fileName) {
		String filename = path + File.separator + fileName;
		ExtendedConfiguration config = ExtendedConfiguration.loadConfiguration(new File(path + File.separator + "config.yml"));
		String typeChosen = config.getString("chest-type");
		HashMap<String, HashMap<String, VirtualChest>> chests = new HashMap<String, HashMap<String, VirtualChest>>();
		HashMap<String, ArrayList<SerializedItemStack>> saved = null;
		if (new File(filename).exists()) {
			FileInputStream fis = null;
			ObjectInputStream in = null;
			try {
				fis = new FileInputStream(filename);
				in = new ObjectInputStream(fis);
				saved = (HashMap<String, ArrayList<SerializedItemStack>>) in.readObject();
				in.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
			}
			if (saved != null) {
				Set<String> names = saved.keySet();
				int i = 0;
				for (ArrayList<SerializedItemStack> al : saved.values()) {
					VirtualChest v;
					if (typeChosen.matches("normal"))
						v = new VirtualChest(names.toArray()[i].toString().toLowerCase());
					else
						v = new VirtualLargeChest(names.toArray()[i].toString().toLowerCase());
					for (SerializedItemStack sis : al)
						v.addItemStack(new ItemStack(sis.id, sis.count, sis.damage));
					HashMap<String, VirtualChest> tmp = new HashMap<String, VirtualChest>();
					tmp = new HashMap<String, VirtualChest>();
					if (v instanceof VirtualLargeChest) {
						tmp.put(names.toArray()[i].toString().toLowerCase(), new VirtualLargeChest(v));
						createChestFile((String) names.toArray()[i], names.toArray()[i].toString().toLowerCase(), "large");
					} else {
						tmp.put(names.toArray()[i].toString().toLowerCase(), new VirtualChest(v));
						createChestFile((String) names.toArray()[i], names.toArray()[i].toString().toLowerCase(), "normal");
					}
					HashMap<String, VirtualChest> tmp2 = new HashMap<String, VirtualChest>();
					tmp2.putAll(tmp);
					chests.put((String) names.toArray()[i], tmp2);
					createDefaultChest((String) names.toArray()[i], names.toArray()[i].toString().toLowerCase());
					i++;
				}
				return chests;
			}
			return null;
		} else
			return null;
	}
}