package acetil.modjam.common.network;

import acetil.modjam.common.ModJam;
import acetil.modjam.common.entity.DispenserItemEntity;
import acetil.modjam.common.entity.ModEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Supplier;

public class DispenserEntitySpawnMessage {
    Vec3d spawnPos;
    Vec3d initialVel;
    UUID uuid;
    int entityId;
    public DispenserEntitySpawnMessage (DispenserItemEntity entity) {
        spawnPos = entity.getPositionVec();
        initialVel = entity.getMotion();
        entityId = entity.getEntityId();
        uuid = entity.getUniqueID();
    };
    public DispenserEntitySpawnMessage (PacketBuffer buf) {
        spawnPos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        initialVel = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        entityId = buf.readInt();
        uuid = buf.readUniqueId();
    }
    public void writePacket (PacketBuffer buf) {
        buf.writeDouble(spawnPos.getX());
        buf.writeDouble(spawnPos.getY());
        buf.writeDouble(spawnPos.getZ());
        buf.writeDouble(initialVel.getX());
        buf.writeDouble(initialVel.getY());
        buf.writeDouble(initialVel.getZ());
        buf.writeInt(entityId);
        buf.writeUniqueId(uuid);
    }
    public void handlePacket (Supplier<NetworkEvent.Context> ctx) {
        ModJam.LOGGER.log(Level.DEBUG, "Handling spawn packet!");
        ctx.get().enqueueWork(() -> {
           ClientWorld world = Minecraft.getInstance().world;
           DispenserItemEntity entity = new DispenserItemEntity(ModEntities.DISPENSER_ITEM_ENTITY.get(), world)
                   .setVelocityCustom(initialVel);
           entity.setPacketCoordinates(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
           entity.setEntityId(entityId);
           entity.setUniqueId(uuid);
           world.addEntity(entityId, entity);
        });
        ctx.get().setPacketHandled(true);
    }
}
