package acetil.inventula.common.containers;

import acetil.inventula.common.Inventula;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;

public class SuperDispenserContainer extends Container {
    private static final int OWN_SLOTS = 9;
    private static final int SLOT_SIZE = 18;
    private static final int OWN_X_START = 62;
    private static final int OWN_Y_START = 17;
    private static final int OWN_ROW_SIZE = 3;
    private static final int INVENTORY_ROWS = 3;
    private static final int INVENTORY_COLS = 9;
    private static final int HOTBAR_SIZE = 9;
    private static final int INVENTORY_START_X = 8;
    private static final int INVENTORY_START_Y = 84;
    private static final int HOTBAR_OFFSET = 58;
    public SuperDispenserContainer (int windowId, PlayerInventory inv, IItemHandler itemHandler, BlockPos pos) {
        super(ModContainers.SUPER_DISPENSER.get(), windowId);
        addSlots(itemHandler, inv);
    }
    public SuperDispenserContainer (int windowId, PlayerInventory inv, PacketBuffer extraData) {
        super(ModContainers.SUPER_DISPENSER.get(), windowId);
        TileEntity te = inv.player.world.getTileEntity(extraData.readBlockPos());
        if (te != null) {
            addSlots(te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                    .orElseGet(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY::getDefaultInstance), inv);
        } else {
            Inventula.LOGGER.log(Level.WARN, "Attempted to open super dispenser gui with null tile entity!");
        }
    }
    @Override
    public boolean canInteractWith (PlayerEntity playerIn) {
        return true;
    }
    private void addSlots (IItemHandler handler, PlayerInventory inv) {
        addOwnSlots(handler);
        addPlayerSlots(inv);
    }
    private void addOwnSlots (IItemHandler handler) {
        for (int i = 0; i < OWN_SLOTS; i++) {
            addSlot(new SlotItemHandler(handler, i, OWN_X_START + SLOT_SIZE * (i % OWN_ROW_SIZE),
                    OWN_Y_START + SLOT_SIZE * (i / OWN_ROW_SIZE)));
        }
    }
    private void addPlayerSlots (PlayerInventory inv) {
        for (int i = 0; i < INVENTORY_ROWS; i++) {
            for (int j = 0; j < INVENTORY_COLS; j++) {
                addSlot(new Slot(inv, j + i * INVENTORY_COLS + HOTBAR_SIZE, INVENTORY_START_X + j * SLOT_SIZE,
                        INVENTORY_START_Y + i * SLOT_SIZE));
            }
        }
        for (int i = 0; i < HOTBAR_SIZE; i++) {
            addSlot(new Slot(inv, i, INVENTORY_START_X + i * SLOT_SIZE,
                    INVENTORY_START_Y + HOTBAR_OFFSET));
        }
    }

    @Override
    public ItemStack transferStackInSlot (PlayerEntity playerIn, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = getSlot(index);
        if (slot.getHasStack()) {
            ItemStack stack1 = slot.getStack();
            stack = stack1.copy();
            if (index < OWN_SLOTS) {
                if (!this.mergeItemStack(stack1, OWN_SLOTS, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(stack1, 0, OWN_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
            if (stack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }
        return stack;
    }
}
