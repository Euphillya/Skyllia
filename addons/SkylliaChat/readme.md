# SkylliaChat

**SkylliaChat** is a versatile and user-friendly chat extension designed for Skyblock servers running on the Skyllia platform. This plugin enhances in-game communication by enabling island-specific messaging, allowing players to interact seamlessly with their island members.

## Features

- **Island-Specific Chat:** Toggle island messaging to communicate exclusively with your island members.
- **Customizable Chat Format:** Define the appearance of chat messages to match your server's theme.
- **Admin Commands:** Easily reload configurations to apply changes without restarting the server.
- **Permission-Based Access:** Control access to chat functionalities with granular permissions.
- **Seamless Integration:** Fully compatible with the Skyllia platform, ensuring smooth performance and reliability.
- **Folia Support:** Optimized for Folia, providing enhanced performance on multithreaded servers.

## Requirements

- **Minecraft Server:** Compatible with Paper/Folia servers running Minecraft version 1.20.1 or greater.
- **Skyllia Platform:** Ensure the Skyllia plugin is installed and active on your server.
- **Java:** Java 21 or higher is recommended for optimal performance.

## Installation

1. **Download SkylliaChat:**
   - Obtain the latest version of SkylliaChat from the [official repository](https://github.com/Euphillya/Skyllia/tree/dev/addons/SkylliaChat).

2. **Install Dependencies:**
   - Ensure that the Skyllia plugin is installed and properly configured on your server.

3. **Configure the Plugin:**
   - Place the `SkylliaChat.jar` file into your server's `plugins` directory.
   - Edit the configuration file (`config.yml`) to customize chat formats and messages as desired.

4. **Reload the Plugin:**
   - Use the admin command `/isadmin chat_reload` to apply configuration changes without restarting the server.

## Usage

- **Player Commands:**
  - `/is chat` - Toggle island messaging on or off.
  
- **Admin Commands:**
  - `/isadmin chat_reload` - Reload the plugin's configuration files.

## Permissions

- **skylliachat.use:** Allows players to use the island chat feature.
- **skylliachat.reload:** Grants permission to reload the plugin's configuration.

## Configuration

Customize the chat format and messages by editing the `config.yml` file. Below is an example of the configuration options:

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

## Support

For support, please join our [Discord server](https://discord.gg/uUJQEB7XNN).

## Contributing

Contributions are welcome! Please read the [contribution guidelines](../../CONTRIBUTING.md) before submitting a pull request.

## License

SkylliaChat is licensed under the [MIT License](../../LICENSE).