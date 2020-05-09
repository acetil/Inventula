package acetil.modjam.common.particle;

import acetil.modjam.common.constants.Constants;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModParticles {
    public static DeferredRegister<ParticleType<?>> PARTICLES = new DeferredRegister<>(ForgeRegistries.PARTICLE_TYPES, Constants.MODID);

    public static RegistryObject<ParticleType<DispenserItemParticleData>> ITEM_PARTICLE = PARTICLES.register("item_particle",
            () -> new ParticleType<>(true, DispenserItemParticleData.DESERIALIZER));
}
