package acetil.inventula.common.network;

import acetil.inventula.client.WorldParticleTracker;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class DispenserParticleRemoveMessage {
    private int entityId;
    public DispenserParticleRemoveMessage (PacketBuffer buf) {
        this.entityId = buf.readInt();
    }
    public DispenserParticleRemoveMessage (int entityId) {
        this.entityId = entityId;
    }
    public int getEntityId () {
        return entityId;
    }
    public void writePacket (PacketBuffer buf) {
        buf.writeInt(entityId);
    }
    public void handlePacket (Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> WorldParticleTracker.removeParticle(entityId));
        ctx.get().setPacketHandled(true);
    }
}
