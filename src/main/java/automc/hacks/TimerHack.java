package automc.hacks;

import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;

// TODO: Don't make this static, you anti-java hippie
public class TimerHack {

	private static float defaultTimeScale = 1f;
	private static float targetTimeScale = 3f;
    private static Field clientTimer = null;

    private static boolean enabled = false;
    
    public static void setTime(float timerTime) {
    	TimerHack.targetTimeScale = timerTime;
    }

	public static void enable() {
		enabled = true;
	}
	
	public static void tick() {
		
		float defaultTick = 20f;
		updateClientTickrate(defaultTick * (enabled? targetTimeScale : defaultTimeScale));
	}

	public static void disable() {
		// Disable
		enabled = false;
	}

	public static boolean isEnabled() {
		return enabled;
	}
	
	public static void toggle() {
		if (isEnabled())
			disable();
		else
			enable();
	}

	public static void updateClientTickrate(float tickrate) {
        Minecraft mc = Minecraft.getMinecraft();
        if(mc == null) return; // Oops! Try again!
        try {
            if(clientTimer == null) {
                for(Field f : mc.getClass().getDeclaredFields()) {
                    if(f.getType() == Timer.class) {
                        clientTimer = f;
                        clientTimer.setAccessible(true);
                        break;
                    }
                }
            }
            Timer t = (Timer) clientTimer.get(mc);

            Field tickLengthField = t.getClass().getDeclaredField("tickLength");
            tickLengthField.setAccessible(true);
            float targetTickLength = 1000F / tickrate;
            tickLengthField.set(t, targetTickLength);

            //t.elapsedTicks *= tickrate;
            //t.elapsedPartialTicks *= tickrate;
            //clientTimer.set(mc, new Timer(tickrate));
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}