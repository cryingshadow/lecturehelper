package lecturehelper;

import java.util.*;
import java.util.function.*;

import lecturehelper.structures.*;
import ocp.*;

public class LectureExtractor implements Function<OCEntry, LectureForCalendar> {

    public static final LectureExtractor INSTANCE = new LectureExtractor();

    private static final String LECTURE_PATTERN = ".+ \\((Vorlesung|E-Learning|Hybrid-Vorlesung)\\) \\| (\\w|\\s|,)+";

    private LectureExtractor() {}

    @Override
    public LectureForCalendar apply(final OCEntry entry) {
        final String subject = entry.subject();
        if (!subject.matches(LectureExtractor.LECTURE_PATTERN)) {
            return new LectureForCalendar("other", Set.of());
        }
        return LectureForCalendar.parse(
            subject.substring(0, subject.indexOf(" (")) + subject.substring(subject.indexOf(") ") + 1)
        );
    }

}
