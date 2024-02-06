以下是 Skyllia 插件的常规命令：

**创建和管理岛屿：**

- `skyllia.island.command.create`：创建一个岛屿。
- `skyllia.island.command.create.<type>`：创建特定类型的岛屿。
- `skyllia.island.command.delete`：删除自己的岛屿。
- `skyllia.island.command.sethome`：设置自己岛屿的出生点和访问点。
- `skyllia.island.command.biome`：选择自己岛屿区块的生物群系。
- `skyllia.island.command.setwarp`：在自己的岛屿上设置一个 传送点 并命名。
- `skyllia.island.command.delwarp`：从自己的岛屿上删除一个 传送点。

**成员和访客管理：**

- `skyllia.island.command.expel`：将访客从自己的岛屿上驱逐。
- `skyllia.island.command.invite`：邀请玩家成为自己岛屿的成员。
- `skyllia.island.command.kick`：将低级别成员从自己的岛屿上排除。
- `skyllia.island.command.permission`：修改低级别成员的权限。
- `skyllia.island.command.promote`：将成员晋升到低于自己的级别。
- `skyllia.island.command.demote`：降级成员，除非他的级别相等或更高。

**导航和访问：**

- `skyllia.island.command.visit`：访问其他玩家的岛屿。
- `skyllia.island.command.home`：传送到自己岛屿的出生点。
- `skyllia.island.command.warp`：传送到自己岛屿上的 传送点。
- `skyllia.island.command.leave`：离开自己所在的岛屿（岛主无法使用）。
- `skyllia.island.command.access`：打开或关闭自己岛屿的访问权限。

**玩家岛屿的特定权限：**

- 修改权限的命令：`/skyllia permissions <type> <ban/visitor/member/moderator/co_owner/owner> <permissions> <true/false>`。

<type>中的内容可以是

- DEMOTE：允许玩家根据等级降级成员。
- PROMOTE：允许玩家根据等级晋升成员。
- KICK：允许玩家根据等级排除成员。
- ACCESS：允许玩家打开或关闭岛屿。
- SET_HOME：允许玩家重新定义岛屿的出生点。
- INVITE：允许玩家邀请没有岛屿的成员。
- SET_BIOME：允许玩家更改岛屿区块的生物群系。
- SET_WARP：允许玩家在岛屿上创建一个 传送点。
- DEL_WARP：允许玩家从岛屿上删除一个 传送点。
- TP_WARP：允许玩家传送到岛屿上的一个 传送点。
- EXPEL：允许玩家将一个人从岛屿上驱逐。
- MANAGE_PERMISSION：允许玩家管理不同等级的权限。
