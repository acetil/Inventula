package acetil.inventula.common.tile;

import acetil.inventula.common.Inventula;
import acetil.inventula.common.constants.ConfigConstants;
import acetil.inventula.common.entity.DispenserItemEntity;
import acetil.inventula.common.util.QuadConsumer;
import acetil.inventula.common.util.QuadFunction;
import net.minecraft.block.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.ExperienceBottleEntity;
import net.minecraft.entity.item.FireworkRocketEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;

public class DefaultSuperDispenserBehaviour {
    private static final double DIRECTION_MULT = 0.5;
    private static final double POSITION_OFFSET = 0.5;
    private static final double DISPENSER_ENTITY_VELOCITY = 1;
    private static final float DEFAULT_DAMAGE = 1.0f;
    private static final float DEFAULT_KNOCKBACK = 0.1f;
    private static final float DEFAULT_KNOCKBACK_MULTIPLIER = 0.5f;
    private static final int FIRE_TIME_MULTIPLIER = 4;
    private static final float ARROW_SPEED = 1.1f;
    private static final float ARROW_INACCURACY = 6.0f;
    private static final float ARROW_Y_OFFSET = 0.1f;
    private static final float POTION_SPEED = ARROW_SPEED * 1.25f;
    private static final float POTION_INACCURACY = ARROW_INACCURACY * 0.5f;
    private static final float FIREWORK_Y_OFFSET = 0.2f;
    private static final float FIREWORK_SPEED = 0.5f;
    private static final float FIREWORK_INACCURACY = 1.0f;
    private static final float CHARGE_DOFF_MULT = 0.3f;
    private static final float CHARGE_VEL_RND_MULT = 0.05f;
    private static final int REQUIRED_HONEY_LEVEL = 5;
    private static final int DISPENSER_SOUND_ID = 1000;
    private static final int DISPENSER_PARTICLES_ID = 2000;
    private static final int BLOCK_DESTRUCTION_ID = 2001;
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
    public static final QuadConsumer<ItemStack, World, BlockPos, Direction> DEFAULT_ON_SUCCESS =
            (ItemStack stack, World world, BlockPos pos, Direction d) -> {
        world.playEvent(DISPENSER_SOUND_ID, pos, 0);
        world.playEvent(DISPENSER_PARTICLES_ID, pos, d.getIndex());
    };
    private static final QuadFunction<ItemStack, World, BlockPos, Direction, Boolean> FLUID_BEHAVIOUR =
            (ItemStack stack, World world, BlockPos pos, Direction d) -> {
        Inventula.LOGGER.log(Level.DEBUG, "Attempting to pick up fluid!");
        BlockState state = world.getBlockState(pos);
        Inventula.LOGGER.log(Level.DEBUG, "Block: {}", state.getBlock().getRegistryName().toString());
        if (state.getBlock() instanceof IBucketPickupHandler) {
            Inventula.LOGGER.log(Level.DEBUG, "Bucket pickup handler!");
            Fluid fluid = ((IBucketPickupHandler) state.getBlock()).pickupFluid(world, pos, state);
            if (fluid != Fluids.EMPTY) {
                world.addEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(fluid.getFilledBucket())));
                return true;
            }
        }
        return false;
    };
    public static final QuadFunction<ItemStack, World, BlockPos, Direction, Boolean> SPAWN_DISPENSER_ENTITY =
            (ItemStack stack, World world, BlockPos pos, Direction d) -> {
                // TODO: deal with case when block in front of dispenser
                Inventula.LOGGER.log(Level.DEBUG, "Spawning dispenser entity at {}!", System.currentTimeMillis());
                Vec3i vec1 = d.getDirectionVec();
                Vec3d velVec = new Vec3d(vec1).scale(ConfigConstants.SERVER.INITIAL_DISPENSER_ENTITY_SPEED.get());
                Vec3d offVec = getOffsetSpawnVec(pos, d);
                /*PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunkAt(pos)),
                        new DispenserEntitySpawnMessage(stack.copy().split(1), offVec, velVec, 10));*/
                world.addEntity(new DispenserItemEntity(world, offVec).setVelocityCustom(velVec)
                        .setItemStack(stack.copy().split(1)).setDispenserPos(pos));
                return true;
            };
    public static void addDefaultInitialBehaviours () {
        Inventula.LOGGER.log(Level.INFO, "Adding default initial dispenser behaviours!");
        SuperDispenserBehaviour.registerInitial(MATCH_NOT_EMPTY, ITEM_STACK_SHRINK,
                (ItemStack stack, World world, BlockPos pos, Direction direction) -> {
            Inventula.LOGGER.log(Level.DEBUG, "Dispensed item in direction: " + direction.getName());
            Vec3i vec1 = direction.getDirectionVec();
            Vec3d spawnVec = getOffsetSpawnVec(pos, direction);
            ItemEntity entity = new ItemEntity(world, spawnVec.getX(),spawnVec.getY(),
                    spawnVec.getZ(), stack.copy().split(1));
            entity.setMotion((double)vec1.getX()*0, (double)vec1.getY() * 0, (double)vec1.getZ() * 0);
            world.addEntity(entity);
            return true;
        });
        SuperDispenserBehaviour.registerInitial(MATCH_NOT_EMPTY, ITEM_STACK_SHRINK, SPAWN_DISPENSER_ENTITY);
        SuperDispenserBehaviour.registerInitial(matchesItem(Items.ARROW), ITEM_STACK_SHRINK,
                (ItemStack stack, World world, BlockPos pos, Direction d) -> {
            if (world.getBlockState(pos.add(d.getXOffset(), d.getYOffset(), d.getZOffset())).getBlock() != Blocks.AIR) {
                return false;
            }
            Vec3d spawnVec = getOffsetSpawnVec(pos, d);
            ArrowEntity entity = new ArrowEntity(world, spawnVec.getX(), spawnVec.getY(), spawnVec.getZ());
            entity.pickupStatus = AbstractArrowEntity.PickupStatus.ALLOWED;
            entity.shoot(d.getXOffset(), d.getYOffset() + ARROW_Y_OFFSET, d.getZOffset(), ARROW_SPEED, ARROW_INACCURACY);
            world.addEntity(entity);
            return true;
        });
        SuperDispenserBehaviour.registerInitial(matchesItem(Items.TIPPED_ARROW), ITEM_STACK_SHRINK,
                (ItemStack stack, World world, BlockPos pos, Direction d) -> {
            if (world.getBlockState(pos.offset(d)).getBlock() != Blocks.AIR) {
                return false;
            }
            Vec3d spawnVec = getOffsetSpawnVec(pos, d);
            ArrowEntity entity = new ArrowEntity(world, spawnVec.getX(), spawnVec.getY(), spawnVec.getZ());
            entity.setPotionEffect(stack);
            entity.pickupStatus = AbstractArrowEntity.PickupStatus.ALLOWED;
            entity.shoot(d.getXOffset(), d.getYOffset() + ARROW_Y_OFFSET, d.getZOffset(), ARROW_SPEED, ARROW_INACCURACY);
            world.addEntity(entity);
            return true;
        });
        SuperDispenserBehaviour.registerInitial(matchesItem(Items.SPECTRAL_ARROW), ITEM_STACK_SHRINK,
                (ItemStack stack, World world, BlockPos pos, Direction d) -> {
            if (world.getBlockState(pos.offset(d)).getBlock() != Blocks.AIR) {
                return false;
            }
            Vec3d spawnVec = getOffsetSpawnVec(pos, d);
            SpectralArrowEntity entity = new SpectralArrowEntity(world, spawnVec.getX(), spawnVec.getY(), spawnVec.getZ());
            entity.pickupStatus = AbstractArrowEntity.PickupStatus.ALLOWED;
            entity.shoot(d.getXOffset(), d.getYOffset() + ARROW_Y_OFFSET, d.getZOffset(), ARROW_SPEED, ARROW_INACCURACY);
            world.addEntity(entity);
            return true;
        });
        SuperDispenserBehaviour.registerInitial(matchesItem(Items.EGG), ITEM_STACK_SHRINK,
                (ItemStack stack, World world, BlockPos pos, Direction d) -> {
            if (world.getBlockState(pos.offset(d)).getBlock() != Blocks.AIR) {
                return false;
            }
            Vec3d spawnVec = getOffsetSpawnVec(pos, d);
            EggEntity entity = Util.make(new EggEntity(world, spawnVec.getX(), spawnVec.getY(), spawnVec.getZ()),
                    (EggEntity e) -> e.setItem(stack));
            entity.shoot(d.getXOffset(), d.getYOffset() + ARROW_Y_OFFSET, d.getZOffset(), ARROW_SPEED, ARROW_INACCURACY);
            world.addEntity(entity);
            return true;
        });
        SuperDispenserBehaviour.registerInitial(matchesItem(Items.SNOWBALL), ITEM_STACK_SHRINK, (ItemStack stack, World world, BlockPos pos, Direction d) -> {
            if (world.getBlockState(pos.offset(d)).getBlock() != Blocks.AIR) {
                return false;
            }
            Vec3d spawnVec = getOffsetSpawnVec(pos, d);
            SnowballEntity entity = Util.make(new SnowballEntity(world, spawnVec.getX(), spawnVec.getY(), spawnVec.getZ()),
                    (SnowballEntity e) -> e.setItem(stack));
            entity.shoot(d.getXOffset(), d.getYOffset() + ARROW_Y_OFFSET, d.getZOffset(), ARROW_SPEED, ARROW_INACCURACY);
            world.addEntity(entity);
            return true;
        });
        SuperDispenserBehaviour.registerInitial(matchesItem(Items.EXPERIENCE_BOTTLE), ITEM_STACK_SHRINK,
                (ItemStack stack, World world, BlockPos pos, Direction d) -> {
            if (world.getBlockState(pos.offset(d)).getBlock() != Blocks.AIR) {
                return false;
            }
            Vec3d spawnVec = getOffsetSpawnVec(pos, d);
            ExperienceBottleEntity entity = Util.make(new ExperienceBottleEntity(world, spawnVec.getX(), spawnVec.getY(), spawnVec.getZ()),
                    (ExperienceBottleEntity e) -> e.setItem(stack));
            entity.shoot(d.getXOffset(), d.getYOffset() + ARROW_Y_OFFSET, d.getZOffset(), POTION_SPEED, POTION_INACCURACY);
            world.addEntity(entity);
            return true;
        });
        SuperDispenserBehaviour.registerInitial((ItemStack stack) -> stack.getItem() == Items.SPLASH_POTION || stack.getItem() == Items.LINGERING_POTION,
                ITEM_STACK_SHRINK, (ItemStack stack, World world, BlockPos pos, Direction d) -> {
            if (world.getBlockState(pos.offset(d)).getBlock() != Blocks.AIR) {
                return false;
            }
            Vec3d spawnVec = getOffsetSpawnVec(pos, d);
            PotionEntity entity = Util.make(new PotionEntity(world, spawnVec.getX(), spawnVec.getY(), spawnVec.getZ()),
                    (PotionEntity e) -> e.setItem(stack));
            entity.shoot(d.getXOffset(), d.getYOffset() + ARROW_Y_OFFSET, d.getZOffset(), POTION_SPEED, POTION_INACCURACY);
            world.addEntity(entity);
            return true;
        });
        SuperDispenserBehaviour.registerInitial((ItemStack stack) -> stack.getItem() instanceof SpawnEggItem, ITEM_STACK_SHRINK,
                (ItemStack stack, World world, BlockPos pos, Direction d) -> {
            if (world.getBlockState(pos.offset(d)).getBlock() != Blocks.AIR) {
                return false;
            }
            ((SpawnEggItem)stack.getItem()).getType(stack.getTag())
                    .spawn(world, stack, null, pos.offset(d), SpawnReason.DISPENSER, d != Direction.UP, false);
            return true;
        });
        SuperDispenserBehaviour.registerInitial(matchesItem(Items.FIREWORK_ROCKET), ITEM_STACK_SHRINK,
                (ItemStack stack, World world, BlockPos pos, Direction d) -> {
            if (world.getBlockState(pos.offset(d)).getBlock() != Blocks.AIR) {
                return false;
            }
            Vec3d dVec = new Vec3d(d.getDirectionVec());
            FireworkRocketEntity entity = new FireworkRocketEntity(world, stack, dVec.getX() + pos.getX(),
                    pos.getY() + FIREWORK_Y_OFFSET, dVec.getZ() + pos.getZ(), true);
            entity.shoot(dVec.getX(), dVec.getY(), dVec.getZ(), FIREWORK_SPEED, FIREWORK_INACCURACY);
            world.addEntity(entity);
            return true;
        });
        SuperDispenserBehaviour.registerInitial(matchesItem(Items.FIRE_CHARGE), ITEM_STACK_SHRINK,
                (ItemStack stack, World world, BlockPos pos, Direction d) -> {
            if (world.getBlockState(pos.offset(d)).getBlock() != Blocks.AIR) {
                return false;
            }
            Vec3d dVec = new Vec3d(d.getDirectionVec());
            Vec3d offVec = getOffsetSpawnVec(pos, d);
            Random rand = world.getRandom();
            Vec3d startVec = offVec.add(dVec.scale(CHARGE_DOFF_MULT));
            Vec3d velVec = dVec.add(new Vec3d(rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian()).scale(CHARGE_VEL_RND_MULT));
            world.addEntity(Util.make(new SmallFireballEntity(world, startVec.getX(), startVec.getY(), startVec.getZ(),
                    velVec.getX(), velVec.getY(), velVec.getZ()), (SmallFireballEntity e) -> e.setStack(stack)));
            return true;
        });
        SuperDispenserBehaviour.registerInitial((ItemStack stack) -> stack.getItem() instanceof BucketItem && stack.getItem() != Items.BUCKET,
                (ItemStack stack) -> new ItemStack(Items.BUCKET), (ItemStack stack, World world, BlockPos pos, Direction d) -> {
            BlockState state = world.getBlockState(pos.offset(d));
            if (((BucketItem) stack.getItem()).tryPlaceContainedLiquid(null, world, pos.offset(d), null)) {
                ((BucketItem) stack.getItem()).onLiquidPlaced(world, stack, pos.offset(d));
                return true;
            }
            return false;
        });
        SuperDispenserBehaviour.registerInitial((ItemStack stack) -> stack.getItem() instanceof FlintAndSteelItem, (ItemStack stack) -> stack,
                (ItemStack stack, World world, BlockPos pos, Direction d) -> true);
        SuperDispenserBehaviour.registerInitial((ItemStack stack) -> stack.getItem() instanceof FlintAndSteelItem, DEGRADE,
                (ItemStack stack, World world, BlockPos pos, Direction d) -> {
            BlockPos newPos = pos.offset(d);
            BlockState state = world.getBlockState(newPos);
            if (FlintAndSteelItem.canSetFire(state, world, newPos)) {
                world.setBlockState(newPos, Blocks.FIRE.getDefaultState());
                return true;
            } else if (FlintAndSteelItem.isUnlitCampfire(state)) {
                world.setBlockState(newPos, state.with(BlockStateProperties.LIT, true));
                return true;
            } else if (state.isFlammable(world, newPos, d.getOpposite())) {
                state.catchFire(world, pos, d.getOpposite(), null);
                if (state.getBlock() instanceof TNTBlock) {
                    world.removeBlock(newPos, false);
                }
                return true;
            }
            return false;
        });
        SuperDispenserBehaviour.registerInitial(matchesItem(Items.TNT), ITEM_STACK_SHRINK,
                (ItemStack stack, World world, BlockPos pos, Direction d) -> {
            BlockPos offsetPos = pos.offset(d);
            world.addEntity(new TNTEntity(world, offsetPos.getX() + 0.5, offsetPos.getY(), offsetPos.getZ() + 0.5, null));
            return true;
        });

    }
    @SuppressWarnings("deprecation")
    public static void addDefaultEffectBehaviours () {
        Inventula.LOGGER.log(Level.INFO, "Adding default effect dispenser behaviours!");
        SuperDispenserBehaviour.registerEffect((ItemStack stack) -> stack.getItem() instanceof BlockItem, DESTROY,
                (ItemStack stack, World world, BlockPos pos, Direction d) -> {
            BlockPos newPos = pos.add(d.getDirectionVec());
            BlockItem item = (BlockItem) stack.getItem();
            ActionResultType result = item.tryPlace(new CustomBlockItemUseContext(world, stack, pos, d));
            return result.isSuccessOrConsume();
        });
        SuperDispenserBehaviour.registerEffect(MATCH_NOT_EMPTY, DEGRADE, (ItemStack stack, World world, BlockPos pos, Direction d) -> {
            BlockState state = world.getBlockState(pos);
            if (stack.getItem().getToolTypes(stack).contains(state.getHarvestTool()) &&
                    !(stack.getItem() instanceof PickaxeItem && !stack.canHarvestBlock(state))) {
                Block.spawnDrops(state, world, pos, null, FakePlayerFactory.getMinecraft((ServerWorld) world), stack);
                SoundType soundType = state.getSoundType();
                world.playSound(pos.getX(), pos.getY(), pos.getZ(), soundType.getBreakSound(),
                        SoundCategory.BLOCKS, soundType.getVolume(), soundType.getPitch(), false);
                world.playEvent(BLOCK_DESTRUCTION_ID, pos, Block.getStateId(state));
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
                return true;
            } else {
                return false;
            }
        });
        SuperDispenserBehaviour.registerEffect(matchesItem(Items.STICK), NO_CHANGE,
                (ItemStack stack, World world, BlockPos pos, Direction d) -> {
            BlockState state = world.getBlockState(pos);
            System.out.println(state.getHarvestTool());
            if (state.getHarvestTool() == null) {
                Block.spawnDrops(state, world, pos, null, FakePlayerFactory.getMinecraft((ServerWorld) world), stack);
                SoundType soundType = state.getSoundType();
                world.playSound(pos.getX(), pos.getY(), pos.getZ(), soundType.getBreakSound(),
                        SoundCategory.BLOCKS, soundType.getVolume(), soundType.getPitch(), false);
                world.playEvent(BLOCK_DESTRUCTION_ID, pos, Block.getStateId(state));
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
            if (te == null || !ConfigConstants.SERVER.ALLOW_ITEM_HANDLER_DISPENSER.get()) {
                return false;
            }
            LazyOptional<IItemHandler> itemHandlerOptional = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d);
            if (itemHandlerOptional.isPresent()) {
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
        SuperDispenserBehaviour.registerEffect((ItemStack stack) -> stack.getItem() == Items.BUCKET, DESTROY, FLUID_BEHAVIOUR);
        SuperDispenserBehaviour.registerEffect(matchesItem(Items.GLASS_BOTTLE), (ItemStack stack) -> new ItemStack(Items.HONEY_BOTTLE),
                (ItemStack stack, World world, BlockPos pos, Direction d) -> {
            BlockState state = world.getBlockState(pos);
            if (state.isIn(BlockTags.BEEHIVES) && state.get(BeehiveBlock.HONEY_LEVEL) >= REQUIRED_HONEY_LEVEL) {
                ((BeehiveBlock)state.getBlock()).takeHoney(world, state, pos, null, BeehiveTileEntity.State.BEE_RELEASED);
                return true;
            }
            return false;
        });
        SuperDispenserBehaviour.registerEffect((ItemStack stack) -> stack.getItem() instanceof ShearsItem, DEGRADE,
                (ItemStack stack, World world, BlockPos pos, Direction d) -> {
            BlockState state = world.getBlockState(pos);
            if (state.isIn(BlockTags.BEEHIVES) && state.get(BeehiveBlock.HONEY_LEVEL) >= REQUIRED_HONEY_LEVEL) {
                BeehiveBlock.dropHoneyComb(world, pos);
                ((BeehiveBlock)state.getBlock()).takeHoney(world, state, pos, null, BeehiveTileEntity.State.BEE_RELEASED);
                return true;
            }
            return false;
        });
    }
    public static void addDefaultFluidBehaviours () {
        SuperDispenserBehaviour.registerFluid((ItemStack stack) -> stack.getItem() == Items.BUCKET, DESTROY, FLUID_BEHAVIOUR);
        SuperDispenserBehaviour.registerFluid(matchesItem(Items.GLASS_BOTTLE), (ItemStack stack) -> PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), Potions.WATER),
                (ItemStack stack, World world, BlockPos pos, Direction d) -> world.getFluidState(pos).isTagged(FluidTags.WATER));
    }

    @SuppressWarnings("deprecation")
    public static void addDefaultEntityBehaviours () {

        SuperDispenserBehaviour.registerEntity(MATCH_NOT_EMPTY,
                DESTROY, (ItemStack stack, Entity entity, Direction d) -> {
            if (entity instanceof LivingEntity && ((LivingEntity) entity).attackable()) {
                ItemStack stack1 = stack.copy();
                damageEntity(stack, entity, d, 1);
                stack1.hitEntity((LivingEntity) entity, FakePlayerFactory.getMinecraft((ServerWorld) entity.world));
                entity.world.addEntity(new ItemEntity(entity.world, entity.getPosX(), entity.getPosY(), entity.getPosZ(), stack1));
                return true;
            }
            return false;
        });
        SuperDispenserBehaviour.registerEntity((ItemStack stack) -> stack.getItem() instanceof ShearsItem, DEGRADE,
                (ItemStack stack, Entity entity, Direction d) -> {
            if (entity instanceof IShearable && ((IShearable)entity).isShearable(stack, entity.getEntityWorld(), entity.getPosition())) {
                 List<ItemStack> stacks = ((IShearable)entity).onSheared(stack, entity.getEntityWorld(), entity.getPosition(),
                         EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack));
                 for (ItemStack s : stacks) {
                     entity.world.addEntity(new ItemEntity(entity.world, entity.getPosX(), entity.getPosY(), entity.getPosZ(), s));
                 }
                 return true;
            }
            return false;
        });
        SuperDispenserBehaviour.registerEntity(matchesItem(Items.BUCKET), DESTROY,
                (ItemStack stack, Entity entity, Direction d) -> {
            if (entity.getType() == EntityType.COW && !((CowEntity)entity).isChild()) {
                // no way of getting around this unfortunately. Probably have to add modded cow entities manually
                // hopefully mods have better ways of doing it
                // you can't milk those
                entity.world.addEntity(new ItemEntity(entity.getEntityWorld(), entity.getPosX(),
                        entity.getPosY(), entity.getPosZ(), new ItemStack(Items.MILK_BUCKET)));
                return true;
            }
            return false;
        });
    }
    private static void damageEntity (ItemStack stack, Entity entity, Direction d, float initialDmg) {
        float dmg = initialDmg;
        float addedDmg = 0;
        float totalMult = 1;
        for (AttributeModifier mod :  stack.getAttributeModifiers(EquipmentSlotType.MAINHAND)
                .get(SharedMonsterAttributes.ATTACK_DAMAGE.getName())) {
            AttributeModifier.Operation op = mod.getOperation();
            if (op == AttributeModifier.Operation.ADDITION) {
                addedDmg += mod.getAmount();
            } else if (op == AttributeModifier.Operation.MULTIPLY_BASE) {
                dmg *= mod.getAmount();
            } else {
                totalMult *= mod.getAmount();
            }
        }
        dmg = (dmg + addedDmg) * totalMult;
        float knockback = EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, stack) * DEFAULT_KNOCKBACK_MULTIPLIER
                + DEFAULT_KNOCKBACK;
        Vec3d dVec = new Vec3d(d.getDirectionVec());
        entity.attackEntityFrom(DamageSource.GENERIC, dmg);
        ((LivingEntity) entity).knockBack(FakePlayerFactory.getMinecraft((ServerWorld) entity.world), knockback,
                dVec.getX(), dVec.getZ());
        int fireTime = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, stack) * FIRE_TIME_MULTIPLIER;
        if (fireTime > 0) {
            entity.setFire(fireTime);
        }
    }
    private static Predicate<ItemStack> matchesItem (Item i) {
        return (ItemStack stack) -> stack.getItem() == i;
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
