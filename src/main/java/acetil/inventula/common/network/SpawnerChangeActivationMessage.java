package acetil.inventula.common.network;

import acetil.inventula.common.tile.EternalSpawnerTile;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SpawnerChangeActivationMessage {
    BlockPos pos;
    boolean newActivation;
    public SpawnerChangeActivationMessage (BlockPos pos, boolean newActivation) {
        this.pos = pos;
        this.newActivation = newActivation;
    }
    public SpawnerChangeActivationMessage (PacketBuffer buf) {
        this.pos = buf.readBlockPos();
        this.newActivation = buf.readBoolean();
    }
    public void writePacket (PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeBoolean(newActivation);
    }
    public void handlePacket (Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TileEntity te = Minecraft.getInstance().world.getTileEntity(this.pos);
            if (te instanceof EternalSpawnerTile) {
                ((EternalSpawnerTile) te).setActivated(newActivation);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
