package acetil.inventula.common.tile;

import acetil.inventula.common.Inventula;
import acetil.inventula.common.block.ModBlocks;
import acetil.inventula.common.util.VecHelp;
import com.sun.javafx.geom.Vec2d;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CraftingDropperTile extends TileEntity {
    ItemStackHandler itemHandler;
    MaskedItemHandler maskedHandler;
    LazyOptional<IItemHandler> itemHandlerOptional;
    List<Integer> maskedSlots = new ArrayList<>();
    public CraftingDropperTile () {
        super(ModBlocks.CRAFTING_DROPPER_TILE.get());
        itemHandler = new ItemStackHandler(9) {
            @Override
            protected void onContentsChanged (int slot) {
                super.onContentsChanged(slot);
                CraftingDropperTile.this.markDirty();
            }
        };
        maskedHandler = new MaskedItemHandler(itemHandler);
        itemHandlerOptional = LazyOptional.of(() -> maskedHandler);
    }
    public void craft (Direction d) {

    }
    public void clearItems (Direction d) {

    }
    public void updateMask (Direction d, BlockRayTraceResult rayTrace) {
        if (rayTrace.getFace() != d.getOpposite()) {
            return;
        }
        Vec2d faceVec = VecHelp.getFaceVec(rayTrace.getHitVec().subtract(new Vec3d(pos.getX(), pos.getY(), pos.getZ())), d);
        //System.out.println("HitVec: " + faceVec);
        int slot = (int)Math.floor(faceVec.y * 3) * 3 + (int)Math.floor(faceVec.x * 3);
        if (maskedSlots.contains(slot)) {
            maskedHandler.removeMaskedSlot(slot);
            maskedSlots.remove(slot);
            Inventula.LOGGER.log(Level.DEBUG, "Unmasked slot {}", slot);
        } else {
            maskedHandler.addMaskedSlot(slot);
            maskedSlots.add(slot);
            Inventula.LOGGER.log(Level.DEBUG, "Masked slot {}", slot);
        }
    }
    @Override
    public void read (CompoundNBT compound) {
        super.read(compound);
        itemHandler.deserializeNBT(compound.getCompound("items"));
        setMaskedSlots(compound.getIntArray("masked"));
    }

    @Override
    public CompoundNBT write (CompoundNBT compound) {
        super.write(compound);
        compound.put("items", itemHandler.serializeNBT());
        compound.putIntArray("masked", getMaskedSlots());
        return compound;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability (@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandlerOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    private int[] getMaskedSlots () {
        int[] slots = new int[maskedSlots.size()];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = maskedSlots.get(i);
        }
        return slots;
    }
    private void setMaskedSlots (int[] slots) {
        maskedSlots.clear();
        maskedHandler.clearMaskedSlots();
        for (int slot : slots) {
            maskedSlots.add(slot);
            maskedHandler.addMaskedSlot(slot);
        }
    }
    public static class MaskedItemHandler implements IItemHandler {
        private int size;
        private ItemStackHandler itemHandler;
        private List<Integer> maskedSlots = new ArrayList<>();
        public MaskedItemHandler (ItemStackHandler handler) {
            itemHandler = handler;
            size = handler.getSlots();
        }
        @Override
        public int getSlots () {
            return size;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot (int slot) {
            return itemHandler.getStackInSlot(slot);
        }

        @Nonnull
        @Override
        public ItemStack insertItem (int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (maskedSlots.contains(slot) || itemHandler.getStackInSlot(slot).getCount() > 0) {
                return stack;
            }
            ItemStack stack1 = stack.copy();
            itemHandler.insertItem(slot, stack1.split(1), simulate);
            return stack1;
        }

        @Nonnull
        @Override
        public ItemStack extractItem (int slot, int amount, boolean simulate) {
            return itemHandler.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit (int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid (int slot, @Nonnull ItemStack stack) {
            return !maskedSlots.contains(slot);
        }
        public void addMaskedSlot (int slotNum) {
            maskedSlots.add(slotNum);
        }
        public void removeMaskedSlot (int slotNum) {
            if (maskedSlots.contains(slotNum)) {
                maskedSlots.remove(slotNum);
            }
        }
        public void clearMaskedSlots () {
            maskedSlots.clear();
        }
    }
}
