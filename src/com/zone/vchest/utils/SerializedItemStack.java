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

import java.io.Serializable;

/** @author Balor (aka Antoine Aflalo) */
public class SerializedItemStack implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3850609824930291839L;
	public int count;
	public int id;
	public short damage;
	
	public SerializedItemStack(int i, int j, short k) {
		id = i;
		count = j;
		damage = k;
	}
}
