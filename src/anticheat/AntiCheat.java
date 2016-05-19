package anticheat;

import anticheat.events.PlayerLeaveEvent;
import anticheat.util.DB;
import anticheat.util.DB.Databases;
import anticheat.util.Timer;
import org.bukkit.plugin.java.JavaPlugin;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;

public class AntiCheat extends JavaPlugin {
    private static AntiCheat instance = null;

    @Override
    public void onEnable() {
        getLogger().info("Please wait, we are confirming your API key with OutsideTheBlock...");
        String key = checkVersion();
        if (key != null) {
            if (key.equalsIgnoreCase("4vA0DmTnU00XhIGHXg9i35y57OARvtqr")) {
                getLogger().info("Your key has successfully been confirmed with OutsideTheBlock. Thank you!");
            } else {
                getLogger().severe("WARNING! We could not confirm your plugin install with OutsideTheBlock.");
                getLogger().severe("Your attempt to enable this plugin has been logged!");
                return;
            }
        } else {
            getLogger().severe("WARNING! We could not confirm your plugin install with OutsideTheBlock.");
            getLogger().severe("Your attempt to enable this plugin has been logged!");
            return;
        }

        instance = this;
        for (Databases database : Databases.values()) {
            database.connect();
        }
        DB.values();
        new Timer();
        new PlayerLeaveEvent();
        new AntiCheatBase();
    }

    @Override
    public void onDisable() {
        for (Databases database : Databases.values()) {
            database.disconnect();
        }
    }

    public static AntiCheat getInstance() {
        return instance;
    }

    private String checkVersion() {
        try {
            URL url = new URL("https://internal.twittuhc.com/tnh/auth.php?key=0e381777N1xa22CIsY39kCk77FZoe6d4");
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.addRequestProperty("User-Agent", "Mozilla/4.76");
            con.setInstanceFollowRedirects(true);
            DataInputStream input = new DataInputStream(con.getInputStream());
            int c;
            StringBuilder resultBuf = new StringBuilder();
            while ((c = input.read()) != -1) {
                resultBuf.append((char) c);
            }
            input.close();
            return resultBuf.toString().replace("\n", "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
