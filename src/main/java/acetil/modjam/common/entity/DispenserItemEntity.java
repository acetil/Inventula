package acetil.modjam.common.entity;

import acetil.modjam.common.ModJam;
import acetil.modjam.common.network.DispenserEntitySpawnMessage;
import acetil.modjam.common.network.PacketHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkDirection;
import org.apache.logging.log4j.Level;

public class DispenserItemEntity extends ProjectileItemEntity {
    private static final DataParameter<BlockPos> DISPENSER_POS =
            EntityDataManager.createKey(DispenserItemEntity.class, DataSerializers.BLOCK_POS);
    private Vec3d initialPos;
    private Vec3d initialVel;
    public DispenserItemEntity (EntityType<? extends ProjectileItemEntity> type, World worldIn) {
        super(type, worldIn);
    }
    public DispenserItemEntity (World worldIn, double x, double y, double z) {
        super(ModEntities.DISPENSER_ITEM_ENTITY.get(), worldIn);
        this.setPosition(x,y,z);
        initialPos = new Vec3d(x, y, z);
    }
    public DispenserItemEntity (World worldIn, Vec3d pos) {
        this(worldIn, pos.getX(), pos.getY(), pos.getZ());
    }
    public DispenserItemEntity setVelocityCustom (Vec3d vel) {
        this.setMotion(vel);
        initialVel = vel;
        return this;
    }
    public DispenserItemEntity setItemStack (ItemStack stack) {
        setItem(stack);
        return this;
    }
    @Override
    public void setItem(ItemStack stack) {
        this.getDataManager().set(ITEMSTACK_DATA, stack);
    }

    public DispenserItemEntity setDispenserPos (BlockPos pos) {
        this.getDataManager().set(DISPENSER_POS, pos);
        return this;
    }

    public DispenserItemEntity setPosition (Vec3d pos) {
        setPosition(pos.x, pos.y, pos.z);
        return this;
    }
    public BlockPos getDispenserPos () {
        return getDataManager().get(DISPENSER_POS);
    }
    @Override
    protected Item getDefaultItem () {
        return this.getDataManager().get(ITEMSTACK_DATA).getItem();
    }

    @Override
    protected void registerData () {
        super.registerData();
        this.getDataManager().register(DISPENSER_POS, BlockPos.ZERO);
    }

    @Override
    protected void onImpact (RayTraceResult result) {
        if (world.isRemote) {
            return;
        }
        if (result.getType() == RayTraceResult.Type.BLOCK) {
            BlockPos pos = ((BlockRayTraceResult)result).getPos();
            Direction d = ((BlockRayTraceResult)result).getFace();
            ItemStack stack = this.getDataManager().get(ITEMSTACK_DATA);
            ModJam.LOGGER.log(Level.DEBUG, "Dispenser Entity hit block at ({}, {}, {}) on face {}",
                    pos.getX(), pos.getY(), pos.getZ(), d.getName());
            killEntity(stack);
        } else if (result.getType() == RayTraceResult.Type.ENTITY) {
            // TODO
            ItemStack stack =this.getDataManager().get(ITEMSTACK_DATA);
            killEntity(stack);
        }
    }
    private void killEntity (ItemStack stack) {
        world.addEntity(new ItemEntity(world, this.lastTickPosX, this.lastTickPosY, this.lastTickPosZ,
                stack));
        this.remove();
    }

    @Override
    public void writeAdditional (CompoundNBT compound) {
        super.writeAdditional(compound);
        BlockPos pos = this.getDataManager().get(DISPENSER_POS);
        CompoundNBT posNBT = new CompoundNBT();
        posNBT.putInt("x", pos.getX());
        posNBT.putInt("y", pos.getY());
        posNBT.putInt("z", pos.getZ());
        compound.put("dispenser_pos", posNBT);
    }

    @Override
    public void readAdditional (CompoundNBT compound) {
        super.readAdditional(compound);
        CompoundNBT posNBT = compound.getCompound("dispenser_pos");
        this.getDataManager().set(DISPENSER_POS, new BlockPos(posNBT.getInt("x"), posNBT.getInt("y"), posNBT.getInt("z")));
    }

    @Override
    public IPacket<?> createSpawnPacket () {
        return PacketHandler.INSTANCE.toVanillaPacket(new DispenserEntitySpawnMessage(this.getDataManager().get(ITEMSTACK_DATA),
                initialPos, initialVel, 10), NetworkDirection.PLAY_TO_CLIENT);
    }
}
