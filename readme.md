# Skyllia - Skyblock Plugin for Minecraft

Skyllia is a Skyblock plugin designed primarily for Folia but also compatible with Paper (1.20.1+). This plugin offers an API for extending features to customize your server, with minimal external functionalities to maintain simplicity.

## Table of Contents

1. [Wiki](#wiki)
2. [Contact](#contact)
3. [bStats](#bstats)
4. [Plugin Features](#plugin-features)
5. [Prerequisites](#prerequisites)
6. [Installation](#installation)
7. [Download Plugin](#download-plugin)
8. [Configuration](#configuration)
9. [API](#api)
    - [Gradle Groovy](#gradle-groovy)
    - [Example Usage](#example-usage)
10. [Commands & Permissions](#commands--permissions)
11. [Compatible Software](#compatible-software)
12. [Island Generation](#island-generation)
13. [Addons](#addons)
14. [Contribution](#contribution)
15. [License](#license)

## Wiki

- [FR_FR](https://github.com/Euphillya/Skyllia/wiki)
- [EN_EN](https://github.com/Euphillya/Skyllia/tree/dev/wiki/en_en/all_files)
- [ZH_CN](https://github.com/Euphillya/Skyllia/tree/dev/wiki/zh_cn/all_files)

## Contact

[![Join us on Discord](https://discord.com/api/guilds/1196471429936463943/widget.png?style=banner2)](https://discord.gg/uUJQEB7XNN)

## bStats

[![](https://bstats.org/signatures/bukkit/Skyllia.svg)](https://bstats.org/plugin/bukkit/Skyllia/20874)

## Plugin Features

Skyllia is a Skyblock plugin that will essentially run on Folia but work on Spigot and Paper (1.20+). The plugin has an API that you can use to add feature extensions to customize your server. The plugin will have very little innovative functionality, and it's not my intention at all that there should be external features (like quests, for example).

## Prerequisites

- A MariaDB database
- Paper 1.20.1+ or Folia 1.20+
- Java 21

## Installation

1. Ensure you have the required prerequisites installed.
2. Download the latest version of Skyllia from the [Releases](https://modrinth.com/plugin/skyllia) page.
3. Place the downloaded `.jar` file into your server's `plugins` directory.
4. Start your server to generate the default configuration files.
5. Configure the plugin to your liking by editing the configuration files in the `plugins/Skyllia` directory.
6. Restart your server to apply the changes.

## Download Plugin

- [Development Builds](https://github.com/Euphillya/Skyllia/actions)
- [Release Versions](https://modrinth.com/plugin/skyllia)

## Configuration

Configuration files are located in the `plugins/Skyllia` directory. Customize settings to fit your server's needs. Detailed configuration instructions can be found in the [Wiki](https://github.com/Euphillya/Skyllia/wiki/Configuration).

## API

### Gradle Groovy

To add Skyllia API to your project, use the following configuration:

```groovy
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/Euphillya/Skyllia")
    }
}

dependencies {
    compileOnly("fr.euphyllia.skyllia:api:VERSION")
}
```

### Example Usage

Here is a simple example of how to use the Skyllia API:

```java
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;

import java.util.UUID;

Island playerIsland = SkylliaAPI.getIslandByPlayerId(player.getUniqueId()).join();
UUID islandId = playerIsland.getId();
```

## Commands & Permissions

Skyllia comes with a set of commands and permissions to manage the plugin effectively. For a full list of commands and their permissions, refer to the [Commands & Permissions](https://github.com/Euphillya/Skyllia/wiki/Commands-and-Permissions) section in the Wiki.

## Compatible Software

|                      Software                       |   Version   |
|:---------------------------------------------------:|:-----------:|
|    [PaperMC](https://papermc.io/downloads/paper)    | 1.20.1-1.21 |
|           [Purpur](https://purpurmc.org)            | 1.20.1-1.21 |
|     [Folia](https://papermc.io/software/folia)      |  1.20-1.21  |
| [Spigot](https://www.spigotmc.org) (Not Recommended) |  Cancelled  |

## Island Generation

Each island is generated in a single region file, with configurable region distances to suit your server's needs.

## Addons

- [SkylliaOre](https://github.com/Euphillya/Skyllia-Ore) by Euphyllia (Ore Generator)
- [Insight Skyllia](https://github.com/Euphillya/Insights-Skyllia) by Euphyllia
- [PAPI Skyllia](https://github.com/Euphillya/Skyllia-PAPI) by Euphyllia

## Contribution

We welcome contributions from the community! If you'd like to contribute, please follow these steps:

1. Fork the repository.
2. Create a new branch (`git checkout -b feature-branch`).
3. Commit your changes (`git commit -m 'Add new feature'`).
4. Push to the branch (`git push origin feature-branch`).
5. Open a Pull Request.

For more detailed guidelines, refer to the [Contributing Guide](CONTRIBUTING.md).

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

For more information and detailed documentation, please refer to the [Wiki](https://github.com/Euphillya/Skyllia/wiki).

Feel free to contribute, open issues, or join us on Discord for support and discussions!
