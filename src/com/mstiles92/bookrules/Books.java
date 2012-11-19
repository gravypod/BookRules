package com.mstiles92.bookrules;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class Books {

	private final BookRulesPlugin plugin;
	private YamlConfiguration booksConfig;
	private File configFile;
	private boolean loaded;
	private String filename;
	
	public Books(BookRulesPlugin plugin) {
		this.plugin = plugin;
		loaded = false;
	}
	
	public void load(String filename) {
		configFile = new File(plugin.getDataFolder(), filename);
		this.filename = filename;
		
		if (configFile.exists()) {
			booksConfig = new YamlConfiguration();
			
			try {
				booksConfig.load(configFile);
			}
			catch (FileNotFoundException e) {
				// TODO: Handle exception
			}
			catch (IOException e) {
				// TODO: Handle exception
			}
			catch (InvalidConfigurationException e) {
				// TODO: Handle exception
			}
			loaded = true;
		} else {
			try {
				configFile.createNewFile();
				booksConfig = new YamlConfiguration();
				booksConfig.load(configFile);
			}
			catch (IOException e) {
				// TODO: Handle exception
			}
			catch (InvalidConfigurationException e) {
				// TODO: Handle exception
			}
		}
	}
	
	public void clear() {
		configFile = new File(plugin.getDataFolder(), filename);
		
		try {
			configFile.delete();
			configFile.createNewFile();
			booksConfig = new YamlConfiguration();
			booksConfig.load(configFile);
		}
		catch (IOException e) {
			// TODO: Handle exception
		}
		catch (InvalidConfigurationException e) {
			// TODO: Handle exception
		}
		
	}
	
	public void save() {
		try {
			booksConfig.save(configFile);
		}
		catch (IOException e) {
			// TODO: Handle exception
		}
	}
	
	public File getFile() {
		return this.configFile;
	}
	
	public YamlConfiguration getConfig() {
		if (!loaded) {
			load("books.yml");
		}
		return booksConfig;
	}
	
	public String lookupID(String title) {
		Set<String> contents = this.getConfig().getKeys(false);
		String value;
		
		for (int x = 0; x < contents.size(); x++) {
			value = (String) this.getConfig().getConfigurationSection(String.valueOf(x)).get("Title");
			
			if (value == title) {
				return String.valueOf(x);
			}
		}
		return null;
	}
}
