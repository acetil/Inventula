package acetil.inventula.common.tile;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Set;

public class CraftingWrapper extends CraftingInventory {
    private ItemStackHandler handler;
    public CraftingWrapper (ItemStackHandler handler) {
        super(null, 3, 3);
        this.handler = handler;
    }
    @Override
    public int getSizeInventory () {
        return handler.getSlots();
    }

    @Override
    public boolean isEmpty () {
        for (int i = 0; i < handler.getSlots(); i++) {
            if (handler.getStackInSlot(i) != ItemStack.EMPTY) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot (int index) {
        return handler.getStackInSlot(index);
    }

    @Override
    public ItemStack removeStackFromSlot (int index) {
        ItemStack stack = handler.getStackInSlot(index);
        handler.setStackInSlot(index, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public ItemStack decrStackSize (int index, int count) {
        return handler.extractItem(index, count, false);
    }

    @Override
    public void setInventorySlotContents (int index, ItemStack stack) {
        handler.setStackInSlot(index, stack);
    }

    @Override
    public void markDirty () {

    }

    @Override
    public boolean isUsableByPlayer (PlayerEntity player) {
        return true;
    }

    @Override
    public void clear () {
        for (int i = 0; i < handler.getSlots(); i++) {
            handler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @Override
    public int getHeight () {
        return 3;
    }

    @Override
    public int getWidth () {
        return 3;
    }

    @Override
    public void fillStackedContents (RecipeItemHelper helper) {
        for (int i = 0; i < handler.getSlots(); i++) {
            helper.accountStack(handler.getStackInSlot(i));
        }
    }

    @Override
    public int getInventoryStackLimit () {
        return 1;
    }

    @Override
    public void openInventory (PlayerEntity player) {

    }

    @Override
    public void closeInventory (PlayerEntity player) {

    }

    @Override
    public boolean isItemValidForSlot (int index, ItemStack stack) {
        return handler.isItemValid(index, stack);
    }

    @Override
    public int count (Item itemIn) {
        int count = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.getItem() == itemIn) {
                count += stack.getCount();
            }
        }
        return count;
    }

    @Override
    public boolean hasAny (Set<Item> set) {
        for (int i = 0; i < handler.getSlots(); i++) {
            if (set.contains(handler.getStackInSlot(i).getItem())) {
                return true;
            }
        }
        return false;
    }
}
