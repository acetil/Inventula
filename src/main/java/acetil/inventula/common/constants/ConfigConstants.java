package acetil.inventula.common.constants;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ConfigConstants {
    public static ForgeConfigSpec SERVER_SPEC;
    public static Server SERVER;
    public static class Server {
        public ForgeConfigSpec.BooleanValue ALLOW_ITEM_HANDLER_DISPENSER;
        public ForgeConfigSpec.ConfigValue<Double> INITIAL_DISPENSER_ENTITY_SPEED;
        public Server (ForgeConfigSpec.Builder builder) {
            builder.comment("Inventula server side config");
            builder.push("inventula");
            ALLOW_ITEM_HANDLER_DISPENSER = builder.comment("Change this to allow or deny dispenser item entities to enter item storage blocks")
                    .define("allow_dispenser_item_handler_interaction", true);
            INITIAL_DISPENSER_ENTITY_SPEED = builder.comment("Change this to change the speed of the dispenser shooting items (only new behaviour)")
                    .define("dispenser_speed", 1.0);
            builder.pop();
        }
        public static void bakeConfigs () {
            Pair<Server, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(Server::new);
            SERVER = pair.getLeft();
            SERVER_SPEC = pair.getRight();
        }
    }
}
