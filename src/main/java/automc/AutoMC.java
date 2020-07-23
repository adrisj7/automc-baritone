package automc;


import org.lwjgl.input.Keyboard;

import automc.baritone.AutoMCBaritone;
import automc.combat.CombatRunner;
import automc.command.CommandParser;
import automc.containers.ContainerHandler;
import automc.control.InputOverrider;
import automc.definitions.LoopState;
import automc.hacks.FullBright;
import automc.hacks.Hacks;
import automc.items.ItemTaskCatalogue;
import automc.items.ItemWorkDictionary;
import automc.player.EntityScanner;
import automc.player.PlayerController;
import automc.survival.SurvivalRunner;
import automc.tasksystem.TaskRunner;
import automc.utility.DebugBreaker;
import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.event.events.ChatEvent;
import baritone.api.process.IBaritoneProcess;
import baritone.process.MineProcess;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.ContainerEnchantment;

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
	public CombatRunner combatRunner;
	public SurvivalRunner survivalRunner;

	public EntityScanner entityScanner;

	public ItemTaskCatalogue itemTaskCatalogue;
	public ContainerHandler containerHandler;

	public Hacks hacks;
	
	public InputOverrider inputOverride;

	private CommandParser commandParser;

	private LoopState previousLoopState;

	// TODO: This is p bad lol
	private boolean cancelLastFrame = false;

	// CONFIG
	// TODO: Move to config folder maybe?
	public ItemWorkDictionary itemWorkDictionary;

	public AutoMCBaritone customBaritone;

	public AutoMC() {
		Logger.debug(this, "AutoMC Constructor");

		player = new PlayerController();
		
		taskRunner = new TaskRunner();
		combatRunner = new CombatRunner();
		survivalRunner = new SurvivalRunner();
		
		entityScanner = new EntityScanner();

		itemTaskCatalogue = new ItemTaskCatalogue();
		containerHandler = new ContainerHandler();
		
		hacks = new Hacks();

		itemWorkDictionary = new ItemWorkDictionary();
//		itemWorkDictionary.init();

		previousLoopState = LoopState.TITLE_SCREEN;
		customBaritone = new AutoMCBaritone(this);
		

		inputOverride = new InputOverrider();

		commandParser = new CommandParser();

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

		player.reset();

		// Init runners
		taskRunner.start();

		// Reload config files, so we can quickly change em
		itemWorkDictionary.init();

		FullBright.enable();
	}

	public void onPlayerDisconnect() {
		// Don't stop the task runner just yet.
		// There are rare situations where you might want to run stuff while disconnected
		// for example, to auto-reconnect.
		combatRunner.stop();
		survivalRunner.stop();
	}


	/**
	 * When Minecraft ticks. Period.
	 */
	public void onTick() {

		DebugBreaker.onTick();
		// If we press cancel, stop the runners.
		if (isCancelPressing()) {
			onCancel();
			cancelLastFrame = true;
		} else {
			cancelLastFrame = false;

			// Pause ticking when we're breaked.
			if (DebugBreaker.isBreaked()) {
				return;
			}
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_J)) {
			taskRunner.printTaskChain();
			/*
			AutoMC.getAutoMC().player.inventory.putItemInEnchantingTable("book");
			int enchantment = 3;
			AutoMC.getAutoMC().player.inventory.putLapisInEnchantingTable(enchantment);
			ContainerEnchantment c = (ContainerEnchantment) Minecraft.getMinecraft().player.openContainer;
			if (enchantment <= c.enchantLevels.length) {
				//c.enchantItem(Minecraft.getMinecraft().player, enchantment - 1);
				Minecraft.getMinecraft().playerController.sendEnchantPacket(c.windowId, enchantment - 1);
				AutoMC.getAutoMC().player.inventory.getEnchantingTableOutput();
			}
			*/
		}
		LoopState state = LoopState.GAME;
		if (Minecraft.getMinecraft().player != null) {
			// In game
			if (previousLoopState == LoopState.TITLE_SCREEN) {
				onPlayerInit();
			}
			hacks.tick();
			player.onTick();
			containerHandler.onTick();
			entityScanner.onTick();
			combatRunner.tick(state);
			survivalRunner.tick(state);
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

	public void onInputTick() {
		inputOverride.onTick();
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
		inputOverride.reset();
		if (cancelLastFrame) return;
		combatRunner.stop();
		survivalRunner.stop();
		taskRunner.printTaskChain();
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
		((MineProcess)getBaritone().getMineProcess()).resetBlacklist();
	}

	// TODO: Parameterize this somehow
	private boolean isCancelPressing() {
		return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_K); 
	}
}
