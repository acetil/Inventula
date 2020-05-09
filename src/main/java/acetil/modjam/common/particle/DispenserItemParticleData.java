package acetil.modjam.common.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.arguments.IRangeArgument;
import net.minecraft.command.arguments.ItemInput;
import net.minecraft.command.arguments.ItemParser;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;

public class DispenserItemParticleData implements IParticleData {
    private ItemStack stack;
    private int lifetime;
    public DispenserItemParticleData (ItemStack stack, int lifetime) {
        this.stack = stack;
        this.lifetime = lifetime;
    }
    public static final IParticleData.IDeserializer<DispenserItemParticleData> DESERIALIZER = new IDeserializer<DispenserItemParticleData>() {
        @Override
        public DispenserItemParticleData deserialize (ParticleType<DispenserItemParticleData> particleTypeIn, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            ItemParser itemParser = (new ItemParser(reader, false)).parse();
            ItemStack stack = (new ItemInput(itemParser.getItem(), itemParser.getNbt())).createStack(1, false);
            reader.expect(' ');
            int lifetime = reader.readInt();
            return new DispenserItemParticleData(stack, lifetime);
        }

        @Override
        public DispenserItemParticleData read (ParticleType<DispenserItemParticleData> particleTypeIn, PacketBuffer buffer) {
            return new DispenserItemParticleData(buffer.readItemStack(), buffer.readInt());
        }
    };
    public ItemStack getStack () {
        return stack;
    }
    public int getLifetime () {
        return lifetime;
    }
    @Override
    public ParticleType<?> getType () {
        return ModParticles.ITEM_PARTICLE.get();
    }

    @Override
    public void write (PacketBuffer buffer) {
        buffer.writeItemStack(stack);
        buffer.writeInt(lifetime);
    }

    @Override
    public String getParameters () {
        return ModParticles.ITEM_PARTICLE.getId().toString() + " " +
                new ItemInput(stack.getItem(), stack.getTag()).serialize() + " " + lifetime;
    }
}
