package lecturehelper;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ScriptWriter {

    public static void writeExecutableScript(
        final Path root,
        final String name,
        final List<String> lines
    ) throws IOException {
        final File script = root.resolve(name).toFile();
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(script))) {
            for (final String line : lines) {
                writer.write(line);
                writer.write("\n");
            }
        }
        script.setExecutable(true);
    }

}
