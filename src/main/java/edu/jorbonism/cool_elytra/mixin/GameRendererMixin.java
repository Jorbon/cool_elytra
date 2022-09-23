package edu.jorbonism.cool_elytra.mixin;

import edu.jorbonism.cool_elytra.CoolElytraClient;
import edu.jorbonism.cool_elytra.config.CoolElytraConfig;
import edu.jorbonism.cool_elytra.config.CoolElytraConfig.Mode;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

	@Final @Shadow private MinecraftClient client;
	
	@Inject(at = @At("HEAD"), method = "renderWorld")
	public void renderWorld(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
		if (CoolElytraConfig.modMode == Mode.CLASSIC) {

			// original camera rolling
			if (this.client.player != null && this.client.player.isFallFlying() && !(this.client.player.isTouchingWater() || this.client.player.isInLava())) {
				Vec3d facing = this.client.player.getRotationVecClient();
				Vec3d velocity = this.getPlayerInstantaneousVelocity(tickDelta);
				double horizontalFacing2 = facing.horizontalLengthSquared();
				double horizontalSpeed2 = velocity.horizontalLengthSquared();
				float rollAngle = 0.0f;
				if (horizontalFacing2 > 0.0D && horizontalSpeed2 > 0.0D) {
					double dot = (velocity.x * facing.x + velocity.z * facing.z) / Math.sqrt(horizontalFacing2 * horizontalSpeed2); // acos(dot) = angle between facing and velocity vectors
					if (dot >= 1.0) dot = 1.0; // hopefully fix world disappearing occassionally which I assume would be due to ^^^ sqrt precision limits
					else if (dot <= -1.0) dot = -1.0;
					double direction = Math.signum(velocity.x * facing.z - velocity.z * facing.x); // = which side laterally each vector is on
					rollAngle = (float)(Math.atan(Math.sqrt(horizontalSpeed2) * Math.acos(dot) * CoolElytraConfig.wingPower) * direction * CoolElytraClient.TODEG);
				}
				// smooth changes to the roll angle and remove the bumpy crunchy
				rollAngle = (float)((1.0 - CoolElytraConfig.rollSmoothing) * rollAngle + CoolElytraConfig.rollSmoothing * CoolElytraClient.lastRollAngle);
				CoolElytraClient.lastRollAngle = rollAngle;
				
				matrix.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(rollAngle));
			} else {
				CoolElytraClient.lastRollAngle = 0.0f;
			}

		} else if (CoolElytraConfig.modMode == Mode.REALISTIC) {

			// real rolling flight
			if (this.client.player == null || !this.client.player.isFallFlying()) return;

			double angle = -Math.acos(CoolElytraClient.left.dotProduct(CoolElytraClient.getAssumedLeft(this.client.player.getYaw()))) * CoolElytraClient.TODEG;
			if (CoolElytraClient.left.getY() < 0) angle *= -1;
			matrix.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion((float)angle));
			CoolElytraClient.lastRollAngle = (float)angle;

		}
		

		
	}

	public Vec3d getPlayerInstantaneousVelocity(float tickDelta) {
		// copying over the important bits of elytra flight code and cleaning it up
		// this is to smooth some jitteriness caused by rotation being frame-accurate but velocity only changing each tick

		assert this.client.player != null;
		Vec3d velocity = this.client.player.getVelocity();
		if (tickDelta < 0.01f)
			return velocity;
		
		double newvx = velocity.x;
		double newvy = velocity.y;
		double newvz = velocity.z;
		double gravity = 0.08;
		
		Vec3d facing = this.client.player.getRotationVector();
		float pitchRadians = this.client.player.getPitch() * 0.017453292f;
		double horizontalFacing2 = facing.horizontalLengthSquared();
		double horizontalFacing = Math.sqrt(horizontalFacing2);
		double horizontalSpeed = velocity.horizontalLength();

		newvy += gravity * (-1.0 + horizontalFacing2 * 0.75);

		if (horizontalFacing > 0.0) {
			if (velocity.y < 0.0) { // falling
				double lift = newvy * -0.1 * horizontalFacing2;
				newvx += facing.x * lift / horizontalFacing;
				newvy += lift;
				newvz += facing.z * lift / horizontalFacing;
			}

			if (pitchRadians < 0.0f) { // facing upwards
				double lift = horizontalSpeed * -(double)MathHelper.sin(pitchRadians) * 0.04;
				newvx += -facing.x * lift / horizontalFacing;
				newvy += lift * 3.2;
				newvz += -facing.z * lift / horizontalFacing;
			}

			newvx += (facing.x / horizontalFacing * horizontalSpeed - velocity.x) * 0.1;
			newvz += (facing.z / horizontalFacing * horizontalSpeed - velocity.z) * 0.1;
		}

		newvx *= 0.9900000095367432;
		newvy *= 0.9800000190734863;
		newvz *= 0.9900000095367432;

		if (CoolElytraClient.isRocketing) {
			newvx += facing.x * 0.1 + (facing.x * 1.5 - newvx) * 0.5;
			newvy += facing.y * 0.1 + (facing.y * 1.5 - newvy) * 0.5;
			newvz += facing.z * 0.1 + (facing.z * 1.5 - newvz) * 0.5;
		}

		return new Vec3d(MathHelper.lerp(tickDelta, velocity.x, newvx), MathHelper.lerp(tickDelta, velocity.y, newvy), MathHelper.lerp(tickDelta, velocity.z, newvz));
	}
}
