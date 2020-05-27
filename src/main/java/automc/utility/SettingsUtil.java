package automc.utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

public class SettingsUtil {
    private static boolean isComment(String line) {
        return line.startsWith("#") || line.startsWith("//");
    }

    public static <T> T readJson(Path file, Class<T> classOfT) throws IOException, JsonSyntaxException, JsonIOException {
		try (FileReader scan = new FileReader(file.toFile())) {
			Gson gson = new Gson();
			return gson.fromJson(new JsonReader(scan), classOfT);
        }
    }

	public static String readEntireFile(Path file) throws IOException {
		String result = "";
        try (BufferedReader scan = Files.newBufferedReader(file)) {
            String line;
            while ((line = scan.readLine()) != null) {
                if (line.isEmpty() || isComment(line)) {
                    continue;
                }
                result += line + "\n";
            }
        }
        return result;
    }

}
