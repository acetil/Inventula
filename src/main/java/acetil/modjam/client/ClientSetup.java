package acetil.modjam.client;

import acetil.modjam.client.particle.DispenserItemParticle;
import acetil.modjam.common.particle.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {
    public static void setup (final FMLClientSetupEvent event) {
    }
    public static void registerParticleFactories (final ParticleFactoryRegisterEvent event) {
        Minecraft.getInstance().particles.registerFactory(ModParticles.ITEM_PARTICLE.get(), DispenserItemParticle::new);
    }
}
