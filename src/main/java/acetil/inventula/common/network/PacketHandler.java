package acetil.inventula.common.network;

import acetil.inventula.common.constants.Constants;
import net.minecraft.network.INetHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(Constants.MODID, "main"),
            () -> Constants.PROTOCOL_VERSION, Constants.PROTOCOL_VERSION::equals, Constants.PROTOCOL_VERSION::equals);
    private static int id = 0;
    public static void registerMessages () {
        INSTANCE.registerMessage(id++, DispenserEntitySpawnMessage.class, DispenserEntitySpawnMessage::writePacket,
                DispenserEntitySpawnMessage::new, DispenserEntitySpawnMessage::handlePacket);
        INSTANCE.registerMessage(id++, DispenserParticleRemoveMessage.class, DispenserParticleRemoveMessage::writePacket,
                DispenserParticleRemoveMessage::new, DispenserParticleRemoveMessage::handlePacket);
        INSTANCE.registerMessage(id++, SpawnerChangeActivationMessage.class, SpawnerChangeActivationMessage::writePacket,
                SpawnerChangeActivationMessage::new, SpawnerChangeActivationMessage::handlePacket);
        INSTANCE.registerMessage(id++, CrafterItemSlotChangeMessage.class, CrafterItemSlotChangeMessage::writePacket,
                CrafterItemSlotChangeMessage::new, CrafterItemSlotChangeMessage::handleMessage);
        INSTANCE.registerMessage(id++, CrafterMaskChangeMessage.class, CrafterMaskChangeMessage::writePacket,
                CrafterMaskChangeMessage::new, CrafterMaskChangeMessage::handlePacket);
    }
}
