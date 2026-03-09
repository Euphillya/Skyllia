<div align="center">

# <img src="https://cdn.modrinth.com/data/fGbtispn/d3a18500b1a981a2207407035798e918437d0f94_96.webp" height="25"> Skyllia

### The first Skyblock plugin fully compatible with Folia

[![Folia](https://img.shields.io/badge/Folia-Compatible-green.svg)](https://papermc.io/software/folia)
[![Paper](https://img.shields.io/badge/Paper-1.20.1+-blue.svg)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/license-MIT-blue)](https://github.com/Euphillya/Skyllia)

[Documentation](https://skyllia.eupyllia.moe) • [GitHub](https://github.com/Euphillya/Skyllia) • [Modrinth](https://modrinth.com/plugin/skyllia) • [Discord](https://discord.gg/uUJQEB7XNN)

[![Servers & Players](https://faststats.dev/embed/default:6978bcc2-1574-41ea-b5ae-242398b401c7:servers-and-players?w=800&h=300)](https://faststats.dev/project/skyllia/skyllia-plugin)
</div>

---

## 📖 About

**Skyllia** is a modern, high-performance Skyblock plugin, designed from the ground up to fully leverage the
multi-threaded architecture of **Folia**. The first of its kind to offer native Folia compatibility, Skyllia
delivers exceptional performance even with hundreds of simultaneous players.

## ✨ Key Features

### 🚀 Performance & Architecture

- **First Folia-compatible Skyblock plugin** — Native multi-threaded architecture
- **Asynchronous system** — No freezes, no lag (as long as your server configuration allows)

### 💾 Data Management

- **Database support** — SQLite, MariaDB, PostgreSQL
- **HikariCP connection pool** — Optimal performance
- **Asynchronous saving** — Zero performance impact

### 🏝️ Island Management

- **Instant creation** — Optimized queue system
- **Customizable schematics** — WorldEdit/FAWE or built-in system
- **Multi-world** — Overworld, Nether, End all configurable
- **Automatic portals** — Smooth navigation between dimensions
- **Configurable spacing** — Adjustable distance between islands

### 👥 Permission System

- **6 predefined roles** — Owner, Co-Owner, Moderator, Member, Visitor, Ban
- **50+ granular permissions** — Full control per role
- **Temporary trust** — Configurable guest access
- **Modular system** — Create your own roles

### 🎯 Advanced Features

- **Island warps** — Quick teleportation to your island
- **Customizable biomes** — Change the atmosphere of your island
- **Per-island game rules** — PvP, mobs, explosions, etc.
- **Ban system** — Ban griefers from your island
- **Invitations** — Invitation system with expiry

### 🔌 Integrations

- **PlaceholderAPI** — Variables for scoreboard, tab, etc.
- **Vault** — Economy support (via SkylliaBank)
- **WorldEdit/FAWE** — High-performance schematics
- **Insights** — Block limits (via InsightsSkyllia)

## 🎮 Official Addons

Extend Skyllia with 5 free official addons:

| Addon                | Description                                           |
|----------------------|-------------------------------------------------------|
| **SkylliaBank**      | Shared island bank system with transaction history    |
| **SkylliaOre**       | Configurable automatic ore generator                  |
| **SkylliaChallenge** | Daily/weekly challenges with rewards                  |
| **SkylliaChat**      | Per-island chat with multiple channels                |
| **InsightsSkyllia**  | Block and entity limits per island                    |

## 📋 Requirements

- **Server**: Folia 1.20+ or Paper 1.20.1+
- **Java**: 21 or higher
- **RAM**: 4GB minimum, 8GB+ recommended
- **Database** (optional): MariaDB 10.5+ or PostgreSQL 12+ (prefer PostgreSQL for large servers)

### Compatible plugins (optional)

- WorldEdit or FastAsyncWorldEdit (recommended)
- Vault (for SkylliaBank)
- PlaceholderAPI (for placeholders)
- LuckPerms (for server permissions)

## 🚀 Quick Installation

1. **Download** Skyllia from [Modrinth](https://modrinth.com/plugin/skyllia)
   or [GitHub Releases](https://github.com/Euphillya/Skyllia/releases)
2. **Place** the .jar in `plugins/`
3. **Start** the server (first initialization)
4. **Configure** in `plugins/Skyllia/config/`
5. **Restart** and you're good to go!

### Minimal Configuration

```toml
# config.toml
[settings.island]
region-distance = 10  # 5120 blocks between islands, 1 region = 512x512
max-islands = 500_000

# database.toml - SQLite by default
[sqlite]
enabled = true
```

For a full installation guide, check the [documentation]().

## 📚 Documentation

Full documentation is available on GitBook:

**🔗 [skyllia.euphyllia.moe](https://skyllia.euphyllia.moe)**

## 💻 For Developers

Skyllia provides a complete API to create your own addons:

```xml
<!-- Maven -->
<repository>
    <id>euphyllia-repo</id>
    <url>https://repo.euphyllia.moe/repository/maven-public/</url>
</repository>

<dependency>
<groupId>fr.euphyllia.skyllia</groupId>
<artifactId>api</artifactId>
<version>3.x</version>
<scope>provided</scope>
</dependency>
```

```java
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.island.Island;
import org.bukkit.entity.Player;

Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
if(island != null){
   player.sendMessage("Your island: "+ island.getId());
}
```

See the [API documentation](#) for more information.

## 🎯 Main Commands

### Players

```
/island create <type>    - Create an island
/island home             - Teleport to your island
/island invite <player>  - Invite a player
/island delete           - Delete your island
/island biome <biome>    - Change the biome
/island expel <player>   - Expel a visitor
```

### Administrators

```
/isadmin reload              - Reload configs
/isadmin database            - Database management
/isadmin purge <days>        - Clean up inactive islands
/isadmin setsize <player> <size> - Modify island size
/isadmin teleport <player>   - TP to a player's island
```

Full list: [Player commands](#) • [Admin commands](#)

## 🤝 Contributing

Skyllia is open source, you can contribute via:

- **Bug reports** — [GitHub Issues](https://github.com/Euphillya/Skyllia/issues)
- **Suggestions** — Discord or GitHub Discussions
- **Community addons** — Share your creations!

## 📞 Support

- **GitHub**: [Euphillya/Skyllia](https://github.com/Euphillya/Skyllia)
- **Issues**: [Report a bug](https://github.com/Euphillya/Skyllia/issues)
- **Discord**: [Euphyllia Server](https://discord.gg/uUJQEB7XNN)

## 📜 License

Skyllia is licensed under the MIT License. For more details, see the [LICENSE](LICENSE) file in the GitHub repository.
