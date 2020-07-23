package automc.survival;

import automc.AutoMC;
import automc.Runner;
import automc.combat.CombatRunner;
import automc.definitions.LoopState;

/**
 * 	This makes sure our character doesn't die, basically.
 *
 */
public class SurvivalRunner extends Runner{

	public FoodEater eater;

	public SurvivalRunner() {
		eater = new FoodEater();
	}

	@Override
	protected void onStart() {
		
	}

	@Override
	protected void onTick(LoopState loopState) {
		if (loopState != LoopState.GAME) return;
		if (shouldPause()) return;
		eater.onTick();
	}

	@Override
	protected void onStop() {
		eater.stop();
	}

	private boolean shouldPause() {
		// Priority goes to combat: Don't eat if we're fighting.
		CombatRunner cr = AutoMC.getAutoMC().combatRunner;
		if (cr.aura.isBlocking() || cr.killer.isAttacking()) {
			return true;
		}
		return false;
	}
}
