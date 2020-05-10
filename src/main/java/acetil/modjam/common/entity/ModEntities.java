package acetil.modjam.common.entity;

import acetil.modjam.common.constants.Constants;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModEntities {
    public static DeferredRegister<EntityType<?>> ENTITIES = new DeferredRegister<>(ForgeRegistries.ENTITIES, Constants.MODID);

    public static RegistryObject<EntityType<DispenserItemEntity>> DISPENSER_ITEM_ENTITY = ENTITIES.register("dispenser_item",
            () -> EntityType.Builder.<DispenserItemEntity>create(DispenserItemEntity::new, EntityClassification.MISC)
                    .size(0.25f, 0.25f).build("dispenser_item"));
}
