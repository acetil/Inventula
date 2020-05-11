package acetil.modjam.common.network;

import acetil.modjam.common.ModJam;
import acetil.modjam.common.particle.DispenserItemParticleData;
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
    int entityId = -1;
    ItemStack stack;
    int lifetime;
    public DispenserEntitySpawnMessage (ItemStack stack, Vec3d spawnPos, Vec3d initialVel, int lifetime) {
        this.stack = stack;
        this.spawnPos = spawnPos;
        this.initialVel = initialVel;
        this.lifetime = lifetime;
    }
    public DispenserEntitySpawnMessage (ItemStack stack, Vec3d spawnPos, Vec3d initialVel, int lifetime, int entityId) {
        this(stack, spawnPos, initialVel, lifetime);
        this.entityId = entityId;
    }
    public DispenserEntitySpawnMessage (PacketBuffer buf) {
        stack = buf.readItemStack();
        spawnPos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        initialVel = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        lifetime = buf.readInt();
        entityId = buf.readInt();
        //entityId = buf.readInt();
        //uuid = buf.readUniqueId();
    }
    public void writePacket (PacketBuffer buf) {
        buf.writeItemStack(stack);
        buf.writeDouble(spawnPos.getX());
        buf.writeDouble(spawnPos.getY());
        buf.writeDouble(spawnPos.getZ());
        buf.writeDouble(initialVel.getX());
        buf.writeDouble(initialVel.getY());
        buf.writeDouble(initialVel.getZ());
        buf.writeInt(lifetime);
        buf.writeInt(entityId);
        //buf.writeInt(entityId);
        //buf.writeUniqueId(uuid);
    }
    public void handlePacket (Supplier<NetworkEvent.Context> ctx) {
        ModJam.LOGGER.log(Level.DEBUG, "Handling spawn packet at {}!", System.currentTimeMillis());
        ctx.get().enqueueWork(() -> {
           ClientWorld world = Minecraft.getInstance().world;
           /*DispenserItemEntity entity = new DispenserItemEntity(ModEntities.DISPENSER_ITEM_ENTITY.get(), world);
           entity.setMotion(initialVel);
           entity.setPacketCoordinates(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
           entity.setEntityId(entityId);
           entity.setUniqueId(uuid);
           world.addEntity(entityId, entity);*/
            world.addParticle(new DispenserItemParticleData(stack, lifetime, entityId), true, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(),
                    initialVel.getX(), initialVel.getY(), initialVel.getZ());
           ModJam.LOGGER.log(Level.DEBUG, "Spawning particle at {}. Pos: ({}, {}, {})", System.currentTimeMillis(), spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());

        });
        ctx.get().setPacketHandled(true);
    }
}
