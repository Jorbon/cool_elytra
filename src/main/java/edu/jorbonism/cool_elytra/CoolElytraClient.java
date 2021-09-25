package edu.jorbonism.cool_elytra;

import edu.jorbonism.cool_elytra.config.CoolElytraConfig;
import net.fabricmc.api.ClientModInitializer;

public class CoolElytraClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CoolElytraConfig.init("cool_elytra", CoolElytraConfig.class);
    }
}
