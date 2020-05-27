package automc;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class Logger {

	public static void debug(Object from, Object message) {
		log(from, "\u00A77" + message);
	}

	public static void debug(Object message) {
		log("\u00A77" + message);
	}

	public static void log(Object from, Object message) {
		String objectName = from.getClass().getSimpleName();
		if (objectName == "") {
			objectName = from.toString();
		}
		String object = "[" + objectName + "]: ";
		debug(object + message.toString());
	}

	public static void log(Object message) {
		if (Minecraft.getMinecraft().player == null) {
			System.out.println("#####LOGGER (since player is null)#####: " + message);
			return;
		}
		String msg = "\u00A72\u00A7l\u00A7o[AutoMC] \u00A7r" + message.toString();
		ITextComponent m = new TextComponentString(msg);
		Minecraft.getMinecraft().player.sendMessage(m);
	}
	public static void logError(Object message, boolean stacktrace) {
		debug("\u00A7cError: " + message.toString());

		if (stacktrace) {
			// Also log stack trace
			System.err.println("Error Stack trace:");
			StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();
			for (int i = 1; i < stackTraces.length; i++) {
			     System.err.println(stackTraces[i]);
			}
		}
	}
	public static void logError(Object message) {
		logError(message, true);
	}
}
