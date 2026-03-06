<div align="center">

# <img src="https://cdn.modrinth.com/data/fGbtispn/d3a18500b1a981a2207407035798e918437d0f94_96.webp" height="25"> Skyllia

### Le premier plugin Skyblock entièrement compatible Folia

[![Folia](https://img.shields.io/badge/Folia-Compatible-green.svg)](https://papermc.io/software/folia)
[![Paper](https://img.shields.io/badge/Paper-1.21+-blue.svg)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/license-MIT-blue)](https://github.com/Euphillya/Skyllia)

[Documentation](https://skyllia.eupyllia.moe) • [GitHub](https://github.com/Euphillya/Skyllia) • [Modrinth](https://modrinth.com/plugin/skyllia) • [Discord](https://discord.gg/uUJQEB7XNN)

[![](https://bstats.org/signatures/bukkit/Skyllia.svg)](https://bstats.org/plugin/bukkit/Skyllia/20874)
</div>

---

## 📖 À propos

**Skyllia** est un plugin Skyblock moderne et performant, conçu dès le départ pour tirer pleinement parti de
l'architecture multi-threadée de **Folia**. Premier du genre à offrir une compatibilité native avec Folia, Skyllia
garantit des performances exceptionnelles même avec des centaines de joueurs simultanés.

## ✨ Fonctionnalités principales

### 🚀 Performance & Architecture

- **Premier plugin Skyblock compatible Folia** - Architecture multi-threadée native
- **Système asynchrone** - Pas de freeze, pas de lag (tant que votre configuration serveur le permet)

### 💾 Gestion des données

- **Base de données** - SQLite, MariaDB, PostgreSQL
- **Pool de connexions HikariCP** - Performances optimales
- **Sauvegarde asynchrone** - Aucun impact sur les performances

### 🏝️ Gestion des îles

- **Création instantanée** - File d'attente optimisée
- **Schématiques personnalisables** - WorldEdit/FAWE ou système interne
- **Multi-mondes** - Overworld, Nether, End configurables
- **Portails automatiques** - Navigation fluide entre dimensions
- **Espacement configurable** - Distance entre les îles ajustable

### 👥 Système de permissions

- **6 rôles prédéfinis** - Owner, Co-Owner, Moderator, Member, Visitor, Ban
- **50+ permissions granulaires** - Contrôle total par rôle
- **Trust temporaire** - Accès invités configurables
- **Système modulaire** - Créez vos propres rôles

### 🎯 Fonctionnalités avancées

- **Warps d'île** - Téléportation rapide sur votre île
- **Biomes personnalisables** - Changez l'ambiance de votre île
- **Game rules par île** - PvP, mobs, explosions, etc.
- **Système de ban** - Bannissez les griefers de votre île
- **Invitations** - Système d'invitation avec expiration

### 🔌 Intégrations

- **PlaceholderAPI** - Variables pour scoreboard, tab, etc.
- **Vault** - Support économie (via SkylliaBank)
- **WorldEdit/FAWE** - Schématiques haute performance
- **Insights** - Limites de blocs (via InsightsSkyllia)

## 🎮 Addons officiels

Étendez Skyllia avec 5 addons officiels gratuits :

| Addon                | Description                                      |
|----------------------|--------------------------------------------------|
| **SkylliaBank**      | Système bancaire partagé par île avec historique |
| **SkylliaOre**       | Générateur automatique de minerais configurable  |
| **SkylliaChallenge** | Défis quotidiens/hebdomadaires avec récompenses  |
| **SkylliaChat**      | Chat séparé par île avec canaux multiples        |
| **InsightsSkyllia**  | Limites de blocs et entités par île              |

## 📋 Prérequis

- **Serveur** : Folia 1.20+ ou Paper 1.20.1+
- **Java** : 21 ou supérieur
- **RAM** : 4GB minimum, 8GB+ recommandé
- **Base de données** (optionnel) : MariaDB 10.5+ ou PostgreSQL 12+ (privilégiez PostgreSQL pour les gros serveurs)

### Plugins compatibles (optionnels)

- WorldEdit ou FastAsyncWorldEdit (recommandé)
- Vault (pour SkylliaBank)
- PlaceholderAPI (pour placeholders)
- LuckPerms (pour permissions serveur)

## 🚀 Installation rapide

1. **Téléchargez** Skyllia sur [Modrinth](https://modrinth.com/plugin/skyllia)
   ou [GitHub Releases](https://github.com/Euphillya/Skyllia/releases)
2. **Placez** le .jar dans `plugins/`
3. **Démarrez** le serveur (première initialisation)
4. **Configurez** dans `plugins/Skyllia/config/`
5. **Redémarrez** et c'est prêt !

### Configuration minimale

```toml
# config.toml
[settings.island]
region-distance = 10  # 5120 blocs entre îles, 1 region = 512x512
max-islands = 500_000

# database.toml - SQLite par défaut
[sqlite]
enabled = true
```

Pour une installation complète, consultez la [documentation]().

## 📚 Documentation

La documentation complète est disponible sur GitBook :

**🔗 [skyllia.euphyllia.moe](https://skyllia.euphyllia.moe)**

### Sections principales

- **[Démarrage rapide](#)** - Installation en 5 minutes
- **[Configuration](#)** - Guide complet de configuration
- **[Commandes](#)** - 26 commandes joueur + commandes admin
- **[Addons](#)** - Documentation des addons officiels
- **[API](#)** - Développez vos propres addons
- **[FAQ](#)** - Questions fréquentes

## 💻 Pour les développeurs

Skyllia fournit une API complète pour créer vos propres addons :

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
if(island !=null){
        player.

sendMessage("Votre île : "+island.getId());
        }
```

Consultez la [documentation API](#) pour plus d'informations.

## 🎯 Commandes principales

### Joueurs

```
/island create <type> - Créer une île
/island home - Se téléporter à son île
/island invite <joueur> - Inviter un joueur
/island delete - Supprimer son île
/island biome <biome> - Changer le biome
/island expel <joueur> - Expulser un visiteur
```

### Administrateurs

```
/isadmin reload - Recharger les configs
/isadmin database - Gestion base de données
/isadmin purge <jours> - Nettoyer îles inactives
/isadmin setsize <joueur> <taille> - Modifier taille
/isadmin teleport <joueur> - TP vers île d'un joueur
```

Liste complète : [Commandes joueur](#) • [Commandes admin](#)

## 🤝 Contribution

Skyllia est open source, vous pouvez contribuer via :

- **Rapports de bugs** - [Issues GitHub](https://github.com/Euphillya/Skyllia/issues)
- **Suggestions** - Discord ou GitHub Discussions
- **Addons communautaires** - Partagez vos créations !

## 📞 Support

- **GitHub** : [Euphillya/Skyllia](https://github.com/Euphillya/Skyllia)
- **Issues** : [Signaler un bug](https://github.com/Euphillya/Skyllia/issues)
- **Discord** : [Serveur Euphyllia](https://discord.gg/uUJQEB7XNN)

## 📜 Licence

Skyllia est sous licence MIT. Pour plus de détails, consultez le fichier [LICENSE](LICENSE) dans le dépôt GitHub.