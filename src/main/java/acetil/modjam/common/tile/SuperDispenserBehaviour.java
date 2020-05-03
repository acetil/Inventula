package acetil.modjam.common.tile;

import acetil.modjam.common.util.TriFunction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class SuperDispenserBehaviour {
    private static List<InitialBehaviour> initialBehaviours;
    public static void registerInitial (Predicate<ItemStack> itemPredicate, Function<ItemStack, ItemStack> stackFunction,
                                 TriFunction<World, BlockPos, Direction, Boolean> behaviour) {
        initialBehaviours.add(0, new InitialBehaviour(itemPredicate, stackFunction, behaviour));
    };

    private static class InitialBehaviour {
        public InitialBehaviour (Predicate<ItemStack> itemPredicate, Function<ItemStack, ItemStack> stackFunction,
                                 TriFunction<World, BlockPos, Direction, Boolean> behaviour) {
            this.itemPredicate = itemPredicate;
            this.stackFunction = stackFunction;
            this.behaviour = behaviour;
        }
        public Predicate<ItemStack> itemPredicate;
        public Function<ItemStack, ItemStack> stackFunction;
        public TriFunction<World, BlockPos, Direction, Boolean> behaviour;
    }

}
