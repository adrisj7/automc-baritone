package automc.combat;

import automc.Runner;
import automc.definitions.LoopState;

/**
 * Handles things like killAura and targeting specific mobs to attack.
 * 
 * In the future, this will handle combat with other players. For anarchy
 * this will mostly involve fleeing from combat.
 *
 */
public class CombatRunner extends Runner {

	public KillAuraForceField aura;
	public Killer killer;

	public CombatRunner() {
		super();
		aura = new KillAuraForceField();
		killer = new Killer();
	}

	@Override
	protected void onStart() {
		
	}

	@Override
	protected void onTick(LoopState loopState) {
		aura.onTick();
		killer.onTick();
	}

	@Override
	protected void onStop() {
		killer.stop();
	}

}
