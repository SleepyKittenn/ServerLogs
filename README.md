# ServerLogs-Bukkit

![Java](https://img.shields.io/badge/Java-17-blue) 
![Version](https://img.shields.io/badge/version-1.1.0-yellow.svg)
![API-Version](https://img.shields.io/badge/api--version-1.13-lightgrey.svg)

ServerLogs-Bukkit is a Minecraft plugin designed to log various player activities on your server. It records events like player joins, quits, chat messages, commands, and game mode changes. Additionally, it supports sending logs to a specified webhook at configurable intervals and supports log file rotation.

## Features

- **Player Join/Leave Logging:** Track when players join or leave the server.
- **Chat Logging:** Record chat messages sent by players.
- **Command Logging:** Capture commands issued by players.
- **Game Mode Change Logging:** Log when players change their game mode.
- **Webhook Integration:** Send logs to a specified webhook URL at defined intervals.
- **Configurable Logging:** Easily configure which actions to log and for which players.
- **IP Anonymization:** Option to hide players' IP addresses in the logs.
- **Log File Rotation:** Automatically rotate log files daily to manage file sizes.

## Installation

1. **Download** the latest version of the plugin from the [releases](https://github.com/your-username/ServerLogs-Bukkit/releases) page.
2. **Place** the `ServerLogs-Bukkit.jar` file into your server's `plugins` directory.
3. **Start** the server to generate the default configuration files.
4. **Edit** the `config.yml` file in the `plugins/ServerLogs-Bukkit` directory to customize your settings.
5. **Reload** the server or use `/serverlogs reload` to apply the configuration changes.

## Configuration

The main configuration file `config.yml` allows you to customize the logging behavior, webhook settings, and privacy options:

```yaml
# Webhook configuration
webhook:
  # URL of the webhook to where logs will be sent
  url: "your_webhook_url_here"
  # Interval (in seconds) at which the logs will be sent to the webhook
  interval: 60 # interval in seconds

# Logging configuration
# If true, logs activities of all players. If false, only logs activities of specific players
logAllPlayers: true
# List of specific players whose activities should be logged. Works only if logAllPlayers is set to false
specificPlayers:
  - examplePlayer # Example player username

# Categories of activities to log
log:
  # Log when players join the server
  join: true
  # Log when players leave the server
  leave: true
  # Log players' chat messages
  chat: true
  # Log when players issue commands
  command: true
  # Log when players change their game mode
  gamemode: true

# Privacy settings
# If true, hides players' IP addresses in the logs
privacy:
  anonymizeIP: true
```

## Commands

- **/serverlogs reload**: Reload the ServerLogs plugin configuration.
    - **Permission**: `serverlogs.reload`

## Permissions

- **serverlogs.reload**: Allows the player to reload the ServerLogs plugin configuration.
    - **Default**: `op`

## Development

### Building From Source

1. **Clone** the repository:
    ```sh
    git clone https://github.com/your-username/ServerLogs-Bukkit.git
    cd ServerLogs-Bukkit
    ```

2. **Build** the plugin using Maven:
    ```sh
    mvn clean package
    ```

3. **Find** the compiled JAR in the `target` directory.

## Contributing

Contributions are welcome! Feel free to submit a pull request or open an issue to address bugs, suggest new features, or improve documentation.

## Credits

Developed by **sleepy**.

## Support

For any questions or issues, please open an issue on the [GitHub repository](https://github.com/SleepyKittenn/ServerLogs/issues).

---

Thank you for using ServerLogs-Bukkit! 🎉
