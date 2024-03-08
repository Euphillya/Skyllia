## 文档

- FR_FR : https://github.com/Euphillya/Skyllia/wiki
- EN_EN : not yet available
- ZH_CN : https://github.com/Euphillya/Skyllia/tree/dev/wiki/zh_cn/all_files

## 联系我们 :

[![在Discord上与我们联系](https://discord.com/api/guilds/1196471429936463943/widget.png?style=banner2)](https://discord.gg/uUJQEB7XNN)

## Wiki

- FR_FR : https://github.com/Euphillya/Skyllia/wiki
- EN_EN : not yet available
- ZH_CN : https://github.com/Euphillya/Skyllia/tree/dev/wiki/zh_cn/all_files

## Skyllia是什么

Skyllia是一个Skyblock插件，它将在Folia上运行。 该插件几乎没有创新功能，我并不打算添加外部功能（例如任务）。
该插件有一个API，您可以使用它来添加功能扩展以自定义您的服务器。

## 下载插件

- Dev : https://github.com/Euphillya/Skyllia/actions
- Alpha : https://github.com/Euphillya/Skyllia/releases

## API

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

## 兼容的服务端

|                      服务端                      |      版本       |
|:---------------------------------------------:|:-------------:|
| [PaperMC](https://papermc.io/downloads/paper) | 1.20.1-1.20.4 |
|        [Purpur](https://purpurmc.org)         | 1.20.1-1.20.4 |
|  [Folia](https://papermc.io/software/folia)   | 1.19.4-1.20.4 |
|                    Spigot                     |     不支持~      |
|                    Bukkit                     |     不支持哦~     |

## 生成岛

每个岛屿将在单个区域文件上生成！
