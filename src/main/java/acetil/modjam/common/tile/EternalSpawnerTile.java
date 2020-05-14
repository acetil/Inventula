package acetil.modjam.common.tile;

import acetil.modjam.common.ModJam;
import acetil.modjam.common.block.ModBlocks;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;

public class EternalSpawnerTile extends TileEntity implements ITickableTileEntity {
    // Based on MobSpawnerTileEntity (can't simply override because no TileEntityType constructor
    // TODO: Rewrite
    private AbstractSpawner spawner = new AbstractEternalSpawner() {

        @Override
        public void broadcastEvent (int id) {
            EternalSpawnerTile.this.world.addBlockEvent(EternalSpawnerTile.this.pos, ModBlocks.ETERNAL_SPAWNER.get(), id, 0);
        }

        @Override
        public World getWorld () {
            return EternalSpawnerTile.this.world;
        }

        @Override
        public BlockPos getSpawnerPosition () {
            return EternalSpawnerTile.this.pos;
        }
    };
    public EternalSpawnerTile () {
        super(ModBlocks.ETERNAL_SPAWNER_TILE.get());
    }

    @Override
    public void tick () {
        spawner.tick();
    }

    @Override
    public void read (CompoundNBT compound) {
        super.read(compound);
        spawner.read(compound);
    }

    @Override
    public CompoundNBT write (CompoundNBT compound) {
        super.write(compound);
        spawner.write(compound);
        return compound;
    }
    public AbstractSpawner getSpawnerLogic () {
        return spawner;
    }

    @Override
    public boolean receiveClientEvent (int id, int type) {
        return spawner.setDelayToMin(id) || super.receiveClientEvent(id, type);
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket () {
        return new SUpdateTileEntityPacket(this.pos, 1, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag () {
        CompoundNBT nbt = this.write(new CompoundNBT());
        nbt.remove("SpawnPotentials");
        return nbt;
    }

    @Override
    public boolean onlyOpsCanSetNbt () {
        return true;
    }
}
