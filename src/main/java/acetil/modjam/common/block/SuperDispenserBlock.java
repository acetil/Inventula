package acetil.modjam.common.block;

import acetil.modjam.common.tile.SuperDispenserTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class SuperDispenserBlock extends Block {
    public SuperDispenserBlock (Properties properties) {
        super(properties);
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
}
