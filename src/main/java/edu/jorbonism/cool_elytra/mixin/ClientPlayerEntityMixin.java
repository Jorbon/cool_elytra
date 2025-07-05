package edu.jorbonism.cool_elytra.mixin;

import com.mojang.authlib.GameProfile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import edu.jorbonism.cool_elytra.CoolElytraClient;
import edu.jorbonism.cool_elytra.config.CoolElytraConfig;
import edu.jorbonism.cool_elytra.config.CoolElytraConfig.Mode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec3d;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
	
	public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) { super(world, profile); }
	
	@Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/ClientPlayNetworkHandler;Lnet/minecraft/stat/StatHandler;Lnet/minecraft/client/recipebook/ClientRecipeBook;Lnet/minecraft/util/PlayerInput;Z)V")
	public void init(MinecraftClient client, ClientWorld world, ClientPlayNetworkHandler networkHandler, StatHandler stats, ClientRecipeBook recipeBook, PlayerInput lastPlayerInput, boolean lastSprinting, CallbackInfo ci) {
		CoolElytraClient.left = CoolElytraClient.getAssumedLeft(this.getYaw());
	}
	
	@Override
	public void changeLookDirection(double cursorDeltaX, double cursorDeltaY) {
		Vec3d facing = this.getRotationVecClient();
		
        // set left vector to the assumed upright left if not in realistic
		if (!this.isGliding() || CoolElytraConfig.modMode != Mode.REALISTIC) {
			CoolElytraClient.left = CoolElytraClient.getAssumedLeft(this.getYaw());
            if (CoolElytraConfig.modMode == Mode.CLASSIC) {
                CoolElytraClient.left = CoolElytraClient.rotateAxisAngle(CoolElytraClient.left, facing, CoolElytraClient.rollAngle * CoolElytraClient.TORAD);
            }
			super.changeLookDirection(cursorDeltaX, cursorDeltaY);
			return;
		}
		
		// recompute left vector since it tends to drift off of perpendicular/normalized
		CoolElytraClient.left = CoolElytraClient.left.subtract(facing.multiply(CoolElytraClient.left.dotProduct(facing))).normalize();
		
		// pitch
		facing = CoolElytraClient.rotateAxisAngle(facing, CoolElytraClient.left, -0.15 * cursorDeltaY * CoolElytraClient.TORAD * CoolElytraConfig.pitchSensitivity);
		
		
		double rollAngle = 0.15 * cursorDeltaX * CoolElytraClient.TORAD;
		double yawAngle = 0.15 * CoolElytraClient.cursorDeltaZ * CoolElytraClient.TORAD;
		CoolElytraClient.cursorDeltaZ = 0;
		if ((this.isSneaking() ^ CoolElytraConfig.swap) && !CoolElytraClient.isKeyUpdate) {
			double tmp = rollAngle;
			rollAngle = yawAngle;
			yawAngle = tmp;
		}
		
		// yaw
		if (!CoolElytraClient.isKeyUpdate) yawAngle *= CoolElytraConfig.yawSensitivity;
		Vec3d up = facing.crossProduct(CoolElytraClient.left);
		facing = CoolElytraClient.rotateAxisAngle(facing, up, yawAngle);
		CoolElytraClient.left = CoolElytraClient.rotateAxisAngle(CoolElytraClient.left, up, yawAngle);
		
		// roll
		if (!CoolElytraClient.isKeyUpdate) rollAngle *= CoolElytraConfig.rollSensitivity;
		CoolElytraClient.left = CoolElytraClient.rotateAxisAngle(CoolElytraClient.left, facing, rollAngle);
		
		
		double deltaY = -Math.asin(facing.getY()) * CoolElytraClient.TODEG - this.getPitch();
		double deltaX = -Math.atan2(facing.getX(), facing.getZ()) * CoolElytraClient.TODEG - this.getYaw();
		
		super.changeLookDirection(deltaX / 0.15, deltaY / 0.15);
    }
	
	@Override
	public void travel(Vec3d movementInput) {
		CoolElytraClient.strafeInput = Math.signum(movementInput.x);
		super.travel(movementInput);
	}
}