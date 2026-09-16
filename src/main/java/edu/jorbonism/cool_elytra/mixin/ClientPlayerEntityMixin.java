package edu.jorbonism.cool_elytra.mixin;

import com.mojang.authlib.GameProfile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import edu.jorbonism.cool_elytra.CoolElytraClient;
import edu.jorbonism.cool_elytra.config.CoolElytraConfig;
import edu.jorbonism.cool_elytra.config.CoolElytraConfig.Mode;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.chat.ChatAbilities;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayer {
	
	public ClientPlayerEntityMixin(ClientLevel world, GameProfile profile) { super(world, profile); }
	
	@Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/ClientPacketListener;Lnet/minecraft/stats/StatsCounter;Lnet/minecraft/client/ClientRecipeBook;Lnet/minecraft/world/entity/player/Input;ZLnet/minecraft/client/multiplayer/chat/ChatAbilities;)V")
	public void init(Minecraft client, ClientLevel world, ClientPacketListener networkHandler, StatsCounter stats, ClientRecipeBook recipeBook, Input lastPlayerInput, boolean lastSprinting, ChatAbilities chatAbilities, CallbackInfo ci) {
		CoolElytraClient.left = CoolElytraClient.getAssumedLeft(this.getYRot());
	}
	
	@Override
	public void turn(double cursorDeltaX, double cursorDeltaY) {
		Vec3 facing = this.getForward();
		
        // set left vector to the assumed upright left if not in realistic
		if (!this.isFallFlying() || CoolElytraConfig.modMode != Mode.REALISTIC) {
			CoolElytraClient.left = CoolElytraClient.getAssumedLeft(this.getYRot());
            if (CoolElytraConfig.modMode == Mode.CLASSIC) {
                CoolElytraClient.left = CoolElytraClient.rotateAxisAngle(CoolElytraClient.left, facing, CoolElytraClient.rollAngle * CoolElytraClient.TORAD);
            }
			super.turn(cursorDeltaX, cursorDeltaY);
			return;
		}
		
		// recompute left vector since it tends to drift off of perpendicular/normalized
		CoolElytraClient.left = CoolElytraClient.left.subtract(facing.scale(CoolElytraClient.left.dot(facing))).normalize();
		
		// pitch
		facing = CoolElytraClient.rotateAxisAngle(facing, CoolElytraClient.left, -0.15 * cursorDeltaY * CoolElytraClient.TORAD * CoolElytraConfig.pitchSensitivity);
		
		
		double rollAngle = 0.15 * cursorDeltaX * CoolElytraClient.TORAD;
		double yawAngle = 0.15 * CoolElytraClient.cursorDeltaZ * CoolElytraClient.TORAD;
		CoolElytraClient.cursorDeltaZ = 0;
		if ((this.isShiftKeyDown() ^ CoolElytraConfig.swap) && !CoolElytraClient.isKeyUpdate) {
			double tmp = rollAngle;
			rollAngle = yawAngle;
			yawAngle = tmp;
		}
		
		// yaw
		if (!CoolElytraClient.isKeyUpdate) yawAngle *= CoolElytraConfig.yawSensitivity;
		Vec3 up = facing.cross(CoolElytraClient.left);
		facing = CoolElytraClient.rotateAxisAngle(facing, up, yawAngle);
		CoolElytraClient.left = CoolElytraClient.rotateAxisAngle(CoolElytraClient.left, up, yawAngle);
		
		// roll
		if (!CoolElytraClient.isKeyUpdate) rollAngle *= CoolElytraConfig.rollSensitivity;
		CoolElytraClient.left = CoolElytraClient.rotateAxisAngle(CoolElytraClient.left, facing, rollAngle);
		
		
		double deltaY = -Math.asin(facing.y()) * CoolElytraClient.TODEG - this.getXRot();
		double deltaX = -Math.atan2(facing.x(), facing.z()) * CoolElytraClient.TODEG - this.getYRot();
		
		super.turn(deltaX / 0.15, deltaY / 0.15);
    }
	
	@Override
	public void travel(Vec3 movementInput) {
		CoolElytraClient.strafeInput = Math.signum(movementInput.x);
		super.travel(movementInput);
	}
}
