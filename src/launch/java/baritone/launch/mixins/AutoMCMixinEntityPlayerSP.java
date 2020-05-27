package baritone.launch.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import automc.AutoMC;
import baritone.api.event.events.ChatEvent;
import net.minecraft.client.entity.EntityPlayerSP;

/**
 * This class "connects" our project TO minecraft.
 * This is to make sure we do as little code modification to baritone's source code as possible,
 * UNLESS we're very specifically tweaking it or fixing big issues.
 */

@Mixin(EntityPlayerSP.class)
public final class AutoMCMixinEntityPlayerSP {
	@Inject(
            method = "sendChatMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void sendChatMessage(String msg, CallbackInfo ci) {
		ChatEvent event = new ChatEvent(msg);
		AutoMC.getAutoMC().onChat(event);
		if (event.isCancelled()) {
			ci.cancel();
		}
		/*
        ChatEvent event = new ChatEvent(msg);
        IBaritone baritone = BaritoneAPI.getProvider().getBaritoneForPlayer((EntityPlayerSP) (Object) this);
        if (baritone == null) {
            return;
        }
        baritone.getGameEventHandler().onSendChatMessage(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
        */
    }

	/*
	@Inject(
			method = "displayGUIChest",
			at = @At("RETURN")
	)
	private void openContainer(IInventory chestInventory, CallbackInfo ci) {
		AutoMC.getAutoMC().containerHandler.onChestGui(chestInventory);
	}
	*/

	
}
