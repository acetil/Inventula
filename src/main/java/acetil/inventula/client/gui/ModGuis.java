package acetil.inventula.client.gui;

import acetil.inventula.common.containers.ModContainers;
import net.minecraft.client.gui.ScreenManager;

public class ModGuis {
    public static void registerGuis () {
        ScreenManager.registerFactory(ModContainers.SUPER_DISPENSER.get(), SuperDispenserGui::new);
    }
}
