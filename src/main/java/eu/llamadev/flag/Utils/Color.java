package eu.llamadev.flag.Utils;

import org.bukkit.ChatColor;

public class Color {

    public static String translate(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String strip(String text) {
        return ChatColor.stripColor(text);
    }
}
