package automc;

import automc.definitions.LoopState;

public abstract class Runner {
	boolean running;

	public Runner() {
		this.running = false;
	}

	public void start() {
		if (!running) {
			running = true; // Setting this before init, for consistency.
			onStart();
		}
	}

	public void tick(LoopState loopState) {
		if (!running) return;
		onTick(loopState);
	}

	public void stop() {
		if (!running) return;
		running = false;
		onStop();
	}

	public boolean isRunning() {
		return running;
	}

	/**
	 * Called when the runner is stared.
	 */
	protected abstract void onStart();

	/**
	 * Called when the game is periodically updated.
	 */
	protected abstract void onTick(LoopState loopState);

	/**
	 * Called when the runner is stopped.
	 */
	protected abstract void onStop();
}
