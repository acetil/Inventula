package acetil.inventula.client;

import acetil.inventula.client.particle.DispenserItemParticle;
import acetil.inventula.common.particle.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class ParticleSetup {
    public static void setupParticles (final ParticleFactoryRegisterEvent event) {
        Minecraft.getInstance().particles.registerFactory(ModParticles.ITEM_PARTICLE.get(), DispenserItemParticle::new);
    }
}
