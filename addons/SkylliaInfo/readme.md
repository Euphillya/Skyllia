# SkylliaInfo

**SkylliaInfo** is a comprehensive information extension designed for Skyblock servers running on the Skyllia platform.
This plugin provides detailed insights into player islands, enhancing the gaming experience by allowing players and
administrators to access vital island statistics and information effortlessly.

## Features

- **Detailed Island Information:** Retrieve comprehensive details about any player's island, including owner, size,
  members, creation date, and location.
- **Integration with Skyllia Extensions:** Seamlessly integrates with other Skyllia plugins such as SkylliaBank,
  SkylliaOre, and SkylliaValue to display extended information like bank balances, generator types, and island values.
- **Customizable Chat Formats:** Define how information is displayed using customizable chat formats to match your
  server's theme.
- **Admin and Player Commands:** Provides both player and admin commands for accessing island information and managing
  configurations.
- **Folia Support:** Optimized for Folia, ensuring enhanced performance on multithreaded servers.
- **Permission-Based Access:** Control access to various functionalities with granular permissions for players and
  administrators.

## Requirements

- **Minecraft Server:** Compatible with Paper/Folia servers running Minecraft version 1.20.1 or greater.
- **Skyllia Platform:** Ensure the Skyllia plugin is installed and active on your server.
- **Java:** Java 21 or higher is recommended for optimal performance.
- **Dependencies:**
    - **Skyllia:** Required for core functionalities.
    - **SkylliaOre:** Optional, for displaying generator types.
    - **SkylliaBankAddon:** Optional, for displaying bank-related information.
    - **SkylliaValue:** Optional, for displaying island values (**Proprietary to excalia.fr**).
      Contact [excalia.fr](https://www.excalia.fr) or [discord.gg](http://discord.gg/excalia) for access.

## Installation

1. **Download SkylliaInfo:**
    - Obtain the latest version of SkylliaInfo from
      the [official repository](https://github.com/Euphillya/Skyllia/tree/dev/addons/SkylliaInfo).

2. **Install Dependencies:**
    - Ensure that the Skyllia plugin is installed and properly configured on your server.
    - Install any optional dependencies (SkylliaOre, SkylliaBankAddon, SkylliaValue) if you wish to utilize their
      features.

3. **Configure the Plugin:**
    - Place the `SkylliaInfo.jar` file into your server's `plugins` directory.
    - Edit the `config.yml` file to customize message formats and other settings as desired.

4. **Reload the Plugin:**
    - Restart your Minecraft server to load the SkylliaInfo plugin.

## Usage

- **Player Commands:**
    - `/is info` or `/skyllia info` - Display detailed information about your island.
    - `/is info <player>` or `/skyllia info <player>` - Display detailed information about another player's island (
      requires appropriate permissions).

- **Admin Commands:**
    - `/isadmin info reload` or `/skylliadmin info reload` - Reload the plugin's configuration without restarting the
      server.

## Permissions

- **skylliainfo.use:** Allows players to use the island info command.
- **skylliainfo.admin.reload:** Grants permission to reload the plugin's configuration.

## Configuration

Customize the chat formats and messages by editing the `config.yml` file. Below is an example of the configuration
options:

```yaml
chat:
  format: "<red>[Messaging Island] %player_name%: <gray>%message%"

message:
  config:
    reloaded: "<green>Configuration successfully reloaded!"
  chat:
    enabled: "<green>Island Messaging Enabled."
    disabled: "<red>Island messaging Disabled."
```

### Configuration Options

- **chat.format:** Defines the format of the island chat messages. You can customize the colors and placeholders as
  needed.
- **message.config.reloaded:** Message displayed when the configuration is successfully reloaded.
- **message.chat.enabled:** Message displayed when island messaging is enabled for a player.
- **message.chat.disabled:** Message displayed when island messaging is disabled for a player.

## Example Commands

- **View Your Island Info:**
  ```
  /is info
  ```
  Displays information such as island ID, owner, size, max members, creation date, online members, and location.

- **View Another Player's Island Info:**
  ```
  /is info <player>
  ```
  Displays the specified player's island information. Requires `skylliainfo.use` permission.

- **Reload Configuration (Admin Only):**
  ```
  /isadmin info reload
  ```
  Reloads the `config.yml` file to apply any changes without restarting the server. Requires `skylliainfo.admin.reload`
  permission.

## Support

For support, please join our [Discord server](https://discord.gg/uUJQEB7XNN).

## Contributing

Contributions are welcome! Please read the [contribution guidelines](../../CONTRIBUTING.md) before submitting a pull
request.

## License

SkylliaInfo is licensed under the [MIT License](../../LICENSE).