package com.mstiles92.bookrules;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lib.PatPeter.SQLibrary.SQLite;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.mstiles92.bookrules.lib.CraftWrittenBook;
import com.mstiles92.bookrules.lib.MetricsLite;
import com.mstiles92.bookrules.lib.WrittenBook;

public class BookRulesPlugin extends JavaPlugin {
	public Books books;
	public final String tag = ChatColor.BLUE + "[BookRules] " + ChatColor.GREEN;
	public boolean updateAvailable = false;
	public String latestKnownVersion;
	public SQLite sqlite;
	
	public void onEnable() {
		getCommand("rulebook").setExecutor(new BookRulesCommandExecutor(this));
		getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
		loadConfig();
		latestKnownVersion = this.getDescription().getVersion();
		if (getConfig().getBoolean("Check-for-Updates")) {
			this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new UpdateChecker(this), 40, 216000);
		}
		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		} catch (IOException e) {
			log("Failed to start metrics!");
		}
		
		sqlite = new SQLite(this.getLogger(), "BookRules", "players", this.getDataFolder().getAbsolutePath());
		try {
			sqlite.open();
			sqlite.query("CREATE TABLE IF NOT EXISTS players(Id INTEGER PRIMARY KEY AUTOINCREMENT, Name VARCHAR(20));");
		} catch (SQLException e) {
			this.getLogger().warning("SQLite initialization error: " + e.getMessage());
			this.getPluginLoader().disablePlugin(this);
		}
	}
	
	public void onDisable() {
		books.save();
		sqlite.close();
	}
	
	public void loadConfig() {
		getConfig().options().copyDefaults(true);
		saveConfig();
		books = new Books(this);
		books.load("books.yml");
		//orderBooks();	  //TODO: Remove
	}
	
	public void log(String message) {
		if (getConfig().getBoolean("Verbose")) {
			getLogger().info(message);
		}
	}
	
	public int getCurrentID() {
		Set<String> set = books.getConfig().getKeys(false);
		return (set.size() + 1);
	}
	
	public boolean giveBook(Player p, String ID) {
		if (books.getConfig().get(ID) == null) {
			ID = books.lookupID(ID);
			if (ID == null) {
				return false;
			}
		}
		
		WrittenBook book = new CraftWrittenBook();
		
		book.setTitle(books.getConfig().getString(ID + ".Title"));
		book.setAuthor(books.getConfig().getString(ID + ".Author"));
		
		Map<String, Object> map = books.getConfig().getConfigurationSection(ID + ".Pages").getValues(false);
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < map.size(); i++) {
			list.add(i, ((String) map.get("Page-" + String.valueOf(i))));
		}
		
		book.setPages(list);
		
		p.getInventory().addItem(book.getItemStack(1));
		
		sqlite.query("UPDATE players SET Book" + ID + "=1 WHERE Name='" + p.getName() + "';");
		
		return true;
	}
	
	public boolean giveAllBooks(Player p) {
		Set<String> set = books.getConfig().getKeys(false);
		if (set.size() == 0) {
			return false;
		}
		
		ResultSet rs = sqlite.query("SELECT * FROM players WHERE Name='" + p.getName() + "';");
		try {
			if (!rs.next()) {
				rs.close();
				sqlite.query("INSERT INTO players(Name) VALUES('" + p.getName() + "');");
				this.log("Player not yet in database, adding now...");
				rs = sqlite.query("SELECT * FROM players WHERE Name='" + p.getName() + "';");
				rs.next();
			}
			
			ArrayList<String> notGiven = new ArrayList<String>();
			for (String s : set) {
				if (rs.getShort("Book" + s) != 1) {
					notGiven.add(s);
				} else {
					this.log("Player already given book " + s);
				}
			}
			rs.close();
			
			if (notGiven.size() > 0) {
				p.sendMessage(this.tag + this.getConfig().getString("Welcome-Message"));
			}
			
			for (String s : notGiven) {
				this.log("Player given book " + s);
				giveBook(p, s);
			}
		} catch (SQLException e) {
			this.getLogger().warning("SQLite query error: " + e.getMessage());
		}
		
		return true;
	}
	
	public void addBook(WrittenBook book) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		
		String[] pages = book.getPagesArray();
		
		for (Integer i = 0; i < pages.length; i++) {
			map.put("Page-" + i.toString(), pages[i]);
		}
		String ID = String.valueOf(getCurrentID());
		books.getConfig().createSection(ID + ".Pages", map);
		books.getConfig().set(ID + ".Title", book.getTitle());
		books.getConfig().set(ID + ".Author", book.getAuthor());
		books.save();
		sqlite.query("ALTER TABLE players ADD Book" + ID + " TINYINT(1);");
	}
	
	public boolean deleteBook(String ID) {
		if (books.getConfig().getConfigurationSection(ID) != null) {
			books.getConfig().set(ID, null);
			books.save();
			sqlite.query("ALTER TABLE players DROP COLUMN Book" + ID + ";");
			//orderBooks(); //TODO: Remove
			return true;
		} else {
			return false;
		}
	}
	
	public List<String> readAllBooks() {
		ArrayList<String> list = new ArrayList<String>();
		Set<String> keys = books.getConfig().getKeys(false);
		for (String ID : keys) {
			String line = ID + " - " + books.getConfig().getString(ID + ".Title") + " by " + books.getConfig().getString(ID + ".Author");
			list.add(line);
		}
		
		return list;
	}
	/*
	public void orderBooks() {
		Set<String> set = books.getConfig().getKeys(false);
		YamlConfiguration tempConfig = books.getConfig();
		int id = 1;
		books.clear();
		
		for(String s : set) {
			books.getConfig().set(String.valueOf(id), tempConfig.getConfigurationSection(s));
			id++;
		}
		
		books.save();
	}*/
}
