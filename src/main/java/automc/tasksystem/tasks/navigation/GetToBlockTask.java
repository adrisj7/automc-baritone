package automc.tasksystem.tasks.navigation;

import automc.AutoMC;
import automc.Logger;
import automc.definitions.LoopState;
import automc.tasksystem.Task;
import baritone.api.utils.BlockOptionalMeta;
import net.minecraft.block.Block;

public class GetToBlockTask extends Task {

	private BlockOptionalMeta block;

	public GetToBlockTask(BlockOptionalMeta block) {
		this.block = block;
	}
	public GetToBlockTask(Block block) {
		this(new BlockOptionalMeta(block));
	}

	@Override
	protected void onInit() {
		Logger.debug(this, "Get to block task: INIT");
		AutoMC.getAutoMC().getBaritone().getGetToBlockProcess().getToBlock(block);
	}

	@Override
	public boolean areEqual(Task t) {
		if (t instanceof GetToBlockTask ) {
			GetToBlockTask gtbt = (GetToBlockTask) t;
			return (gtbt.block.getBlock().equals(block.getBlock()) && gtbt.block.getMeta() == block.getMeta());
		}
		return false;
	}
	@Override
	protected void onTick(LoopState state) {
		if (!AutoMC.getAutoMC().getBaritone().getGetToBlockProcess().isActive()) {
			stop();
		}
	}
	@Override
	public boolean isDone() {
		// Manual
		return false;
	}
	@Override
	protected void onFinish() {
		AutoMC.getAutoMC().getBaritone().getGetToBlockProcess().onLostControl();
	}
	@Override
	protected void onInterrupt() {
		onFinish();
	}
	@Override
	protected boolean areConditionsMet() {
		return true;
	}

}
