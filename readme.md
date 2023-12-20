## Plugin Skyblock for Folia

SkyFolia is a Skyblock plugin that will essentially run on Folia. 
The plugin will have very little innovative functionality, and it's not my intention at all that there should be external features (like quests, for example). 
The plugin has an API that you can use to add feature extensions to customize your server.

## Generation Island

Each island will be generated on a single region file! 

## Configuration

```toml
config-version = 1
verbose = false

[worlds]
	[worlds.skyfolia_world]
		environment = "NORMAL"
	[worlds.skyfolia_nether]
		environment = "NETHER"

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

```