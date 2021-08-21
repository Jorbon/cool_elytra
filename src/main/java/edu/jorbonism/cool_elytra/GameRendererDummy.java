package edu.jorbonism.cool_elytra;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.MathHelper;

public class GameRendererDummy extends GameRenderer {
    private float viewDistance;
    private float skyDarkness;
    private float lastSkyDarkness;

    public GameRendererDummy(MinecraftClient client, ResourceManager resourceManager, BufferBuilderStorage buffers) {
        super(client, resourceManager, buffers);
    }

    public void update(float viewDistance, float skyDarkness, float lastSkyDarkness) {
        this.viewDistance = viewDistance;
        this.skyDarkness = skyDarkness;
        this.lastSkyDarkness = lastSkyDarkness;
    }

    public float getViewDistance() {
        return this.viewDistance;
    }

    public float getSkyDarkness(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.lastSkyDarkness, this.skyDarkness);
     }
}
