package edu.jorbonism.cool_elytra.mixin;

import edu.jorbonism.cool_elytra.CoolElytraClient;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Camera.class)
public abstract class CameraMixin {

	@ModifyArg(method = "setRotation", at = @At(value = "INVOKE", target =
		"Lorg/joml/Quaternionf;rotationYXZ(FFF)Lorg/joml/Quaternionf;"
	), index = 2)
	protected float setRotation(float angleY, float angleX, float angleZ) {
		float angle = (float)(CoolElytraClient.rollAngle * CoolElytraClient.TORAD);
		if (!CoolElytraClient.isFrontView) angle = -angle;

		return angleZ + angle;
	}
}
