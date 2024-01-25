## 联系我们 :

[![在Discord上与我们联系](https://discord.com/api/guilds/1196471429936463943/widget.png?style=banner2)](https://discord.gg/uUJQEB7XNN)

## Skyllia是什么

Skyllia是一个Skyblock插件，它将在Folia上运行。 该插件几乎没有创新功能，我并不打算添加外部功能（例如任务）。 该插件有一个API，您可以使用它来添加功能扩展以自定义您的服务器。

## 下载插件

- Dev : https://github.com/Euphillya/Skyllia/actions
- Alpha : https://github.com/Euphillya/Skyllia/releases

## 兼容的服务端

|                    服务端                     |     版本      |
|:-----------------------------------------------:|:----------------:|
|  [PaperMC](https://papermc.io/downloads/paper)  |  1.20.1-1.20.4   |
|         [Purpur](https://purpurmc.org)          |  1.20.1-1.20.4   |
|   [Folia](https://papermc.io/software/folia)    |  1.19.4-1.20.2   |
|                     Spigot                      | 不支持~  |
|                     Bukkit                      | 不支持哦~  |

## 生成岛

每个岛屿将在单个区域文件上生成！

## 配置文件

### 插件配置文件

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

plugins/Skyllia/language.toml (支持 MiniMessage)

```toml
config-version = 1
verbose = false

[island]
	[island.access]
		close = "空岛已经关闭了捏~"
		open = "空岛开放了哦~"
	[island.expel]
		not-enough-args = "命令是不是没有打完呢QWQ : /skyfolia expel <player>"
		player-not-in-island = "这个人貌似不在你的空岛上捏"
		player-failed = "怎么想都不可能把这个人踹出去嘛QWQ"
	[island.permission]
		fail-high-equals-status = "不能更改你自己的权限或者比你权限高的人的权限啦~你还想左脚踩右脚能上天?还是反客为主?"
	[island.delete]
		success = "这个空岛已经被成功的删除啦"
		only-owner = "还想删别人的空岛？做梦去吧~"
	[island.generic]
		not-member = "这个玩家不是空岛的成员哦~"
		error = "哎呀！出问题了。请联系管理员。"
		only-owner = "只有这所空岛的管理员才能干这个事情哦~"

		[island.generic.location]
			not-safe = "位置不确定啦！没有办法传送。"
		[island.generic.player]
			not-found = "有这个人吗？"
			offline = "这个人现在不在服务器里呀（恼）"
			no-island = "不如想一想，你现在到底有没有空岛？貌似没有诶！"
			permission-denied = "又想反客为主？你没权限啦！"
			already-exist = "你已经有一个空岛啦！快去看看吧~"
			command-already-execution = "命令已经在运行啦！就等一下，好不好？"
			not-in-island = "我相信，此刻你脚下的这个地方，一定是你的空岛！"
	[island.home]
		success = "回家啦~"

		[island.home.set]
			success = "搬家成功！成功设置了“家”的地方"
	[island.promote]
		fail = "玩家 %s 没有办法升级啦~"
		fail-high-equals-status = "你不能把一个玩家提升到跟你一样的权限水平或者更高的权限水平。小心别人反客为主，或者是左脚踩右脚能上天。"
		success = "玩家 %s 升级成功啦！"
		not-enough-args = "我相信你的命令应该没有打完吧 : /skyfolia promote <member>"
	[island.demote]
		fail = "玩家 %s 不能再降级啦！"
		fail-high-equals-status = "别人等级比你高，你当然不能降级别人了。 "
		success = "玩家 %s 降级成功"
		not-enough-args = "命令没有打完 : /skyfolia demote <member>"
	[island.kick]
		fail-high-equals-status = "你不能踢掉一个跟你同级别的或者是比你权限高的人"
		success = "已经不再是岛上的一个人啦~"
		failed = "没有办法踢掉这个玩家呢。如果问题依然存在，联系管理员啊QWQ"
	[island.biome]
		change-in-progress = "已经正在更改生物群落了，这需要一定的时间。当然如果我做完了的话，我会通知主人喵~"
		success = "这个区块的生物群落已经变化完了喵~"
		only-island = "你得在一个岛上我才能执行这个命令啊QWQ"
		not-enough-args = "明明又没有打完 : /skyfolia biome <biome>"
		biome-not-exist = "生物群系 %s 不存在！"
	[island.leave]
		success = "已经成功离开岛屿了，下次见~"
		failed = "斯~出不来了！如果问题依然存在，去联系管理员吧。"
		he-is-owner = "你自己就是主人，你还想离开？"
	[island.permissions]
		permissions-invalid = "这个权限怎么都感觉不存在呀？"
		role-invalid = "输入的值无效，可以是: <OWNER/CO_OWNER/MODERATOR/MEMBER/VISITOR/BAN>"
		not-enough-args = "明明没有打完 : /skyfolia permission <island/commands/inventory> <OWNER/CO_OWNER/MODERATOR/MEMBER/VISITOR/BAN> <PERMISSION_NAME> <true/false>"
		permission-type-invalid = "类型无效！可以是 : <island/commands/inventory>"

		[island.permissions.update]
			success = "权限已更新！"
			failed = "更改失败！"
	[island.transfert]
		success = "空岛的新主人是 : %new_owner% 喵！"
	[island.create]
		type-no-exist = "这个类型的岛屿不存在啦~"
		finish = "创建成功。欢迎来到你的岛屿！"
		error = "这中间好像出了点问题，要不咱们再试试？如果依然有问题就联系管理员吧。"
		in-progress = "正在创建中，可能需要一点点时间。"
		schem-no-exist = "这个空岛的文件找不到啦~请联系管理员"
	[island.invite]
		already-on-an-island = "已经在岛上啦~"
		not-enough-args = "命令没打完 : /skyfolia invite <add/accept/decline> <player/island_owner>"

		[island.invite.add]
			notification-player = "玩家 %player_invite% 邀请你访问他的空岛！要想同意可以输入指令 : /skyfolia invite accept %player_invite%. 不想同意可以输入指令 : /skyfolia invite decline %player_invite%"
			pending = "已经向玩家 %s 发送了邀请。请等待他的回应。"
			not-enough-args = "命令没有打完，你需要指定空岛哦 : /skyfolia invite add <player>"
		[island.invite.decline]
			owner-not-island = "没有找到玩家 %s 的空岛"
			not-enough-args = "命令没有打完，你需要指定空岛哦 : /skyfolia invite decline <island_owner>"
		[island.invite.accept]
			owner-not-island = "找不到玩家 %s 的空岛了"
			success = "你现在是岛上的一位成员啦！欢迎~"
			max-member-exceeded = "这个空岛满人了QWQ"
			not-enough-args = "命令没有打完，你需要指定你想要加入的岛屿哦: /skyfolia invite accept <island_owner>"
	[island.visit]
		island-not-open = "这个空岛没有开放访问哦"
		success = "你已经被传送到了玩家 %player% 的岛屿"
		player-not-island = "这个玩家貌似没有空岛"
		not-enough-args = "我觉得你还是需要指定一下你到底想访问谁的岛屿 : /skyfolia visit <player>"
	[island.warp]
		success = "传送点 : %s 创建成功"
		not-enough-args = "创建个传送点，你也得说一下你这个传送点叫啥呀？: /skyfolia <set/del>warp <warp_name>"

		[island.warp.teleport]
			not-exist = "你这个传送点好像不存在呀？"
			success = "传送成功~"
		[island.warp.delete]
			success = "传送点删除成功~"
			can-not-delete-home = "不能删除home这个传送点哦"

```
