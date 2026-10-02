package net.veroxuniverse.verox_rpg_prog.client.territory;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.veroxuniverse.verox_rpg_prog.RPGProgression;
import net.veroxuniverse.verox_rpg_prog.territory.BorderBox;
import org.joml.Matrix4f;

import java.util.List;

@EventBusSubscriber(modid = RPGProgression.MOD_ID, value = Dist.CLIENT)
public final class TerritoryBorderRenderer {

    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/forcefield.png");
    private static final double FADE_DISTANCE = 12.0;
    private static final float MAX_ALPHA = 0.75F;
    private static final float UV_SCALE = 0.5F;

    private TerritoryBorderRenderer() {}

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) return;

        List<BorderBox> boxes = ClientTerritoryBorders.getBoxes();
        if (boxes.isEmpty()) return;

        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        Matrix4f matrix = poseStack.last().pose();

        float scroll = (Util.getMillis() % 3000L) / 3000.0F;
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        boolean hasGeometry = false;

        for (BorderBox box : boxes) {
            hasGeometry |= addBox(buffer, matrix, box, camera, scroll);
        }

        poseStack.popPose();

        MeshData mesh = buffer.build();
        if (!hasGeometry || mesh == null) {
            if (mesh != null) {
                mesh.close();
            }
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.polygonOffset(-3.0F, -3.0F);
        RenderSystem.enablePolygonOffset();

        BufferUploader.drawWithShader(mesh);

        RenderSystem.disablePolygonOffset();
        RenderSystem.polygonOffset(0.0F, 0.0F);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private static boolean addBox(BufferBuilder buffer, Matrix4f matrix, BorderBox box, Vec3 camera, float scroll) {
        float minX = box.minX();
        float minY = box.minY();
        float minZ = box.minZ();
        float maxX = box.maxX() + 1.0F;
        float maxY = box.maxY() + 1.0F;
        float maxZ = box.maxZ() + 1.0F;

        float flash = ClientTerritoryBorders.flashStrength(box);
        int r = (box.color() >> 16) & 0xFF;
        int g = (box.color() >> 8) & 0xFF;
        int b = box.color() & 0xFF;
        boolean drawn = false;

        boolean insideZ = camera.z > minZ - FADE_DISTANCE && camera.z < maxZ + FADE_DISTANCE;
        boolean insideX = camera.x > minX - FADE_DISTANCE && camera.x < maxX + FADE_DISTANCE;

        if (insideZ) {
            drawn |= addWallX(buffer, matrix, minX, minY, maxY, minZ, maxZ, alpha(Math.abs(camera.x - minX), flash), r, g, b, scroll);
            drawn |= addWallX(buffer, matrix, maxX, minY, maxY, minZ, maxZ, alpha(Math.abs(camera.x - maxX), flash), r, g, b, scroll);
        }
        if (insideX) {
            drawn |= addWallZ(buffer, matrix, minZ, minY, maxY, minX, maxX, alpha(Math.abs(camera.z - minZ), flash), r, g, b, scroll);
            drawn |= addWallZ(buffer, matrix, maxZ, minY, maxY, minX, maxX, alpha(Math.abs(camera.z - maxZ), flash), r, g, b, scroll);
        }
        return drawn;
    }

    private static float alpha(double distance, float flash) {
        float byDistance = (float) Math.max(0.0, 1.0 - distance / FADE_DISTANCE);
        return Math.max(byDistance, flash) * MAX_ALPHA;
    }

    private static boolean addWallX(BufferBuilder buffer, Matrix4f matrix, float x, float minY, float maxY, float minZ, float maxZ,
                                    float alpha, int r, int g, int b, float scroll) {
        if (alpha <= 0.01F) return false;
        int a = Math.round(alpha * 255.0F);

        buffer.addVertex(matrix, x, minY, minZ).setUv(minZ * UV_SCALE + scroll, minY * UV_SCALE).setColor(r, g, b, a);
        buffer.addVertex(matrix, x, minY, maxZ).setUv(maxZ * UV_SCALE + scroll, minY * UV_SCALE).setColor(r, g, b, a);
        buffer.addVertex(matrix, x, maxY, maxZ).setUv(maxZ * UV_SCALE + scroll, maxY * UV_SCALE).setColor(r, g, b, a);
        buffer.addVertex(matrix, x, maxY, minZ).setUv(minZ * UV_SCALE + scroll, maxY * UV_SCALE).setColor(r, g, b, a);
        return true;
    }

    private static boolean addWallZ(BufferBuilder buffer, Matrix4f matrix, float z, float minY, float maxY, float minX, float maxX,
                                    float alpha, int r, int g, int b, float scroll) {
        if (alpha <= 0.01F) return false;
        int a = Math.round(alpha * 255.0F);

        buffer.addVertex(matrix, minX, minY, z).setUv(minX * UV_SCALE + scroll, minY * UV_SCALE).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, minY, z).setUv(maxX * UV_SCALE + scroll, minY * UV_SCALE).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, maxY, z).setUv(maxX * UV_SCALE + scroll, maxY * UV_SCALE).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, maxY, z).setUv(minX * UV_SCALE + scroll, maxY * UV_SCALE).setColor(r, g, b, a);
        return true;
    }
}