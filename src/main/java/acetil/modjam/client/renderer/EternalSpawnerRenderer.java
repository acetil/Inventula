package acetil.modjam.client.renderer;

import acetil.modjam.common.tile.EternalSpawnerTile;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.spawner.AbstractSpawner;


public class EternalSpawnerRenderer extends TileEntityRenderer<EternalSpawnerTile> {
    public EternalSpawnerRenderer (TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render (EternalSpawnerTile tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,
                        int combinedLightIn, int combinedOverlayIn) {
        // copied from MobSpawnerTileEntityRenderer
        // TODO: rewrite
        matrixStackIn.push();
        matrixStackIn.translate(0.5, 0.4, 0.5);
        AbstractSpawner spawner = tileEntityIn.getSpawnerLogic();
        Entity entity = spawner.getCachedEntity();
        if (entity != null) {
            float constant1 = 0.53125f;
            float constant2 = Math.max(entity.getWidth(), entity.getHeight());
            if (constant2 > 1.0f) {
                constant1 /= constant2;
            }

            matrixStackIn.rotate(Vector3f.YP.rotationDegrees((float)
                    MathHelper.lerp(partialTicks, spawner.getPrevMobRotation(), spawner.getMobRotation() * 10.0f)));
            matrixStackIn.scale(constant1, constant1, constant1);
            Minecraft.getInstance().getRenderManager().renderEntityStatic(entity, 0, 0, 0, 0,
                    partialTicks, matrixStackIn, bufferIn, combinedLightIn);
        }
        matrixStackIn.pop();
    }
}
