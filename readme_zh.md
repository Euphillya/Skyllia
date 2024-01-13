# SkyFolia

Skyfolia是一能在folia上运行的空岛插件！该插件没有什么创新的内容，没有计划制作任何外部功能（例如任务）。该插件具有API，您可以使用它来添加功能扩展以自定义服务器。

## 下载插件

暂时不会提供插件Jar，因为插件还没有完成！一旦我发现它“可用”，我将提供插件。
但是，插件现在不是很稳定。我在业余时间开发，因此无法很快的解决所有问题。
尽管如此，请随时告诉我遇到的任何问题，例如：
- 性能问题（建议使用Spark）
- 轻微或致命错误

## 兼容插件

- Folia：你猜猜我们插件为什么叫Skyfolia（斜眼笑）
- Paper和fork：将使用Folia的调度程序API进行测试：[将一些Folia API移动到Paper以便易于兼容（＃9360）]（https://github.com/PaperMC/Paper/commit/d6d4c78e7d88f3fcd274bceab1e6b022224096ef）
- Spigot：不兼容这玩意
- Bukkit：Spigot都不兼容还看这个干啥（译者很调皮）

## 生成岛

每个岛屿将在单个区域文件上生成！

## 配置文件

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

plugins/SkyFolia/language.toml (支持 MiniMessage)
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
