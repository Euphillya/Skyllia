# SkylliaBank

**SkylliaBank** is a powerful and feature-rich banking extension designed for Skyblock servers running on the Skyllia platform. This plugin seamlessly integrates with Vault to provide robust economic management for player islands, enhancing the overall gameplay experience by allowing players to manage their island's finances with ease.

## Features

- **Comprehensive Banking System:** Manage island balances, deposits, withdrawals, and balance inquiries effortlessly.
- **Admin Commands:** Administrators can oversee and modify player island balances, ensuring fair play and effective server management.
- **Vault Integration:** Fully compatible with Vault, allowing integration with a wide range of economy plugins.
- **Asynchronous Database Operations:** Utilizes MariaDB for reliable and efficient data storage, ensuring smooth performance even under heavy load.
- **Permission-Based Access:** Granular permissions to control access to various banking functionalities for players and administrators.
- **User-Friendly Commands:** Intuitive command structure for both players and admins to interact with the banking system seamlessly.
- **Folia Support:** Optimized for Folia, providing enhanced performance on multithreaded servers.
- 
## Requirements

- **Minecraft Server:** Compatible with Paper/Folia servers.
- **Skyllia Platform:** Ensure the Skyllia plugin is installed and active on your server.
- **Vault Plugin:** Required for economy integration.
- **MariaDB Database:** For storing banking data securely and efficiently.

## Installation

1. **Download SkylliaBank:**
    - Obtain the latest version of SkylliaBank from the [official repository](https://github.com/Euphillya/Skyllia/tree/dev/addons/SkylliaBank).

2. **Install Dependencies:**
    - Ensure that Vault and Skyllia are installed on your server.
    - Set up a MariaDB database and configure the necessary credentials.

3. **Configure the Plugin:**
    - Place the `SkylliaBank.jar` file into your server's `plugins` directory.
    - Edit the configuration files as needed to connect to your MariaDB database and customize settings.

4. **Restart the Server:**
    - Restart your Minecraft server to load the SkylliaBank plugin.

## Usage

- **Player Commands:**
    - `/is bank balance` - Check your island's balance.
    - `/is bank deposit <amount>` - Deposit money into your island's bank.
    - `/is bank withdraw <amount>` - Withdraw money from your island's bank.

- **Admin Commands:**
    - `/skylliadmin bank balance <player>` - View a player's island balance.
    - `/skylliadmin bank deposit <player> <amount>` - Deposit money into a player's island bank.
    - `/skylliadmin bank withdraw <player> <amount>` - Withdraw money from a player's island bank.
    - `/skylliadmin bank setbalance <player> <amount>` - Set a player's island bank balance.

## Support

For support, please join our [Discord server](https://discord.gg/uUJQEB7XNN).

## Contributing

Contributions are welcome! Please read the [contribution guidelines](../../CONTRIBUTING.md) before submitting a pull request.

## License

SkylliaBank is licensed under the [MIT License](../../LICENSE).

