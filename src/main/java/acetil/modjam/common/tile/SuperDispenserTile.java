package acetil.modjam.common.tile;

import acetil.modjam.common.block.ModBlocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SuperDispenserTile extends TileEntity {
    private ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> itemOptional = LazyOptional.empty();
    private static final int NUM_SLOTS = 9;
    public SuperDispenserTile () {
        super(ModBlocks.SUPER_DISPENSER_TILE.get());

        itemHandler = new ItemStackHandler(NUM_SLOTS) {
            @Override
            protected void onContentsChanged (int slot) {
                super.onContentsChanged(slot);
                SuperDispenserTile.this.markDirty();
            }
        };
        itemOptional = LazyOptional.of(() -> itemHandler);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability (@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemOptional.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void read (@Nonnull CompoundNBT compound) {
        super.read(compound);
        if (compound.contains("items")) {
            itemHandler.deserializeNBT(compound.getCompound("items"));
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write (@Nonnull CompoundNBT compound) {
        compound.put("items", itemHandler.serializeNBT());
        return super.write(compound);
    }
}
