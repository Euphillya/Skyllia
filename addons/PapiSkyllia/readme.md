# Skyllia Placeholder Processor

This document describes the placeholder system used in [Skyllia](https://modrinth.com/plugin/skyllia), along with the different types of placeholders available, roles, permissions, and game rules.

## Introduction

The **Placeholder Processor** is a system that dynamically displays information related to islands, permissions, ore generators, and game rules in the Skyllia game. These placeholders can be used in various interfaces to display player-specific and island-specific data.

This system utilizes **PlaceholderAPI (PAPI)** to manage placeholders via the custom **SkylliaExpansion**.

## Installation

### Prerequisites

- **Minecraft Server with Folia or Paper**.
- **PlaceholderAPI (PAPI)** installed on the server.
- **Skyllia Plugin** installed on the server.

### Installation Steps

1. **Download PlaceholderAPI (PAPI):**

   - Download PAPI from [SpigotMC](https://www.spigotmc.org/resources/placeholderapi.6245/).
   - Place the `.jar` file in your server's `plugins` folder.
   - Restart the server to load PAPI.

2. **Download Skyllia:**

   - Download Skyllia from [Modrinth](https://modrinth.com/plugin/skyllia).
   - Place the `.jar` file in your server's `plugins` folder.
   - Restart the server to load Skyllia and config it.

3. **Integrate Expansion-skyllia:**

   - Compile the `Expansion-skyllia` class and place the corresponding `.jar` in your server's `plugins/PlaceholderAPI/expansions/` folder.
   - If the Skyllia plugin already provides the expansion, ensure it is enabled.

4. **Register the Expansion:**

   - Run the command `/papi reload` to reload PlaceholderAPI and detect new expansions.
   - Verify that the Skyllia expansion is registered using `/papi list`.

5. **Check Available Placeholders:**

   - Use the command `/papi ecloud list` to list available expansions.
   - If necessary, install the Skyllia expansion via `/papi ecloud download Skyllia`, then `/papi reload`.

### Optional: Integrate Skyllia-Ore

**Skyllia-Ore** is an optional plugin that extends Skyllia by adding a placeholder for ore generators. You can find it on [GitHub](https://github.com/Euphillya/Skyllia-Ore).

If **Skyllia-Ore** is installed, it unlocks the `%skyllia_ore%` placeholder, which displays the current ore generator in use on the player's island. If Skyllia-Ore is not installed, this placeholder will not function, but the rest of the Skyllia plugin will work as expected.

#### Installation Steps:

1. **Download Skyllia-Ore:**

   - Download the latest release from [GitHub](https://github.com/Euphillya/Skyllia-Ore).
   - Place the `.jar` file in your server's `plugins` folder.
   - Restart the server to load Skyllia-Ore.

2. **Verify Skyllia-Ore Integration:**

   - Run `/papi reload` to reload PlaceholderAPI.
   - Test Skyllia-Ore-specific placeholders like `%skyllia_ore%` to ensure proper functionality.

## Using Placeholders

Placeholders are special strings that will be replaced with dynamic values. They generally follow this format:

```
%skyllia_{placeholder_type_parameters}%
```

**Important:** Placeholders must be enclosed with `%` symbols and prefixed with `skyllia_` to be recognized by PAPI.

### Island Placeholders

Placeholders related to islands provide general information about a specific island.

#### List of Island Placeholders

- `%skyllia_island_size%`: Size of the island.
- `%skyllia_island_members_max_size%`: Maximum number of members on the island.
- `%skyllia_island_members_size%`: Current number of members on the island.
- `%skyllia_island_rank%`: The player's role on the island.
- `%skyllia_island_tps%`: TPS (Ticks Per Second) of the island.

**Example Usage:**

```text
Your island has a size of %skyllia_island_size% blocks.
```

### Ore Generator Placeholder (Optional)

If the **Skyllia-Ore** plugin is installed, the following placeholder will be available:

- `%skyllia_ore%`: Displays the current ore generator in use on the player's island.

**Example Usage:**

```text
Your current ore generator is %skyllia_ore%.
```

If the Skyllia-Ore plugin is not installed, this placeholder will not return any values.

### Permissions Placeholders

These placeholders allow you to check if a specific role has a certain permission on the island.

#### Format

```
%skyllia_permissions_{roleType}_{permissionsType}_{permissionName}%
```

- **roleType**: The role type (see the [Roles](#roles) section).
- **permissionsType**: The type of permissions (`COMMANDS`, `ISLAND`, `INVENTORY`).
- **permissionName**: The name of the permission (see the [Permissions](#permissions) sections).

**Example:**

```
%skyllia_permissions_MEMBER_COMMANDS_INVITE%
```

This placeholder returns `true` if the `MEMBER` role has the `INVITE` permission in commands, otherwise `false`.

### Gamerule Placeholders

These placeholders allow you to check the status of a specific game rule on the island.

#### Format

```
%skyllia_gamerule_{ruleName}%
```

- **ruleName**: The name of the game rule (see the [Game Rules](#game-rules-gamerules) section).

**Example:**

```text
%skyllia_gamerule_DISABLE_FIRE_SPREADING%
```

This placeholder returns `true` if fire spreading is disabled on the island, otherwise `false`.

## Roles

Roles define the access levels of members on an island.

| Role Name | Value |
|-----------|-------|
| OWNER     | 4     |
| CO_OWNER  | 3     |
| MODERATOR | 2     |
| MEMBER    | 1     |
| VISITOR   | 0     |
| BAN       | -1    |

## Permission Types

The available permission types are:

- **COMMANDS**: Permissions related to commands.
- **ISLAND**: Permissions related to interactions with the island.
- **INVENTORY**: Permissions related to the use of inventories.

## Permissions

### Command Permissions (COMMANDS)

| Permission Name     | Description                               |
|---------------------|-------------------------------------------|
| DEMOTE              | Demote players.                           |
| PROMOTE             | Promote players.                          |
| KICK                | Kick players from the island.             |
| ACCESS              | Access certain island features.           |
| SET_HOME            | Set the home point on the island.         |
| INVITE              | Invite players to the island.             |
| SET_BIOME           | Set the biome of the island.              |
| SET_WARP            | Set a teleportation point.                |
| DEL_WARP            | Delete a teleportation point.             |
| TP_WARP             | Teleport to a warp point.                 |
| EXPEL               | Expel players from the island.            |
| MANAGE_PERMISSION   | Manage island permissions.                |
| BAN                 | Ban players from the island.              |
| UNBAN               | Unban players from the island.            |
| MANAGE_TRUST        | Manage trust levels on the island.        |
| MANAGE_GAMERULE     | Manage game rules of the island.          |

### Inventory Permissions (INVENTORY)

| Permission Name       | Description                                   |
|-----------------------|-----------------------------------------------|
| OPEN_CHEST            | Open chests.                                  |
| OPEN_ANVIL            | Use anvils.                                   |
| OPEN_WORKBENCH        | Use crafting tables.                          |
| OPEN_ENCHANTING       | Use enchanting tables.                        |
| OPEN_BREWING          | Use brewing stands.                           |
| OPEN_SMITHING         | Use smithing tables.                          |
| OPEN_BEACON           | Use beacons.                                  |
| OPEN_SHULKER_BOX      | Open shulker boxes.                           |
| OPEN_FURNACE          | Use furnaces.                                 |
| OPEN_LECTERN          | Use lecterns.                                 |
| OPEN_CRAFTER          | Use crafters (version 1.20.4).                |
| OPEN_LOOM             | Use looms.                                    |
| OPEN_GRINDSTONE       | Use grindstones.                              |
| OPEN_STONECUTTER      | Use stonecutters.                             |
| OPEN_CARTOGRAPHY      | Use cartography tables.                       |
| OPEN_MERCHANT         | Interact with merchants.                      |
| OPEN_HOPPER           | Use hoppers.                                  |
| OPEN_BARREL           | Open barrels.                                 |
| OPEN_BLAST_FURNACE    | Use blast furnaces.                           |
| OPEN_SMOKER           | Use smokers.                                  |
| OPEN_DISPENSER        | Use dispensers.                               |
| OPEN_DROPPER          | Use droppers.                                 |

### Island Permissions (ISLAND)

| Permission Name     | Description                                 |
|---------------------|---------------------------------------------|
| BLOCK_BREAK         | Break blocks.                               |
| BLOCK_PLACE         | Place blocks.                               |
| BUCKETS             | Use buckets.                                |
| REDSTONE            | Use redstone components.                    |
| PVP                 | Engage in player versus player combat.      |
| KILL_MONSTER        | Kill monsters.                              |
| KILL_ANIMAL         | Kill animals.                               |
| DROP_ITEMS          | Drop items.                                 |
| PICKUP_ITEMS        | Pick up items.                              |
| USE_NETHER_PORTAL   | Use Nether portals.                         |
| USE_END_PORTAL      | Use End portals.                            |
| INTERACT_ENTITIES   | Interact with entities.                     |
| KILL_UNKNOWN_ENTITY | Kill unknown entities.                      |
| KILL_NPC            | Kill NPCs (non-player characters).          |
| INTERACT            | Perform general interactions on the island. |

## Game Rules (Gamerules)

Game rules allow you to customize the behavior of the island by enabling or disabling certain features.

| Game Rule Name               | Description                                            |
|------------------------------|--------------------------------------------------------|
| DISABLE_SPAWN_HOSTILE        | Disable spawning of hostile mobs on the island.        |
| DISABLE_SPAWN_PASSIVE        | Disable spawning of passive mobs on the island.        |
| DISABLE_SPAWN_UNKNOWN        | Disable spawning of unknown mobs on the island.        |
| DISABLE_HUMAN_EXPLOSION      | Disable explosions caused by players on the island.    |
| DISABLE_MOB_EXPLOSION        | Disable explosions caused by mobs on the island.       |
| DISABLE_ENDERMAN_PICK_BLOCK  | Prevent Endermen from picking up blocks on the island. |
| DISABLE_FIRE_SPREADING       | Disable fire spreading on the island.                  |
| DISABLE_MOB_PICKUP_ITEMS     | Prevent mobs from picking up items on the island.      |
| DISABLE_UNKNOWN_EXPLOSION    | Disable unknown explosions on the island.              |
| DISABLE_PASSIF_MOB_GRIEFING  | Prevent passive mobs from griefing on the island.      |
| DISABLE_HOSTILE_MOB_GRIEFING | Prevent hostile mobs from griefing on the island.      |
| DISABLE_UNKNOWN_MOB_GRIEFING | Prevent unknown mobs from griefing on the island.      |

## Usage Examples

### Get the Island Size

```text
Your island has a size of %skyllia_island_size% blocks.
```

### Check if a Role Has a Specific Permission

To check if the `MODERATOR` role has the `KICK` permission in commands:

```text
Kick permission: %skyllia_permissions_MODERATOR_COMMANDS_KICK%
```

### Check the Status of a Game Rule on the Island

To find out if fire spreading is disabled on the island:

```text
Fire spreading disabled: %skyllia_gamerule_DISABLE_FIRE_SPREADING%
```

### Check the Ore Generator (Optional)

If **Skyllia-Ore** is installed, you can check the name of the current ore generator:

```text
Your current ore generator is %skyllia_ore%.
```

## Notes

- **Case Sensitivity:** Placeholders are case-sensitive for role names, permission names, and game rule names. Use the EXACT names as defined in the tables above.
- **Version Updates:** Some permissions or rules may be specific to certain game versions (e.g., `OPEN_CRAFTER` for version 1.20.4).
- **Default Values:** If a placeholder is malformed or references a non-existent value, it will return `"Invalid placeholder format"` or a default value.
- **PAPI Configuration:** Ensure that custom placeholders are supported by checking PAPI's configuration file and adding support for the Skyllia plugin if necessary.

## Useful Commands

- **Reload PAPI:** `/papi reload` to reload PAPI configurations.
- **Check Available Expansions:** `/papi list` to display the list of available expansions.
- **Install the Skyllia Expansion:** If available on PAPI's cloud, use `/papi ecloud download skyllia` then `/papi reload`.
- **Install the Skyllia-Ore Expansion:** If Skyllia-Ore is installed, test the ore generator placeholder with `/papi parse me %skyllia_ore%`.
- **Test a Placeholder:** `/papi parse me %skyllia_island_size%` to test the placeholder directly in-game.