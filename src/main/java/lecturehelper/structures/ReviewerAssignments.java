package lecturehelper.structures;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class ReviewerAssignments extends ArrayList<ReviewerAssignment> {

    private static final long serialVersionUID = 1L;

    public static void showAssignmentsByReviewer(final File assignmentFile) throws IOException {
        final ReviewerAssignments assignments = new ReviewerAssignments();
        try (BufferedReader reader = new BufferedReader(new FileReader(assignmentFile))) {
            String line = reader.readLine();
            while (line != null) {
                if (line.isBlank()) {
                    line = reader.readLine();
                    continue;
                }
                final String[] columns = line.split(";");
                assignments.add(new ReviewerAssignment(columns[0], columns[1], columns[2], columns[3], columns[4]));
                line = reader.readLine();
            }
        }
        final Map<String, List<String>> byReviewers = new TreeMap<String, List<String>>();
        for (final ReviewerAssignment assignment : assignments) {
            byReviewers.merge(assignment.reviewer1(), List.of(assignment.participant()), ReviewerAssignments::concat);
            byReviewers.merge(assignment.reviewer2(), List.of(assignment.participant()), ReviewerAssignments::concat);
            byReviewers.merge(assignment.reviewer3(), List.of(assignment.participant()), ReviewerAssignments::concat);
        }
        for (final Map.Entry<String, List<String>> entry : byReviewers.entrySet()) {
            System.out.print(entry.getKey());
            System.out.println(":");
            for (final String participant : entry.getValue()) {
                System.out.println(participant);
            }
            System.out.println();
        }
    }

    public static void writeAssignment(final File attendanceList) throws IOException {
        final Path root = attendanceList.getAbsoluteFile().toPath().getParent();
        final File output = root.resolve("reviewerAssignment.csv").toFile();
        try (
            BufferedReader reader = new BufferedReader(new FileReader(attendanceList));
            Writer writer = new BufferedWriter(new FileWriter(output));
        ) {
            final ReviewerAssignments assignments = new ReviewerAssignments(reader);
            for (final ReviewerAssignment assignment : assignments) {
                writer.write(assignment.toString());
                writer.write("\n");
            }
        }
    }

    private static List<String> concat(final List<String> list1, final List<String> list2) {
        return Stream.concat(list1.stream(), list2.stream()).toList();
    }

    public ReviewerAssignments() {
        super();
    }

    public ReviewerAssignments(final BufferedReader reader) throws IOException {
        final List<String> names = new ArrayList<String>();
        String line = reader.readLine();
        while (line != null) {
            if (line.isBlank()) {
                line = reader.readLine();
                continue;
            }
            names.add(line.trim());
            line = reader.readLine();
        }
        final List<String> firstReviewers = new ArrayList<String>(names);
        final List<String> secondReviewers = new ArrayList<String>(names);
        final List<String> thirdReviewers = new ArrayList<String>(names);
        final List<String> pcMembers = new ArrayList<String>(names);
        Collections.shuffle(names);
        Collections.shuffle(firstReviewers);
        Collections.shuffle(secondReviewers);
        Collections.shuffle(thirdReviewers);
        Collections.shuffle(pcMembers);
        int conflict = this.findConflict(names, firstReviewers, secondReviewers, thirdReviewers, pcMembers);
        while (conflict >= 0) {
            this.solveConflict(conflict, names, firstReviewers, secondReviewers, thirdReviewers, pcMembers);
            conflict = this.findConflict(names, firstReviewers, secondReviewers, thirdReviewers, pcMembers);
        }
        for (int i = 0; i < names.size(); i++) {
            this.add(
                new ReviewerAssignment(
                    names.get(i),
                    firstReviewers.get(i),
                    secondReviewers.get(i),
                    thirdReviewers.get(i),
                    pcMembers.get(i)
                )
            );
        }
    }

    public ReviewerAssignments(final Collection<ReviewerAssignment> assignments) {
        super(assignments);
    }

    private int findConflict(
        final List<String> names,
        final List<String> firstReviewers,
        final List<String> secondReviewers,
        final List<String> thirdReviewers,
        final List<String> pcMembers
    ) {
        for (int i = 0; i < names.size(); i++) {
            if (
                this.hasConflict(
                    names.get(i),
                    firstReviewers.get(i),
                    secondReviewers.get(i),
                    thirdReviewers.get(i),
                    pcMembers.get(i)
                )
            ) {
                return i;
            }
        }
        return -1;
    }

    private boolean hasConflict(
        final String name,
        final String reviewer1,
        final String reviewer2,
        final String reviewer3,
        final String pcMember
    ) {
        return name.equals(reviewer1)
            || name.equals(reviewer2)
            || name.equals(reviewer3)
            || name.equals(pcMember)
            || reviewer1.equals(reviewer2)
            || reviewer1.equals(reviewer3)
            || reviewer2.equals(reviewer3);
    }

    private void solveConflict(
        final int conflict,
        final List<String> names,
        final List<String> firstReviewers,
        final List<String> secondReviewers,
        final List<String> thirdReviewers,
        final List<String> pcMembers
    ) {
        final String name = names.get(conflict);
        final String reviewer1 = firstReviewers.get(conflict);
        final String reviewer2 = secondReviewers.get(conflict);
        final String reviewer3 = thirdReviewers.get(conflict);
        final String pcMember = pcMembers.get(conflict);
        final List<Integer> indices = new ArrayList<Integer>(IntStream.range(0, names.size() - 1).boxed().toList());
        Collections.shuffle(indices);
        for (final Integer i : indices) {
            if (i.intValue() == conflict) {
                continue;
            }
            final String otherName = names.get(i);
            final String otherReviewer1 = firstReviewers.get(i);
            final String otherReviewer2 = secondReviewers.get(i);
            final String otherReviewer3 = thirdReviewers.get(i);
            final String otherPcMember = pcMembers.get(i);
            if (
                !this.hasConflict(name, otherReviewer1, reviewer2, reviewer3, pcMember)
                && !this.hasConflict(otherName, reviewer1, otherReviewer2, otherReviewer3, otherPcMember)
            ) {
                firstReviewers.set(i, reviewer1);
                firstReviewers.set(conflict, otherReviewer1);
                return;
            }
            if (
                !this.hasConflict(name, reviewer1, otherReviewer2, reviewer3, pcMember)
                && !this.hasConflict(otherName, otherReviewer1, reviewer2, otherReviewer3, otherPcMember)
            ) {
                secondReviewers.set(i, reviewer2);
                secondReviewers.set(conflict, otherReviewer2);
                return;
            }
            if (
                !this.hasConflict(name, reviewer1, reviewer2, otherReviewer3, pcMember)
                && !this.hasConflict(otherName, otherReviewer1, otherReviewer2, reviewer3, otherPcMember)
            ) {
                thirdReviewers.set(i, reviewer3);
                thirdReviewers.set(conflict, otherReviewer3);
                return;
            }
            if (
                !this.hasConflict(name, reviewer1, reviewer2, reviewer3, otherPcMember)
                && !this.hasConflict(otherName, otherReviewer1, otherReviewer2, otherReviewer3, pcMember)
            ) {
                pcMembers.set(i, pcMember);
                pcMembers.set(conflict, otherPcMember);
                return;
            }
        }
        throw new IllegalStateException("Could not solve conflict!");
    }

}
