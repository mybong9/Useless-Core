package com.sorrowmist.uselesscore.api.data;

import net.minecraftforge.common.ToolAction;
import java.util.Set;

/** Named tool actions added after 1.20.1, retained for callers querying multipurpose tools. */
public final class CoreToolActions {
    public static final ToolAction BRUSH_BRUSH = ToolAction.get("brush_brush");
    public static final ToolAction FIRESTARTER_LIGHT = ToolAction.get("firestarter_light");
    public static final Set<ToolAction> DEFAULT_BRUSH_ACTIONS = Set.of(BRUSH_BRUSH);
    public static final Set<ToolAction> DEFAULT_FLINT_ACTIONS = Set.of(FIRESTARTER_LIGHT);
    private CoreToolActions() {}
}
