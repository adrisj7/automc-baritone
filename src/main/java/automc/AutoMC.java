package automc;

import org.lwjgl.input.Keyboard;

import automc.command.CommandParser;
import automc.containers.ContainerHandler;
import automc.definitions.LoopState;
import automc.items.ItemTaskCatalogue;
import automc.items.ItemWorkDictionary;
import automc.player.PlayerController;
import automc.tasksystem.TaskRunner;
import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.event.events.ChatEvent;
import baritone.api.process.IBaritoneProcess;
import net.minecraft.client.Minecraft;

public class AutoMC {

	// ewwwwww poo poo singleton pattern very stinky >:(
	private static AutoMC instance = null;
	public static AutoMC getAutoMC() {
		if (instance == null) {
			instance = new AutoMC();
		}
		return instance;
	}

	public PlayerController player;
	public TaskRunner taskRunner;

	public ItemTaskCatalogue itemTaskCatalogue;
	public ContainerHandler containerHandler;

	private CommandParser commandParser;

	private LoopState previousLoopState;

	// TODO: This is p bad lol
	private boolean cancelLastFrame = false;

	// CONFIG
	// TODO: Move to config folder maybe?
	public ItemWorkDictionary itemWorkDictionary;
	
	public AutoMC() {
		Logger.debug(this, "AutoMC Constructor");

		commandParser = new CommandParser();
		this.taskRunner = new TaskRunner();
		itemTaskCatalogue = new ItemTaskCatalogue();
		player = new PlayerController();
		containerHandler = new ContainerHandler();

		itemWorkDictionary = new ItemWorkDictionary();
//		itemWorkDictionary.init();

		previousLoopState = LoopState.TITLE_SCREEN;
	}

	public IBaritone getBaritone() {
		return BaritoneAPI.getProvider().getPrimaryBaritone();
	}
	
	public boolean isInGame() {
		return Minecraft.getMinecraft().player != null;
	}

	/**
	 * When the player first loads
	 */
	public void onPlayerInit() {
		Logger.debug(this, "AutoMC Player Init");

		// Init runners
		taskRunner.start();

		// Reload config files, so we can quickly change em
		itemWorkDictionary.init();
	}

	public void onPlayerDisconnect() {
		// Don't stop the task runner just yet.
		// There are rare situations where you might want to run stuff while disconnected
		// for example, to auto-reconnect.

	}


	/**
	 * When Minecraft ticks. Period.
	 */
	public void onTick() {

		// If we press cancel, stop the runners.
		if (isCancelPressing()) {
			onCancel();
			cancelLastFrame = true;
		} else {
			cancelLastFrame = false;
		}

		LoopState state = LoopState.GAME;
		if (Minecraft.getMinecraft().player != null) {
			// In game
			if (previousLoopState == LoopState.TITLE_SCREEN) {
				onPlayerInit();
			}
			player.onTick();
			containerHandler.onTick();
		} else {
			// Not in game
			state = LoopState.TITLE_SCREEN;
			if (previousLoopState == LoopState.GAME) {
				onPlayerDisconnect();
			}
		}

		// Tick runners
		taskRunner.tick(state);

		previousLoopState = state;
	}

	/**
	 * Whenever our player types a chat message on the CLIENT side.
	 * @param evt: The client side chat event.
	 */
	public void onChat(ChatEvent evt) {
		if (commandParser.tryParseCommand(evt.getMessage())) {
			// Interrupt chat.
			evt.cancel();
		}
	}

	public void onCancel() {
		if (cancelLastFrame) return;
		cancelLastFrame = true;
		Logger.debug(this, "\u00A7l\u00A7dCANCELLED");
		taskRunner.stop();
		IBaritoneProcess[] processes = new IBaritoneProcess[] {
			getBaritone().getBuilderProcess(),
			getBaritone().getMineProcess(),
			getBaritone().getCustomGoalProcess(),
			getBaritone().getExploreProcess(),
			getBaritone().getFarmProcess(),
			getBaritone().getFollowProcess(),
			getBaritone().getGetToBlockProcess()
		};
		for(IBaritoneProcess p : processes) {
			p.onLostControl();
		}
		getBaritone().getInputOverrideHandler().clearAllKeys();

	}

	// TODO: Parameterize this somehow
	private boolean isCancelPressing() {
		return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_K); 
	}
}
