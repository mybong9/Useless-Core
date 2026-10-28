package com.sorrowmist.uselesscore.api.crafting;

import java.util.Map;

/** Exact validation before a provider accepts ownership of a material preparation batch. */
public final class CoreMaterialAmounts {
    private CoreMaterialAmounts() {}

    public static <K> boolean matches(Map<K, Long> required, Map<K, Long> supplied, long copies) {
        if (copies <= 0 || required.isEmpty() || !required.keySet().equals(supplied.keySet())) return false;
        try {
            for (var entry : required.entrySet()) {
                Long amount = supplied.get(entry.getKey());
                if (entry.getValue() <= 0 || amount == null
                        || Math.multiplyExact(entry.getValue(), copies) != amount) return false;
            }
            return true;
        } catch (ArithmeticException overflow) {
            return false;
        }
    }
}
