package acetil.inventula.common.block;

import acetil.inventula.common.Inventula;
import acetil.inventula.common.constants.Constants;
import acetil.inventula.common.containers.SuperDispenserContainer;
import acetil.inventula.common.tile.SuperDispenserTile;
import com.sun.java.accessibility.util.java.awt.TextComponentTranslator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.DispenserContainer;
import net.minecraft.inventory.container.INamedContainerProvider;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;

public class SuperDispenserBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public SuperDispenserBlock (Properties properties) {
        super(properties);
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH).with(POWERED, Boolean.FALSE));
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasTileEntity () {
        return true;
    }

    @Override
    public boolean hasTileEntity (BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity (BlockState state, IBlockReader world) {
        return new SuperDispenserTile();
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType onBlockActivated (BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isRemote()) {
            return ActionResultType.SUCCESS;
        }
        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof SuperDispenserTile)) {
            Inventula.LOGGER.log(Level.WARN, "Dispenser at {} has wrong tile entity!", pos);
            return ActionResultType.FAIL;
        }
        ITextComponent localisedName = new TranslationTextComponent(getTranslationKey());
        NetworkHooks.openGui((ServerPlayerEntity) player, new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName () {
                return localisedName;
            }

            @Nullable
            @Override
            public Container createMenu (int windowId, PlayerInventory inv, PlayerEntity entity) {
                IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                        .orElseGet(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY::getDefaultInstance);
                return new SuperDispenserContainer(windowId, inv, handler, pos);
            }
        }, pos);
        return ActionResultType.SUCCESS;
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

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged (BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
        if (worldIn.isBlockPowered(pos) && !state.get(POWERED)) {
            worldIn.setBlockState(pos, state.with(POWERED, true));
            if (worldIn.getTileEntity(pos) != null && worldIn.getTileEntity(pos) instanceof SuperDispenserTile && !worldIn.isRemote) {
                ((SuperDispenserTile) worldIn.getTileEntity(pos)).dispense(state.get(FACING));
            }
        } else if (!worldIn.isBlockPowered(pos) && state.get(POWERED)) {
            worldIn.setBlockState(pos, state.with(POWERED, false));
        }
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

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasComparatorInputOverride (BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getComparatorInputOverride (BlockState blockState, World worldIn, BlockPos pos) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te != null && te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
            // Adapted from Container.calcRedstoneFromInventory
            int numItems = 0;
            double totalFraction = 0.0;
            IItemHandler itemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                ItemStack stack = itemHandler.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    totalFraction += (double)stack.getCount() / Math.min(itemHandler.getSlotLimit(i), stack.getMaxStackSize());
                    numItems++;
                }
            }
            return Math.floor(totalFraction / itemHandler.getSlots() * 14.0) + numItems > 0 ? 1 : 0;
        }
        return super.getComparatorInputOverride(blockState, worldIn, pos);
    }
}
