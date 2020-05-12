package acetil.modjam.common.tile;

import acetil.modjam.common.ModJam;
import acetil.modjam.common.util.QuadFunction;
import acetil.modjam.common.util.TriFunction;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class SuperDispenserBehaviour {
    private static final List<Behaviour<Behaviour.QuadArg<World, BlockPos, Direction>>> initialBehaviours = new ArrayList<>();
    private static final List<Behaviour<Behaviour.QuadArg<World, BlockPos, Direction>>> effectBehaviours = new ArrayList<>();
    private static final List<Behaviour<Behaviour.QuadArg<World, BlockPos, Direction>>> fluidBehaviours = new ArrayList<>();
    private static final List<Behaviour<Behaviour.TriArg<Entity, Direction>>> entityBehaviours = new ArrayList<>();
    public static void registerInitial (Predicate<ItemStack> itemPredicate, Function<ItemStack, ItemStack> stackFunction,
                                 QuadFunction<ItemStack, World, BlockPos, Direction, Boolean> behaviour) {
        // for what happens when the item is dispensed
        // itemstack function is what happens TO THE SLOT, and triggers only when behaviour returns true
        // effects are added in order of increasing priority
        // boolean is for the success of the behaviour.
        initialBehaviours.add(0, new Behaviour<>(itemPredicate, stackFunction, composeQuadArg(behaviour)));
    }

    public static void registerEffect (Predicate<ItemStack> itemPredicate, Function<ItemStack, ItemStack> stackFunction,
                                             QuadFunction<ItemStack, World, BlockPos, Direction, Boolean> behaviour) {
        // for what happens after the dispensed entity contacts a block
        // importantly, unlike initial itemstack function, this one is what happens to the dropped itemstack
        effectBehaviours.add(0, new Behaviour<>(itemPredicate, stackFunction, composeQuadArg(behaviour)));
    }
    public static void registerFluid (Predicate<ItemStack> itemPredicate, Function<ItemStack, ItemStack> stackFunction,
                                      QuadFunction<ItemStack, World, BlockPos, Direction, Boolean> behaviour) {
        fluidBehaviours.add(0, new Behaviour<>(itemPredicate, stackFunction, composeQuadArg(behaviour)));
    }
    public static void registerEntity (Predicate<ItemStack> itemPredicate, Function<ItemStack, ItemStack> stackFunction,
                                       TriFunction<ItemStack, Entity, Direction, Boolean> behaviour) {
        entityBehaviours.add(0, new Behaviour<>(itemPredicate, stackFunction, composeTriArg(behaviour)));
    }

    public static ItemStack evaluateInitial (ItemStack stack, World world, BlockPos pos, Direction direction, boolean doContinue) {
        return evaluateBehaviours(new Behaviour.QuadArg<>(stack, world, pos, direction), initialBehaviours, doContinue);
    }
    public static ItemStack evaluateEffect (ItemStack stack, World world, BlockPos pos, Direction direction) {
        return evaluateBehaviours(new Behaviour.QuadArg<>(stack, world, pos, direction), effectBehaviours, true);
    }
    public static ItemStack evaluateEffectFluid (ItemStack stack, World world, BlockPos pos, Direction direction) {
        ModJam.LOGGER.log(Level.DEBUG, "Evaluting fluid effects!");
        return evaluateBehaviours(new Behaviour.QuadArg<>(stack, world, pos, direction), fluidBehaviours, true);
    }
    public static ItemStack evaluateEntity (ItemStack stack, Entity entity, Direction direction) {
        return evaluateBehaviours(new Behaviour.TriArg<>(stack, entity, direction), entityBehaviours, true);
    }
    public static <T extends IHasStack> ItemStack evaluateBehaviours (T t, List<Behaviour<T>> behaviours,
                                               boolean doContinue) {
        ItemStack resultStack = t.getStack();
        for (Behaviour<T> b : behaviours) {
            if (b.testStack(t.getStack())) {
                boolean result = b.tryEvaluateBehaviour(t);
                if (result) {
                    resultStack = b.applyStackFunction(t.getStack());
                }
                if (result || !doContinue) {
                    break;
                }
            }
        }
        return resultStack;
    }
    private static <T, U, V> Function<Behaviour.QuadArg<T, U, V>, Boolean> composeQuadArg (QuadFunction<ItemStack, T, U, V, Boolean> func) {
        return (Behaviour.QuadArg<T, U, V> quadArg) -> func.apply(quadArg.stack, quadArg.t, quadArg.u, quadArg.v);
    }
    private static <T, U> Function<Behaviour.TriArg<T, U>, Boolean> composeTriArg (TriFunction<ItemStack, T, U, Boolean> func) {
        return (Behaviour.TriArg<T, U> triArg) -> func.apply(triArg.stack, triArg.t, triArg.u);
    }
    private interface IHasStack {
        ItemStack getStack ();
    }
    private static class Behaviour<T> {
        private final Predicate<ItemStack> itemPredicate;
        private final Function<ItemStack, ItemStack> stackFunction;
        private final Function<T, Boolean> behaviour;
        public Behaviour (Predicate<ItemStack> itemPredicate, Function<ItemStack, ItemStack> stackFunction,
                          Function<T, Boolean> behaviour) {
            this.itemPredicate = itemPredicate;
            this.stackFunction = stackFunction;
            this.behaviour = behaviour;
        }
        public boolean testStack (ItemStack stack) {
            return itemPredicate.test(stack);
        }
        public ItemStack applyStackFunction (ItemStack stack) {
            return stackFunction.apply(stack);
        }
        public boolean tryEvaluateBehaviour (T t) {
            return behaviour.apply(t);
        }
        public static class QuadArg <T, U, V> implements IHasStack{
            public final ItemStack stack;
            public final T t;
            public final U u;
            public final V v;
            public QuadArg (ItemStack stack, T t, U u, V v) {
                this.stack = stack;
                this.t = t;
                this.u = u;
                this.v = v;
            }

            @Override
            public ItemStack getStack () {
                return stack;
            }
        }
        public static class TriArg <T, U> implements IHasStack{
            public final ItemStack stack;
            public final T t;
            public final U u;
            public TriArg (ItemStack stack, T t, U u) {
                this.stack = stack;
                this.t = t;
                this.u = u;
            }

            @Override
            public ItemStack getStack () {
                return stack;
            }
        }
    }
}
