package automc.hacks;

import net.minecraft.client.Minecraft;

// TODO: Don't make this static, you anti-java hippie
public class FullBright {

	private static final float FULL_GAMMA = 1000000;

	private static float defaultGamma;

	public static void enable() {
		if (!isEnabled()) {
			defaultGamma = Minecraft.getMinecraft().gameSettings.gammaSetting;
			Minecraft.getMinecraft().gameSettings.gammaSetting = FULL_GAMMA;
		}
	}

	public static void disable() {
		Minecraft.getMinecraft().gameSettings.gammaSetting = defaultGamma;
	}

	private static boolean isEnabled() {
		return (Minecraft.getMinecraft().gameSettings.gammaSetting == FULL_GAMMA);
	}
}
