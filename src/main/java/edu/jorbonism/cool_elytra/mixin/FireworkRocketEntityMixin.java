package edu.jorbonism.cool_elytra.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import edu.jorbonism.cool_elytra.CoolElytraClient;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin {

	@Shadow private LivingEntity attachedToEntity;
	@Shadow private int life;
	@Shadow private int lifetime;

	@Inject(method = "tick", at = @At("HEAD"))
	public void tick(CallbackInfo ci) {
		if (this.isAttachedToEntity() && this.attachedToEntity != null && this.attachedToEntity instanceof Player && this.attachedToEntity.isFallFlying()) {
			CoolElytraClient.isRocketing = this.life < this.lifetime;
		}
	}
	
	@Shadow private boolean isAttachedToEntity() { return false; }
	
}
