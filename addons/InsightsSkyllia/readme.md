# SkylliaInsight

**SkylliaInsightAddon** is an addon for the [Insights](https://github.com/InsightsPlugin/Insights) plugin, designed to integrate the Skyllia platform's island management features with Insights' region limiting capabilities. This addon allows server administrators to monitor and enforce limits on player islands, ensuring a balanced and optimized gameplay experience.

## Features

- **Region Integration:** Seamlessly integrates Skyllia islands with Insights, enabling region-based limits on island activities.
- **Custom Limit Configurations:** Utilize Insights' powerful limits configuration to manage tile, group, and permission-based restrictions on islands.
- **Dynamic Region Detection:** Automatically detects island regions based on chunk locations, ensuring accurate limit enforcement.
- **Folia Support:** Optimized for Folia, ensuring enhanced performance on multithreaded servers.
- **Permission-Based Access:** Control access to various functionalities with granular permissions for administrators.

## Requirements

- **Minecraft Server:** Compatible with Paper/Folia servers running Minecraft version 1.20.1 or greater.
- **Skyllia Platform:** Ensure the Skyllia plugin is installed and active on your server.
- **Insights Plugin:** Required for region limiting and monitoring functionalities.
- **Java:** Java 21 is required for optimal performance.
- **Dependencies:**
  - **SkylliaOre:** Optional, for displaying generator types.
  - **SkylliaBankAddon:** Optional, for displaying bank-related information.
  - **SkylliaValue:** Optional, for displaying island values (proprietary plugin).

## Installation

1. **Download SkylliaInsightAddon:**
   - Obtain the latest version of SkylliaInsightAddon from the [official repository](https://github.com/Euphillya/Skyllia/tree/dev/addons/SkylliaInsight).

2. **Install Dependencies:**
   - Ensure that the Skyllia and Insights plugins are installed and properly configured on your server.
   - Install any optional dependencies (SkylliaOre, SkylliaBankAddon) if you wish to utilize their features.
   - For SkylliaValue, ensure you have the proprietary plugin installed as it is required for displaying island values.

3. **Configure the Plugin:**
   - Place the `SkylliaInsightAddon.jar` file into your server's `plugins/Insights/addons/` directory.
   - Ensure that the `Insights` plugin is properly configured according to your server's requirements.

4. **Restart the Server:**
   - Restart your Minecraft server to load the SkylliaInsight plugin.

## Usage

SkylliaInsight works in the background to integrate Skyllia islands with Insights' region limiting system. Administrators can manage and configure limits through the Insights configuration files.

### Setting Up Limits

To set up limits for Skyllia islands, configure the `limits.yml` file in the Insights plugin directory as follows:

```yaml
limit:
  type: "GROUP"
  bypass-permission: "insights.bypass.limit.island"
  name: "SkylliaIslandLimits"
  limit: 1000
  regex: false
  materials:
    - "STONE"
    - "DIRT"
    - "WOOD"
  entities:
    - "ARMOR_STAND"
    - "PAINTING"
```

### Example Configurations

#### Tile Limit

```yaml
limit:
  type: "TILE"
  bypass-permission: "insights.bypass.limit.tile"
  name: "Tiles"
  limit: 256
  excluded-materials:
    - "AIR"
```

#### Group Limit

```yaml
limit:
  type: "GROUP"
  bypass-permission: "insights.bypass.limit.redstone"
  name: "Redstone"
  limit: 64
  regex: false
  materials:
    - "REDSTONE_WIRE"
    - "REDSTONE_BLOCK"
    - "HOPPER"
    - "DISPENSER"
    - "DROPPER"
    - "TRIPWIRE_HOOK"
    - "REDSTONE_LAMP"
    - "STICKY_PISTON"
    - "PISTON"
    - "REDSTONE_TORCH"
    - "TNT"
    - "NOTE_BLOCK"
    - "LEVER"
    - "REPEATER"
    - "COMPARATOR"
```

#### Permission Limit

```yaml
limit:
  type: "PERMISSION"
  bypass-permission: "insights.bypass.limit.permission"
  materials:
    "DIAMOND_BLOCK": 10
    "GOLD_BLOCK": 20
  entities:
    "ITEM_FRAME": 15
    "PAINTING": 5
```

## Configuration

Customize the limits and how they apply to Skyllia islands by editing the `limits.yml` file in the Insights plugin directory. Below is an overview of the configuration options:

### Basic Layout

```yaml
limit:
  type: "<LIMIT_TYPE>"
  bypass-permission: "<BYPASS_PERMISSION>"
```

- **`<LIMIT_TYPE>`:** Must be one of the following types: `TILE`, `GROUP`, `PERMISSION`.
- **`<BYPASS_PERMISSION>`:** Specifies the permission required to bypass this limit.

### Optional Settings

```yaml
limit:
  settings:
    enabled-worlds:
      whitelist: false
      worlds:
        - "<WORLD>"
    enabled-addons:
      whitelist: false
      addons:
        - "Skyllia"
    disallow-placement-outside-region: <DISALLOW_OUTSIDE_REGIONS>
```

- **`<WORLD>`:** The name of a world to apply the limit to (can be a blacklist or whitelist).
- **`<ADDON>`:** The name of an addon to apply the limit to (can be a blacklist or whitelist).
- **`<DISALLOW_OUTSIDE_REGIONS>`:** Whether to disallow placement outside defined regions.

## Support

For support, please join our [Discord server](https://discord.gg/uUJQEB7XNN).

## Contributing

Contributions are welcome! Please read the [contribution guidelines](../../CONTRIBUTING.md) before submitting a pull request.

## License

SkylliaInfo is licensed under the [MIT License](../../LICENSE).