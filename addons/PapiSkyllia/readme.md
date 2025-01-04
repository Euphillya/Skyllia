# Skyllia PlaceholderAPI Expansion

**Skyllia Placeholder** is an expansion for [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) that integrates with the Skyllia Plugin, providing dynamic placeholders to enhance in-game displays such as scoreboards, chat messages, and more. This expansion offers placeholders related to island management, permissions, game rules, and integrates with optional addons like SkylliaOre, SkylliaValue, and SkylliaBank.

## Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Available Placeholders](#available-placeholders)
   - [Island Placeholders](#island-placeholders)
   - [Permissions Placeholders](#permissions-placeholders)
   - [Game Rules Placeholders](#game-rules-placeholders)
   - [Addon Placeholders](#addon-placeholders)
- [Usage](#usage)
- [Addons](#addons)
   - [SkylliaOre](#skylliaore)
   - [SkylliaValue](#skylliavalue)
   - [SkylliaBank](#skylliabank)
- [Contributing](#contributing)
- [License](#license)

## Features

- **Island Information**: Retrieve dynamic data about a player's Skyblock island.
- **Permissions Status**: Check and display the status of various permissions.
- **Game Rules Status**: Display the status of game rules applied to an island.
- **Addon Integration**: Extend functionality with placeholders from optional addons like Ore management, Value tracking, and Banking systems.
- **Efficient Caching**: Utilizes caching mechanisms to optimize performance and reduce server load.

## Prerequisites

Before installing Skyllia Placeholder, ensure that the following prerequisites are met:

- **Minecraft Server**: [Folia](https://papermc.io/software/folia) or [Paper](https://papermc.io/software/paper) server.
- **Skyllia Plugin**: Ensure that the Skyllia Plugin is installed and properly configured on your server.
- **PlaceholderAPI**: Install [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) on your server.
- **Addons (Optional)**: Depending on the placeholders you intend to use, install the following addons:
   - [SkylliaOre](#skylliaore)
   - [SkylliaValue](#skylliavalue)
   - [SkylliaBank](#skylliabank)

## Installation

1. **Download**:
   - Obtain the latest `PapiSkyllia-1.x.x-all.jar` from the [releases](https://github.com/Euphillya/Skyllia/actions) page of the Skyllia Placeholder repository.

2. **Installation**:
   - Place the `PapiSkyllia-1.x.x-all.jar` file into your server's `plugins/PlaceholderAPI/expansions` directory.
   - Start or restart your server to generate the necessary configuration files and load the expansion.

3. **Enable Expansion**:
   - Skyllia Placeholder automatically registers itself with PlaceholderAPI upon server startup. Ensure there are no errors in the server console related to the expansion.

## Available Placeholders

Skyllia Placeholder provides a variety of placeholders categorized into Island, Permissions, Game Rules, and Addon-specific placeholders.

### Island Placeholders

Retrieve information about a player's Skyblock island.

| **Placeholder**                     | **Description**                                                 |
|-------------------------------------|-----------------------------------------------------------------|
| `%skyllia_island_size%`             | Returns the size of the player's island.                        |
| `%skyllia_island_members_max_size%` | Returns the maximum number of members allowed on the island.    |
| `%skyllia_island_members_size%`     | Returns the current number of members on the island.            |
| `%skyllia_island_rank%`             | Returns the rank of the player on the island.                   |
| `%skyllia_island_tps%`              | Returns the TPS (Ticks Per Second) of the island's world chunk. |

### Permissions Placeholders

Check the status of various permissions assigned to roles within an island.

| **Placeholder Format**                                      | **Description**                                                                                                                |
|-------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------|
| `%skyllia_permissions_<role>_<type>_<permission>%`          | Checks if a specific role has a particular permission. Replace `<role>`, `<type>`, and `<permission>` with appropriate values. |
| **Example**: `%skyllia_permissions_admin_commands_promote%` | Checks if the `admin` role has the `PROMOTE` permission under `COMMANDS` type.                                                 |

#### Permissions Types

- **ISLAND**: General island permissions.
- **COMMANDS**: Administrative command permissions.
- **INVENTORY**: Inventory-related permissions.

### Game Rules Placeholders

Display the status of specific game rules applied to an island.

| **Placeholder**                                         | **Description**                                                                                                       |
|---------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------|
| `%skyllia_gamerule_<gamerule>%`                         | Returns the status (`true` or `false`) of a specific game rule. Replace `<gamerule>` with the desired game rule name. |
| **Example**: `%skyllia_gamerule_disable_spawn_hostile%` | Returns whether hostile mob spawning is disabled on the island.                                                       |

### Addon Placeholders

Extend functionality with placeholders from optional addons.

#### SkylliaOre Addon

| **Placeholder**                      | **Description**                                               |
|--------------------------------------|---------------------------------------------------------------|
| `%skyllia_ore_generator_name%`       | Returns the name of the ore generator on the island.          |

#### SkylliaValue Addon

| **Placeholder**              | **Description**                                  |
|------------------------------|--------------------------------------------------|
| `%skyllia_value_experience%` | Displays the player's experience on the island.  |
| `%skyllia_value_level%`      | Displays the player's level based on experience. |

#### SkylliaBank Addon

| **Placeholder**         | **Description**                                        |
|-------------------------|--------------------------------------------------------|
| `%skyllia_bank_live%`   | Shows the live balance in the player's bank account.   |
| `%skyllia_bank_cached%` | Shows the cached balance for performance optimization. |

## Game Rules and Permissions

### Game Rules (`GameRuleIsland`)

Represents various game rules that can be applied to a Skyblock island.

| **Game Rule**                  | **Description**                                         | **Permission Value** |
|--------------------------------|---------------------------------------------------------|----------------------|
| `DISABLE_SPAWN_HOSTILE`        | Disables the spawning of hostile mobs on the island.    | 1                    |
| `DISABLE_SPAWN_PASSIVE`        | Disables the spawning of passive mobs on the island.    | 2                    |
| `DISABLE_SPAWN_UNKNOWN`        | Disables the spawning of unknown mobs on the island.    | 4                    |
| `DISABLE_HUMAN_EXPLOSION`      | Disables explosions caused by players on the island.    | 8                    |
| `DISABLE_MOB_EXPLOSION`        | Disables explosions caused by mobs on the island.       | 16                   |
| `DISABLE_ENDERMAN_PICK_BLOCK`  | Disables Endermen from picking up blocks on the island. | 32                   |
| `DISABLE_FIRE_SPREADING`       | Disables fire spreading on the island.                  | 64                   |
| `DISABLE_MOB_PICKUP_ITEMS`     | Disables mobs from picking up items on the island.      | 128                  |
| `DISABLE_UNKNOWN_EXPLOSION`    | Disables unknown explosions on the island.              | 256                  |
| `DISABLE_PASSIF_MOB_GRIEFING`  | Disables passive mob griefing on the island.            | 512                  |
| `DISABLE_HOSTILE_MOB_GRIEFING` | Disables hostile mob griefing on the island.            | 1,024                |
| `DISABLE_UNKNOWN_MOB_GRIEFING` | Disables unknown mob griefing on the island.            | 2,048                |
| `DISABLE_PLAYER_GRIEFING`      | Disables player griefing on the island.                 | 4,096                |

### Permissions

Skyllia utilizes a robust permissions system to control player actions and interactions on their islands. Permissions are categorized into different types: **ISLAND**, **COMMANDS**, and **INVENTORY**.

#### 1. **Island Permissions (`PermissionsIsland`)**

Controls general interactions and behaviors on the island.

| **Permission**        | **Description**                                  | **Value** |
|-----------------------|--------------------------------------------------|-----------|
| `BLOCK_BREAK`         | Permission to break blocks.                      | 1         |
| `BLOCK_PLACE`         | Permission to place blocks.                      | 2         |
| `BUCKETS`             | Permission to use buckets.                       | 4         |
| `REDSTONE`            | Permission to use redstone components.           | 8         |
| `PVP`                 | Permission to engage in player vs player combat. | 16        |
| `KILL_MONSTER`        | Permission to kill monsters.                     | 32        |
| `KILL_ANIMAL`         | Permission to kill animals.                      | 64        |
| `DROP_ITEMS`          | Permission to drop items.                        | 128       |
| `PICKUP_ITEMS`        | Permission to pick up items.                     | 256       |
| `USE_NETHER_PORTAL`   | Permission to use nether portals.                | 512       |
| `USE_END_PORTAL`      | Permission to use end portals.                   | 1,024     |
| `INTERACT_ENTITIES`   | Permission to interact with entities.            | 2,048     |
| `KILL_UNKNOWN_ENTITY` | Permission to kill unknown entities.             | 4,096     |
| `KILL_NPC`            | Permission to kill NPCs (non-player characters). | 8,192     |
| `INTERACT`            | Permission to perform general interactions.      | 16,384    |

#### 2. **Command Permissions (`PermissionsCommandIsland`)**

Controls administrative commands on the island.

| **Permission**      | **Description**                                       | **Value** |
|---------------------|-------------------------------------------------------|-----------|
| `DEMOTE`            | Permission to demote players.                         | 1         |
| `PROMOTE`           | Permission to promote players.                        | 2         |
| `KICK`              | Permission to kick players from the island.           | 4         |
| `ACCESS`            | Permission to access certain island features.         | 8         |
| `SET_HOME`          | Permission to set a home location on the island.      | 16        |
| `INVITE`            | Permission to invite players to the island.           | 32        |
| `SET_BIOME`         | Permission to set a biome on the island.              | 64        |
| `SET_WARP`          | Permission to set a warp point on the island.         | 128       |
| `DEL_WARP`          | Permission to delete a warp point from the island.    | 256       |
| `TP_WARP`           | Permission to teleport to a warp point on the island. | 512       |
| `EXPEL`             | Permission to expel players from the island.          | 1,024     |
| `MANAGE_PERMISSION` | Permission to manage island permissions.              | 2,048     |
| `BAN`               | Permission to ban players from the island.            | 4,096     |
| `UNBAN`             | Permission to unban players from the island.          | 8,192     |
| `MANAGE_TRUST`      | Permission to manage trust levels on the island.      | 16,384    |
| `MANAGE_GAMERULE`   | Permission to manage game rules on the island.        | 32,768    |

#### 3. **Inventory Permissions (`PermissionsInventory`)**

Controls access to various inventory-related blocks and features on the island.

| **Permission**       | **Description**                                                                                                                   | **Value** |
|----------------------|-----------------------------------------------------------------------------------------------------------------------------------|-----------|
| `OPEN_CHEST`         | Permission to open chests.                                                                                                        | 1         |
| `OPEN_ANVIL`         | Permission to open anvils.                                                                                                        | 2         |
| `OPEN_WORKBENCH`     | Permission to open workbenches.                                                                                                   | 4         |
| `OPEN_ENCHANTING`    | Permission to open enchanting tables.                                                                                             | 8         |
| `OPEN_BREWING`       | Permission to open brewing stands.                                                                                                | 16        |
| `OPEN_SMITHING`      | Permission to open smithing tables.                                                                                               | 32        |
| `OPEN_BEACON`        | Permission to open beacons.                                                                                                       | 64        |
| `OPEN_SHULKER_BOX`   | Permission to open shulker boxes.                                                                                                 | 128       |
| `OPEN_FURNACE`       | Permission to open furnaces.                                                                                                      | 256       |
| `OPEN_LECTERN`       | Permission to open lecterns.                                                                                                      | 512       |
| `OPEN_CRAFTER`       | Permission to open crafting tables.                                                                                               | 1,024     |
| `OPEN_LOOM`          | Permission to open looms.                                                                                                         | 2,048     |
| `OPEN_GRINDSTONE`    | Permission to open grindstones.                                                                                                   | 4,096     |
| `OPEN_STONECUTTER`   | Permission to open stonecutters.                                                                                                  | 8,192     |
| `OPEN_CARTOGRAPHY`   | Permission to open cartography tables.                                                                                            | 16,384    |
| `OPEN_MERCHANT`      | Permission to open merchants.                                                                                                     | 32,768    |
| `OPEN_HOPPER`        | Permission to open hoppers.                                                                                                       | 65,536    |
| `OPEN_BARREL`        | Permission to open barrels.                                                                                                       | 131,072   |
| `OPEN_BLAST_FURNACE` | Permission to open blast furnaces.                                                                                                | 262,144   |
| `OPEN_SMOKER`        | Permission to open smokers.                                                                                                       | 524,288   |
| `OPEN_SMITHING_NEW`  | **Deprecated** Permission to open the new smithing tables (old permission). This permission is deprecated and should not be used. | 1,048,576 |
| `OPEN_DISPENSER`     | Permission to open dispensers.                                                                                                    | 2,097,152 |
| `OPEN_DROPPER`       | Permission to open droppers.                                                                                                      | 4,194,304 |

## Usage

### Integrating Placeholders

1. **Identify the Placeholder**:
   - Determine which placeholder you want to use based on the available placeholders listed above.

2. **Insert Placeholder**:
   - Use the placeholder in your server's configuration files or within plugins that support PlaceholderAPI.
   - **Example**: Adding placeholders to a scoreboard configuration:
     ```yaml
     lines:
       - "Island Size: %skyllia_island_size%"
       - "Members: %skyllia_island_members_size%/%skyllia_island_members_max_size%"
       - "Rank: %skyllia_island_rank%"
       - "TPS: %skyllia_island_tps%"
     ```

## Addons

Skyllia Placeholder supports several addons to extend its functionality. Ensure that these addons are installed in your `plugins` directory to utilize their respective placeholders.

### SkylliaOre

Enhances ore management on your Skyblock islands by providing placeholders related to ore generation and management.

- **Placeholders**:
   - `%skyllia_ore_generator_name%` - Returns the name of the ore generator on the island.

### SkylliaValue

Tracks and manages experience and level values for players on their islands.

- **Placeholders**:
   - `%skyllia_value_experience%` - Displays the player's experience.
   - `%skyllia_value_level%` - Displays the player's level based on experience.

### SkylliaBank

Introduces a banking system for players to manage their in-game currency.

- **Placeholders**:
   - `%skyllia_bank_live%` - Shows the live balance in the player's bank account.
   - `%skyllia_bank_cached%` - Shows the cached balance for performance optimization.

## Contributing

Contributions are welcome! To contribute to Skyllia Placeholder, please follow these steps:

1. **Fork the Repository**:
   - Click the "Fork" button on the repository page to create your own copy.

2. **Create a New Branch**:
   - ```bash
     git checkout -b feature/YourFeature
     ```

3. **Commit Your Changes**:
   - ```bash
     git commit -am 'Add some feature'
     ```

4. **Push to the Branch**:
   - ```bash
     git push origin feature/YourFeature
     ```

5. **Open a Pull Request**:
   - Navigate to your forked repository and click "Compare & pull request".

Please ensure that your contributions adhere to the project's coding standards and include appropriate documentation.

## License

Skyllia Placeholder is released under the [MIT License](../../LICENSE).