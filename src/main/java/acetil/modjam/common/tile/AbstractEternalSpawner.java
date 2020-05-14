package acetil.modjam.common.tile;

import net.minecraft.world.spawner.AbstractSpawner;

public abstract class AbstractEternalSpawner extends AbstractSpawner {
    private boolean isActivated = true;
    @Override
    protected boolean isActivated () {
        return isActivated;
    }
    public void setIsActivated (boolean isActivated) {
        this.isActivated = isActivated;
    }
}
