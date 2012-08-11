/*This file is part of GiftPost .

    GiftPost is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GiftPost is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GiftPost.  If not, see <http://www.gnu.org/licenses/>.*/
package com.zone.vchest.commands;

import com.zone.vchest.VirtualChestWorker;

import org.bukkit.command.CommandSender;

/**
 *
 * @author Balor
 */
public interface GPCommand
{

    /**
     * Execute commands
     * @param gp
     * @param sender
     * @param args
     */
    public void execute(VirtualChestWorker gpw, CommandSender sender, String[] args);

    /**
     * Validate a command to check if it should be executed
     *
     * @param lwc
     * @param command
     * @param args
     * @return
     */
    public boolean validate(VirtualChestWorker gpw, CommandSender sender, String[] args);
    /**
     * @return the name of the perm to add in the permFile.
     */
    public String getPermName();
    /**
     * 
     * @return the command help
     */
    public String getHelp();
}
