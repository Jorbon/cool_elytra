package edu.jorbonism.cool_elytra.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class CoolElytraConfig extends MidnightConfig {
	@Entry public static Mode modMode = Mode.CLASSIC;
	public enum Mode { DISABLED, CLASSIC, REALISTIC }
	@Comment public static Comment classic;
	@Entry(min = 0) public static double wingPower = 1.25;
	@Entry(min = 0, max = 1) public static double rollSmoothing = 0.85;
	@Comment public static Comment realistic;
	@Entry public static boolean swap = false;
	@Entry(min = 0) public static double rollSensitivity = 1;
	@Entry(min = 0) public static double yawSensitivity = 1;
	@Entry(min = 0) public static double pitchSensitivity = 1;
}
