package acetil.modjam.common.block;

import acetil.modjam.common.tile.EternalSpawnerTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EternalSpawnerBlock extends SpawnerBlock {

    public EternalSpawnerBlock (Properties properties) {
        super(properties);
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

}
