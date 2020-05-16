package acetil.inventula.common.network;

import acetil.inventula.common.tile.CraftingDropperTile;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class CrafterMaskChangeMessage {
    private int slot;
    private boolean mask;
    private BlockPos pos;
    public CrafterMaskChangeMessage (int slot, boolean mask, BlockPos pos) {
        this.slot = slot;
        this.mask = mask;
        this.pos = pos;
    }
    public CrafterMaskChangeMessage (PacketBuffer buf) {
        slot = buf.readInt();
        mask = buf.readBoolean();
        pos = buf.readBlockPos();
    }
    public void writePacket (PacketBuffer buf) {
        buf.writeInt(slot);
        buf.writeBoolean(mask);
        buf.writeBlockPos(pos);
    }
    public void handlePacket (Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            System.out.println("Updating mask!");
            TileEntity te = Minecraft.getInstance().world.getTileEntity(pos);
            if (te instanceof CraftingDropperTile) {
                ((CraftingDropperTile) te).updateMask(slot, mask);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
