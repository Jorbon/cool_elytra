package edu.jorbonism.cool_elytra.mixin;

import com.mojang.blaze3d.systems.RenderSystem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import edu.jorbonism.cool_elytra.GameRendererDummy;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

	@Shadow private LightmapTextureManager lightmapTextureManager;
	@Shadow private MinecraftClient client;
	@Shadow private boolean renderHand;
	@Shadow private float viewDistance;
	@Shadow private int ticks;
	@Shadow private Camera camera;
	@Shadow private ResourceManager resourceManager;
	@Shadow private BufferBuilderStorage buffers;
	@Shadow private float lastSkyDarkness;
	@Shadow private float skyDarkness;

	private GameRendererDummy dummy = null;
	
	@Inject(at = @At("HEAD"), method = "renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V", cancellable = true)
	public void renderWorld(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
		this.lightmapTextureManager.update(tickDelta);
		if (this.client.getCameraEntity() == null) {
			this.client.setCameraEntity(this.client.player);
		}
		
		this.updateTargetedEntity(tickDelta);
		this.client.getProfiler().push("center");
		boolean bl = this.shouldRenderBlockOutline();
		this.client.getProfiler().swap("camera");
		Camera camera = this.camera;
		this.viewDistance = (float)(this.client.options.viewDistance * 16);
		MatrixStack matrixStack = new MatrixStack();
		double d = this.getFov(camera, tickDelta, true);
		matrixStack.peek().getModel().multiply(this.getBasicProjectionMatrix(d));
		this.bobViewWhenHurt(matrixStack, tickDelta);
		if (this.client.options.bobView) {
			this.bobView(matrixStack, tickDelta);
		}
		
		float f = MathHelper.lerp(tickDelta, this.client.player.lastNauseaStrength, this.client.player.nextNauseaStrength) * this.client.options.distortionEffectScale * this.client.options.distortionEffectScale;
		if (f > 0.0F) {
			int i = this.client.player.hasStatusEffect(StatusEffects.NAUSEA) ? 7 : 20;
			float g = 5.0F / (f * f + 5.0F) - f * 0.04F;
			g *= g;
			Vec3f vec3f = new Vec3f(0.0F, MathHelper.SQUARE_ROOT_OF_TWO / 2.0F, MathHelper.SQUARE_ROOT_OF_TWO / 2.0F);
			matrixStack.multiply(vec3f.getDegreesQuaternion(((float)this.ticks + tickDelta) * (float)i));
			matrixStack.scale(1.0F / g, 1.0F, 1.0F);
			float h = -((float)this.ticks + tickDelta) * (float)i;
			matrixStack.multiply(vec3f.getDegreesQuaternion(h));
		}
		
		Matrix4f matrix4f = matrixStack.peek().getModel();
		this.loadProjectionMatrix(matrix4f);
		camera.update(this.client.world, (Entity)(this.client.getCameraEntity() == null ? this.client.player : this.client.getCameraEntity()), !this.client.options.getPerspective().isFirstPerson(), this.client.options.getPerspective().isFrontView(), tickDelta);

		// the new lines
		if (this.client.player.isFallFlying() && !(this.client.player.isTouchingWater() || this.client.player.isInLava())) {
			Vec3d facing = this.client.player.getRotationVecClient();
			Vec3d velocity = this.client.player.getVelocity();
			if (this.client.player.isSneaking()) {
				System.out.println("facing: " + facing);
				System.out.println("velocity: " + velocity);
			}
			double flen2 = facing.horizontalLengthSquared();
			double speed2 = velocity.horizontalLengthSquared();
			float rollAngle = 0.0f;
			if (flen2 > 0.0D && speed2 > 0.0D) {
				double dot = (velocity.x * facing.x + velocity.z * facing.z) / Math.sqrt(flen2 * speed2); // acos(dot) = angle between facing and velocity vectors
				double direction = Math.signum(velocity.x * facing.z - velocity.z * facing.x); // = which side laterally each vector is on
				double speedmodbase = 5.0;
				if (speed2 < speedmodbase)
					dot *= Math.sqrt(speed2) / speedmodbase; // make lean scale with speed
				rollAngle = (float)(direction * Math.acos(dot) * 57.29577951308);
			}
			
			matrix.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(rollAngle));
		}
		// end of new lines

		matrix.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
		matrix.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(camera.getYaw() + 180.0F));
		this.client.worldRenderer.setupFrustum(matrix, camera.getPos(), this.getBasicProjectionMatrix(Math.max(d, this.client.options.fov)));

		// this line has to be fixed too
		this.client.worldRenderer.render(matrix, tickDelta, limitTime, bl, camera, this.getDummy(), this.lightmapTextureManager, matrix4f);

		this.client.getProfiler().swap("hand");
		if (this.renderHand) {
			RenderSystem.clear(256, MinecraftClient.IS_SYSTEM_MAC);
			this.renderHand(matrix, camera, tickDelta);
		}
	
		this.client.getProfiler().pop();

		ci.cancel();
	}

	public GameRendererDummy getDummy() {
		if (this.dummy == null)
			this.dummy = new GameRendererDummy(client, resourceManager, buffers);
		this.dummy.update(this.viewDistance, this.skyDarkness, this.lastSkyDarkness);
		return this.dummy;
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
