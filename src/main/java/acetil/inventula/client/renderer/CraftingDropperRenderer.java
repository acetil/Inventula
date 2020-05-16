package acetil.inventula.client.renderer;

import acetil.inventula.common.tile.CraftingDropperTile;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.sun.javafx.geom.Vec3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class CraftingDropperRenderer extends TileEntityRenderer<CraftingDropperTile> {
    // TODO: make logic smarter
    private static final float ITEM_SCALE = (float)4/16;
    private static final float INITIAL_TRANSLATE = (float) 12/ 16;
    private static final float INITAL_OUTWARD = (float) 1/16;
    private static final float SLOT_OVER = (float) 20/16;
    private final ItemStack MASK_STACK;
    public CraftingDropperRenderer (TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
        MASK_STACK = new ItemStack(Items.BARRIER);
    }
    @Override
    public void render (CraftingDropperTile tileEntity, float partialTicks, MatrixStack matrixStackIn,
                        IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        Direction direction = tileEntity.getDirection();
        Quaternion quaternion = getRotationQuaternion(direction);
        matrixStackIn.push();
        matrixStackIn.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        translateInitial(matrixStackIn, direction);
        Vec3d xVec = getRelXVec(direction);
        Vec3d yVec = getRelYVec(direction);
        LazyOptional<IItemHandler> handlerOp = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        RenderHelper.enableStandardItemLighting();
        int blockLight = tileEntity.getWorld().getLightFor(LightType.BLOCK, tileEntity.getPos());
        int skyLight = tileEntity.getWorld().getLightFor(LightType.SKY, tileEntity.getPos());
        if (handlerOp.isPresent()) {
            IItemHandler handler = handlerOp.orElseGet(null);
            for (int i = 0; i < handler.getSlots(); i++) {
                // TODO: cleanup
                if (handler.getStackInSlot(i) != ItemStack.EMPTY) {
                    matrixStackIn.push();
                    getRelativePosition(matrixStackIn, xVec, yVec, i);
                    matrixStackIn.rotate(quaternion);
                    renderItem(handler.getStackInSlot(i), tileEntity.getWorld(),renderer, matrixStackIn, bufferIn,
                            LightTexture.packLight(blockLight, skyLight), combinedOverlayIn);
                    matrixStackIn.pop();
                } else if (tileEntity.isSlotMasked(i)) {
                    matrixStackIn.push();
                    getRelativePosition(matrixStackIn, xVec, yVec, i);
                    matrixStackIn.rotate(quaternion);
                    renderItem(MASK_STACK, tileEntity.getWorld(),renderer, matrixStackIn, bufferIn,
                            LightTexture.packLight(blockLight, skyLight), combinedOverlayIn);
                    matrixStackIn.pop();
                }
            }
        }
        matrixStackIn.pop();
        RenderHelper.disableStandardItemLighting();
    }
    @SuppressWarnings("deprecation")
    private void renderItem (ItemStack stack, World world, ItemRenderer itemRenderer, MatrixStack matrixStack,
                             IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
        IBakedModel model = itemRenderer.getItemModelWithOverrides(stack, world, null);
        itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED, false,
                matrixStack, buffer, combinedLightIn, combinedOverlayIn , model);
    }
    private void getRelativePosition (MatrixStack stack, Vec3d xVec, Vec3d yVec, int slot) {
        int realSlot = 9 - slot - 1;
        Vec3d overalVec = xVec.scale(realSlot % 3).add(yVec.scale(realSlot / 3));
        stack.translate(overalVec.getX(), overalVec.getY(), overalVec.getZ());
    }
    private void translateInitial (MatrixStack stack, Direction d) {
        switch (d.getOpposite()) {
            case NORTH:
                stack.translate(INITIAL_TRANSLATE, INITIAL_TRANSLATE, 0);
                break;
            case EAST:
                stack.translate(4, INITIAL_TRANSLATE, INITIAL_TRANSLATE);
                break;
            case WEST:
                stack.translate(0, INITIAL_TRANSLATE, 4 - INITIAL_TRANSLATE);
                break;
            case SOUTH:
                stack.translate(4 - INITIAL_TRANSLATE, INITIAL_TRANSLATE, 4);
                break;
            case UP:
                stack.translate(4 - INITIAL_TRANSLATE, 4, 4 - INITIAL_TRANSLATE);
                break;
            case DOWN:
                //stack.translate(INITIAL_TRANSLATE, 0, -INITIAL_TRANSLATE);
                stack.translate(4 - INITIAL_TRANSLATE, 0, 1.5 - INITIAL_TRANSLATE);
        }
    }
    private Vec3d getRelXVec (Direction d) {
        switch (d.getOpposite()) {
            case NORTH:
                return new Vec3d(SLOT_OVER, 0, 0);
            case EAST:
                return new Vec3d(0, 0, SLOT_OVER);
            case WEST:
                return new Vec3d(0, 0, -SLOT_OVER);
            case SOUTH:
                return new Vec3d(-SLOT_OVER, 0, 0);
            case UP:
                return new Vec3d( - SLOT_OVER, 0, 0);
            case DOWN:
                return new Vec3d( - SLOT_OVER, 0, 0);
        }
        return new Vec3d(0, 0, 0); //shouldn't get here
    }
    private Vec3d getRelYVec (Direction d) {
        return getRelXVec(d).crossProduct(new Vec3d(d.getOpposite().getDirectionVec()));
    }
    private Quaternion getRotationQuaternion (Direction d) {
        switch (d.getOpposite()) {
            case NORTH:
                return new Quaternion(new Vector3f(0, 1, 0), 0, true);
            case EAST:
                return new Quaternion(new Vector3f(0, 1, 0), -90, true);
            case WEST:
                return new Quaternion(new Vector3f(0, 1, 0), 90, true);
            case SOUTH:
                return new Quaternion(new Vector3f(0, 1, 0), 180, true);
            case UP:
                return new Quaternion(new Vector3f(1, 0, 0), -90, true);
            case DOWN:
                return new Quaternion(new Vector3f(1, 0, 0), 90, true);
        }
        return new Quaternion(new Vector3f(0, 1, 0), 0, true); // shouldn't get here
    }
}
