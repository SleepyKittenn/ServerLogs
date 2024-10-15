package live.amsleepy.serverlogsbukkit;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerLogsCommandExecutor implements CommandExecutor, TabCompleter {

    private final ServerLogs_Bukkit plugin;
    private final String prefix = ChatColor.DARK_PURPLE + "[ServerLogs] " + ChatColor.GOLD;

    public ServerLogsCommandExecutor(ServerLogs_Bukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (sender instanceof Player && !sender.hasPermission("serverlogs.reload")) {
                sender.sendMessage(prefix + "You do not have permission to execute this command.");
                return true;
            } else if (sender instanceof ConsoleCommandSender || sender instanceof Player) {
                plugin.reloadConfig();
                plugin.config = plugin.getConfig();
                sender.sendMessage(prefix + "ServerLogs configuration reloaded.");
                plugin.getLogger().info("ServerLogs configuration reloaded by " + sender.getName());
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if ("reload".startsWith(args[0].toLowerCase())) {
                completions.add("reload");
            }
            return completions;
        }
        return Collections.emptyList();
    }
}