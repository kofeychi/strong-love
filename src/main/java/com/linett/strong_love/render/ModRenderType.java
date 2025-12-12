package com.linett.strong_love.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class ModRenderType extends RenderType {

    public ModRenderType(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
        super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
    }

    public static RenderType Shine() {
    return create("shine", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, true, true, RenderType.CompositeState.builder()
            .setShaderState(RenderType.RENDERTYPE_LIGHTNING_SHADER)
            .setTransparencyState(SHINE_TRANSPARENCY)
            .setCullState(NO_CULL)
            .setLightmapState(NO_LIGHTMAP)
            .setDepthTestState(LEQUAL_DEPTH_TEST)
            .setWriteMaskState(COLOR_WRITE)
            .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
            .setOutputState(RenderStateShard.PARTICLES_TARGET)
            .createCompositeState(true));
}

    protected static final RenderStateShard.TransparencyStateShard SHINE_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("eyes_alpha_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });

}


