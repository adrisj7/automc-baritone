package baritone.launch.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import automc.AutoMC;
import net.minecraft.client.gui.inventory.GuiContainer;

@Mixin(GuiContainer.class)
public class AutoMCMixinGuiScreen {

    @Inject(
    		method = "onGuiClosed",
    		at = @At("HEAD")
	)
	public void onGuiClosed(CallbackInfo ci) {
    	AutoMC.getAutoMC().containerHandler.onGuiClose();
    }
}
