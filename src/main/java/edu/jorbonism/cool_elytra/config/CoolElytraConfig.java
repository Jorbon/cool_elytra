package edu.jorbonism.cool_elytra.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class CoolElytraConfig extends MidnightConfig {
	@Entry public static Mode modMode = Mode.CLASSIC;
	public enum Mode { DISABLED, CLASSIC, REALISTIC }
	@Comment public static Comment classic;
	@Entry public static double wingPower = 1.25;
	@Entry(min = 0, max = 1) public static double rollSmoothing = 0.85;
	@Comment public static Comment realistic;
	@Entry public static boolean swap = false;
	@Entry public static double rollSensitivity = 1;
	@Entry public static double yawSensitivity = 1;
	@Entry public static double pitchSensitivity = 1;
	@Entry public static double keyRollSensitivity = 1;
	@Entry public static double keyYawSensitivity = 2;
	@Entry public static double keyRollMomentum = 1;
	@Entry public static double keyYawMomentum = 1;
	@Entry public static double keyRollSpeedCap = 40;
	@Entry public static double keyYawSpeedCap = 15;
}
