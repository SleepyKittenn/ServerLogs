package live.amsleepy.serverlogsbukkit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class ServerLogs_Bukkit extends JavaPlugin implements Listener {

    public FileConfiguration config;
    private File logDirectory;
    private File logFile;
    private final List<String> logBuffer = new ArrayList<>();
    private final String prefix = ChatColor.DARK_PURPLE + "[ServerLogs] " + ChatColor.GOLD;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        logDirectory = new File(getDataFolder(), "logs");
        if (!logDirectory.exists()) {
            logDirectory.mkdirs();
        }
        createNewLogFile();

        getServer().getPluginManager().registerEvents(this, this);

        ServerLogsCommandExecutor commandExecutor = new ServerLogsCommandExecutor(this);
        PluginCommand cmd = getCommand("serverlogs");
        if (cmd != null) {
            cmd.setExecutor(commandExecutor);
        } else {
            getLogger().severe("Command 'serverlogs' is not defined in plugin.yml");
        }

        long interval = config.getLong("webhook.interval", 60) * 20L;
        new BukkitRunnable() {
            @Override
            public void run() {
                sendLogsToWebhook();
            }
        }.runTaskTimer(this, interval, interval);

        String pluginVersion = getDescription().getVersion();
        getLogger().info(prefix + "ServerLogs-Bukkit v" + pluginVersion + " enabled, made by sleepy.");
    }

    @Override
    public void onDisable() {
        getLogger().info(prefix + " ServerLogs-Bukkit disabled!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (shouldLog(event.getPlayer(), "join")) {
            log(event.getPlayer(), "joined the game");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (shouldLog(event.getPlayer(), "leave")) {
            log(event.getPlayer(), "left the game");
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (shouldLog(event.getPlayer(), "chat")) {
            log(event.getPlayer(), "sent message: " + event.getMessage());
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (shouldLog(player, "command")) {
            log(player, "issued command: " + event.getMessage());
        }
    }

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        if (shouldLog(event.getPlayer(), "gamemode")) {
            log(event.getPlayer(), "changed gamemode to " + event.getNewGameMode().toString());
        }
    }

    private boolean shouldLog(Player player, String category) {
        boolean logAll = config.getBoolean("logAllPlayers", true);
        List<String> specificPlayers = config.getStringList("specificPlayers");
        boolean logCategory = config.getBoolean("log." + category, true);
        return logCategory && (logAll || specificPlayers.contains(player.getName()));
    }

    private synchronized void log(Player player, String action) {
        checkLogFileRotation();

        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        boolean hideIP = config.getBoolean("privacy.anonymizeIP", false);
        String ip = hideIP ? "hidden" : (player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "unknown");
        String username = player.getName();
        String world = player.getWorld().getName();
        String coords = player.getLocation().getBlockX() + ", " + player.getLocation().getBlockY() + ", " + player.getLocation().getBlockZ();

        String logEntry = String.format("%s - [%s] %s@%s %s: %s", time, ip, username, world, coords, action);
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(logEntry + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
        logBuffer.add(logEntry);

        String message = prefix + logEntry;
        Bukkit.getConsoleSender().sendMessage(message);
    }

    public synchronized void logCommand(String senderName, String command, String args, String response) {
        checkLogFileRotation();

        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        String logEntry = String.format("%s - Command issued by %s: /%s %s; Response: %s", time, senderName, command, args, response);
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(logEntry + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
        logBuffer.add(logEntry);

        String message = prefix + logEntry;
        Bukkit.getConsoleSender().sendMessage(message);
    }

    private void sendLogsToWebhook() {
        List<String> logsToSend;
        synchronized (logBuffer) {
            if (logBuffer.isEmpty()) {
                return;
            }
            logsToSend = new ArrayList<>(logBuffer);
            logBuffer.clear();
        }

        String webhookUrl = config.getString("webhook.url");
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            getLogger().warning("Webhook URL is not set!");
            return;
        }

        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("content", String.join("\n", logsToSend));

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = json.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 204) {
                getLogger().warning(prefix + " Failed to send logs to webhook! Response code: " + responseCode);
            }
        } catch (Exception e) {
            getLogger().warning(prefix + " Failed to send logs to webhook! Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to check if a new log file needs to be created (daily rotation)
    private void checkLogFileRotation() {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if (logFile == null || !logFile.getName().contains(currentDate)) {
            createNewLogFile();
        }
    }

    // Method to create a new log file based on the current date
    private void createNewLogFile() {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        logFile = new File(logDirectory, "ServerLogs-" + currentDate + ".txt");
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}