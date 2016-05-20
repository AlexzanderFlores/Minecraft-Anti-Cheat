package anticheat.util;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtil {

    private static String chatBar;

    static {
        StringBuilder chatBarBuilder = new StringBuilder();
        for (int x = 0; x < 34; x++) {
            chatBarBuilder.append(MessageCharacter.BOX_LINE.toString());
        }
        chatBar = chatBarBuilder.toString();
    }

    public static void newLine(CommandSender sender) {
        sender.sendMessage("");
    }

    public static void message(CommandSender player, String... messages) {
        Validate.notNull(messages, "Messages are null");

        for (String message : messages) {
            player.sendMessage(StringUtil.colorize(message));
        }
    }

    public static void messagePrefix(CommandSender player, MessageType type, String... messages) {
        Validate.notNull(messages, "Messages are null");

        for (String message : messages) {
            message(player, type.getPrefix() + message);
        }
    }

    public static void sendAll(MessageType type, String... messages) {
        Validate.notNull(messages, "Messages are null");

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String message : messages) {
                message(player, type.getPrefix() + message);
            }
        }
    }

    public static void sendAll(String... messages) {
        Validate.notNull(messages, "Messages are null");

        for (Player player : Bukkit.getOnlinePlayers()) {
            message(player, messages);
        }
    }

    public static void sendBordered(CommandSender sender, BorderedMessageType type, String... messages) {
        Validate.notNull(messages, "Messages are null");

        message(sender, type.getBorderColor() + MessageCharacter.BOX_TOP_LEFT_CORNER.toString() + chatBar);
        message(sender, type.getBorderColor() + MessageCharacter.BOX_SIDE.toString() + "  " + type);
        message(sender, type.getBorderColor() + MessageCharacter.BOX_SIDE.toString());
        for (String message : messages) {
            message(sender, type.getBorderColor() + MessageCharacter.BOX_SIDE.toString() + "  &7" + message);
        }
        message(sender, type.getBorderColor() + MessageCharacter.BOX_SIDE.toString());
    }

    public enum MessageType {
        GOOD(StringUtil.colorize("&a&lOSTBNoHacks &7" + MessageCharacter.DOT + " &a")),
        BAD(StringUtil.colorize("&c&lOSTBNoHacks &7" + MessageCharacter.DOT + " &c"));

        private String prefix;

        MessageType(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return this.prefix;
        }
    }

    public enum BorderedMessageType {
        OUTSIDE_THE_BLOCK(ChatColor.GOLD, ChatColor.DARK_GRAY, "OutsideTheBlock"),
        SEVERE_WARNING(ChatColor.RED, ChatColor.DARK_RED, "Severe Warning"),
        WARNING(ChatColor.YELLOW, ChatColor.GOLD, "Warning"),
        NOTIFICATION(ChatColor.AQUA, ChatColor.DARK_AQUA, "Notification");

        private ChatColor titleColor, borderColor;
        private String title;

        BorderedMessageType(ChatColor titleColor, ChatColor borderColor, String title) {
            this.titleColor = titleColor;
            this.borderColor = borderColor;
            this.title = title;
        }

        public ChatColor getTitleColor() {
            return titleColor;
        }

        public ChatColor getBorderColor() {
            return borderColor;
        }

        public String getTitle() {
            return title;
        }

        @Override
        public String toString() {
            return titleColor.toString() + ChatColor.BOLD.toString() + title;
        }
    }

    public enum MessageCharacter {

        RAQUO('\u00bb'),
        LAQUO('\u00ab'),
        DOT('\u25CF'),
        BOX_TOP_LEFT_CORNER('\u2554'),
        BOX_BOTTOM_LEFT_CORNER('\u255a'),
        BOX_LINE('\u2550'),
        BOX_SIDE('\u2551');

        private char c;

        MessageCharacter(char c) {
            this.c = c;
        }

        public char getC() {
            return c;
        }

        @Override
        public String toString() {
            return Character.toString(c);
        }
    }
}
