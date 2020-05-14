package acetil.modjam.client;

import acetil.modjam.client.particle.DispenserItemParticle;
import acetil.modjam.client.renderer.DispenserItemRenderer;
import acetil.modjam.client.renderer.EternalSpawnerRenderer;
import acetil.modjam.common.block.ModBlocks;
import acetil.modjam.common.entity.DispenserItemEntity;
import acetil.modjam.common.entity.ModEntities;
import acetil.modjam.common.particle.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {
    public static void setup (final FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.DISPENSER_ITEM_ENTITY.get(), (EntityRendererManager m) ->
                new DispenserItemRenderer(m, Minecraft.getInstance().getItemRenderer()));
        //ClientRegistry.bindTileEntityRenderer(ModBlocks.ETERNAL_SPAWNER_TILE.get(), EternalSpawnerRenderer::new);
        //CapabilityParticleTracker.register();
    }
    public static void registerParticleFactories (final ParticleFactoryRegisterEvent event) {
        Minecraft.getInstance().particles.registerFactory(ModParticles.ITEM_PARTICLE.get(), DispenserItemParticle::new);
    }
}
