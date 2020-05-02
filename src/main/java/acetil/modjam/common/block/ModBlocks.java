package acetil.modjam.common.block;

import acetil.modjam.common.constants.Constants;
import acetil.modjam.common.tile.SuperDispenserTile;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
@SuppressWarnings("unused")
public class ModBlocks {
    public static DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, Constants.MODID);
    public static DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, Constants.MODID);
    public static DeferredRegister<TileEntityType<?>> TILE_ENTITIES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, Constants.MODID);

    public static RegistryObject<Block> SUPER_DISPENSER = BLOCKS.register("super_dispenser",
            () -> new SuperDispenserBlock(Block.Properties.create(Material.ROCK)));

    private static RegistryObject<Item> SUPER_DISPENSER_ITEM = ITEMS.register("super_dispenser",
            () -> new BlockItem(SUPER_DISPENSER.get(), new Item.Properties()));

    public static RegistryObject<TileEntityType<?>> SUPER_DISPENSER_TILE = TILE_ENTITIES.register("super_dispenser",
            () -> TileEntityType.Builder.create(SuperDispenserTile::new, SUPER_DISPENSER.get()).build(null));
}
