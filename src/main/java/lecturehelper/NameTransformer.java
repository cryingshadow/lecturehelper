package lecturehelper;

import java.util.*;
import java.util.regex.*;

public class NameTransformer {

    private static final Pattern NAME_PATTERN = Pattern.compile("\\[[^\\]]+\\]");

    public static Optional<String> decodeNameFromFile(final String fileName) {
        final Matcher matcher = NameTransformer.NAME_PATTERN.matcher(fileName);
        if (!matcher.find()) {
            return Optional.empty();
        }
        return Optional.of(fileName.substring(matcher.start() + 1, matcher.end() - 1));
    }

    public static String encodeNameForFile(final String name) {
        return String.format("[%s]", NameTransformer.toASCII(name.replaceAll(" ", "_")));
    }

    public static boolean matches(final String name, final String decodedNameFromFile) {
        return NameTransformer.toASCII(name).equals(decodedNameFromFile);
    }

    private static String toASCII(final String name) {
        return name
            .replaceAll("ä", "ae")
            .replaceAll("Ä", "Ae")
            .replaceAll("ö", "oe")
            .replaceAll("Ö", "Oe")
            .replaceAll("ü", "ue")
            .replaceAll("Ü", "Ue")
            .replaceAll("ß", "ss")
            .replaceAll("Á", "A")
            .replaceAll("á", "a")
            .replaceAll("É", "E")
            .replaceAll("é", "e")
            .replaceAll("[^\\x00-\\x7F]", "");
    }

}
