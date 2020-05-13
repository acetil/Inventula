package acetil.modjam.client.particle;

import acetil.modjam.client.WorldParticleTracker;
import acetil.modjam.common.ModJam;
import acetil.modjam.common.particle.DispenserItemParticleData;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

public class DispenserItemParticle extends Particle {
    private static final double GRAVITY_ACCELERATION = 0.03;
    private ItemStack stack;
    private ItemRenderer itemRenderer;
    private static final Vec3d ACC_VEC = new Vec3d(0, -1 * GRAVITY_ACCELERATION, 0);
    private Vec3d velVec;
    private Vec3d posVec;
    private int entityId;
    public DispenserItemParticle (DispenserItemParticleData data, World worldIn, double posX, double posY,
                                  double posZ, double velX, double velY, double velZ) {
        super(worldIn, posX, posY, posZ);
        posVec = new Vec3d(posX, posY, posZ);
        velVec = new Vec3d(velX, velY, velZ);
        this.stack = data.getStack();
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.setMaxAge(data.getLifetime());
        System.out.println("Making particle!");
        entityId = data.getEntityId();
        WorldParticleTracker.addParticle(this);
    }
    public int getEntityId () {
        return entityId;
    }
    @Override
    public void renderParticle (IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
        IRenderTypeBuffer.Impl renderBuffer = Minecraft.getInstance().worldRenderer.renderTypeTextures.getBufferSource();
        IBakedModel model = itemRenderer.getItemModelWithOverrides(stack, world, null);
        MatrixStack matrixStack  = new MatrixStack();
        matrixStack.push();

        Vec3d partialPos = posVec.add(velVec.scale(partialTicks))
                .add(ACC_VEC.scale(partialTicks * partialTicks));
        int blockLight = world.getLightFor(LightType.BLOCK, new BlockPos(partialPos));
        int skyLight = world.getLightFor(LightType.SKY, new BlockPos(partialPos));
        Vec3d view = renderInfo.getProjectedView();
        matrixStack.translate(partialPos.getX() - view.getX(), partialPos.getY() - view.getY(),
                partialPos.getZ() - view.getZ());
        this.itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.GROUND, false, matrixStack,renderBuffer,
                LightTexture.packLight(blockLight, skyLight), OverlayTexture.NO_OVERLAY, model);
        matrixStack.pop();
        renderBuffer.finish();
    }

    @Override
    public IParticleRenderType getRenderType () {
        return IParticleRenderType.CUSTOM;
    }

    @Override
    public void tick () {
        posVec = posVec.add(velVec).add(ACC_VEC);
        velVec = velVec.add(ACC_VEC);
    }
}
