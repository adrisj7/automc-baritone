package automc.baritone;

import automc.AutoMC;
import baritone.Baritone;

public class AutoMCBaritone {

    InteractWithGoalBlockProcess interactWithBlockProcess;

	public AutoMCBaritone(AutoMC parent) {
        interactWithBlockProcess = new InteractWithGoalBlockProcess((Baritone) parent.getBaritone());
	}

    public InteractWithGoalBlockProcess getInteractWithGoalBlockProcess() {
    	return this.interactWithBlockProcess;
    }

}
