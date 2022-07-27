package edu.jorbonism.cool_elytra.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class CoolElytraConfig extends MidnightConfig {
	@Comment public static Comment text_mode;
	@Entry public static int mode = 1;
	@Comment public static Comment text_wingPower;
	@Entry(min = 0) public static double wingPower = 1.25;
	@Comment public static Comment text_rollSmoothing;
	@Entry(min = 0, max = 1) public static double rollSmoothing = 0.85;
}
