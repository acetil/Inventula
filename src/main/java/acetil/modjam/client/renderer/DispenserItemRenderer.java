package acetil.modjam.client.renderer;

import acetil.modjam.common.ModJam;
import acetil.modjam.common.entity.DispenserItemEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class DispenserItemRenderer extends EntityRenderer<DispenserItemEntity> {
    private ItemRenderer itemRenderer;
    public DispenserItemRenderer (EntityRendererManager renderManager, ItemRenderer itemRenderer) {
        super(renderManager);
        this.itemRenderer = itemRenderer;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void render (DispenserItemEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn,
                        IRenderTypeBuffer bufferIn, int packedLightIn) {
        // mostly copied from ItemRenderer
        System.out.println("Rendering dispenser item!");
        matrixStackIn.push();
        ItemStack stack = entityIn.getItem();
        IBakedModel model = itemRenderer.getItemModelWithOverrides(stack, entityIn.world, null);
        this.itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.GROUND, false, matrixStackIn,
                bufferIn, packedLightIn, OverlayTexture.NO_OVERLAY, model);
        matrixStackIn.pop();
    }

    @Override
    @SuppressWarnings("deprecation")
    public ResourceLocation getEntityTexture (DispenserItemEntity entity) {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
    }

}
