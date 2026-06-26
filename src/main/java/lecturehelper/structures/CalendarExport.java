package lecturehelper.structures;

import java.io.*;
import java.util.*;

import lecturehelper.*;
import ocp.*;

public class CalendarExport extends LinkedHashMap<LectureForCalendar, List<OCEntry>>{

    private static final long serialVersionUID = 1L;

    public static CalendarExport parseCalendarExport(final File calendarExport) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(calendarExport))) {
            return new CalendarExport(OCEntry.parseAndGroup(reader, LectureExtractor.INSTANCE));
        }
    }

    private CalendarExport(final Map<LectureForCalendar, List<OCEntry>> map) {
        super(map);
    }

}
