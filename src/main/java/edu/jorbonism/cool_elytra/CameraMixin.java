package edu.jorbonism.cool_elytra;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

//@Mixin(Camera.class)
public class CameraMixin {
	@Shadow private float pitch;
	@Shadow private float yaw;
	private float roll;
	@Shadow private Quaternion rotation;
	@Shadow private Vec3f horizontalPlane;
	@Shadow private Vec3f verticalPlane;
	@Shadow private Vec3f diagonalPlane;
	@Shadow private Entity focusedEntity;

	@Inject(at = @At("HEAD"), method = "setRotation(FF)V", cancellable = true)
	protected void setRotation(float yaw, float pitch, CallbackInfo ci) {
		this.pitch = pitch;
    	this.yaw = yaw;

		if (this.focusedEntity instanceof LivingEntity)
			this.roll = ((LivingEntity)this.focusedEntity).getRoll();
		else this.roll = 0.0f;

    	this.rotation.set(0.0F, 0.0F, 0.0F, 1.0F);

		// the single new line, idk what it does in game but just in case
		//this.rotation.hamiltonProduct(Vec3f.POSITIVE_Z.getDegreesQuaternion(this.roll));

    	this.rotation.hamiltonProduct(Vec3f.POSITIVE_Y.getDegreesQuaternion(-yaw));
    	this.rotation.hamiltonProduct(Vec3f.POSITIVE_X.getDegreesQuaternion(pitch));

    	this.horizontalPlane.set(0.0F, 0.0F, 1.0F);
    	this.horizontalPlane.rotate(this.rotation);
    	this.verticalPlane.set(0.0F, 1.0F, 0.0F);
    	this.verticalPlane.rotate(this.rotation);
    	this.diagonalPlane.set(1.0F, 0.0F, 0.0F);
    	this.diagonalPlane.rotate(this.rotation);
		ci.cancel();
	}
}
