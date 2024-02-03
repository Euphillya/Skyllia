## Contact :

[![Join us on Discord](https://discord.com/api/guilds/1196471429936463943/widget.png?style=banner2)](https://discord.gg/uUJQEB7XNN)

## bStats
[![](https://bstats.org/signatures/bukkit/Skyllia.svg)](https://bstats.org/plugin/bukkit/Skyllia/20874)

## Plugin Skyblock for Folia

Skyllia is a Skyblock plugin that will essentially run on Folia.
The plugin will have very little innovative functionality, and it's not my intention at all that there should be
external features (like quests, for example).
The plugin has an API that you can use to add feature extensions to customize your server.

## Download Plugin

- Dev : https://github.com/Euphillya/Skyllia/actions
- Alpha : https://github.com/Euphillya/Skyllia/releases

## API

Gradle Groovy

```groovy
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/Euphillya/Skyllia")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") ?: System.getenv("TOKEN")
        }
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

|                   Software                    |     Version     |
|:---------------------------------------------:|:---------------:|
| [PaperMC](https://papermc.io/downloads/paper) |  1.20.1-1.20.4  |
|        [Purpur](https://purpurmc.org)         |  1.20.1-1.20.4  |
|  [Folia](https://papermc.io/software/folia)   |  1.19.4-1.20.2  |
|                    Spigot                     | Not supported ! |
|                    Bukkit                     | Not supported ! |

## Generation Island

Each island will be generated on a single region file with distance region configurable!
