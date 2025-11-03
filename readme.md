# Skyllia - Skyblock Plugin for Minecraft

Skyllia is a Skyblock plugin designed primarily for Folia but also compatible with Paper (1.20.1+). The plugin has an
API that you can use to add feature extensions to customize your server. The plugin will have very little innovative
functionality, and it's not my intention at all that there should be external features (like quests, for example).

## bStats

[![](https://bstats.org/signatures/bukkit/Skyllia.svg)](https://bstats.org/plugin/bukkit/Skyllia/20874)

## Contact

[![Join us on Discord](https://discord.com/api/guilds/1196471429936463943/widget.png?style=banner2)](https://discord.gg/uUJQEB7XNN)

## Prerequisites

- A MariaDB database (recommended) or SQLite for local storage (not recommended for production use).
- Folia or Paper server running Minecraft version 1.20.1 or higher, but Paper is not recommended for production use.
- Java 21
- [WorldEdit-Folia](https://github.com/Euphillya/WorldEdit-Folia/actions)
  or [FAWE](https://ci.athion.net/job/FastAsyncWorldEdit/)
  or [WorldEdit](https://modrinth.com/plugin/worldedit/versions?l=bukkit)) (recommended for island generation).

## Installation

1. Ensure you have the required prerequisites installed.
2. Download the latest version of Skyllia from the [Releases](https://modrinth.com/plugin/skyllia) page.
3. Place the downloaded `.jar` file into your server's `plugins` directory.
4. Start your server to generate the default configuration files.
5. Configure the plugin to your liking by editing the configuration files in the `plugins/Skyllia` directory.
6. Restart your server to apply the changes.

## Download Plugin

- [Release Versions on Modrinth](https://modrinth.com/plugin/skyllia)
- [Release Versions on GitHub](https://github.com/Euphillya/Skyllia/releases)

## Configuration (Need update)

Configuration files are located in the `plugins/Skyllia` directory. Customize settings to fit your server's needs.
Detailed configuration instructions can be found in the [Wiki](https://github.com/Euphillya/Skyllia/wiki/Configuration).

## API

### Gradle Groovy

To add Skyllia API to your project, use the following configuration:

```groovy
repositories {
    maven {
        url = uri("https://repo.euphyllia.moe/repository/maven-public/")
    }
}

dependencies {
    compileOnly("fr.euphyllia.skyllia:api:[VERSION]")
    compileOnly("fr.euphyllia.skyllia:plugin:[VERSION]")
    compileOnly("fr.euphyllia.skyllia:database:[VERSION]")
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

Skyllia comes with a set of commands and permissions to manage the plugin effectively. For a full list of commands and
their permissions, refer to
the [Commands & Permissions](https://github.com/Euphillya/Skyllia/wiki/Commands-and-Permissions) section in the Wiki.

## Compatible Software

|                   Software                    |    Version     |
|:---------------------------------------------:|:--------------:|
| [PaperMC](https://papermc.io/downloads/paper) | 1.20.1-1.21.10 |
|        [Purpur](https://purpurmc.org)         | 1.20.1-1.21.10 |
|  [Folia](https://papermc.io/software/folia)   |  1.20-1.21.10  |

## Island Generation

Each island is generated in a single region file, with configurable region distances to suit your server's needs.

## Addons

- [SkylliaOre](https://github.com/Euphillya/Skyllia/tree/dev/addons/SkylliaOre) by Euphyllia (Ore Generator)
- [Insight Skyllia](https://github.com/Euphillya/Skyllia/tree/dev/addons/InsightsSkyllia) by Euphyllia
- [PAPI Skyllia](https://github.com/Euphillya/Skyllia/tree/dev/addons/PapiSkyllia) by Euphyllia
- [SkylliaChat](https://github.com/Euphillya/Skyllia/tree/master/addons/SkylliaChat) by Euphyllia (Chat Integration)
- [SkylliaBank](https://github.com/Euphillya/Skyllia/tree/master/addons/SkylliaBank) by Euphyllia (Island Bank System)
- [SkylliaChallenge](https://github.com/Euphillya/Skyllia/tree/master/addons/SkylliaChallenge) by Euphyllia (Challenges
  System)

## Contribution

We welcome contributions from the community! If you'd like to contribute, please follow these steps:

1. Fork the repository.
2. Create a new branch (`git checkout -b feature-branch`).
3. Commit your changes (`git commit -m 'Add new feature'`).
4. Push to the branch (`git push origin feature-branch`).
5. Open a Pull Request.

For more detailed guidelines, refer to the [Contributing Guide](CONTRIBUTING.md).

## License

This project is licensed under the MIT License. See the [LICENCE](LICENSE) file for details.

For more information and detailed documentation, please refer to the [Wiki](https://github.com/Euphillya/Skyllia/wiki).

Feel free to contribute, open issues, or join us on Discord for support and discussions!