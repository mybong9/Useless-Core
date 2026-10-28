package com.sorrowmist.uselesscore.api.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Stable attribute identities and replacement operations for Forge 1.20.1. */
public final class CoreAttributes {
    public static final UUID BASE_ATTACK_DAMAGE_ID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    public static final UUID BASE_ATTACK_SPEED_ID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    private CoreAttributes() {}

    public static AttributeModifier create(ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        return new AttributeModifier(UUID.nameUUIDFromBytes(id.toString().getBytes(StandardCharsets.UTF_8)),
                id.toString(), amount, operation);
    }

    public static void replaceTransient(AttributeInstance attribute, AttributeModifier modifier) {
        attribute.removeModifier(modifier.getId());
        attribute.addTransientModifier(modifier);
    }
}
