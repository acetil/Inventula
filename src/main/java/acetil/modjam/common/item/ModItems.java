package acetil.modjam.common.item;

import acetil.modjam.common.constants.Constants;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings("unused")
public class ModItems {
    public static DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, Constants.MODID);
    public static RegistryObject<Item> SPAWNER_CONVERTER = ITEMS.register("eternal_eye",
            () -> new SpawnerConverterItem(new Item.Properties()));
}
