package acetil.modjam.common.tile;

import acetil.modjam.common.ModJam;
import acetil.modjam.common.util.QuadFunction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class SuperDispenserBehaviour {
    private static List<Behaviour> initialBehaviours = new ArrayList<>();
    private static List<Behaviour> effectBehaviours = new ArrayList<>();
    private static List<Behaviour> fluidBehaviours = new ArrayList<>();
    public static void registerInitial (Predicate<ItemStack> itemPredicate, Function<ItemStack, ItemStack> stackFunction,
                                 QuadFunction<ItemStack, World, BlockPos, Direction, Boolean> behaviour) {
        // for what happens when the item is dispensed
        // itemstack function is what happens TO THE SLOT, and triggers only when behaviour returns true
        // effects are added in order of increasing priority
        // boolean is for the success of the behaviour.
        initialBehaviours.add(0, new Behaviour(itemPredicate, stackFunction, behaviour));
    };

    public static void registerEffect (Predicate<ItemStack> itemPredicate, Function<ItemStack, ItemStack> stackFunction,
                                             QuadFunction<ItemStack, World, BlockPos, Direction, Boolean> behaviour) {
        // for what happens after the dispensed entity contacts a block
        // importantly, unlike initial itemstack function, this one is what happens to the dropped itemstack
        effectBehaviours.add(0, new Behaviour(itemPredicate, stackFunction, behaviour));
    }
    public static void registerFluid (Predicate<ItemStack> itemPredicate, Function<ItemStack, ItemStack> stackFunction,
                                      QuadFunction<ItemStack, World, BlockPos, Direction, Boolean> behaviour) {
        fluidBehaviours.add(0, new Behaviour(itemPredicate, stackFunction, behaviour));
    }
    public static ItemStack evaluateInitial (ItemStack stack, World world, BlockPos pos, Direction direction, boolean doContinue) {
        return evaluateBehaviours(stack, world, pos, direction, initialBehaviours, doContinue);
    }
    public static ItemStack evaluateEffect (ItemStack stack, World world, BlockPos pos, Direction direction) {
        return evaluateBehaviours(stack, world, pos, direction, effectBehaviours, true);
    }
    public static ItemStack evaluateEffectFluid (ItemStack stack, World world, BlockPos pos, Direction direction) {
        ModJam.LOGGER.log(Level.DEBUG, "Evaluting fluid effects!");
        return evaluateBehaviours(stack, world, pos, direction, fluidBehaviours, true);
    }
    public static ItemStack evaluateBehaviours (ItemStack stack, World world, BlockPos pos, Direction direction, List<Behaviour> behaviours,
                                               boolean doContinue) {
        ItemStack resultStack = stack;
        for (Behaviour b : behaviours) {
            if (b.itemPredicate.test(stack)) {
                boolean result = b.behaviour.apply(stack, world, pos, direction);
                if (result) {
                    resultStack = b.stackFunction.apply(stack);
                }
                if (result || !doContinue) {
                    break;
                }
            }
        }
        return resultStack;
    }
    private static class Behaviour {
        public Behaviour (Predicate<ItemStack> itemPredicate, Function<ItemStack, ItemStack> stackFunction,
                          QuadFunction<ItemStack, World, BlockPos, Direction, Boolean> behaviour) {
            this.itemPredicate = itemPredicate;
            this.stackFunction = stackFunction;
            this.behaviour = behaviour;
        }
        public Predicate<ItemStack> itemPredicate;
        public Function<ItemStack, ItemStack> stackFunction;
        public QuadFunction<ItemStack, World, BlockPos, Direction, Boolean> behaviour;
    }

}
