package acetil.modjam.common.block;

import acetil.modjam.common.tile.EternalSpawnerTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
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
    public void neighborChanged (BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
        if (worldIn.isBlockPowered(pos) != state.get(POWERED)) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof EternalSpawnerTile) {
                ((EternalSpawnerTile) te).setActivated(!worldIn.isBlockPowered(pos));
                worldIn.setBlockState(pos, state.with(POWERED, worldIn.isBlockPowered(pos)));
            }
        }
    }
}
