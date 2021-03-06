package acetil.inventula.client;

import acetil.inventula.client.particle.DispenserItemParticle;
import acetil.inventula.common.Inventula;
import net.minecraft.client.particle.Particle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class WorldParticleTracker {
    private static List<DispenserItemParticle> particles = new ArrayList<>();
    public static void addParticle (DispenserItemParticle p) {
        particles.add(p);
    }
    public static void removeParticle (int entityId) {
        particles = particles.stream().filter(Particle::isAlive).collect(Collectors.toList());
        for (int i = 0; i < particles.size(); i++) {
            if (particles.get(i).getEntityId() == entityId) {
                particles.get(i).setExpired();
                particles.remove(i);
                return;
            }
        }
        Inventula.LOGGER.log(Level.WARN, "Particle tracker received remove particle request for particle that no longer exists!");
    }
}
