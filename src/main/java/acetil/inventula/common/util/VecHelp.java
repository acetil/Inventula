package acetil.inventula.common.util;

import com.sun.javafx.geom.Vec2d;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;

public class VecHelp {
    // as in proj_{onto}(vec)
    public static Vec3d project (Vec3d vec, Vec3d onto) {
        return onto.scale(onto.dotProduct(vec) / onto.lengthSquared());
    }
    public static Vec2d getFaceVec (Vec3d hitVec, Direction d) {
        Vec3d dVec = new Vec3d(d.getDirectionVec());
        Vec3d projVec = hitVec.subtract(VecHelp.project(hitVec, dVec));
        Vec2d finalVec;
        if (d.getAxis() == Direction.Axis.X) {
            finalVec = new Vec2d(1 - (projVec.getZ() * -1 * dVec.getX() + (1 + dVec.getX()) / 2), 1 - projVec.getY());
        } else if (d.getAxis() == Direction.Axis.Y) {
            finalVec = new Vec2d(projVec.getX(), projVec.getZ() * -1 * dVec.getY() + (1 + dVec.getY()) / 2);
        } else {
            finalVec = new Vec2d(projVec.getX() * - 1 * dVec.getZ() + (1 + dVec.getZ()) / 2, 1 - projVec.getY());
        }
        return finalVec;
    }
}
