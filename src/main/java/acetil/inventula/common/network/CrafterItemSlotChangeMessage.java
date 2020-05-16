package acetil.inventula.common.network;

import acetil.inventula.common.tile.CraftingDropperTile;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.function.Supplier;

public class CrafterItemSlotChangeMessage {
    private int slot;
    private ItemStack stack;
    private BlockPos pos;
    public CrafterItemSlotChangeMessage (int slot, ItemStack stack, BlockPos pos) {
        this.slot = slot;
        this.stack = stack;
        this.pos = pos;
    }
    public CrafterItemSlotChangeMessage (PacketBuffer buf) {
        slot = buf.readInt();
        stack = buf.readItemStack();
        pos = buf.readBlockPos();
    }
    public void writePacket (PacketBuffer buf) {
        buf.writeInt(slot);
        buf.writeItemStack(stack);
        buf.writeBlockPos(pos);
    }
    public void handleMessage (Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TileEntity te = Minecraft.getInstance().world.getTileEntity(pos);
            if (te instanceof CraftingDropperTile) {
                ((CraftingDropperTile) te).setStackInSlot(slot, stack);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
