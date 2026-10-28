# Useless-Core
Shared APIs and client UI infrastructure for the Useless-CE-Unofficial Forge 1.20.1 mod family.

Minecraft **1.20.1**, **Forge**, mod ID `useless_core`, current source version **1.0.15**.

本仓库为该模组的独立源码仓库。请查看 [构建说明 / Building](BUILDING.md)
了解四个仓库的联合构建方式；发布更新记录见 [CHANGELOG.md](CHANGELOG.md)。

<!-- releases:start -->
## 下载 / Releases

最新可安装 JAR、对应源码标签及校验文件见 [GitHub Releases](https://github.com/mybong9/Useless-Core/releases/latest)。
安装前请查看下方依赖版本表；构建用第三方 API JAR 不属于本模组发布文件。
<!-- releases:end -->

<!-- runtime-dependencies:start -->
## 运行环境与依赖版本

对应发布版本：**1.0.15**。本工程目标为 **Minecraft 1.20.1 / Forge**，使用 **Java 17+**（编译目标为 Java 17）。
以下依赖范围直接依据该版本发布 JAR 的 `META-INF/mods.toml`，不是编译时所用依赖版本。

### 必需前置

| 依赖 | Mod ID | 最低版本 / 加载范围 | 适用端 |
| --- | --- | --- | --- |
| Minecraft Forge | `forge` | 47 或更高| 客户端、服务端 |
| Minecraft | `minecraft` | 1.20.1  | 客户端、服务端 |

### 可选联动

该模块没有在加载声明中设置额外的可选模组版本要求。

可选联动不要求安装；安装时须符合上表版本范围，并安装该联动模组自身要求的前置。未声明最低版本的其他动态联动不在此处编造版本下限。

### 内置运行库

**MixinExtras Forge 0.5.5 或更高，通过 Jar-in-Jar 内置并选择兼容版本，通常无需另外安装；它不是名为 `mixinextras` 的必装 Forge 模组。Core 本身没有声明 AE2 为必需前置。

Minecraft 的宽松加载声明不代表本工程已支持其他 Minecraft 主版本。开发构建及本地编译 API 清单见 [BUILDING.md](BUILDING.md)。
<!-- runtime-dependencies:end -->

## Source and attribution

The Useless Mod/Stretcher port retains the original authors' credit and license notices.
Upstream credit is recorded in the source, existing license files and mod metadata.
Third-party resource licenses remain attached to their respective assets.

Source code, resources and build scripts are included. Game saves, runtime folders,
crash reports, local configuration, caches, credentials and third-party dependency JARs are excluded.
