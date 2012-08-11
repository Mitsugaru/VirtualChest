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

import org.bukkit.Bukkit;

/** @author Balor (aka Antoine Aflalo) */
public class VirtualLargeChest extends VirtualChest {
	
	public VirtualLargeChest(String chestName) {
		super(chestName);
		chest = Bukkit.createInventory(this, 54, chestName);
	}
	
	public VirtualLargeChest(VirtualLargeChest v) {
		this(v.getName());
		this.addItemStack(v.getContents());
	}
	
	public VirtualLargeChest(VirtualChest v) {
		super(v);
	}
	
	@Override
	public VirtualLargeChest clone() {
		try {
			VirtualLargeChest result = (VirtualLargeChest) super.clone();
			return result;
		} catch (Exception e) {
			throw new AssertionError();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.aranai.virtualchest.VirtualChest#getType()
	 */
	@Override
	public ChestType getType() {
		return ChestType.LARGE;
	}
}
