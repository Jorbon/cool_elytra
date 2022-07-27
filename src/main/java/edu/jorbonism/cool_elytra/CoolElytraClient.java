package edu.jorbonism.cool_elytra;

import org.lwjgl.glfw.GLFW;

import edu.jorbonism.cool_elytra.config.CoolElytraConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.Vec3d;

public class CoolElytraClient implements ClientModInitializer {

	
	public static double lastRollAngle = 0;
	public static boolean isRocketing = false;
    
    @Override
    public void onInitializeClient() {
        CoolElytraConfig.init("cool_elytra", CoolElytraConfig.class);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (keyRealism.wasPressed()) CoolElytraConfig.mode = 2;
			while (keyClassic.wasPressed()) CoolElytraConfig.mode = 1;
			while (keyDisable.wasPressed()) CoolElytraConfig.mode = 0;
		});
    }


    public static Vec3d left;
	public static final double TORAD = Math.PI / 180;
	public static final double TODEG = 1 / TORAD;


	private static KeyBinding keyClassic = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.cool_elytra.classic", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.cool_elytra"));
	private static KeyBinding keyRealism = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.cool_elytra.realism", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.cool_elytra"));
	private static KeyBinding keyDisable = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.cool_elytra.disable", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.cool_elytra"));
	



	public static Vec3d getAssumedLeft(float yaw) {
		yaw *= TORAD;
		return new Vec3d(-Math.cos(yaw), 0, -Math.sin(yaw));
	}

	public static Vec3d rotateAxisAngle(Vec3d v, Vec3d axis, double angle) {
		double c = Math.cos(angle);
		double s = Math.sin(angle);
		double t = 1.0 - c;

		//double l = axis.lengthSquared();
		//if (l == 0) return v;
		//if (l != 1) axis = axis.multiply(1/Math.sqrt(l));

		double x = (c + axis.x*axis.x*t) * v.getX(),
			   y = (c + axis.y*axis.y*t) * v.getY(),
			   z = (c + axis.z*axis.z*t) * v.getZ(),
		tmp1 = axis.x*axis.y*t,
		tmp2 = axis.z*s;
		y += (tmp1 + tmp2) * v.getX();
		x += (tmp1 - tmp2) * v.getY();
		tmp1 = axis.x*axis.z*t;
		tmp2 = axis.y*s;
		z += (tmp1 - tmp2) * v.getX();
		x += (tmp1 + tmp2) * v.getZ();
		tmp1 = axis.y*axis.z*t;
		tmp2 = axis.x*s;
		z += (tmp1 + tmp2) * v.getY();
		y += (tmp1 - tmp2) * v.getZ();
		
		return new Vec3d(x, y, z);
	}
    
}
