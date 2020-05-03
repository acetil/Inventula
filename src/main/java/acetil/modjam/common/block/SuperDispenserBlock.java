package acetil.modjam.common.block;

import acetil.modjam.common.tile.SuperDispenserTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

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
    public void onBlockClicked (BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        // do gui stuff
        super.onBlockClicked(state, worldIn, pos, player);
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
            if (worldIn.getTileEntity(pos) != null && worldIn.getTileEntity(pos) instanceof SuperDispenserTile) {
                ((SuperDispenserTile) worldIn.getTileEntity(pos)).dispense(state.get(FACING));
            }
        } else if (!worldIn.isBlockPowered(pos) && state.get(POWERED)) {
            worldIn.setBlockState(pos, state.with(POWERED, false));
        }
    }
}
