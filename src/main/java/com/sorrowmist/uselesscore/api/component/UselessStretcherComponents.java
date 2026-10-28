package com.sorrowmist.uselesscore.api.component;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import com.sorrowmist.uselesscore.api.network.codec.CoreStreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import com.sorrowmist.uselesscore.api.network.codec.CoreByteBufCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import com.sorrowmist.uselesscore.api.component.CoreDataComponentType;
import com.sorrowmist.uselesscore.api.component.CoreDataComponentHolder;

/** Data components for the wondrous staff's time-acceleration control. */
public final class UselessStretcherComponents {
/** Selected acceleration multiplier. 0 = off; valid presets range from 2 to 1024. */
    public static final CoreDataComponentHolder<Integer> WONDROUS_STAFF_SPEED =
            register("wondrous_staff_speed", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(CoreStreamCodec.of(FriendlyByteBuf::writeVarInt, FriendlyByteBuf::readVarInt)));

    /** Opt-in timer-only entity acceleration. Old stacks retain complete-tick behavior. */
    public static final CoreDataComponentHolder<Boolean> ENTITY_TIMER_MODE =
            register("entity_timer_mode", builder -> builder.persistent(Codec.BOOL)
                    .networkSynchronized(CoreStreamCodec.of(FriendlyByteBuf::writeBoolean, FriendlyByteBuf::readBoolean)));
    public static final CoreDataComponentHolder<Integer> ENTITY_TIMER_SPEED =
            integerComponent("entity_timer_speed");

    /** When true, the acceleration never expires. */
    public static final CoreDataComponentHolder<Boolean> WONDROUS_STAFF_PERMANENT =
            register("wondrous_staff_permanent", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(CoreStreamCodec.of(FriendlyByteBuf::writeBoolean, FriendlyByteBuf::readBoolean)));

    /**
     * Staff acceleration mode: 0 = normal, 1 = permanent, 2 = permanent without idle throttling.
     * The separate legacy boolean above is retained so pre-mode stacks can still be read.
     */
    public static final CoreDataComponentHolder<Integer> WONDROUS_STAFF_MODE =
            register("wondrous_staff_mode", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(CoreStreamCodec.of(FriendlyByteBuf::writeVarInt, FriendlyByteBuf::readVarInt)));

    /** Enables the staff's summon catalog and summon action. */
    public static final CoreDataComponentHolder<Boolean> WONDROUS_STAFF_SUMMON_ENABLED =
            register("wondrous_staff_summon_enabled", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(CoreStreamCodec.of(FriendlyByteBuf::writeBoolean, FriendlyByteBuf::readBoolean)));

    /** Enables Shift+right-click refresh for loot-table containers and container minecarts. */
    public static final CoreDataComponentHolder<Boolean> WONDROUS_STAFF_LOOT_REFRESH =
            register("wondrous_staff_loot_refresh", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(CoreStreamCodec.of(FriendlyByteBuf::writeBoolean, FriendlyByteBuf::readBoolean)));

    /** Last-used virtual bookshelf settings, copied onto a table when selected. */
    public static final CoreDataComponentHolder<Integer> APOTHEOSIS_ETERNA =
            integerComponent("apotheosis_eterna");
    public static final CoreDataComponentHolder<Integer> APOTHEOSIS_QUANTA =
            integerComponent("apotheosis_quanta");
    public static final CoreDataComponentHolder<Integer> APOTHEOSIS_ARCANA =
            integerComponent("apotheosis_arcana");
    public static final CoreDataComponentHolder<Integer> APOTHEOSIS_CLUES =
            integerComponent("apotheosis_clues");
    public static final CoreDataComponentHolder<Boolean> APOTHEOSIS_TABLE_SELECTION =
            register("apotheosis_table_selection", builder -> builder.persistent(Codec.BOOL)
                    .networkSynchronized(CoreStreamCodec.of(FriendlyByteBuf::writeBoolean, FriendlyByteBuf::readBoolean)));
    /** Whether Shift+right-click places an independent range instead of accelerating one target. */
    public static final CoreDataComponentHolder<Boolean> RANGE_PLACEMENT_MODE =
            register("range_placement_mode", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(CoreStreamCodec.of(FriendlyByteBuf::writeBoolean, FriendlyByteBuf::readBoolean)));

    /** While true, right-clicking a machine only toggles its type in the range filter list. */
    public static final CoreDataComponentHolder<Boolean> RANGE_FILTER_MARKING_MODE =
            register("range_filter_marking_mode", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(CoreStreamCodec.of(FriendlyByteBuf::writeBoolean, FriendlyByteBuf::readBoolean)));

    /** False marks the acceleration list; true marks the idle-sleep list. */
    public static final CoreDataComponentHolder<Boolean> RANGE_MARK_SLEEP_LIST =
            register("range_mark_sleep_list", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(CoreStreamCodec.of(FriendlyByteBuf::writeBoolean, FriendlyByteBuf::readBoolean)));

    /** Counts from the last placed or edited range, shown beside the two list controls. */
    public static final CoreDataComponentHolder<Integer> RANGE_ACCELERATION_MARK_COUNT =
            integerComponent("range_acceleration_mark_count");
    public static final CoreDataComponentHolder<Integer> RANGE_SLEEP_MARK_COUNT =
            integerComponent("range_sleep_mark_count");

    public static final CoreDataComponentHolder<Integer> RANGE_SIZE_X =
            integerComponent("range_size_x");
    public static final CoreDataComponentHolder<Integer> RANGE_SIZE_Y =
            integerComponent("range_size_y");
    public static final CoreDataComponentHolder<Integer> RANGE_SIZE_Z =
            integerComponent("range_size_z");

    /** Signed offset of the range center from the placement anchor, bounded to +/-15. */
    public static final CoreDataComponentHolder<Integer> RANGE_OFFSET_X =
            integerComponent("range_offset_x");
    public static final CoreDataComponentHolder<Integer> RANGE_OFFSET_Y =
            integerComponent("range_offset_y");
    public static final CoreDataComponentHolder<Integer> RANGE_OFFSET_Z =
            integerComponent("range_offset_z");

    /** Legacy-compatible storage: true now means the sleep list is a whitelist. */
    public static final CoreDataComponentHolder<Boolean> RANGE_IDLE_THROTTLE_DISABLED =
            register("range_idle_throttle_disabled", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(CoreStreamCodec.of(FriendlyByteBuf::writeBoolean, FriendlyByteBuf::readBoolean)));

    public static final CoreDataComponentHolder<Boolean> RANGE_WHITELIST_MODE =
            register("range_whitelist_mode", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(CoreStreamCodec.of(FriendlyByteBuf::writeBoolean, FriendlyByteBuf::readBoolean)));

    public static final CoreDataComponentHolder<List<ResourceLocation>> RANGE_FILTERS =
            register("range_filters", builder -> builder
                    .persistent(ResourceLocation.CODEC.listOf().xmap(List::copyOf, List::copyOf))
                    .networkSynchronized(CoreStreamCodec.<FriendlyByteBuf, List<ResourceLocation>>of(
                            (buffer, values) -> {
                                buffer.writeVarInt(values.size());
                                for (ResourceLocation value : values) CoreByteBufCodecs.RESOURCE_LOCATION.encode(buffer, value);
                            },
                            buffer -> {
                                int size = buffer.readVarInt();
                                if (size < 0 || size > 128) throw new IllegalArgumentException("Invalid range filter count: " + size);
                                List<ResourceLocation> values = new ArrayList<>(size);
                                for (int i = 0; i < size; i++) values.add(CoreByteBufCodecs.RESOURCE_LOCATION.decode(buffer));
                                return List.copyOf(values);
                            })));

    /** Small server-store reference replacing the pattern assembly's potentially multi-megabyte item data. */
    public static final CoreDataComponentHolder<String> PATTERN_ASSEMBLY_REF =
            register("pattern_assembly_ref", builder -> builder
                    .persistent(Codec.STRING)
                    .networkSynchronized(CoreStreamCodec.<FriendlyByteBuf, String>of((buffer, value) -> buffer.writeUtf(value),
                            buffer -> buffer.readUtf())));

    public static final CoreDataComponentHolder<String> MYRIAD_MOLD_REF =
            register("myriad_mold_ref", builder -> builder
                    .persistent(Codec.STRING)
                    .networkSynchronized(CoreStreamCodec.<FriendlyByteBuf, String>of((buffer, value) -> buffer.writeUtf(value),
                            buffer -> buffer.readUtf())));

    public static final CoreDataComponentHolder<Integer> MYRIAD_MOLD_COUNT =
            integerComponent("myriad_mold_count");

    private UselessStretcherComponents() {
    }

    public static void init(IEventBus modEventBus) {
        // Custom data remains NBT-backed in Forge 1.20.1 and has no vanilla registry lifecycle.
    }

    private static <T> CoreDataComponentHolder<T> register(
            String name, UnaryOperator<CoreDataComponentType.Builder<T>> operator) {
        return new CoreDataComponentHolder<>(operator.apply(CoreDataComponentType.builder(new ResourceLocation("useless_stretcher", name))).build());
    }

    private static CoreDataComponentHolder<Integer> integerComponent(String name) {
        return register(name, builder -> builder
                .persistent(Codec.INT)
                .networkSynchronized(CoreStreamCodec.of(FriendlyByteBuf::writeVarInt, FriendlyByteBuf::readVarInt)));
    }
}
