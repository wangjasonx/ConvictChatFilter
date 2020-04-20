package me.tsyrac.convictchatfilter;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;

public class Playerlist extends JavaPlugin {

    private static File file;
    private static FileConfiguration customPlayerFile;

    //Finds or generates the custom player file
    public static void setup(){
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("ConvictChatFilter").getDataFolder(), "playerlist.yml");

        if(!file.exists()){
            try{
                file.createNewFile();
            } catch(IOException e){
                e.getStackTrace();
            }

        }

        customPlayerFile = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration getPlayerFile(){
        return customPlayerFile;
    }

    public static void save(){
        try{
            customPlayerFile.save(file);
        } catch(IOException e){
            e.getStackTrace();
            System.out.println("Couldn't save the file!");
        }

    }

    public static void reload(){
        customPlayerFile = YamlConfiguration.loadConfiguration(file);
    }

}
