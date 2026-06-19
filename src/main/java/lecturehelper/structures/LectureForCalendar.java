package lecturehelper.structures;

import java.util.*;
import java.util.stream.*;

public record LectureForCalendar(String title, Set<String> groups) {

    public static LectureForCalendar parse(String line) {
        String[] parts = line.split("\\|");
        return new LectureForCalendar(
            parts[0].trim(),
            Arrays.stream(parts[1].split(",")).map(String::trim).collect(Collectors.toSet())
        );
    }
    
}
