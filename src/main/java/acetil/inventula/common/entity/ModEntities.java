package acetil.inventula.common.entity;

import acetil.inventula.common.constants.Constants;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModEntities {
    public static DeferredRegister<EntityType<?>> ENTITIES = new DeferredRegister<>(ForgeRegistries.ENTITIES, Constants.MODID);

    public static RegistryObject<EntityType<DispenserItemEntity>> DISPENSER_ITEM_ENTITY = ENTITIES.register("dispenser_item",
            () -> EntityType.Builder.<DispenserItemEntity>create(DispenserItemEntity::new, EntityClassification.MISC)
                    .size(0.25f, 0.25f).immuneToFire().build("dispenser_item"));
}
