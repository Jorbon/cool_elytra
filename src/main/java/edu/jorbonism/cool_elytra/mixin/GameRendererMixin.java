package edu.jorbonism.cool_elytra.mixin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Scanner;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

	@Shadow private MinecraftClient client;
	@Shadow private float viewDistance;
	private float previousRollAngle = 0.0f;
	private double wingPower = 1.25;
	private double rollSmoothing = 0.85;
	private boolean configLoaded = false;
	private static File configFile = new File(FileSystems.getDefault().getPath(System.getProperty("user.dir"), "config", "cool_elytra.cfg").toString());
	
	@Inject(at = @At("HEAD"), method = "renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V")
	public void renderWorld(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {

		// reload config when render distance changes
		if (this.viewDistance != (float)(this.client.options.viewDistance * 16))
			this.configLoaded = false;

		if (!this.configLoaded)
			this.loadConfig();

		if (this.client.player.isFallFlying() && !(this.client.player.isTouchingWater() || this.client.player.isInLava())) {
			Vec3d facing = this.client.player.getRotationVecClient();
			Vec3d velocity = this.getPlayerInstantaneousVelocity(tickDelta);
			if (this.client.player.isSneaking()) {
				System.out.println("facing: " + facing);
				System.out.println("velocity: " + velocity);
			}
			double horizontalFacing2 = facing.horizontalLengthSquared();
			double horizontalSpeed2 = velocity.horizontalLengthSquared();
			float rollAngle = 0.0f;
			if (horizontalFacing2 > 0.0D && horizontalSpeed2 > 0.0D) {
				double dot = (velocity.x * facing.x + velocity.z * facing.z) / Math.sqrt(horizontalFacing2 * horizontalSpeed2); // acos(dot) = angle between facing and velocity vectors
				if (dot >= 1.0) dot = 1.0; // hopefully fix world disappearing occassionally which I assume would be due to ^^^ sqrt precision limits
				else if (dot <= -1.0) dot = -1.0;
				double direction = Math.signum(velocity.x * facing.z - velocity.z * facing.x); // = which side laterally each vector is on
				rollAngle = (float)(Math.atan(Math.sqrt(horizontalSpeed2) * Math.acos(dot) * this.wingPower) * direction * 57.29577951308);
			}
			// smooth changes to the roll angle and remove the bumpy crunchy
			rollAngle = (float)((1.0 - this.rollSmoothing) * rollAngle + this.rollSmoothing * this.previousRollAngle);
			this.previousRollAngle = rollAngle;
			
			matrix.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(rollAngle));
		} else {
			this.previousRollAngle = 0.0f;
		}
	}

	public Vec3d getPlayerInstantaneousVelocity(float tickDelta) {
		// copying over the important bits of elytra flight code and cleaning it up
		// this is to smooth some jitteriness caused by rotation being frame-accurate but velocity only changing each tick

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

		return new Vec3d(MathHelper.lerp(tickDelta, velocity.x, newvx), MathHelper.lerp(tickDelta, velocity.y, newvy), MathHelper.lerp(tickDelta, velocity.z, newvz));
	}

	public void loadConfig() {
		try {
			Scanner reader = new Scanner(configFile);
			// separate by char #0 (end of file) so it gets the entire file together
			reader.useDelimiter(Character.toString((char) 0));
			String configString = reader.next();
			reader.close();

			// parse values
			String[] data = configString.split("\n");
			for (int n = 0; n < data.length; n++) {
				String[] parts = data[n].split("#", 2)[0].split("=");
				if (parts.length >= 2) {
					String name = parts[0].trim();
					if (name.equals("turning_force"))
						this.wingPower = Double.parseDouble(parts[1].trim());
						if (this.wingPower < 0) this.wingPower = 0;
					else if (name.equals("roll_smoothing"))
						this.rollSmoothing = Double.parseDouble(parts[1].trim());
						if (this.rollSmoothing < 0) this.rollSmoothing = 0;
						if (this.rollSmoothing > 1) this.rollSmoothing = 1;
				}
			}
			
			System.out.println("Loaded Cool Elytra config from file");

		} catch (FileNotFoundException e) {
			System.out.println("No Cool Elytra config found, creating a new one");

			this.wingPower = 1.25;
			this.rollSmoothing = 0.85;
			
			// generate config file
			try {
				configFile.createNewFile();
				FileWriter writer = new FileWriter(configFile);
				writer.write("# Cool Elytra config\n\n# Controls sensitivity\n# Higher = more roll\nturning_force = 1.25\n\n# Smooths changes to roll angle\n# Closer to 1 is smoother\n# 0 is no smoothing, 1 stops roll completely\nroll_smoothing = 0.85\n");
				writer.close();
			} catch (IOException f) {
				System.out.println("Couldn't create a Cool Elytra config file, using default settings");
			}
		}

		this.configLoaded = true;
	}

	@Shadow public void loadProjectionMatrix(Matrix4f matrix4f) {}
	@Shadow public Matrix4f getBasicProjectionMatrix(double d) { return null; }
	@Shadow public void updateTargetedEntity(float tickDelta) {}
	@Shadow public void renderHand(MatrixStack matrix, Camera camera2, float tickDelta) {}
	@Shadow public double getFov(Camera camera2, float tickDelta, boolean b) { return 0; }
	@Shadow public void bobViewWhenHurt(MatrixStack matrixStack, float tickDelta) {}
	@Shadow public void bobView(MatrixStack matrixStack, float tickDelta) {}
	@Shadow public boolean shouldRenderBlockOutline() { return false; }
}
