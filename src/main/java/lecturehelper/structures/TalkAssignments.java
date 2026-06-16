package lecturehelper.structures;

import java.io.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

public class TalkAssignments extends LinkedList<TalkAssignment> {

    private static final long serialVersionUID = 1L;

    private static String extractTopic(final String topicEntry) {
        if (topicEntry.contains(")")) {
            return topicEntry.substring(topicEntry.indexOf(')') + 2).strip();
        }
        return topicEntry.strip();
    }

    public TalkAssignments(final BufferedReader assignmentReader, final List<LocalDateTime> dates) throws IOException {
        final List<TopicAssignment> assignmentsWithoutDates = new ArrayList<TopicAssignment>();
        String line = assignmentReader.readLine();
        while (line != null && !line.isBlank()) {
            final String[] assignment = line.split("->");
            if (assignment.length != 2) {
                throw new IOException("File must contain assignments of the form participant -> topic!");
            }
            assignmentsWithoutDates.add(
                new TopicAssignment(assignment[0].strip(), TalkAssignments.extractTopic(assignment[1]))
            );
            line = assignmentReader.readLine();
        }
        final Map<LocalDate, List<LocalDateTime>> datesByDate =
            dates.stream().collect(
                Collectors.toMap(
                    LocalDateTime::toLocalDate,
                    d -> List.of(d),
                    (l1, l2) -> Stream.concat(l1.stream(), l2.stream()).toList()
                )
            );
        int availableDates = dates.size();
        final int neededDates = assignmentsWithoutDates.size();
        final List<LocalDate> keys = new ArrayList<LocalDate>(datesByDate.keySet());
        Collections.sort(keys);
        for (final LocalDate key : keys) {
            final int datesAtDate = datesByDate.get(key).size();
            if (datesAtDate > availableDates - neededDates) {
                break;
            } else {
                availableDates -= datesAtDate;
                datesByDate.remove(key);
            }
        }
        int i = 0;
        keys.retainAll(datesByDate.keySet());
        outer: for (final LocalDate key : keys) {
            for (final LocalDateTime date : datesByDate.get(key)) {
                this.add(new TalkAssignment(assignmentsWithoutDates.get(i), date));
                i++;
                if (i == neededDates) {
                    break outer;
                }
            }
        }
    }

}
