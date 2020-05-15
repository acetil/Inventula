package acetil.inventula.common.containers;

import acetil.inventula.common.constants.Constants;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModContainers {
    public static DeferredRegister<ContainerType<?>> CONTAINERS = new DeferredRegister<>(ForgeRegistries.CONTAINERS, Constants.MODID);
    public static RegistryObject<ContainerType<SuperDispenserContainer>> SUPER_DISPENSER = CONTAINERS.register("super_dispenser",
            () -> IForgeContainerType.create(SuperDispenserContainer::new));
}
