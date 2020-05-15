package acetil.inventula.common;

import acetil.inventula.client.ClientEvents;
import acetil.inventula.client.ClientSetup;
import acetil.inventula.common.block.ModBlocks;
import acetil.inventula.common.constants.Constants;
import acetil.inventula.common.entity.ModEntities;
import acetil.inventula.common.item.ModItems;
import acetil.inventula.common.network.PacketHandler;
import acetil.inventula.common.particle.ModParticles;
import acetil.inventula.common.tile.DefaultSuperDispenserBehaviour;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Constants.MODID)
public class Inventula {
    public static final Logger LOGGER = LogManager.getLogger();

    public Inventula () {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        ClientEvents.registerEvents(FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientEvents::attachCapabilities);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        registerDefferedRegisters();
    }

    private void setup (final FMLCommonSetupEvent event) {
        DefaultSuperDispenserBehaviour.addDefaultInitialBehaviours();
        DefaultSuperDispenserBehaviour.addDefaultEffectBehaviours();
        DefaultSuperDispenserBehaviour.addDefaultFluidBehaviours();
        DefaultSuperDispenserBehaviour.addDefaultEntityBehaviours();
        PacketHandler.registerMessages();
    }
    private void clientSetup (final FMLClientSetupEvent event) {
        ClientSetup.setup(event);
    }

    private void enqueueIMC (final InterModEnqueueEvent event) {

    }

    private void processIMC (final InterModProcessEvent event) {

    }
    private void registerDefferedRegisters () {
        ModBlocks.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());

        ModBlocks.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());

        ModBlocks.TILE_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());

        ModEntities.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());

        ModParticles.PARTICLES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
