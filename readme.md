English | [中文](./readme_zh.md)

## Contact :

[![Join us on Discord](https://discord.com/api/guilds/1196471429936463943/widget.png?style=banner2)](https://discord.gg/uUJQEB7XNN)

## Plugin Skyblock for Folia

Skyllia is a Skyblock plugin that will essentially run on Folia.
The plugin will have very little innovative functionality, and it's not my intention at all that there should be
external features (like quests, for example).
The plugin has an API that you can use to add feature extensions to customize your server.

## Download Plugin

- Dev : https://github.com/Euphillya/Skyllia/actions
- Alpha : https://github.com/Euphillya/Skyllia/releases

## Compatible Plugin

- Folia: (that's why the plugin exists)
- Paper and fork: To be tested with the addition of Folia's scheduler API to
  Paper: [Move some Folia API to Paper for easy compat (#9360)](https://github.com/PaperMC/Paper/commit/d6d4c78e7d88f3fcd274bceab1e6b022224096ef)
- Spigot : no !
- Bukkit: no !

## Generation Island

Each island will be generated on a single region file!

## Configuration

plugins/Skyllia/config.toml

```toml
config-version = 1
verbose = false

[island-starter]

[island-starter.example-schem]

[island-starter.example-schem.worlds]

[island-starter.example-schem.worlds.sky-overworld]
name = "example-schem"
schematic = "./schematics/default.schem"

[settings]

[settings.player]

[settings.player.island]

[settings.player.island.delete]
clear-inventory = true
clear-enderchest = true
clear-experience = true

[worlds]

[worlds.sky-overworld]
environment = "NORMAL"
portal-tp = "sky-overworld"

[sgbd]

[sgbd.mariadb]
hostname = "127.0.0.1"
password = "azerty"
database = "sky_folia"
host = "3306"
maxPool = 5
username = "root"
useSSL = false
timeOut = 500

[island]

[island.create]
default-schem-key = "example-schem"

[island-types]

[island-types.example]
size = 50.0
name = "example"
max-members = 3

[config]
max-island = 1_000_000

```

plugins/Skyllia/language.toml (Support MiniMessage)

```toml
config-version = 1
verbose = false

[island]

[island.promote]
fail = "Le joueur %s ne peut pas être promu."
fail-high-equals-status = "Vous ne pouvez pas promouvoir un joueur à votre rang ou d'un rang plus élevé."
success = "Le joueur %s a été promu."
not-enough-args = "La commande n'est pas complète : /skyllia promote <member>"

[island.demote]
fail = "Le joueur %s ne peut pas être rétrogradé."
fail-high-equals-status = "Vous ne pouvez pas rétrograder un joueur à votre rang ou celui au dessus."
success = "Le joueur %s a été rétrogradé."
not-enough-args = "La commande n'est pas complète : /skyllia demote <member>"

[island.biome]
change-in-progress = "Changement de biome en cours. Veuillez notez que ça prends du temps... Un message vous avertira quand le processus sera achevé."
success = "Le changement de biome dans le chunk où vous étiez est terminé !"
only-island = "La commande ne peut être exécuté seulement sur une île"
not-enough-args = "La commande n'est pas complète : /skyllia biome <biome>"
biome-not-exist = "Le biome %s n'existe pas."

[island.transfert]
success = "Le nouveau propriétaire de l'ile est : %new_owner%"

[island.create]
type-no-exist = "Le type d'île sélectionné n'existe pas."
finish = "Bienvenue sur votre île !"
error = "Une erreur s'est produite lors de la création de l'ile"
in-progress = "L'île est en cours de création"
schem-no-exist = "La schematic pour créer l'ile n'existe pas."

[island.invite]
already-on-an-island = "Vous êtes déjà sur une île !"
not-enough-args = "La commande n'est pas complète : /skyllia invite <add/accept/decline> <player/island_owner>"

[island.invite.add]
notification-player = "Le joueur %player_invite% vous a invité sur son île. Pour accepter : /skyllia invite accept %player_invite%. Pour décliner : /skyllia invite decline %player_invite%"
pending = "Le joueur %s a bien été invité. En attente d'une réponse..."
not-enough-args = "Vous devez préciser sur quel île vous souhaiter décliner : /skyllia invite add <player>"

[island.invite.decline]
owner-not-island = "L'île du joueur %s n'a pas été trouvé."
not-enough-args = "Vous devez préciser sur quel île vous souhaiter décliner : /skyllia invite decline <island_owner>"

[island.invite.accept]
owner-not-island = "L'île du joueur %s n'a pas été trouvé."
success = "Vous êtes dorénavant membre de l'île !"
max-member-exceeded = "Le seuil de place de membre de l'ile a été atteints. Vous ne pouvez pas rejoindre l'île."
not-enough-args = "Vous devez préciser sur quel île vous souhaiter rejoindre : /skyllia invite accept <island_owner>"

[island.generic]
not-member = "Le joueur n'est pas membre de l'ile"
error = "Une erreur s'est produite. Merci de contacter un administrateur."
only-owner = "Seul le propriétaire de l'île peut faire ça."

[island.generic.player]
not-found = "Le joueurs est introuvable."
no-island = "Vous n'avez pas d'île !"
permission-denied = "Vous n'avez pas la permission de faire cela."
already-exist = "Vous avez déjà une île."
command-already-execution = "La commande est déjà en cours d'execution, veuillez patienter quelques instants."
not-in-island = "Vous devez être sur votre île."
warp-create = "Votre warp : %s a été crée."

```
