package acetil.modjam.common.item;

import acetil.modjam.common.block.EternalSpawnerBlock;
import acetil.modjam.common.block.ModBlocks;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpawnerConverterItem extends Item {
    public SpawnerConverterItem (Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType onItemUse (ItemUseContext context) {
        World world = context.getWorld();
        if (world.isRemote) {
            return ActionResultType.SUCCESS;
        } else {
            ItemStack stack = context.getItem();
            BlockPos pos = context.getPos();
            if (world.getBlockState(pos).getBlock() == Blocks.SPAWNER) {
                TileEntity te = world.getTileEntity(pos);
                if (te instanceof MobSpawnerTileEntity) {
                    CompoundNBT nbt = te.write(new CompoundNBT());
                    world.setBlockState(pos, ModBlocks.ETERNAL_SPAWNER.get().getDefaultState());
                    TileEntity te2 = world.getTileEntity(pos);
                    te2.read(nbt);
                    stack.shrink(1);
                    return ActionResultType.SUCCESS;
                }
            }
        }
        return super.onItemUse(context);
    }
}
