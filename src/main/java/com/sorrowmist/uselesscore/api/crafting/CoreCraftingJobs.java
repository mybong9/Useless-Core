package com.sorrowmist.uselesscore.api.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.GenericStack;
import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Function;

/** Shared lifecycle, dispatch policies and persistent attachments for crafting CPU integrations. */
public final class CoreCraftingJobs {
    public record Context(UUID craftingJobId, Object cpu, Object grid, GenericStack finalOutput) {
        public Context {
            Objects.requireNonNull(craftingJobId, "craftingJobId");
            Objects.requireNonNull(cpu, "cpu");
        }
    }

    public record Dispatch(Context job, IPatternDetails pattern, ICraftingProvider provider, long dispatchedCrafts) {
        public Dispatch {
            if (dispatchedCrafts <= 0) throw new IllegalArgumentException("Dispatch count must be positive");
        }
    }

    public enum Result { COMPLETED, CANCELLED, FAILED }

    public interface Listener {
        default void onJobStarted(Context context) {}
        default void onPatternDispatched(Dispatch event) {}
        default void onJobFinished(Context context, Result result) {}
    }

    public interface Attachment {
        ResourceLocation id();
        CompoundTag save(HolderLookup.Provider registries);
        void load(CompoundTag tag, HolderLookup.Provider registries);
        void clear(Result result);
    }

    private static final Map<ResourceLocation, Function<Context, ? extends Attachment>> FACTORIES = new LinkedHashMap<>();
    private static final List<Listener> LISTENERS = new ArrayList<>();
    private static final List<BiPredicate<Context, ICraftingProvider>> POLICIES = new ArrayList<>();
    private static final Map<UUID, State> JOBS = new LinkedHashMap<>();
    private CoreCraftingJobs() {}

    public static synchronized void registerAttachment(ResourceLocation id, Function<Context, ? extends Attachment> factory) {
        if (FACTORIES.putIfAbsent(Objects.requireNonNull(id), Objects.requireNonNull(factory)) != null) {
            throw new IllegalArgumentException("Crafting job attachment already registered: " + id);
        }
    }

    public static synchronized void registerListener(Listener listener) { LISTENERS.add(Objects.requireNonNull(listener)); }
    public static synchronized void registerPolicy(BiPredicate<Context, ICraftingProvider> policy) {
        POLICIES.add(Objects.requireNonNull(policy));
    }

    public static synchronized void start(Context context) {
        if (JOBS.containsKey(context.craftingJobId())) return;
        State state = new State(context);
        JOBS.put(context.craftingJobId(), state);
        try {
            FACTORIES.forEach((id, factory) -> {
                Attachment attachment = Objects.requireNonNull(factory.apply(context));
                if (!id.equals(attachment.id())) throw new IllegalArgumentException("Attachment id mismatch: " + id);
                state.attachments.put(id, attachment);
            });
            LISTENERS.forEach(listener -> listener.onJobStarted(context));
        } catch (RuntimeException | LinkageError exception) {
            JOBS.remove(context.craftingJobId());
            state.attachments.values().forEach(attachment -> attachment.clear(Result.FAILED));
            throw exception;
        }
    }

    public static synchronized void dispatch(Dispatch dispatch) {
        start(dispatch.job());
        LISTENERS.forEach(listener -> listener.onPatternDispatched(dispatch));
    }

    public static synchronized boolean available(Context context, ICraftingProvider provider) {
        if (provider.isBusy()) return false;
        for (var policy : POLICIES) {
            if (!policy.test(context, provider)) return false;
        }
        return true;
    }

    public static synchronized <T extends Attachment> T attachment(UUID jobId, ResourceLocation id, Class<T> type) {
        State state = JOBS.get(jobId);
        Attachment attachment = state == null ? null : state.attachments.get(id);
        return type.isInstance(attachment) ? type.cast(attachment) : null;
    }

    public static synchronized CompoundTag save(Context context, HolderLookup.Provider registries) {
        start(context);
        CompoundTag tag = new CompoundTag();
        JOBS.get(context.craftingJobId()).attachments.forEach(
                (id, attachment) -> tag.put(id.toString(), attachment.save(registries)));
        return tag;
    }

    public static synchronized void load(Context context, CompoundTag tag, HolderLookup.Provider registries) {
        start(context);
        JOBS.get(context.craftingJobId()).attachments.forEach((id, attachment) -> {
            if (tag.contains(id.toString(), Tag.TAG_COMPOUND)) attachment.load(tag.getCompound(id.toString()), registries);
        });
    }

    public static synchronized void finish(Context context, Result result) {
        State state = JOBS.remove(context.craftingJobId());
        try {
            LISTENERS.forEach(listener -> listener.onJobFinished(context, result));
        } finally {
            if (state != null) state.attachments.values().forEach(attachment -> attachment.clear(result));
        }
    }

    public static synchronized void clearAll() {
        for (State state : List.copyOf(JOBS.values())) {
            try {
                finish(state.context, Result.CANCELLED);
            } catch (RuntimeException | LinkageError exception) {
                LogUtils.getLogger().error("Failed to release crafting job {}", state.context.craftingJobId(), exception);
            }
        }
    }

    private static final class State {
        private final Context context;
        private final Map<ResourceLocation, Attachment> attachments = new LinkedHashMap<>();
        private State(Context context) { this.context = context; }
    }
}
