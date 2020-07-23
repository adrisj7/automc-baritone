package baritone.launch.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import automc.AutoMC;
import net.minecraft.client.multiplayer.WorldClient;

@Mixin(WorldClient.class)
public class AutoMCMixInWorldClient {
	@Inject(
            method = "tick",
            at = @At("HEAD")
    )
	public void onTick(CallbackInfo ci) {
    	AutoMC.getAutoMC().onInputTick();
	}
}
