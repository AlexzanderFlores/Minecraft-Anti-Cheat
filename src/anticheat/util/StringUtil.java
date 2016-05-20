package anticheat.util;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class StringUtil {

    public static String colorize(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static String stripColors(String canStrip) {
        for (int i = 0; i < canStrip.length(); i++) {
            if (canStrip.charAt(i) == '&') {
                canStrip = canStrip.replaceFirst("&" + canStrip.charAt(i + 1), "");
            }
        }
        return canStrip;
    }

    public static void broadcast(String... messages) {
        for (String message : Preconditions.checkNotNull(messages, "Messages are null")) {
            Bukkit.getServer().broadcastMessage(colorize(message));
        }
    }

    public static String getOrdinalSuffix(int value) {
        int roundedValue = Math.abs(value);
        int lastDigit = roundedValue % 10;
        int last2Digits = roundedValue % 100;
        switch (lastDigit) {
            case 1:
                return last2Digits == 11 ? "th" : "st";
            case 2:
                return last2Digits == 12 ? "th" : "nd";
            case 3:
                return last2Digits == 13 ? "th" : "rd";
            default:
                return "th";
        }
    }

    public static int toInteger(String string) throws NumberFormatException {
        try {
            return Integer.parseInt(string.replaceAll("[^\\d]", ""));
        } catch (NumberFormatException e) {
            throw new NumberFormatException(string + " isn't a number!");
        }
    }
}
