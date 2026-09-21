package edu.jorbonism.cool_elytra.mixin;

import edu.jorbonism.cool_elytra.CoolElytraClient;
import edu.jorbonism.cool_elytra.config.CoolElytraConfig;
import edu.jorbonism.cool_elytra.config.CoolElytraConfig.Mode;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	
	@Final @Shadow private Minecraft minecraft;
	
	@Inject(at = @At("HEAD"), method = "renderLevel")
	public void renderWorld(DeltaTracker tickCounter, CallbackInfo ci) {
		// timer stuff
		long time = System.nanoTime();
		double frameTime = (time - CoolElytraClient.lastTime) * 1e-9;
		CoolElytraClient.lastTime = time;
		
		float tickDelta = tickCounter.getGameTimeDeltaPartialTick(true);
		
		CoolElytraClient.isFrontView = this.minecraft.options.getCameraType().isMirrored();
		
		if (CoolElytraConfig.modMode == Mode.CLASSIC) {
			// original camera rolling
			if (this.minecraft.player != null && this.minecraft.player.isFallFlying() && !(this.minecraft.player.isInWater() || this.minecraft.player.isInLava())) {
				Vec3 facing = this.minecraft.player.getForward();
				Vec3 velocity = this.getPlayerInstantaneousVelocity(tickDelta);
				double horizontalFacing2 = facing.horizontalDistanceSqr();
				double horizontalSpeed2 = velocity.horizontalDistanceSqr();
				
				double angle = 0;
				if (horizontalFacing2 > 0.0D && horizontalSpeed2 > 0.0D) {
					double dot = (velocity.x * facing.x + velocity.z * facing.z) / Math.sqrt(horizontalFacing2 * horizontalSpeed2); // acos(dot) = angle between facing and velocity vectors
					if (dot >= 1.0) dot = 1.0; // hopefully fix world disappearing occassionally which I assume would be due to ^^^ sqrt precision limits
					else if (dot <= -1.0) dot = -1.0;
					double direction = Math.signum(velocity.x * facing.z - velocity.z * facing.x); // = which side laterally each vector is on
					angle = Math.atan(Math.sqrt(horizontalSpeed2) * Math.acos(dot) * CoolElytraConfig.wingPower) * direction * CoolElytraClient.TODEG;
				}
				if (CoolElytraConfig.rollSmoothingAfterLanding) {
					CoolElytraClient.rollAngle = smoothRollAngle(angle, frameTime);
				} else {
					angle += Math.pow(CoolElytraConfig.rollSmoothing, frameTime * 40) * (CoolElytraClient.rollAngle - angle);
					CoolElytraClient.rollAngle = angle;
				}
			} else {
				if (CoolElytraConfig.rollSmoothingAfterLanding) {
					CoolElytraClient.rollAngle = smoothRollAngle(0, frameTime);
				} else {
					CoolElytraClient.rollAngle = 0.0f;
				}
			}
			
			CoolElytraClient.yawVelocity = 0;
			CoolElytraClient.rollVelocity = 0;
			
		} else if (CoolElytraConfig.modMode == Mode.REALISTIC) {
			// real rolling flight
			
			if (this.minecraft.player != null && this.minecraft.player.isFallFlying()) {
				// handle key input turning
				if (CoolElytraClient.strafeInput != 0 && !(this.minecraft.player.isShiftKeyDown() ^ CoolElytraConfig.swap)) {
					CoolElytraClient.yawVelocity -= CoolElytraClient.strafeInput * frameTime * CoolElytraConfig.keyYawSensitivity * 25;
					if (CoolElytraClient.yawVelocity < -CoolElytraConfig.keyYawSpeedCap) CoolElytraClient.yawVelocity = -CoolElytraConfig.keyYawSpeedCap;
					else if (CoolElytraClient.yawVelocity > CoolElytraConfig.keyYawSpeedCap) CoolElytraClient.yawVelocity = CoolElytraConfig.keyYawSpeedCap;
				} else {
					CoolElytraClient.yawVelocity -= Math.signum(CoolElytraClient.yawVelocity) * Math.min(frameTime * CoolElytraConfig.keyYawSensitivity * 25 / CoolElytraConfig.keyYawMomentum, Math.abs(CoolElytraClient.yawVelocity));
				}
				
				if (CoolElytraClient.strafeInput != 0 && (this.minecraft.player.isShiftKeyDown() ^ CoolElytraConfig.swap)) {
					CoolElytraClient.rollVelocity -= CoolElytraClient.strafeInput * frameTime * CoolElytraConfig.keyRollSensitivity * 25;
					if (CoolElytraClient.rollVelocity < -CoolElytraConfig.keyRollSpeedCap) CoolElytraClient.rollVelocity = -CoolElytraConfig.keyRollSpeedCap;
					else if (CoolElytraClient.rollVelocity > CoolElytraConfig.keyRollSpeedCap) CoolElytraClient.rollVelocity = CoolElytraConfig.keyRollSpeedCap;
				} else {
					CoolElytraClient.rollVelocity -= Math.signum(CoolElytraClient.rollVelocity) * Math.min(frameTime * CoolElytraConfig.keyRollSensitivity * 25 / CoolElytraConfig.keyRollMomentum, Math.abs(CoolElytraClient.rollVelocity));
				}
				
				CoolElytraClient.isKeyUpdate = true;
				CoolElytraClient.cursorDeltaZ = CoolElytraClient.yawVelocity;
				this.minecraft.player.turn(CoolElytraClient.rollVelocity, 0);
				CoolElytraClient.isKeyUpdate = false;
				
				
				double angle = -Math.acos(CoolElytraClient.left.dot(CoolElytraClient.getAssumedLeft(this.minecraft.player.getYRot()))) * CoolElytraClient.TODEG;
				if (CoolElytraClient.left.y() < 0) angle = -angle;
				CoolElytraClient.rollAngle = angle;
				
			} else {
				CoolElytraClient.rollAngle = smoothRollAngle(0, frameTime);
				CoolElytraClient.yawVelocity = 0;
				CoolElytraClient.rollVelocity = 0;
			}
			
		} else {
			CoolElytraClient.rollAngle = 0;
			CoolElytraClient.yawVelocity = 0;
			CoolElytraClient.rollVelocity = 0;
		}
	}

	private double smoothRollAngle(double targetAngle, double frameTime) {
		double smoothing = Math.pow(CoolElytraConfig.rollSmoothing, frameTime * 40);
		double angle = targetAngle + smoothing * (CoolElytraClient.rollAngle - targetAngle);
		return Math.abs(angle) < 0.01 ? 0 : angle;
	}
	
	public Vec3 getPlayerInstantaneousVelocity(float tickDelta) {
		// copying over the important bits of elytra flight code and cleaning it up
		// this is to smooth some jitteriness caused by rotation being frame-accurate but velocity only changing each tick
		
		assert this.minecraft.player != null;
		Vec3 velocity = this.minecraft.player.getDeltaMovement();
		if (tickDelta < 0.01f)
			return velocity;
		
		double newvx = velocity.x;
		double newvy = velocity.y;
		double newvz = velocity.z;
		double gravity = 0.08;
		
		Vec3 facing = this.minecraft.player.getLookAngle();
		float pitchRadians = this.minecraft.player.getXRot() * 0.017453292f;
		double horizontalFacing2 = facing.horizontalDistanceSqr();
		double horizontalFacing = Math.sqrt(horizontalFacing2);
		double horizontalSpeed = velocity.horizontalDistance();
		
		newvy += gravity * (-1.0 + horizontalFacing2 * 0.75);
		
		if (horizontalFacing > 0.0) {
			if (velocity.y < 0.0) { // falling
				double lift = newvy * -0.1 * horizontalFacing2;
				newvx += facing.x * lift / horizontalFacing;
				newvy += lift;
				newvz += facing.z * lift / horizontalFacing;
			}
			
			if (pitchRadians < 0.0f) { // facing upwards
				double lift = horizontalSpeed * -(double)Mth.sin(pitchRadians) * 0.04;
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
		
		return new Vec3(Mth.lerp(tickDelta, velocity.x, newvx), Mth.lerp(tickDelta, velocity.y, newvy), Mth.lerp(tickDelta, velocity.z, newvz));
	}
}
