# Waystones-X MITE

Waystones-X 1.0.15 的 Minecraft 1.6.4-MITE / FishModLoader 移植版。

- 上游项目：`JackOfNoneTrades/WaystonesFork`
- 移植基准：Waystones-X `1.0.15`
- 基准提交：`3bc7742a74ff66de8032db729e8a5bca55565c1d`
- 兼容旧版：保留原普通石碑方块 ID 与既有 NBT 数据

## 主要功能

- 七种双格石碑：普通、砂岩、苔石、石砖、苔石砖、下界砖、末地石
- 放置命名、重名校验、再次命名、公共石碑、所有者和无敌石碑
- 首次右键激活，再次右键打开传送菜单
- 搜索、名称/距离排序、置顶、遗忘、当前石碑重命名
- 玩家独立激活列表、最后绑定和置顶数据，随玩家 NBT 持久化
- 公共石碑只保存到当前世界 NBT，不从共享配置导入，切换或新建世界不会串档
- 返程卷轴确认界面、传送石、背包传送按钮和冷却
- 固定经验费用、距离费用和跨维度附加费用，全部由服务端校验和扣除
- 跨维度传送预加载目标区块，不生成下界传送门，并寻找安全落点
- 铭牌、粒子、声音、传送特效、发光冷却进度和平面/3D 物品图标
- 下界石碑动态岩浆覆盖、末地石碑动态传送门覆盖
- Modernity 内置可选资源包
- 村庄、神殿、要塞、下界堡垒、末地柱和出生点世界生成规则
- 返程卷轴结构战利品
- JourneyMap、Xaero's Minimap、Village Names 和 WAILA 可选兼容
- 英文和简体中文文本

## 操作

- 放置石碑：打开命名界面
- 首次右键已命名石碑：激活
- 再次右键已激活石碑：打开目的地列表
- 按住 Shift 并悬停当前石碑名称：重命名或遗忘当前私人石碑
- 按住 Shift 并悬停目的地：置顶、取消置顶或遗忘私人石碑
- 左侧图标：切换排序、打开配置和查看操作提示
- 长按返程卷轴：打开返回确认界面
- 长按传送石：打开目的地列表

创造物品栏右侧的 `>` 可翻到第二页；“传送石碑”独立标签集中收录七种石碑、返程卷轴和传送石，左侧 `<` 返回原版标签页。

## 配置

首次启动后生成 `config/waystones.properties`。主要配置包括：

- `interDimension` / `globalInterDimension`：私人/公共石碑跨维度传送
- `warpStoneCooldownSeconds` / `globalNoCooldown`：传送冷却
- `xpBaseCost` / `xpBlocksPerLevel` / `xpCrossDimCost`：经验费用
- `showNametag` / `showCooldownOnWaystone` / `overlayClipBounds`：显示效果
- `flatInventoryIcon` / `menusPauseGame`：客户端界面
- `disableWaystoneDrops` / `invulnerableWaystones`：石碑破坏行为
- `sandyWaystonePathBlocks` / `mossyWaystonePathBlocks`：村庄自动变体
- `structureWaystoneRules`：结构生成概率、变体、固定名称、公共状态、维度和群系白名单
- `journeyMap*` / `xaeroMinimap*` / `villageNamesCompat`：可选模组兼容

## MITE 适配说明

Waystones-X 原版面向 Forge 1.7.10，本项目面向 MITE 1.6.4，底层 API 不同。末地柱使用生成器 mixin 原位注入；村庄、神殿、要塞和堡垒使用结构定位后的安全放置，以避开 MITE 中包私有结构组件与级联区块生成问题。已完成地形的区块在服务端加载阶段立即处理石碑生成，避免玩家进世界后才看到出生点石碑突然出现。目的地菜单使用 Waystones-X 1.0.15 的原始 `menu.png`、尺寸、滚动列表和 Shift 悬停交互，并适配 MITE 的渲染与输入 API。

## 环境

- Minecraft 1.6.4-MITE
- FishModLoader 3.4.0 或更高版本
- RustedIronCore 1.5.0 或更高版本
- Java 17
- WAILA 2.0.6（可选）

## 构建

```powershell
.\gradlew.bat clean build
```

构建产物位于 `build/libs/`。

## 许可与署名

本项目基于 BlayTheNinth 的 Waystones 和 jack 维护的 Waystones-X：

- 原 Waystones 代码：MIT License，Copyright 2016 BlayTheNinth
- Waystones-X 新增部分及本移植：LGPLv3
- 修复贡献：kuzuanpa、brandyyn
- 新纹理与 Modernity 资源包：DarkBum
- 中文翻译：Omgise
- 物品图标：JoeCreates，CC-BY-SA 3.0

完整许可证见 `LICENSE` 与 `LICENSE-MIT`。
