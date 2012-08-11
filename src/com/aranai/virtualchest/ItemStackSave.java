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
package com.aranai.virtualchest;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 * @author Balor (aka Antoine Aflalo)
 * 
 */
public class ItemStackSave {
	public int count;
	public int id;
	public short damage;
	public Map<Integer, Integer> echantments = new HashMap<Integer, Integer>();

	public ItemStackSave(String toParse) {
		String infos[] = new String[3];
		infos = toParse.split(";");
		this.id = Integer.parseInt(infos[0]);
		this.count = Integer.parseInt(infos[1]);
		this.damage = Short.parseShort(infos[2]);
	}

	/**
	 * @param is
	 */
	public ItemStackSave(ItemStack is) {
		this.id = is.getTypeId();
		this.count = is.getAmount();
		this.damage = is.getDurability();
		try {
			for (Entry<Enchantment, Integer> e : is.getEnchantments().entrySet())
				echantments.put(e.getKey().getId(), e.getValue());
		} catch (NoSuchMethodError e) {
		}
	}

	public ItemStackSave() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return id + ";" + count + ";" + damage;
	}

	public ItemStack getItemStack() {
		ItemStack is = new ItemStack(id, count, damage);
		try {
			for (Entry<Integer, Integer> ench : echantments.entrySet())
				is.addUnsafeEnchantment(Enchantment.getById(ench.getKey()), ench.getValue());
		} catch (NoSuchMethodError e) {
		}
		return is;
	}
}
