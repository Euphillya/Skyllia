## Wiki

- FR_FR : https://github.com/Euphillya/Skyllia/wiki
- EN_EN : https://github.com/Euphillya/Skyllia/tree/dev/wiki/en_en/all_files
- ZH_CN : https://github.com/Euphillya/Skyllia/tree/dev/wiki/zh_cn/all_files

## Contact :

[![Join us on Discord](https://discord.com/api/guilds/1196471429936463943/widget.png?style=banner2)](https://discord.gg/uUJQEB7XNN)

## bStats

[![](https://bstats.org/signatures/bukkit/Skyllia.svg)](https://bstats.org/plugin/bukkit/Skyllia/20874)

## Plugin Skyblock for Minecraft

Skyllia is a Skyblock plugin that will essentially run on Folia, but work on Spigot and Paper (1.17+).
The plugin will have very little innovative functionality, and it's not my intention at all that there should be
external features (like quests, for example).
The plugin has an API that you can use to add feature extensions to customize your server.

## Download Plugin

- Dev : https://github.com/Euphillya/Skyllia/actions
- Releases Versions : https://modrinth.com/plugin/skyllia

## API

Gradle Groovy

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

Example :

```java

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;

import java.util.UUID;

Island playerIsland = SkylliaAPI.getIslandByPlayerId(player.getUniqueId()).join();
UUID islandId = playerIsland.getId();
```

## Compatible Software

|                      Software                       |    Version    |
|:---------------------------------------------------:|:-------------:|
|    [PaperMC](https://papermc.io/downloads/paper)    |  1.17-1.20.4  |
|           [Purpur](https://purpurmc.org)            |  1.17-1.20.4  |
|     [Folia](https://papermc.io/software/folia)      | 1.19.4-1.20.4 |
| [Spigot](https://www.spigotmc.org)(Not Recommended) |  1.17-1.20.4  |

## Generation Island

Each island will be generated on a single region file with distance region configurable!
