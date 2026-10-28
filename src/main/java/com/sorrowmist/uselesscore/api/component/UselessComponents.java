package com.sorrowmist.uselesscore.api.component;

import com.mojang.serialization.Codec;
import com.sorrowmist.uselesscore.api.enums.tool.EnchantMode;
import com.sorrowmist.uselesscore.api.enums.tool.ConstructionWandCoreMode;
import com.sorrowmist.uselesscore.api.enums.tool.ToolTypeMode;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import com.sorrowmist.uselesscore.api.network.codec.CoreByteBufCodecs;
import com.sorrowmist.uselesscore.api.network.codec.CoreStreamCodec;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;
import com.sorrowmist.uselesscore.api.component.CoreDataComponentType;
import com.sorrowmist.uselesscore.api.component.CoreDataComponentHolder;
import net.minecraft.resources.ResourceLocation;

public final class UselessComponents {
/**
     * 附魔模式组件（EnchantMode）
     * 用于在物品上存储当前的附魔模式（枚举类型 EnchantMode）
     */
    public static final CoreDataComponentHolder<EnchantMode> EnchantModeComponent =
            register("enchant_mode", builder ->
                    builder
                            // 持久化存储：使用字符串 Codec，将枚举转换为字符串保存（保存到 NBT/文件时）
                            // valueOf 用于从字符串转为枚举，Enum::name 用于从枚举转为字符串
                            .persistent(Codec.STRING.xmap(EnchantMode::valueOf, Enum::name))
                            // 网络同步：客户端与服务端同步时使用枚举的原生读写方式
                            .networkSynchronized(CoreStreamCodec.of(
                                    FriendlyByteBuf::writeEnum,                     // 写入枚举
                                    buf -> buf.readEnum(EnchantMode.class)          // 读取枚举
                            ))
            );

    /**
     * 当前工具类型组件（CurrentToolType）
     * 用于在物品上存储当前选中的工具类型（枚举类型 ToolTypeMode）
     * 例如多功能工具在不同模式（镐、斧、铲等）之间切换时使用
     */
    public static final CoreDataComponentHolder<ToolTypeMode> CurrentToolTypeComponent =
            register("tool_type", builder ->
                    builder.persistent(Codec.STRING.xmap(ToolTypeMode::valueOf, Enum::name))
                            .networkSynchronized(CoreStreamCodec.of(
                                    FriendlyByteBuf::writeEnum,
                                    buf -> buf.readEnum(ToolTypeMode.class)
                            ))
            );

    public static final CoreDataComponentHolder<Boolean> WrenchTagEnabledComponent =
            register("wrench_tag_enabled", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    public static final CoreDataComponentHolder<Boolean> ConstructionWandEnabledComponent =
            register("construction_wand_enabled", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    public static final CoreDataComponentHolder<ConstructionWandCoreMode> ConstructionWandCoreComponent =
            register("construction_wand_core", builder ->
                    builder.persistent(Codec.STRING.xmap(ConstructionWandCoreMode::valueOf, Enum::name))
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeEnum,
                                   buf -> buf.readEnum(ConstructionWandCoreMode.class)
                           ))
            );

    /**
     * 土壤右键模式组件（BeefFarmlandMode）
     * 造化杖同时具备铲子与锄头能力，右键泥土/草方块时谁先生效由此决定：
     * false = 铲子优先（变为草径），true = 锄头优先（变为耕地）。
     */
    public static final CoreDataComponentHolder<Boolean> BeefFarmlandModeComponent =
            register("beef_farmland_mode", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    /**
     * 「顺手收菜」组件（BeefCropHarvest）
     * true = 右键成熟作物时由造化杖自身执行收获，并将种子保留于耕地（作物重置为 0 龄），
     * 以避免整合包的右键收菜功能将作物连根拔起。
     */
    public static final CoreDataComponentHolder<Boolean> BeefCropHarvestComponent =
            register("beef_crop_harvest", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    /**
     * 剪刀功能组件（BeefShears）
     * true = 造化杖具备剪刀能力：可对羊、哞菇等 IShearable 实体剪毛/剪掉落，
     * 也向依赖 ItemAbility 的模组暴露 DEFAULT_SHEARS_ACTIONS。
     */
    public static final CoreDataComponentHolder<Boolean> BeefShearsComponent =
            register("beef_shears", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    /**
     * 打火石功能组件（BeefFlintAndSteel）
     * true = 造化杖具备打火石能力：右键可点燃营火/蜡烛，或在可放置火的位置点火。
     */
    public static final CoreDataComponentHolder<Boolean> BeefFlintAndSteelComponent =
            register("beef_flint_and_steel", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    /**
     * 自动熔炼组件（AutoSmelt）
     * true = 挖掘产生的掉落物先按原版烹饪配方（熔炉/高炉/烟熏炉）炼一遍再入包。
     * 只影响挖掘掉落，不改变方块本身的行为。
     */
    public static final CoreDataComponentHolder<Boolean> AutoSmeltComponent =
            register("auto_smelt", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    /**
     * 增强连锁挖矿模式组件（EnhancedChainMiningMode）
     * 用于在物品上存储是否启用增强连锁挖掘（布尔类型）
     * true = 启用增强连锁挖掘，false = 使用普通连锁挖掘
     */
    public static final CoreDataComponentHolder<Boolean> EnhancedChainMiningComponent =
            register("chain_mining", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    /**
     * 强制挖掘组件（ForceMining）
     * 用于在物品上存储是否启用强制挖掘功能（布尔类型）
     */
    public static final CoreDataComponentHolder<Boolean> ForceMiningComponent =
            register("force_mining", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    /**
     * AE存储优先组件（AEStoragePriority）
     * 用于在物品上存储是否启用AE存储优先功能（布尔类型）
     */
    public static final CoreDataComponentHolder<Boolean> AEStoragePriorityComponent =
            register("ae_storage_priority", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    /**
     * AE 连接模式组件（AeNetworkConnect）
     * 开启后右键一台「拥有 AE 网格节点」的机器，把它的节点接入工具绑定无线访问点所在的那张网。
     * 与「顺手收菜 / 时间加速 / 建筑魔杖」互斥（同一时刻仅允许一项占用右键）。
     */
    public static final CoreDataComponentHolder<Boolean> AeNetworkConnectComponent =
            register("ae_network_connect", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    /**
     * 匠心仪式挎包模式（RitualSatchel）
     * 开启后右键已用魔典预览过的五芒星，可直接从绑定的 AE 网络取用方块摆放整座仪式。
     * 仅在安装了 occultism 时才会出现在模式轮盘里。
     */
    public static final CoreDataComponentHolder<Boolean> BeefRitualSatchelComponent =
            register("beef_ritual_satchel", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    /**
     * Ex Deorum 钩子模式（ExDeorumCrook）
     * true = 破坏方块时按 Ex Deorum 的钩子配方额外产出掉落（如树叶的蚕、线）。
     * 仅在安装了 exdeorum 时该模式才会出现在模式轮盘里。
     */
    public static final CoreDataComponentHolder<Boolean> ExDeorumCrookComponent =
            register("exdeorum_crook", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    /**
     * Ex Deorum 锤子模式（ExDeorumHammer）
     * true = 破坏方块时按 Ex Deorum 的锤子配方改写掉落，替代方块原有掉落。
     * 仅在安装了 exdeorum 时该模式才会出现在模式轮盘里。
     */
    public static final CoreDataComponentHolder<Boolean> ExDeorumHammerComponent =
            register("exdeorum_hammer", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    /**
     * Ex Deorum 压缩锤模式（ExDeorumCompressedHammer）
     * true = 破坏方块时按 Ex Deorum 的压缩锤配方改写掉落。压缩锤配方与普通锤配方是两套
     * 独立数据，因此单列为一项开关。仅在安装了 exdeorum 时该模式才会出现在模式轮盘里。
     */
    public static final CoreDataComponentHolder<Boolean> ExDeorumCompressedHammerComponent =
            register("exdeorum_compressed_hammer", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    public static final CoreDataComponentHolder<Boolean> ForceKillEnabledComponent =
            register("force_kill_enabled", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    public static final CoreDataComponentHolder<Boolean> BeefTimeAccelerationEnabledComponent =
            register("beef_time_acceleration_enabled", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    public static final CoreDataComponentHolder<Boolean> BeefInvulnerabilityEnabledComponent =
            register("beef_invulnerability_enabled", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    public static final CoreDataComponentHolder<Boolean> BeefAdvancedStealthEnabledComponent =
            register("beef_advanced_stealth_enabled", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    public static final CoreDataComponentHolder<Boolean> BeefCaptureEnabledComponent =
            register("beef_capture_enabled", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    public static final CoreDataComponentHolder<Boolean> BeefMalumSpiritEnabledComponent =
            register("beef_malum_spirit_enabled", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    public static final CoreDataComponentHolder<Boolean> BeefMysticalAgricultureEnabledComponent =
            register("beef_mystical_agriculture_enabled", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    public static final CoreDataComponentHolder<Boolean> BeefBeheadingEnabledComponent =
            register("beef_beheading_enabled", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    public static final CoreDataComponentHolder<Boolean> BeefTeleportEnabledComponent =
            register("beef_teleport_enabled", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    /**
     * 范围伤害组件（BeefAoeDamage）
     * 用于在物品上存储是否启用范围伤害（布尔类型）
     */
    public static final CoreDataComponentHolder<Boolean> BeefAoeDamageEnabledComponent =
            register("beef_aoe_damage_enabled", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    /**
     * 范围磁力吸附组件（BeefMagnet）
     * 用于在物品上存储是否启用击杀后的范围磁力吸附（布尔类型）
     */
    public static final CoreDataComponentHolder<Boolean> BeefMagnetEnabledComponent =
            register("beef_magnet_enabled", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    /**
     * 杀戮光环组件（BeefKillAura）
     * 用于在物品上存储是否启用杀戮光环（布尔类型）
     * true = 手持造化杖时每 20 tick 对「范围伤害」配置范围内生物结算一次攻击伤害
     */
    public static final CoreDataComponentHolder<Boolean> BeefKillAuraComponent =
            register("beef_kill_aura", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    /**
     * 保护名单模式组件（BeefProtectMode）
     * true = 开启：暂停杀戮光环与范围伤害的结算，方便玩家从容地 Ctrl/Shift 右键添加保护名单，
     * 不会出现「怪先被光环打死、来不及加名单」的情况。
     */
    public static final CoreDataComponentHolder<Boolean> BeefProtectModeComponent =
            register("beef_protect_mode", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    public static final CoreDataComponentHolder<GlobalPos> WIRELESS_LINK_TARGET = register(
            "wireless_link_target",
            builder ->
                    builder.persistent(GlobalPos.CODEC)
                           .networkSynchronized(CoreByteBufCodecs.GLOBAL_POS)
    );

    /**
     * 熔炉数据组件（FurnaceData）
     * 用于存储高级合金炉的完整状态到物品中
     * 包含阶级、物品栏、流体、能量等所有数据
     */
    public static final CoreDataComponentHolder<FurnaceDataComponent> FURNACE_DATA =
            register("furnace_data", builder -> builder
                    .persistent(FurnaceDataComponent.CODEC)
                    .networkSynchronized(FurnaceDataComponent.STREAM_CODEC));

    public static final CoreDataComponentHolder<OmniversalPatternData> OMNIVERSAL_PATTERN_DATA =
            register("omniversal_pattern_data", builder -> builder
                    .persistent(OmniversalPatternData.CODEC)
                    .networkSynchronized(OmniversalPatternData.STREAM_CODEC));
    public static final CoreDataComponentHolder<PatternConverterData> PATTERN_CONVERTER_DATA =
            register("pattern_converter_data", builder -> builder
                    .persistent(PatternConverterData.CODEC)
                    .networkSynchronized(PatternConverterData.STREAM_CODEC));
    public static final CoreDataComponentHolder<GlobalPos> PATTERN_CONVERTER_LINK_TARGET = register(
            // Keep the serialized ID so existing converter links remain valid.
            "mold_hub_link_target",
            builder -> builder
                    .persistent(GlobalPos.CODEC)
                    .networkSynchronized(CoreByteBufCodecs.GLOBAL_POS));
    public static final CoreDataComponentHolder<MultiblockRecoveryData> MULTIBLOCK_RECOVERY_DATA =
            register("multiblock_recovery_data", builder -> builder
                    .persistent(MultiblockRecoveryData.CODEC)
                    .networkSynchronized(MultiblockRecoveryData.STREAM_CODEC));
    public static final CoreDataComponentHolder<MultiblockPartData> MULTIBLOCK_PART_DATA =
            register("multiblock_part_data", builder -> builder
                    .persistent(MultiblockPartData.CODEC)
                    .networkSynchronized(MultiblockPartData.STREAM_CODEC));
    public static final CoreDataComponentHolder<ExternalInventoryReference> EXTERNAL_INVENTORY_REFERENCE =
            register("external_inventory_reference", builder -> builder
                    .persistent(ExternalInventoryReference.CODEC)
                    .networkSynchronized(ExternalInventoryReference.STREAM_CODEC));
    public static final CoreDataComponentHolder<PassiveHatchSettings> PASSIVE_HATCH_SETTINGS =
            register("passive_hatch_settings", builder -> builder
                    .persistent(PassiveHatchSettings.CODEC)
                    .networkSynchronized(PassiveHatchSettings.STREAM_CODEC));
    public static final CoreDataComponentHolder<Long> SMART_DOUBLING_OPERATIONS =
            register("smart_doubling_operations", builder -> builder
                    .persistent(Codec.LONG)
                    .networkSynchronized(CoreStreamCodec.of(
                            FriendlyByteBuf::writeLong,
                            FriendlyByteBuf::readLong)));
    /** The Occultism pentacles recorded on an imprinted ritual blueprint. */
    public static final CoreDataComponentHolder<RitualBlueprintPentacles> RITUAL_BLUEPRINT_PENTACLE =
            register("ritual_blueprint_pentacle", builder -> builder
                    .persistent(RitualBlueprintPentacles.CODEC)
                    .networkSynchronized(RitualBlueprintPentacles.STREAM_CODEC));

    /**
     * 催熟模式组件（BeefRipen）
     * true = 右键时一键催熟目标：可骨粉方块循环施加骨粉直到长满，
     * 幼年动物直接催至成年；潜行右键把这次交互让给其它模组。
     */
    public static final CoreDataComponentHolder<Boolean> BeefRipenComponent =
            register("beef_ripen", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    /**
     * 连点模式组件（BeefAutoClick）—— <b>历史遗留，已不再读写</b>。
     *
     * <p>连点开关现为客户端会话级状态（{@code BeefAutoClicker}），不绑定物品。
     * 这里保留注册仅为了让旧存档里已带该组件的物品能正常反序列化（注销会刷未知组件警告），
     * 任何新代码都不应再读它。</p>
     */
    public static final CoreDataComponentHolder<Boolean> BeefAutoClickComponent =
            register("beef_auto_click", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    /**
     * 强制生长组件（BeefForceGrow）
     * true = 催熟时额外用「随机刻」推进方块，可作用于甘蔗、仙人掌、竹子等
     * 原版骨粉无效的方块。代价是它不挑方块——树叶枯萎、火蔓延、雪冰融化、
     * 耕地退化这些随机刻行为同样会被推，是否开启由玩家自行抉择。
     */
    public static final CoreDataComponentHolder<Boolean> BeefForceGrowComponent =
            register("beef_force_grow", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    /**
     * 生物加速模式组件（BeefEntityTimeAcceleration）
     * true = 潜行右键生物时加速其「计时器」类数值：幼年成长 / 繁殖后冷却、
     * 鸡下蛋间隔、羊剪毛后长毛、村民补货、蜜蜂采蜜、海龟下蛋。
     * 只改数值，<b>不跑 AI / 寻路 / 移动 / 物理</b>。
     *
     * <p>该模式与「保护名单」的 Shift 手势互斥：开启后 Shift+右键生物被解释为
     * 加速，而不是切换保护名单（Ctrl 按种类的手势不受影响）。</p>
     */
    public static final CoreDataComponentHolder<Boolean> BeefEntityTimeAccelerationComponent =
            register("beef_entity_time_acceleration", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    /**
     * 无线物流模式组件（StaffLink）
     * true = 开启「无线物流」：潜行右键容器方块可把它绑进/解绑出造化杖的物流网络，
     * 网络内的搬运规则在独立界面里配置。真正的搬运由服务端引擎执行。
     */
    public static final CoreDataComponentHolder<Boolean> StaffLinkEnabledComponent =
            register("beef_wireless_logistics", builder ->
                    builder.persistent(Codec.BOOL)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeBoolean,
                                   FriendlyByteBuf::readBoolean
                           ))
            );

    /**
     * <b>旧格式</b>：造化杖曾经把物流网络列表挂在自己身上。
     *
     * <p>网络归属已经改成跟着<b>玩家/队伍</b>走（存在服务端的 {@code StaffLinkSavedData} 归属
     * 登记里），所以这两个组件现在只用于<b>迁移</b>：玩家登录（或每 20 tick 的活跃刷新）时把
     * 杖上残留的列表迁到归属者名下，然后就地清空。新代码不要再读写它们。</p>
     */
    public static final CoreDataComponentHolder<List<UUID>> STAFF_LINK_NETWORKS =
            register("staff_link_networks", builder ->
                    builder.persistent(UUIDUtil.CODEC.listOf())
                           .networkSynchronized(CoreByteBufCodecs.uuidList())
            );

    /** <b>旧格式</b>：{@link #STAFF_LINK_NETWORKS} 里当前生效的下标。只用于迁移。 */
    public static final CoreDataComponentHolder<Integer> STAFF_LINK_ACTIVE =
            register("staff_link_active", builder ->
                    builder.persistent(Codec.INT)
                           .networkSynchronized(CoreStreamCodec.of(
                                   FriendlyByteBuf::writeVarInt,
                                   FriendlyByteBuf::readVarInt
                           ))
            );

    // 私有构造器，防止外部实例化（该类仅用于注册静态组件）
    private UselessComponents() {}

    /**
     * 通用注册方法
     *
     * @param name            组件的注册名称（资源位置路径部分）
     * @param builderOperator 对 DataComponentType.Builder 的操作函数
     * @param <T>             组件存储的数据类型
     * @return DeferredHolder，用于延迟获取已注册的 DataComponentType
     */
    private static <T> CoreDataComponentHolder<T> register(
            String name,
            UnaryOperator<CoreDataComponentType.Builder<T>> builderOperator) {
        // 通过 DeferredRegister 注册，实际构建在模组加载时进行
        return new CoreDataComponentHolder<>(builderOperator.apply(CoreDataComponentType.builder(new ResourceLocation("useless_mod", name))).build());
    }

    /**
     * 初始化方法：在模组事件总线上注册 DeferredRegister
     * 通常在模组主类的构造函数中调用：UselessComponents.init(modEventBus);
     * @param modEventBus 模组的事件总线（通常是 MOD 事件总线）
     */
    public static void init(IEventBus modEventBus) {
        // Custom data remains NBT-backed in Forge 1.20.1 and has no vanilla registry lifecycle.
    }
}
