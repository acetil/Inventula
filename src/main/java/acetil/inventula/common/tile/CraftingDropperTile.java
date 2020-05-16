package acetil.inventula.common.tile;

import acetil.inventula.common.Inventula;
import acetil.inventula.common.block.CraftingDropperBlock;
import acetil.inventula.common.block.ModBlocks;
import acetil.inventula.common.network.CrafterItemSlotChangeMessage;
import acetil.inventula.common.network.CrafterMaskChangeMessage;
import acetil.inventula.common.network.PacketHandler;
import acetil.inventula.common.util.VecHelp;
import com.sun.javafx.geom.Vec2d;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CraftingDropperTile extends TileEntity {
    private static final double DROP_SPEED = 0.2;
    private static final double POSITION_OFFSET = 0.5;
    private static final double DIRECTION_MULT = 0.6;
    private ItemStackHandler itemHandler;
    private MaskedItemHandler maskedHandler;
    private LazyOptional<IItemHandler> itemHandlerOptional;
    private Direction direction;
    private boolean gotDirection = false;
    boolean[] maskedSlots = new boolean[9];
    public CraftingDropperTile () {
        super(ModBlocks.CRAFTING_DROPPER_TILE.get());
        itemHandler = new ItemStackHandler(9) {
            @Override
            protected void onContentsChanged (int slot) {
                super.onContentsChanged(slot);
                CraftingDropperTile.this.markDirty();
                if (!world.isRemote) {
                    PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunkAt(pos)),
                            new CrafterItemSlotChangeMessage(slot, getStackInSlot(slot), pos));
                }
            }
        };
        maskedHandler = new MaskedItemHandler(itemHandler);
        itemHandlerOptional = LazyOptional.of(() -> maskedHandler);
        for (int i = 0; i < 9; i++) {
            maskedSlots[i] = false;
        }
    }

    @Override
    public CompoundNBT getUpdateTag () {
        return write(super.getUpdateTag());
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket () {
        return new SUpdateTileEntityPacket(pos, 1, write(new CompoundNBT()));
    }

    @Override
    public void handleUpdateTag (CompoundNBT tag) {
        read(tag);
    }

    @Override
    public void onDataPacket (NetworkManager net, SUpdateTileEntityPacket pkt) {
        read(pkt.getNbtCompound());
    }

    public void craft (Direction d) {
        CraftingWrapper wrapper = new CraftingWrapper(itemHandler);
        Optional<ICraftingRecipe> recipeOp = world.getRecipeManager().getRecipe(IRecipeType.CRAFTING,
                wrapper, world);
        if (recipeOp.isPresent()) {
            ICraftingRecipe recipe = recipeOp.get();
            ItemStack stack = recipe.getCraftingResult(wrapper);
            NonNullList<ItemStack> remaining = recipe.getRemainingItems(wrapper);
            dropItem(d, stack);
            for (ItemStack s : remaining) {
                if (s != ItemStack.EMPTY) {
                    dropItem(d, s);
                }
            }
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                itemHandler.setStackInSlot(i, ItemStack.EMPTY);
            }
            world.playEvent(1000, pos, 0);
            world.playEvent(2000, pos, d.getIndex());
        } else {
            world.playEvent(1001, pos, 0);
        }
    }
    public void clearItems (Direction d) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            dropItem(d, itemHandler.getStackInSlot(i));
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }
    public void dropItem (Direction d, ItemStack stack) {
        TileEntity te = world.getTileEntity(pos.offset(d));
        ItemStack stack1 = stack;
        if (te != null) {
            LazyOptional<IItemHandler> itemOp = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d.getOpposite());
            if (itemOp.isPresent()) {
                IItemHandler handler1 = itemOp.orElseGet(null);
                for (int i = 0; i < handler1.getSlots(); i++) {
                    if (handler1.isItemValid(i, stack1)) {
                        stack1 = handler1.insertItem(i, stack, false);
                    }
                    if (stack1 == ItemStack.EMPTY) {
                        return;
                    }
                }
            }
        }
        Vec3d spawnPos = getOffsetSpawnVec(pos, d);
        ItemEntity entity = new ItemEntity(world, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), stack1);
        entity.setMotion(new Vec3d(d.getDirectionVec()).scale(DROP_SPEED));
        world.addEntity(entity);
    }
    public void updateMask (Direction d, BlockRayTraceResult rayTrace) {
        if (rayTrace.getFace() != d.getOpposite()) {
            return;
        }
        Vec2d faceVec = VecHelp.getFaceVec(rayTrace.getHitVec().subtract(new Vec3d(pos.getX(), pos.getY(), pos.getZ())), d);
        //System.out.println("HitVec: " + faceVec);
        int slot = (int)Math.floor(faceVec.y * 3) * 3 + (int)Math.floor(faceVec.x * 3);
        if (maskedSlots[slot]) {
            maskedHandler.removeMaskedSlot(slot);
            maskedSlots[slot] = false;
            Inventula.LOGGER.log(Level.DEBUG, "Unmasked slot {}", slot);
        } else {
            maskedHandler.addMaskedSlot(slot);
            maskedSlots[slot] = true;
            Inventula.LOGGER.log(Level.DEBUG, "Masked slot {}", slot);
        }
        PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunkAt(pos)),
                new CrafterMaskChangeMessage(slot, maskedSlots[slot], pos));
    }
    public void setDirection () {
        this.direction = world.getBlockState(pos).get(CraftingDropperBlock.FACING);
        System.out.println("Set direction! Direction: " + direction);
    }
    public Direction getDirection () {
        if (!gotDirection) {
            setDirection();
            gotDirection = true;
        }
        return direction;
    }
    public void setStackInSlot (int slot, ItemStack stack) {
        itemHandler.setStackInSlot(slot, stack);
    }
    @Override
    public void read (CompoundNBT compound) {
        super.read(compound);
        itemHandler.deserializeNBT(compound.getCompound("items"));
        setMaskedSlots(compound.getByteArray("masked"));
    }

    @Override
    public CompoundNBT write (CompoundNBT compound) {
        super.write(compound);
        compound.put("items", itemHandler.serializeNBT());
        compound.putByteArray("masked", getMaskedSlots());
        return compound;
    }
    public boolean isSlotMasked (int slot) {
        return maskedSlots[slot];
    }
    public void updateMask (int slot, boolean mask) {
        maskedSlots[slot] = mask;
    }
    private byte[] getMaskedSlots () {
        byte[] bytes = new byte[9];
        for (int i = 0; i < maskedSlots.length; i++) {
            bytes[i] = (byte)(maskedSlots[i] ? 1 : 0);
        }
        return bytes;
    }
    private void setMaskedSlots (byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            maskedSlots[i] = bytes[i] > 0;
            if (maskedSlots[i]) {
                maskedHandler.addMaskedSlot(i);
            }
        }
    }
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability (@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandlerOptional.cast();
        }
        return super.getCapability(cap, side);
    }
    private static Vec3d getOffsetSpawnVec (BlockPos pos, Direction d) {
        Vec3i vec1 = d.getDirectionVec();
        return new Vec3d(pos.getX() + POSITION_OFFSET + vec1.getX() * DIRECTION_MULT,
                pos.getY() + POSITION_OFFSET + vec1.getY() * DIRECTION_MULT,
                pos.getZ() + POSITION_OFFSET + vec1.getZ() * DIRECTION_MULT);
    }
    public static class MaskedItemHandler implements IItemHandler {
        private int size;
        private ItemStackHandler itemHandler;
        private boolean[] maskedSlots = new boolean[9];
        public MaskedItemHandler (ItemStackHandler handler) {
            itemHandler = handler;
            size = handler.getSlots();
            clearMaskedSlots();
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
            if (maskedSlots[slot] || itemHandler.getStackInSlot(slot).getCount() > 0) {
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
            return !maskedSlots[slot];
        }
        public void addMaskedSlot (int slotNum) {
            maskedSlots[slotNum] = true;
        }
        public void removeMaskedSlot (int slotNum) {
            maskedSlots[slotNum] = false;
        }
        public void clearMaskedSlots () {
            for (int i = 0; i < 9; i++) {
                maskedSlots[i] = false;
            }
        }
    }
}
