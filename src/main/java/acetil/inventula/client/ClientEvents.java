package acetil.inventula.client;

import acetil.inventula.common.Inventula;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.apache.logging.log4j.Level;

public class ClientEvents {
    public static void registerEvents (IEventBus eventBus) {
        eventBus.addListener(ClientSetup::registerParticleFactories);
        //eventBus.addListener(ClientEvents::attachCapabilities);
    }
    public static void attachCapabilities (AttachCapabilitiesEvent<World> event) {
        Inventula.LOGGER.log(Level.DEBUG, "Adding client world capabilities!");

    }
}
