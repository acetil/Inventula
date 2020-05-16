package acetil.inventula.common.tile;

import acetil.inventula.common.block.ModBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
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
    private LazyOptional<IItemHandler> itemOptional;
    private static final int NUM_SLOTS = 9;
    private boolean doContinue = true;
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
    private int getDispenseSlot () {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (itemHandler.getStackInSlot(i) != ItemStack.EMPTY) {
                return i;
            }
        }
        return 0;
    }

    public void dispense (Direction direction) {
        int slot = getDispenseSlot();
        itemHandler.setStackInSlot(slot,
                SuperDispenserBehaviour.evaluateInitial(itemHandler.getStackInSlot(slot), world, getPos(), direction, doContinue));
    }

}
