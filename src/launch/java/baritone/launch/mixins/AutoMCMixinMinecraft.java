package baritone.launch.mixins;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import automc.AutoMC;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.Container;

@Mixin(Minecraft.class)
public class AutoMCMixinMinecraft {
    @Inject(
            method = "init",
            at = @At("RETURN")
    )
    private void postInit(CallbackInfo ci) {
        AutoMC.getAutoMC();
    	//BaritoneAPI.getProvider().getPrimaryBaritone();
    }
    
    @Inject(
            method = "runTick",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "net/minecraft/client/Minecraft.currentScreen:Lnet/minecraft/client/gui/GuiScreen;",
                    ordinal = 5,
                    shift = At.Shift.BY,
                    by = -3
            )
    )
    private void runTick(CallbackInfo ci) {
    	AutoMC.getAutoMC().onTick();
    	/*
        final BiFunction<EventState, TickEvent.Type, TickEvent> tickProvider = TickEvent.createNextProvider();

        for (IBaritone baritone : BaritoneAPI.getProvider().getAllBaritones()) {

            TickEvent.Type type = baritone.getPlayerContext().player() != null && baritone.getPlayerContext().world() != null
                    ? TickEvent.Type.IN
                    : TickEvent.Type.OUT;

            baritone.getGameEventHandler().onTick(tickProvider.apply(EventState.PRE, type));
        }
        */
    }
    
	
	@Inject(
			method = "displayGuiScreen",
			at = @At("RETURN")
	)
	private void onDisplayGuiScreen(GuiScreen guiScreenIn, CallbackInfo ci) {
		if (guiScreenIn instanceof GuiChest) {
			Container c = ((GuiChest)guiScreenIn).inventorySlots;
			AutoMC.getAutoMC().containerHandler.onContainerOpen(c);
		}
	}

}
