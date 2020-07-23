package baritone.launch.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import automc.AutoMC;
import net.minecraft.item.ItemFood;

@Mixin(ItemFood.class)
public class AutoMCMixinItemFood {

    @Inject(
    		method = "onFoodEaten",
    		at = @At("HEAD")
	)
	public void onFoodEaten(CallbackInfo ci) {
    	AutoMC.getAutoMC().survivalRunner.eater.onFoodEaten();
    }

}
