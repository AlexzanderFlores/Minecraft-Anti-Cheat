package anticheat.util;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigurationUtil {
	private File file = null;
	private FileConfiguration config = null;
	
	public ConfigurationUtil(String path) {
		file = new File(path);
		config = YamlConfiguration.loadConfiguration(file);
		if(!file.exists()) {
			try {
				config.save(file);
			} catch(IOException e) {
				Bukkit.getLogger().info("Could not save file \"" + path + "\"");
			}
		}
	}
	
	public boolean save() {
		try {
			config.save(file);
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public File getFile() {
		return file;
	}
	
	public FileConfiguration getConfig() {
		return config;
	}
}
