package eu.llamadev.flag.Configurations;
import eu.llamadev.flag.Flag;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Messages {

    private static File file;
    private static FileConfiguration messagesFile;

    public static void setup(){
        file = new File(Flag.getPlugin(Flag.class).getDataFolder(), "messages.yml");

        if (!file.exists()){
            try{
                file.createNewFile();
            }catch (IOException e){
                System.out.println(e);
            }
        }
        messagesFile = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration get(){
        return messagesFile;
    }

    public static void save(){
        try{
            messagesFile.save(file);
        }catch (IOException e){
            System.out.println("Couldn't save file");
        }
    }

    public static void reload(){
        messagesFile = YamlConfiguration.loadConfiguration(file);
    }

}
