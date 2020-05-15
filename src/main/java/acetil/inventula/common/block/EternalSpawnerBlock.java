package acetil.inventula.common.block;

import acetil.inventula.common.tile.EternalSpawnerTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EternalSpawnerBlock extends SpawnerBlock {
    public static BooleanProperty POWERED = BlockStateProperties.POWERED;
    public EternalSpawnerBlock (Properties properties) {
        super(properties);
        this.setDefaultState(this.getDefaultState().with(POWERED, false));
    }

    @Override
    public boolean hasTileEntity (BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity (BlockState state, IBlockReader world) {
        return new EternalSpawnerTile();
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockRenderType getRenderType (BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void fillStateContainer (StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder.add(POWERED));
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState updatePostPlacement (BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        TileEntity te = worldIn.getTileEntity(currentPos);
        if (te instanceof  EternalSpawnerTile) {
            ((EternalSpawnerTile) te).setActivated(!stateIn.get(POWERED));
        }
        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged (BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
        worldIn.setBlockState(pos, state.with(POWERED, worldIn.isBlockPowered(pos)));
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof EternalSpawnerTile) {
            ((EternalSpawnerTile) te).setActivated(!worldIn.isBlockPowered(pos));
        }
    }
}
