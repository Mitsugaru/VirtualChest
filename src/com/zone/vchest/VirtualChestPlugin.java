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

import static com.zone.vchest.utils.Display.sendHelp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.zone.vchest.Metrics.Graph;
import com.zone.vchest.Metrics.Plotter;
import com.zone.vchest.commands.Buy;
import com.zone.vchest.commands.Chest;
import com.zone.vchest.commands.ChestList;
import com.zone.vchest.commands.EmptyChest;
import com.zone.vchest.commands.GPCommand;
import com.zone.vchest.commands.GiveItem;
import com.zone.vchest.commands.Help;
import com.zone.vchest.commands.RemoveChest;
import com.zone.vchest.commands.Rename;
import com.zone.vchest.commands.Send;
import com.zone.vchest.commands.SetChest;
import com.zone.vchest.commands.SetChestLimit;
import com.zone.vchest.commands.Upgrade;
import com.zone.vchest.listeners.DeathEntityListener;
import com.zone.vchest.listeners.GPPlayerListener;
import com.zone.vchest.listeners.PluginListener;
import com.zone.vchest.listeners.SignListener;
import com.zone.vchest.manager.permissions.PermParent;
import com.zone.vchest.manager.permissions.PermissionLinker;
import com.zone.vchest.tools.config.file.ExtendedConfiguration;

/** @author Balor */
public class VirtualChestPlugin extends JavaPlugin {
	
	private VirtualChestWorker gpw;
	private static Server server = null;
	private PermissionLinker permLinker = PermissionLinker.getPermissionLinker("GiftPost");
	Metrics m;
	String updateVersion;
	
	private void registerCommand(Class<?> clazz) {
		try {
			GPCommand command = (GPCommand) clazz.newInstance();
			VirtualChestWorker.getInstance().getCommands().add(command);
			if (command.getPermName() != null)
				permLinker.addPermChild(command.getPermName());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	private void registerCommands() {
		permLinker.addPermParent(new PermParent("giftpost.admin.*"));
		permLinker.addPermParent(new PermParent("giftpost.chest.*"));
		permLinker.setMajorPerm(new PermParent("giftpost.*"));
		permLinker.addPermChild("giftpost.chest.everywhere");
		permLinker.addPermChild("giftpost.admin.empty");
		permLinker.addPermChild("giftpost.admin.limit");
		permLinker.addPermChild("giftpost.admin.sign");
		permLinker.addPermChild("giftpost.admin.free");
		permLinker.addPermChild("giftpost.admin.item");
		permLinker.addPermChild("giftpost.admin.sendallusers");
		permLinker.addPermChild("giftpost.admin.open");
		registerCommand(Chest.class);
		registerCommand(Buy.class);
		registerCommand(Send.class);
		registerCommand(ChestList.class);
		registerCommand(EmptyChest.class);
		registerCommand(Help.class);
		if (!VirtualChestWorker.getInstance().getConfig().getString("only-normal", "false").equals("true"))
			registerCommand(Upgrade.class);
		registerCommand(SetChest.class);
		registerCommand(Rename.class);
		registerCommand(RemoveChest.class);
		registerCommand(SetChestLimit.class);
		registerCommand(GiveItem.class);
		permLinker.registerAllPermParent();
	}
	
	private void setupConfigFiles() {
		ExtendedConfiguration.setClassLoader(getClassLoader());
		if (!new File(getDataFolder().toString()).exists()) {
			new File(getDataFolder().toString()).mkdir();
		}
		File yml = new File(getDataFolder() + "/config.yml");
		if (!yml.exists()) {
			new File(getDataFolder().toString()).mkdir();
			try {
				yml.createNewFile();
			} catch (IOException ex) {
				System.out.println("cannot create file " + yml.getPath());
			}
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(yml, true));
				out.write("use-max-range: 'true'");
				out.newLine();
				out.write("max-range: 100");
				out.newLine();
				out.write("allow-offline: 'true'");
				out.newLine();
				out.write("message-of-the-day: 'true'");
				out.newLine();
				out.write("use-wand: 'true'");
				out.newLine();
				out.write("wand-item-id: " + Material.CHEST.getId());
				out.newLine();
				out.write("auto-save-time: 10");
				out.newLine();
				out.write("max-number-chest: 10");
				out.newLine();
				out.write("world-check: 'true'");
				out.newLine();
				out.write("iConomy: 'true'");
				out.newLine();
				out.write("iConomy-send-price: 1.0");
				out.newLine();
				out.write("iConomy-openchest-price: 1.0");
				out.newLine();
				out.write("iConomy-normalChest-price: 10.0");
				out.newLine();
				out.write("iConomy-largeChest-price: 20.0");
				out.newLine();
				out.write("auto-stack: 'true'");
				out.newLine();
				out.write("auto-sort: 'true'");
				out.newLine();
				out.write("only-normal: 'false'");
				out.newLine();
				out.write("only-sign: 'false'");
				out.newLine();
				out.write("chest-default: normal");
				out.newLine();
				out.write("drop-on-death: 'false'");
				out.newLine();
				out.write("auto-update: 'true'");
				out.newLine();
				// Close the output stream
				out.close();
			} catch (Exception e) {
				System.out.println("cannot write config file: " + e);
			}
		}
	}
	
	private void setupListeners() {
		PluginListener pluginListener = new PluginListener();
		GPPlayerListener pListener = new GPPlayerListener();
		SignListener sListener = new SignListener();
		DeathEntityListener deathListener = new DeathEntityListener();
		registerCommands();
		getServer().getScheduler().scheduleSyncRepeatingTask(this, pListener, 0, 1);
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(pluginListener, this);
		pm.registerEvents(pListener, this);
		pm.registerEvents(sListener, this);
		pm.registerEvents(deathListener, this);
	}
	
	public static Server getBukkitServer() {
		return server;
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public void onEnable() {
		updateVersion = getDescription().getVersion();
		server = getServer();
		VirtualChestWorker.setDisable(false);
		setupConfigFiles();
		getLogger().info("[" + getDescription().getName() + "]" + " (version " + getDescription().getVersion() + ")");
		gpw = VirtualChestWorker.getInstance();
		gpw.setConfig(ExtendedConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml")), this);
		gpw.setfManager(getDataFolder().toString());
		setupListeners();
		if (new File(getDataFolder() + File.separator + "chest.dat").exists()) {
			gpw.transfer();
			new File(getDataFolder() + File.separator + "chest.dat").delete();
		} else if (new File(getDataFolder() + File.separator + "chests.dat").exists()) {
			gpw.convertSave();
			new File(getDataFolder() + File.separator + "chests.dat").delete();
		} else
			gpw.newLoad();
		getLogger().info("[" + getDescription().getName() + "] Chests loaded !");
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
			
			@Override
			public void run() {
				VirtualChestWorker.getInstance().save();
			}
		}, (getConfig().getInt("auto-save-time", 10) * 1200) / 2, getConfig().getInt("auto-save-time", 10) * 1200);
		setupMetrics();
		if (gpw.getConfig().getBoolean("auto-update", true)) {
			server.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
				
				@Override
				public void run() {
					checkUpdates();
				}
			}, 0, 20 * 30);
		}
	}
	
	@Override
	public void onDisable() {
		if (com.zone.vchest.utils.Downloader.pluginName == null) {
			PluginDescriptionFile pdfFile = getDescription();
			gpw.save();
			server.getScheduler().cancelTasks(this);
			VirtualChestWorker.setDisable(true);
			VirtualChestWorker.killInstance();
			getLogger().info("[" + pdfFile.getName() + "]" + " Plugin Disabled. (version " + pdfFile.getVersion() + ")");
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You have to be a player!");
			return true;
		} else {
			if (args.length == 0) {
				sendHelp(sender, 1);
				return true;
			}
			int i = gpw.getCommands().size();
			for (GPCommand cmd : gpw.getCommands()) {
				if (!cmd.validate(gpw, sender, args)) {
					i--;
					continue;
				}
				try {
					cmd.execute(gpw, sender, args);
				} catch (Exception e) {
					getLogger().info("A VirtualChest command threw an exception!");
					getLogger().info("Go here : http://dev.bukkit.org/server-mods/vchest/");
					getLogger().info("and post the content of this log + the content of plugins/VirtualChest/log.txt please, Thanks.");
					e.printStackTrace();
				}
			}
			if (i == 0)
				sendHelp(sender, 1);
		}
		return true;
	}
	
	private void setupMetrics() {
		try {
			m = new Metrics(this);
		} catch (IOException e) {
			return;
		}
		Graph version = m.createGraph("Version");
		version.addPlotter(new Plotter(getDescription().getVersion()) {
			
			@Override
			public int getValue() {
				return 1;
			}
		});
		m.addGraph(version);
		m.start();
	}
	
	private double parseVersion(String toParse) {
		String[] parts = toParse.split("\\.");
		double version = 0;
		for (int i = 0; i < parts.length; i++) {
			version += Integer.parseInt(parts[i]) * Math.pow(10, -2 * i);
		}
		return version;
	}
	
	private void checkUpdates() {
		try {
			URL sourceURL = new URL("https://dl.dropbox.com/u/38069635/VirtualChest/update.yml");
			Configuration source = YamlConfiguration.loadConfiguration(sourceURL.openStream());
			double currentVer = parseVersion(updateVersion);
			double newVer = parseVersion(source.getString("version", getDescription().getVersion()));
			if (newVer > currentVer) {
				getLogger().info("Updating to " + source.getString("version", getDescription().getVersion()));
				getLogger().info("Source: " + "http://dev.bukkit.org/media/files/" + source.getString("dlpath"));
				long start = System.currentTimeMillis();
				URL download = new URL("http://dev.bukkit.org/media/files/" + source.getString("dlpath"));
				ReadableByteChannel rbc = Channels.newChannel(download.openStream());
				getServer().getUpdateFolderFile().mkdirs();
				FileOutputStream fos = new FileOutputStream(new File(getServer().getUpdateFolderFile(), getFile().getName()));
				fos.getChannel().transferFrom(rbc, 0, 1 << 24);
				fos.close();
				rbc.close();
				getLogger().info("Update completed in " + (System.currentTimeMillis() - start) + " ms. Update will be applied on reload.");
				updateVersion = source.getString("version", getDescription().getVersion());
			}
		} catch (MalformedURLException e) {
			return;
		} catch (IOException e) {
			return;
		}
	}
}
