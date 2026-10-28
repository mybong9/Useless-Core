package com.sorrowmist.uselesscore.client.ui;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.sorrowmist.uselesscore.UselessCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;
import java.io.IOException;

/** Analytic antialiasing and one shared two-pass scene blur for our menus. */
@Mod.EventBusSubscriber(modid = UselessCore.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class UiRender {
    private static ShaderInstance shape, blur;
    private static RenderTarget horizontal, blurred;
    private static boolean sceneReady;
    private UiRender() {}
    @SubscribeEvent public static void shaders(RegisterShadersEvent event) throws IOException {
        releaseScene();
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                new ResourceLocation(UselessCore.MOD_ID, "ui_shape"), DefaultVertexFormat.POSITION_TEX), s -> shape = s);
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                new ResourceLocation(UselessCore.MOD_ID, "ui_blur"), DefaultVertexFormat.POSITION_TEX), s -> blur = s);
    }
    /** Capture before any menu content: never feed last frame's interface back into the blur. */
    public static void captureScene(GuiGraphics g) {
        sceneReady = false;
        if (blur == null || shape == null) return;
        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        if (main.width < 1 || main.height < 1) return;
        g.flush();
        int w = Math.max(1, (main.width + 2) / 3), h = Math.max(1, (main.height + 2) / 3);
        if (horizontal == null || horizontal.width != w || horizontal.height != h) {
            releaseScene();
            horizontal = new TextureTarget(w, h, false, Minecraft.ON_OSX);
            blurred = new TextureTarget(w, h, false, Minecraft.ON_OSX);
            horizontal.setFilterMode(GL11.GL_LINEAR); blurred.setFilterMode(GL11.GL_LINEAR);
        }
        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST), scissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        boolean depthWrite = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        int[] scissorBox=new int[4];if(scissor)GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX,scissorBox);
        int previousTexture=RenderSystem.getShaderTexture(0);
        ShaderInstance previous = RenderSystem.getShader();
        if (scissor) RenderSystem.disableScissor();
        RenderSystem.disableDepthTest(); RenderSystem.depthMask(false); RenderSystem.disableBlend();
        try {
            blurPass(main, horizontal, 4.5f / main.width, 0);
            blurPass(horizontal, blurred, 0, 1.5f / horizontal.height);
            sceneReady = true;
        } finally {
            main.bindWrite(true); RenderSystem.depthMask(depthWrite);
            if (depth) RenderSystem.enableDepthTest(); else RenderSystem.disableDepthTest();
            if (scissor) RenderSystem.enableScissor(scissorBox[0],scissorBox[1],scissorBox[2],scissorBox[3]);
            RenderSystem.setShaderTexture(0,previousTexture);
            RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc(); RenderSystem.setShader(() -> previous);
        }
    }
    private static void blurPass(RenderTarget source, RenderTarget destination, float dx, float dy) {
        destination.bindWrite(true); RenderSystem.setShader(() -> blur);
        RenderSystem.setShaderTexture(0, source.getColorTextureId()); blur.safeGetUniform("Step").set(dx, dy);
        var b = Tesselator.getInstance().getBuilder();
        b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        b.vertex(-1, -1, 0).uv(0, 0).endVertex(); b.vertex(1, -1, 0).uv(1, 0).endVertex();
        b.vertex(1, 1, 0).uv(1, 1).endVertex(); b.vertex(-1, 1, 0).uv(0, 1).endVertex();
        BufferUploader.drawWithShader(b.end());
    }
    public static void releaseScene() {
        sceneReady = false;
        if (horizontal != null) horizontal.destroyBuffers();
        if (blurred != null) blurred.destroyBuffers();
        horizontal = blurred = null;
    }
    static boolean shape(GuiGraphics g, float x, float y, float w, float h, float radius,
                         int top, int bottom, int edge, float border, int shadow, float softness, boolean glass) {
        if (shape == null || w <= 0 || h <= 0) return false;
        g.flush(); ShaderInstance previous = RenderSystem.getShader();int previousTexture=RenderSystem.getShaderTexture(0);
        RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc(); RenderSystem.setShader(() -> shape);
        shape.safeGetUniform("Size").set(w, h);
        shape.safeGetUniform("Radius").set(Math.min(radius, Math.min(w, h) * .5f));
        shape.safeGetUniform("Border").set(border);
        colour("TopColor", top); colour("BottomColor", bottom); colour("EdgeColor", edge); colour("ShadowColor", shadow);
        shape.safeGetUniform("Softness").set(softness);
        boolean sample = glass && sceneReady;
        shape.safeGetUniform("Glass").set(sample ? 1f : 0f);
        var main = Minecraft.getInstance().getMainRenderTarget();
        shape.safeGetUniform("FramebufferSize").set((float) main.width, (float) main.height);
        // A non-glass primitive does not sample the framebuffer it is drawing into.
        RenderSystem.setShaderTexture(0, blurred != null ? blurred.getColorTextureId() : 0);
        float pad = Math.max(1, softness * 2.5f);
        var matrix = g.pose().last().pose(); var b = Tesselator.getInstance().getBuilder();
        b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        b.vertex(matrix, x-pad, y-pad, 0).uv(-pad, -pad).endVertex();
        b.vertex(matrix, x-pad, y+h+pad, 0).uv(-pad, h+pad).endVertex();
        b.vertex(matrix, x+w+pad, y+h+pad, 0).uv(w+pad, h+pad).endVertex();
        b.vertex(matrix, x+w+pad, y-pad, 0).uv(w+pad, -pad).endVertex();
        try { BufferUploader.drawWithShader(b.end()); }
        finally { RenderSystem.setShader(() -> previous);RenderSystem.setShaderTexture(0,previousTexture); }
        return true;
    }
    private static void colour(String uniform, int argb) {
        shape.safeGetUniform(uniform).set(((argb >> 16)&255)/255f, ((argb >> 8)&255)/255f,
                (argb&255)/255f, ((argb >>> 24)&255)/255f);
    }
}
