package acetil.modjam.common.tile;

import acetil.modjam.common.ModJam;
import acetil.modjam.common.entity.DispenserItemEntity;
import acetil.modjam.common.network.DispenserEntitySpawnMessage;
import acetil.modjam.common.network.PacketHandler;
import acetil.modjam.common.particle.DispenserItemParticleData;
import acetil.modjam.common.util.QuadFunction;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;

public class DefaultSuperDispenserBehaviour {
    private static final double DIRECTION_MULT = 0.5;
    private static final double POSITION_OFFSET = 0.5;
    private static final double DISPENSER_ENTITY_VELOCITY = 1;
    public static final Function<ItemStack, ItemStack> ITEM_STACK_SHRINK = (ItemStack stack) -> {
        ItemStack stack1 = stack.copy();
        stack1.shrink(1);
        return stack1;
    };
    public static final Predicate<ItemStack> MATCH_NOT_EMPTY = (ItemStack stack) -> !stack.isEmpty();
    public static final Function<ItemStack, ItemStack> NO_CHANGE = (ItemStack stack) -> stack;
    public static final Function<ItemStack, ItemStack> DESTROY = (ItemStack stack) -> ItemStack.EMPTY;
    public static final Function<ItemStack, ItemStack> DEGRADE = (ItemStack stack) -> {
        stack.attemptDamageItem(1, new Random(), null);
        if (stack.getDamage() >= stack.getMaxDamage()) {
            stack.shrink(1);
        }
        return stack;
    };

    public static final QuadFunction<ItemStack, World, BlockPos, Direction, Boolean> SPAWN_DISPENSER_ENTITY =
            (ItemStack stack, World world, BlockPos pos, Direction d) -> {
                // TODO: deal with case when block in front of dispenser
                ModJam.LOGGER.log(Level.DEBUG, "Spawning dispenser entity at {}!", System.currentTimeMillis());
                Vec3i vec1 = d.getDirectionVec();
                Vec3d velVec = new Vec3d(vec1).scale(DISPENSER_ENTITY_VELOCITY);
                Vec3d offVec = getOffsetSpawnVec(pos, d);
                /*PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunkAt(pos)),
                        new DispenserEntitySpawnMessage(stack.copy().split(1), offVec, velVec, 10));*/
                world.addEntity(new DispenserItemEntity(world, offVec).setVelocityCustom(velVec)
                        .setItemStack(stack.copy().split(1)).setDispenserPos(pos));
                return true;
            };
    public static void addDefaultInitialBehaviours () {
        ModJam.LOGGER.log(Level.INFO, "Adding default initial dispenser behaviours!");
        SuperDispenserBehaviour.registerInitial(MATCH_NOT_EMPTY, ITEM_STACK_SHRINK,
                (ItemStack stack, World world, BlockPos pos, Direction direction) -> {
            ModJam.LOGGER.log(Level.DEBUG, "Dispensed item in direction: " + direction.getName());
            Vec3i vec1 = direction.getDirectionVec();
            Vec3d spawnVec = getOffsetSpawnVec(pos, direction);
            ItemEntity entity = new ItemEntity(world, spawnVec.getX(),spawnVec.getY(),
                    spawnVec.getZ(), stack.copy().split(1));
            entity.setMotion((double)vec1.getX()*0, (double)vec1.getY() * 0, (double)vec1.getZ() * 0);
            world.addEntity(entity);
            return true;
        });
        SuperDispenserBehaviour.registerInitial(MATCH_NOT_EMPTY, ITEM_STACK_SHRINK, SPAWN_DISPENSER_ENTITY);
    }
    @SuppressWarnings("deprecation")
    public static void addDefaultEffectBehaviours () {
        ModJam.LOGGER.log(Level.INFO, "Adding default effect dispenser behaviours!");
        SuperDispenserBehaviour.registerEffect((ItemStack stack) -> stack.getItem() instanceof BlockItem, DESTROY,
                (ItemStack stack, World world, BlockPos pos, Direction d) -> {
            ModJam.LOGGER.log(Level.DEBUG, "Attempting to add block!");
            BlockPos newPos = pos.add(d.getDirectionVec());
            BlockItem item = (BlockItem) stack.getItem();
            ActionResultType result = item.tryPlace(new CustomBlockItemUseContext(world, stack, pos, d));
            return result.isSuccessOrConsume();
        });
        SuperDispenserBehaviour.registerEffect(MATCH_NOT_EMPTY, DEGRADE, (ItemStack stack, World world, BlockPos pos, Direction d) -> {
            BlockState state = world.getBlockState(pos);
            if (stack.getItem().getToolTypes(stack).contains(state.getHarvestTool()) && stack.canHarvestBlock(state)) {
                Block.spawnDrops(state, world, pos, null, null, stack);
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
                return true;
            } else {
                return false;
            }
        });
        SuperDispenserBehaviour.registerEffect((ItemStack stack) -> stack.getItem() instanceof HoeItem, DEGRADE,
                (ItemStack stack, World world, BlockPos pos, Direction d) -> {
            int hook = ForgeEventFactory.onHoeUse(new CustomBlockItemUseContext(world, stack, pos, d));
            if (hook != 0) {
                return hook > 0;
            }
            if (d != Direction.DOWN && world.isAirBlock(pos.up())) {
                BlockState state = HoeItem.HOE_LOOKUP.get(world.getBlockState(pos).getBlock());
                if (state != null) {
                    world.setBlockState(pos, state);
                    return true;
                }
            }
            return false;
        });
        SuperDispenserBehaviour.registerEffect(MATCH_NOT_EMPTY, DESTROY, (ItemStack stack, World world, BlockPos pos, Direction d) -> {
            TileEntity te = world.getTileEntity(pos);
            if (te == null) {
                return false;
            }
            LazyOptional<IItemHandler> itemHandlerOptional = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d);
            if (itemHandlerOptional.isPresent()) {
                ModJam.LOGGER.log(Level.DEBUG, "Has item handler!");
                IItemHandler itemHandler = itemHandlerOptional.orElse(null);
                ItemStack stack1 = stack.copy();
                int i = 0;
                while (!stack1.isEmpty() && i < itemHandler.getSlots()) {
                    if (itemHandler.isItemValid(i, stack1)) {
                        stack1 = itemHandler.insertItem(i, stack1, true);
                    }
                    i++;
                }
                if (stack1.isEmpty()) {
                    i = 0;
                    stack1 = stack;
                    while (!stack1.isEmpty() && i < itemHandler.getSlots()) {
                        if (itemHandler.isItemValid(i, stack1)) {
                            stack1 = itemHandler.insertItem(i, stack1, false);
                        }
                        i++;
                    }
                    return true;
                }
            }
            return false;
        });
        SuperDispenserBehaviour.registerEffect((ItemStack stack) -> stack.getItem() instanceof BoneMealItem, DESTROY,
                (ItemStack stack, World world, BlockPos pos, Direction d) -> BoneMealItem.applyBonemeal(stack.copy(), world, pos));

    }
    private static Vec3d getOffsetSpawnVec (BlockPos pos, Direction d) {
        Vec3i vec1 = d.getDirectionVec();
        return new Vec3d(pos.getX() + POSITION_OFFSET + vec1.getX() * DIRECTION_MULT,
                pos.getY() + POSITION_OFFSET + vec1.getY() * DIRECTION_MULT,
                pos.getZ() + POSITION_OFFSET + vec1.getZ() * DIRECTION_MULT);
    }
    private static class CustomBlockItemUseContext extends BlockItemUseContext {
        // NEVER use the hitVec, it isn't accurate!
        public CustomBlockItemUseContext (World worldIn, @Nullable PlayerEntity player, Hand handIn, ItemStack heldItem, BlockRayTraceResult rayTraceResultIn) {
            super(worldIn, player, handIn, heldItem, rayTraceResultIn);
        }
        public CustomBlockItemUseContext (World world, ItemStack stack, BlockPos pos, Direction d) {
            this(world, null, Hand.MAIN_HAND, stack, new BlockRayTraceResult(new Vec3d(0, 0, 0), d, pos, false));
        }

        @Override
        public Direction[] getNearestLookingDirections () {
            return new Direction[]{rayTraceResult.getFace().getOpposite()};
        }
    }
}
