package edu.jorbonism.cool_elytra.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import edu.jorbonism.cool_elytra.CoolElytraClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin {

	@Shadow private LivingEntity shooter;
	@Shadow private int life;
	@Shadow private int lifeTime;

	@Inject(method = "tick", at = @At("HEAD"))
	public void tick(CallbackInfo ci) {
		if (this.wasShotByEntity() && this.shooter != null && this.shooter instanceof PlayerEntity && this.shooter.isFallFlying()) {
			CoolElytraClient.isRocketing = this.life < this.lifeTime;
		}
	}

	@Shadow private boolean wasShotByEntity() { return false; }
	@Shadow public ItemStack getStack() { return null; }
	@Shadow protected void initDataTracker() {}
	
}
