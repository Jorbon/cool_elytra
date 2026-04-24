package edu.jorbonism.cool_elytra;

import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;
import edu.jorbonism.cool_elytra.config.CoolElytraConfig;
import edu.jorbonism.cool_elytra.config.CoolElytraConfig.Mode;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public class CoolElytraClient implements ClientModInitializer {
	
	@Override
    public void onInitializeClient() {
		CoolElytraConfig.init("cool_elytra", CoolElytraConfig.class);
		
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (keyRealism.consumeClick()) CoolElytraConfig.modMode = Mode.REALISTIC;
			while (keyClassic.consumeClick()) CoolElytraConfig.modMode = Mode.CLASSIC;
			while (keyDisable.consumeClick()) CoolElytraConfig.modMode = Mode.DISABLED;
		});
    }
	
	public static long lastTime = System.nanoTime();
	public static double rollAngle = 0;
	public static boolean isRocketing = false;
    public static Vec3 left;
	public static double rollVelocity = 0;
	public static double yawVelocity = 0;
	public static boolean isFrontView = false;
	public static double strafeInput = 0;
	public static boolean isKeyUpdate = false;
	public static double cursorDeltaZ = 0;
	public static final double TORAD = Math.PI / 180;
	public static final double TODEG = 1 / TORAD;
	
	private static final KeyMapping.Category COOL_ELYTRA_CATEGORY = KeyMapping.Category.register(Identifier.parse("cool_elytra"));
	private static KeyMapping keyClassic = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.cool_elytra.classic", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, COOL_ELYTRA_CATEGORY));
	private static KeyMapping keyRealism = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.cool_elytra.realism", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, COOL_ELYTRA_CATEGORY));
	private static KeyMapping keyDisable = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.cool_elytra.disable", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, COOL_ELYTRA_CATEGORY));
	
	
	public static Vec3 getAssumedLeft(float yaw) {
		yaw *= TORAD;
		return new Vec3(-Math.cos(yaw), 0, -Math.sin(yaw));
	}
	
	public static Vec3 rotateAxisAngle(Vec3 v, Vec3 axis, double angle) {
		double c = Math.cos(angle);
		double s = Math.sin(angle);
		double t = 1.0 - c;
		
		// Normalize axis input
		// Not necessary as caller is responsible for axis normality
		//double l = axis.lengthSquared();
		//if (l == 0) return v;
		//if (l != 1) axis = axis.multiply(1/Math.sqrt(l));
		
		double x = (c + axis.x*axis.x*t) * v.x(),
			   y = (c + axis.y*axis.y*t) * v.y(),
			   z = (c + axis.z*axis.z*t) * v.z(),
		tmp1 = axis.x*axis.y*t,
		tmp2 = axis.z*s;
		y += (tmp1 + tmp2) * v.x();
		x += (tmp1 - tmp2) * v.y();
		tmp1 = axis.x*axis.z*t;
		tmp2 = axis.y*s;
		z += (tmp1 - tmp2) * v.x();
		x += (tmp1 + tmp2) * v.z();
		tmp1 = axis.y*axis.z*t;
		tmp2 = axis.x*s;
		z += (tmp1 + tmp2) * v.y();
		y += (tmp1 - tmp2) * v.z();
		
		return new Vec3(x, y, z);
	}
}
