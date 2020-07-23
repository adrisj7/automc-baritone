package automc.control;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.settings.KeyBinding;

public class InputOverrider {

	public static final int LEFT_CLICK =  0 - 100;
	public static final int RIGHT_CLICK = 1 - 100;

	private Set<Integer> forcedInputs;
	private Set<Integer> toClear;

	public InputOverrider() {
		forcedInputs = new HashSet<Integer>();
		toClear = new HashSet<Integer>();
	}

	public void setInputForce(int code, boolean force) {
		if (force) {
			forcedInputs.add(code);
		} else {
			if (forcedInputs.contains(code)) {
				toClear.add(code);
				forcedInputs.remove(code);
			}
		}
	}

	public void onTick() {
		// TODO: Update inputs.
		for(int input : forcedInputs) {
			KeyBinding.setKeyBindState(input, true);
		}
		for(int clear : toClear) {
			KeyBinding.setKeyBindState(clear, false);
		}
		toClear.clear();
	}

	public void reset() {
		forcedInputs.clear();
	}
}
