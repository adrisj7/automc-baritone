package automc.utility;

import org.lwjgl.input.Keyboard;

import automc.Logger;

public class DebugBreaker {

	private static boolean breaked = false;

	public static void breakNow() {
		Logger.logError("BREAKED FOR DEBUG");
		breaked = true;
	}
	
	public static boolean isBreaked() {
		return breaked;
	}

	public static void onTick() {
		if (Keyboard.isKeyDown(Keyboard.KEY_B)) {
			if (breaked) {
				Logger.log("UNBREAKED");
			}
			breaked = false;
		}
	}
}
