package acetil.modjam.common.network;

import acetil.modjam.common.constants.Constants;
import net.minecraft.network.PacketBuffer;
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
    }
}
