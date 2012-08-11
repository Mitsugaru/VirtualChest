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

import org.bukkit.inventory.ItemStack;

import com.zone.vchest.objects.VirtualChest;

/** @author Balor (aka Antoine Aflalo) */
public class Stacker {
	
	public static boolean sortChest(VirtualChest chest) {
		int swapStackI = 0;
		ItemStack swapStack;
		int size = chest.getMcContents().length;
		for (int index = 0; index < size; index++) {
			ItemStack stack = chest.getItemStack(index);
			swapStack = stack;
			swapStackI = index;
			for (int i = index + 1; i < size; i++) {
				ItemStack stack2 = chest.getItemStack(i);
				if ((stack2 != null && stack2.getAmount() != 0 && stack2.getTypeId() != 0) && (swapStack == null || stack2.getTypeId() < swapStack.getTypeId())) {
					swapStackI = i;
					swapStack = stack2;
				}
			}
			if (swapStack != null) {
				if (swapStack != stack) {
					chest.swapItemStack(index, swapStackI);
				}
			} else {
				break;
			}
		}
		return true;
	}
	
	/** Stacks the contents of a chest. */
	public static boolean stackChest(VirtualChest chest) {
		for (int index = 0; index < chest.getMcContents().length; index++) {
			ItemStack stack = chest.getItem(index);
			if (stack != null && stack.getAmount() != 0 && stack.getTypeId() != 0 && stack.getAmount() != stack.getMaxStackSize()) {
				int i = 0;
				for (ItemStack stack2 : chest.getContents()) {
					if (stack2 != null && i != index && stack2.getAmount() != 0 && stack2.getAmount() < stack2.getMaxStackSize() && stack2.getTypeId() == stack.getTypeId() && stack2.getDurability() == stack.getDurability()) {
						int oldCount = stack.getAmount();
						stack.setAmount(Math.min(stack2.getMaxStackSize(), stack.getAmount() + stack2.getAmount()));
						chest.setItem(index, stack);
						stack2.setAmount(Math.max(0, oldCount + stack2.getAmount() - stack2.getMaxStackSize()));
						if (stack2.getAmount() > 0) {
							chest.setItem(i, stack2);
							break;
						} else
							chest.removeItemStack(i);
					}
					i++;
				}
			}
			index++;
		}
		return true;
	}
}
