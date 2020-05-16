package acetil.inventula.common;

import acetil.inventula.client.ClientEvents;
import acetil.inventula.client.ClientSetup;
import acetil.inventula.client.ParticleSetup;
import acetil.inventula.common.block.ModBlocks;
import acetil.inventula.common.constants.ConfigConstants;
import acetil.inventula.common.constants.Constants;
import acetil.inventula.common.containers.ModContainers;
import acetil.inventula.common.entity.ModEntities;
import acetil.inventula.common.item.ModItems;
import acetil.inventula.common.network.PacketHandler;
import acetil.inventula.common.particle.ModParticles;
import acetil.inventula.common.tile.DefaultSuperDispenserBehaviour;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
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
    public static final ItemGroup CUSTOM_ITEM_GROUP = new ItemGroup(Constants.MODID) {
        @Override
        public ItemStack createIcon () {
            return new ItemStack(ModBlocks.CRAFTING_DROPPER_ITEM.get());
        }
    };
    public Inventula () {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientEvents::attachCapabilities);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        registerDefferedRegisters();
        ConfigConstants.Server.bakeConfigs();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ConfigConstants.SERVER_SPEC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ParticleSetup::setupParticles);
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

        ModContainers.CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
