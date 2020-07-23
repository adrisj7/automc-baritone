package automc.hacks;

import org.lwjgl.input.Keyboard;

import automc.Logger;

public class Hacks {

	// TODO: Cleaner system.
	boolean y_was_pressed = false;
	
	public void tick() {
		// TODO: Modules
		TimerHack.tick();

		// TODO: Keep things Modular! Keep track of this in each module.
		boolean y_pressed = Keyboard.isKeyDown(Keyboard.KEY_Y); 
		if (y_pressed && !y_was_pressed) {
			TimerHack.toggle();
			Logger.log("[Timer Hack]: " + (TimerHack.isEnabled()? "ON" : "OFF"));
		}
		y_was_pressed = y_pressed;
	}
}
