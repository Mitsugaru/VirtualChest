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
package com.zone.vchest.objects;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/** VirtualChest for Bukkit
 * 
 * @authors Timberjaw and Balor */
public class VirtualChest implements Cloneable, InventoryHolder {
	
	protected Inventory chest;
	
	/** Constructor
	 * 
	 * @param chestName */
	public VirtualChest(String chestName) {
		chest = Bukkit.createInventory(this, 27, chestName);
	}
	
	public VirtualChest(VirtualChest v) {
		chest = v.chest;
	}
	
	/** Open the chest for the owner */
	public void openChest(Player p) {
		p.openInventory(chest);
	}
	
	/** Add some ItemStack to the chest
	 * 
	 * @param iss */
	public void addItemStack(ItemStack[] iss) {
		addItemStack(iss, false);
	}
	
	/** If we want to create new itemStacks and add it.
	 * 
	 * @param iss
	 * @param create */
	public void addItemStack(ItemStack[] iss, boolean create) {
		if (!create) {
			for (ItemStack is : iss)
				if (is != null)
					addItemStack(is);
		} else {
			for (ItemStack is : iss)
				if (is != null)
					addItemStack(is);
		}
	}
	
	/** adding a ItemStack to the chest
	 * 
	 * @param is
	 * @return */
	public boolean addItemStack(ItemStack is) {
		if (isFull())
			return false;
		return chest.addItem(is).isEmpty();
	}
	
	/** Empty chest */
	public void emptyChest() {
		chest.clear();
	}
	
	/** is Chest Full
	 * 
	 * @return */
	public boolean isFull() {
		for (ItemStack is : chest.getContents()) {
			if (is == null) {
				return false;
			}
		}
		return true;
	}
	
	/** is Chest Empty
	 * 
	 * @return */
	public boolean isEmpty() {
		for (ItemStack is : chest.getContents()) {
			if (is != null) {
				return false;
			}
		}
		return true;
	}
	
	/** Nb of empty cases left
	 * 
	 * @return */
	public int leftCases() {
		int empty = 0;
		for (ItemStack is : chest.getContents()) {
			if (is == null) {
				empty++;
			}
		}
		return empty;
	}
	
	/** Nb of used Cases
	 * 
	 * @return */
	public int usedCases() {
		int used = 0;
		for (ItemStack is : chest.getContents()) {
			if (is != null) {
				used++;
			}
		}
		return used;
	}
	
	/** get all the itemStacks that compose the chest
	 * 
	 * @return */
	public ItemStack[] getMcContents() {
		return chest.getContents();
	}
	
	/** Set the index to the chosen Bukkit ItemStack
	 * 
	 * @param index
	 * @param item */
	public void setItem(int index, ItemStack item) {
		chest.setItem(index, item);
	}
	
	/** Add an Bukkit ItemStack to the virtual chest
	 * 
	 * @param items
	 * @return */
	public HashMap<Integer, ItemStack> addItem(ItemStack... items) {
		return chest.addItem(items);
	}
	
	public void remove(int materialId) {
		org.bukkit.inventory.ItemStack[] items = getContents();
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null && items[i].getTypeId() == materialId) {
				removeItemStack(i);
			}
		}
	}
	
	public void remove(Material material) {
		remove(material.getId());
	}
	
	/** Remove an Bukkit ItemStack from the VirtualChest
	 * 
	 * @param item */
	public void remove(ItemStack item) {
		org.bukkit.inventory.ItemStack[] items = getContents();
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null && items[i].equals(item)) {
				removeItemStack(i);
			}
		}
	}
	
	/** Craftbukkit ItemStack
	 * 
	 * @param index
	 * @return */
	public ItemStack getItem(int index) {
		return chest.getItem(index);
	}
	
	/** Transform every item to a craftbukkit item
	 * 
	 * @return */
	public ItemStack[] getContents() {
		return chest.getContents();
	}
	
	// End of CraftBukkit Code
	/** Search for a given itemStack and remove it.
	 * 
	 * @param is */
	public boolean removeItemStack(ItemStack is) {
		for (int i = 0; i < getMcContents().length; i++)
			if (getMcContents()[i].equals(is)) {
				chest.remove(is);
				return true;
			}
		return false;
	}
	
	public void removeItemStack(int i) {
		chest.remove(i);
	}
	
	/** Return the itemStack
	 * 
	 * @param i
	 * @return */
	public ItemStack getItemStack(int i) {
		return chest.getItem(i);
	}
	
	/** Set a given itemStack
	 * 
	 * @param i
	 * @param is */
	public void setItemStack(int i, ItemStack is) {
		chest.setItem(i, is);
	}
	
	/** Swap 2 items stacks
	 * 
	 * @param from
	 * @param to */
	public void swapItemStack(int from, int to) {
		ItemStack first = getItemStack(from);
		ItemStack second = getItemStack(to);
		setItemStack(from, second);
		setItemStack(to, first);
	}
	
	public String getName() {
		return chest.getName();
	}
	
	public void setName(String name) {
		Inventory old = chest;
		chest = Bukkit.createInventory(this, old.getSize(), name);
		chest.setContents(old.getContents());
	}
	
	@Override
	public VirtualChest clone() {
		try {
			VirtualChest result = (VirtualChest) super.clone();
			return result;
		} catch (CloneNotSupportedException e) {
			throw new AssertionError();
		}
	}
	
	public ChestType getType() {
		return ChestType.NORMAL;
	}
	
	@Override
	public Inventory getInventory() {
		return chest;
	}
}
