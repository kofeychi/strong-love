package com.linett.strong_love.render;

import com.linett.strong_love.common.items.BeamItemRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class ItemWith2DBeamRenderer extends ItemEntityRenderer {

    public ItemWith2DBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ItemEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {

        ItemStack stack = entity.getItem();
        Item item = stack.getItem();
        Level level = entity.level();

        float bounce = calculateBounce(entity, partialTicks);

        if (BeamItemRegistry.ITEM_PROPERTIES.containsKey(item)) {
            renderBillboardBeams(entity, poseStack, buffer, partialTicks, bounce);
        }

        poseStack.pushPose();
        poseStack.translate(0, bounce + 0.4F, 0);

        float time = entity.level().getGameTime() + partialTicks;
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 4F));

        renderItemWithOutline(stack, poseStack, buffer, packedLight, level, entity.getId());

        poseStack.popPose();
    }

    private float calculateBounce(ItemEntity entity, float partialTicks) {
        float age = entity.getAge() + partialTicks;
        float bounce = (float)Math.sin(age / 10.0F) * 0.1F + 0.1F;
        return bounce;
    }

    private void renderItemWithOutline(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer,
                                       int packedLight, Level level, int entityId) {

        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                level,
                entityId
        );

        poseStack.pushPose();
        poseStack.scale(1.1F, 1.1F, 1.1F);

        poseStack.popPose();
    }

    private void renderBillboardBeams(ItemEntity entity, PoseStack poseStack, MultiBufferSource buffer,
                                      float partialTicks, float bounce) {
        poseStack.pushPose();

        float time = entity.level().getGameTime() + partialTicks;
        ItemWith2DBeamRenderer.BeamProperties props = BeamItemRegistry.ITEM_PROPERTIES.get(entity.getItem().getItem());
        int beamCount = props.getBeamCount();
        float beamLength = props.getBeamLength();
        int[] colors = props.getColors();

        poseStack.translate(0, bounce + 0.5F, 0);

        Quaternionf cameraRot = Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation();
        poseStack.mulPose(cameraRot);

        renderBeamsWithOutline(poseStack, buffer, time, beamCount, beamLength, colors);

        poseStack.popPose();
    }

    private void renderBeamsWithOutline(PoseStack poseStack, MultiBufferSource buffer, float time,
                                        int beamCount, float beamLength, int[] colors) {

        float beamWidth = 0.2F;
        float radiusOffset = 0F;

        for (int i = 0; i < beamCount; i++) {
            poseStack.pushPose();

            float angleStep = 360F / beamCount;
            float rotation = i * angleStep + time * 5F;

            float offsetX = (float) Math.cos(Math.toRadians(rotation)) * radiusOffset;
            float offsetZ = (float) Math.sin(Math.toRadians(rotation)) * radiusOffset;
            poseStack.translate(offsetX, 0, offsetZ);

            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));

            float scale = 0.4F + 0.2F * (float)Math.sin(time / 15F + i);

            int color = colors[i % colors.length];
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;

            VertexConsumer mainConsumer = buffer.getBuffer(ModRenderType.Shine());
            renderSingleFlatBeam(poseStack, mainConsumer, beamLength * scale, beamWidth, r, g, b, 60);

            poseStack.popPose();
        }
    }

    private void renderSingleFlatBeam(PoseStack poseStack, VertexConsumer consumer, float length,
                                      float width, int r, int g, int b, int alpha) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();

        int alphaEdge = 0;

        consumer.vertex(matrix4f, 0.0F, 0.0F, 0.0F)
                .color(r, g, b, alpha)
                .normal(matrix3f, 0.0F, 1.0F, 0.0F)
                .endVertex();

        consumer.vertex(matrix4f, -width, length, 0.0F)
                .color(r, g, b, alphaEdge)
                .normal(matrix3f, 0.0F, 1.0F, 0.0F)
                .endVertex();

        consumer.vertex(matrix4f, width, length, 0.0F)
                .color(r, g, b, alphaEdge)
                .normal(matrix3f, 0.0F, 1.0F, 0.0F)
                .endVertex();

        consumer.vertex(matrix4f, 0.0F, 0.0F, 0.0F)
                .color(r, g, b, alpha)
                .normal(matrix3f, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    public static class BeamProperties {
        private final int[] colors;
        private final float beamLength;
        private final int beamCount;

        public BeamProperties(int[] colors, float beamLength, int beamCount) {
            this.colors = colors;
            this.beamLength = beamLength;
            this.beamCount = beamCount;
        }

        public int[] getColors() {
            return colors;
        }

        public float getBeamLength() {
            return beamLength;
        }

        public int getBeamCount() {
            return beamCount;
        }
    }
}