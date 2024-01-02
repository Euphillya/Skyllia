## Plugin Skyblock for Folia

SkyFolia is a Skyblock plugin that will essentially run on Folia. 
The plugin will have very little innovative functionality, and it's not my intention at all that there should be external features (like quests, for example). 
The plugin has an API that you can use to add feature extensions to customize your server.

## Download Plugin

I won't be providing a Plugin Jar just yet, as the plugin isn't finished yet! Once I find it "usable", it will be made available.
HOWEVER, the plugin won't be stable right away. I'm developing in my spare time, so I won't be able to fix every bug that comes along at once. Nevertheless, don't hesitate to tell me about any problems you encounter, such as :
- Performance problems (spark recommended)
- Minor or fatal bug

## Compatible Plugin

- Folia: (that's why the plugin exists)
- Paper and fork: To be tested with the addition of Folia's scheduler API to Paper: [Move some Folia API to Paper for easy compat (#9360)](https://github.com/PaperMC/Paper/commit/d6d4c78e7d88f3fcd274bceab1e6b022224096ef)
- Spigot : no !
- Bukkit: no !

## Generation Island

Each island will be generated on a single region file! 

## Configuration

plugins/SkyFolia/config.toml
```toml
config-version = 1
verbose = false

[settings]

	[settings.player]

		[settings.player.island]

			[settings.player.island.delete]
				clear-inventory = true
				clear-enderchest = true
				clear-experience = true

[sgbd]

	[sgbd.mariadb]
		hostname = "127.0.0.1"
		password = "azerty123@"
		database = "sky_folia"
		host = "3306"
		maxPool = 5
		username = "admin"
		useSSL = false
		timeOut = 500

[worlds]

	[worlds.sky-overworld]
		environment = "NORMAL"
		portal-tp = "sky-overworld"

[island-types]

	[island-types.example]
		world = "sky-overworld"
		size = 50
		name = "example"
		max-members = 3
		schematic = "default.schem"

[config]
	max-island = 1000
```

plugins/SkyFolia/language.toml (Support MiniMessage)
```toml
config-version = 1
verbose = false

[island]

	[island.transfert]
		success = "Le nouveau propriétaire de l'ile est : %new_owner%"

	[island.create]
		finish = "Bienvenue sur votre île !"
		in-progress = "L'île est en cours de création"

	[island.generic]
		not-member = "Le joueur n'est pas membre de l'ile"
		error = "Une erreur s'est produite. Merci de contacter un administrateur."
		only-owner = "Seul le propriétaire de l'île peut faire ça."

		[island.generic.player]
			not-found = "Le joueurs est introuvable."
			no-island = "Vous n'avez pas d'île !"
			already-exist = "Vous avez déjà une île."
			not-in-island = "Vous devez être sur votre île."
			warp-create = "Votre warp : %s a été crée."

```