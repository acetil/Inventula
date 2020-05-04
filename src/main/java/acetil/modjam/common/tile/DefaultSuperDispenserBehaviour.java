package acetil.modjam.common.tile;

import acetil.modjam.common.ModJam;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

import java.util.function.Function;
import java.util.function.Predicate;

public class DefaultSuperDispenserBehaviour {
    public static final Function<ItemStack, ItemStack> ITEM_STACK_SHRINK = (ItemStack stack) -> {
        ItemStack stack1 = stack.copy();
        stack1.shrink(1);
        return stack1;
    };
    public static final Predicate<ItemStack> MATCH_NOT_EMPTY = (ItemStack stack) -> !stack.isEmpty();

    public static void addDefaultInitialBehaviours () {
        ModJam.LOGGER.log(Level.INFO, "Adding default initial dispenser behaviours!");
        SuperDispenserBehaviour.registerInitial(MATCH_NOT_EMPTY, ITEM_STACK_SHRINK,
                (ItemStack stack, World world, BlockPos pos, Direction direction) -> {
            ModJam.LOGGER.log(Level.DEBUG, "Dispensed item in direction: " + direction.getName());
            Vec3i vec1 = direction.getDirectionVec();
            ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5 + (double)vec1.getX(),
                    pos.getY() + 0.5 + (double)vec1.getY(), pos.getZ() + 0.5 + (double)vec1.getZ(), stack.copy().split(1));
            ModJam.LOGGER.log(Level.DEBUG, "Item entity position: {} {} {}", pos.getX() + 0.5 + (double)vec1.getX(),
                    pos.getY() + 0.5 + (double)vec1.getY(), pos.getZ() + 0.5 + (double)vec1.getZ());
            entity.setMotion((double)vec1.getX()*0, (double)vec1.getY() * 0, (double)vec1.getZ() * 0);
            world.addEntity(entity);
            return true;
        });
    }
}
