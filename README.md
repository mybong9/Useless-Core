# Useless-Core
Shared APIs and client UI infrastructure for the Useless-CE-Unofficial Forge 1.20.1 mod family.

Minecraft **1.20.1**, **Forge**, mod ID `useless_core`.

本仓库为该模组的独立源码仓库。请查看 [构建说明 / Building](BUILDING.md)
了解四个仓库的联合构建方式；发布更新记录见 [CHANGELOG.md](CHANGELOG.md)。

<!-- releases:start -->
## 下载 / Releases

最新可安装 JAR、对应源码标签及校验文件见 [GitHub Releases](https://github.com/mybong9/Useless-Core/releases/latest)。
安装前请查看下方依赖版本表；构建用第三方 API JAR 不属于本模组发布文件。
<!-- releases:end -->

<!-- runtime-dependencies:start -->
## 运行环境与依赖版本

对应发布版本：**1.0.19**。Minecraft **1.20.1 / Forge**，Java **17+**。
以下范围依据发布 JAR 的加载声明，其他模组仍需满足各自前置要求。

### 必需前置

| Mod ID | 版本范围 | 适用端 |
| --- | --- | --- |
| `forge` | `[47,)` | 客户端、服务端 |
| `minecraft` | `[1.20.1,)` | 客户端、服务端 |

### 可选联动

| Mod ID | 版本范围 | 适用端 |
| --- | --- | --- |
| 无额外加载依赖 | — | — |

`[x,)` 表示最低 x、无上限；`[0,)` 表示未限定最低版本。未在表中列出的其他动态联动仍可存在。

MixinExtras Forge 0.5.5 或更高由 Core 通过 Jar-in-Jar 内置，通常无需单独安装；Core 本身不要求 AE2。
<!-- runtime-dependencies:end -->

## Source and attribution

The Useless Mod/Stretcher port retains the original authors' credit and license notices.
Upstream credit is recorded in the source, existing license files and mod metadata.
Third-party resource licenses remain attached to their respective assets.

Source code, resources and build scripts are included. Game saves, runtime folders,
crash reports, local configuration, caches, credentials and third-party dependency JARs are excluded.

<!-- project-license:start -->
## 许可证 / License

本项目采用 [MIT License](LICENSE)。再分发时请保留许可证和版权声明。
第三方代码、资源及依赖仍保留各自已有的许可证和版权声明，本许可证不替代这些声明。
<!-- project-license:end -->
