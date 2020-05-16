package acetil.inventula.common.block;

import acetil.inventula.common.Inventula;
import acetil.inventula.common.tile.CraftingDropperTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;

public class CraftingDropperBlock extends Block {
    public static DirectionProperty FACING = BlockStateProperties.FACING;
    public static BooleanProperty POWERED = BlockStateProperties.POWERED;
    public CraftingDropperBlock (Properties properties) {
        super(properties);
    }

    @Override
    protected void fillStateContainer (StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder.add(FACING, POWERED));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement (BlockItemUseContext context) {
        return super.getStateForPlacement(context).with(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public boolean hasTileEntity (BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity (BlockState state, IBlockReader world) {
        return new CraftingDropperTile();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged (BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
        if (state.get(POWERED) && !worldIn.isBlockPowered(pos)) {
            worldIn.setBlockState(pos, state.with(POWERED, false));
        } else if (!state.get(POWERED) && worldIn.isBlockPowered(pos)) {
            worldIn.setBlockState(pos, state.with(POWERED, true));
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof CraftingDropperTile && !worldIn.isRemote()) {
                ((CraftingDropperTile) te).craft(state.get(FACING));
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType onBlockActivated (BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (player.getItemStackFromSlot(EquipmentSlotType.MAINHAND) != ItemStack.EMPTY) {
            return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
        }
        if (worldIn.isRemote()) {
            return ActionResultType.SUCCESS;
        }
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof CraftingDropperTile) {
            Direction d = state.get(FACING);
            if (player.isCrouching()) {
                ((CraftingDropperTile) te).clearItems(d);
            } else {
                ((CraftingDropperTile) te).updateMask(d, hit);
            }
        } else {
            Inventula.LOGGER.log(Level.WARN, "Crafting dropper block at {} has wrong tile entity!", pos);
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public BlockRenderType getRenderType (BlockState state) {
        return BlockRenderType.MODEL;
    }
    @SuppressWarnings("deprecation")
    @Override
    public void onReplaced (BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
            IItemHandler handler = worldIn.getTileEntity(pos)
                    .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                    .orElse(null);
            for (int i = 0; i < handler.getSlots(); i++) {
                if (handler.getStackInSlot(i) != ItemStack.EMPTY) {
                    spawnAsEntity(worldIn, pos, handler.getStackInSlot(i));
                }
            }
            worldIn.updateComparatorOutputLevel(pos, this);
        }
        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }
}
