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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import com.zone.vchest.objects.VirtualChest;

/** @author Balor (aka Antoine Aflalo) */
public class Stacker {
	
	public static boolean sortChest(VirtualChest chest) {
		sort(chest);
		return true;
	}
	
	/** Stacks the contents of a chest. */
	public static boolean stackChest(VirtualChest chest) {
		sort(chest);
		return true;
	}
	
	public static void sort(VirtualChest e) {
		List<ItemStack> stacks = new ArrayList<ItemStack>();
		for (ItemStack is : e.getInventory().getContents()) {
			if (is == null)
				continue;
			for (ItemStack check : stacks) {
				if (check == null)
					continue;
				if (check.getType() == is.getType() && ((check.getData() == null && is.getData() == null) || check.getData().getData() == is.getData().getData())) {
					int transfer = Math.min(is.getAmount(), check.getMaxStackSize() - check.getAmount());
					is.setAmount(is.getAmount() - transfer);
					check.setAmount(check.getAmount() + transfer);
				}
			}
			if (is.getAmount() > 0) {
				stacks.add(is);
			}
		}
		Collections.sort(stacks, new Comparator<ItemStack>() {
			
			@Override
			public int compare(ItemStack o1, ItemStack o2) {
				if (o1.getTypeId() > o2.getTypeId()) {
					return 1;
				} else if (o1.getTypeId() < o2.getTypeId()) {
					return -1;
				} else if (o1.getData() != null && o2.getData() != null && o1.getData().getData() > o2.getData().getData()) {
					return 1;
				} else if (o1.getData() != null && o2.getData() != null && o1.getData().getData() < o2.getData().getData()) {
					return -1;
				} else if (o1.getAmount() > o2.getAmount()) {
					return -1;
				} else if (o1.getAmount() < o2.getAmount()) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		e.getInventory().clear();
		e.getInventory().setContents(stacks.toArray(new ItemStack[0]));
	}
}
