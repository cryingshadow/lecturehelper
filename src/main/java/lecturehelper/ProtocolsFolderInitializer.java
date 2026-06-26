package lecturehelper;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.*;

import lecturehelper.structures.*;

public class ProtocolsFolderInitializer {

    public static void initializeProtocolsFolder(final File assignmentFile, final File classFile) throws IOException {
        final Path root = assignmentFile.getAbsoluteFile().toPath().getParent();
        final Path protocols = root.resolve("protocols");
        protocols.toFile().mkdir();
        final Path quizAnswers = root.resolve("quizAnswers");
        quizAnswers.toFile().mkdir();
        final File metaFile = root.getParent().resolve(Main.META_FILE_NAME).toFile();
        final MetaInformation meta = Main.parseMetaInformation(metaFile);
        final int numberOfTopics = Integer.parseInt(Files.lines(metaFile.toPath()).skip(3).findFirst().get());
        final String place =
            root.toFile().getName().substring(3).toLowerCase().startsWith("m") ? "Mettmann" : "Bergisch Gladbach";
        final List<LocalDateTime> dates = ProtocolsFolderInitializer.toDates(classFile);
        final TalkAssignments assignments;
        try (BufferedReader assignmentReader = new BufferedReader(new FileReader(assignmentFile))) {
            assignments = new TalkAssignments(assignmentReader, dates);
        }
        ProtocolsFolderInitializer.writeBuildFile(protocols);
        final File quizSolutions = root.resolve("quizSolutions").toFile();
        quizSolutions.mkdir();
        try (
            BufferedWriter solutionsWriter =
                new BufferedWriter(new FileWriter(quizSolutions.toPath().resolve("quizSolutions.csv").toFile()))
        ) {
            LocalDate current = LocalDate.MIN;
            int numOfTalksWithoutBreak = 0;
            for (final TalkAssignment assignment : assignments) {
                if (!assignment.date().toLocalDate().equals(current)) {
                    current = assignment.date().toLocalDate();
                    ProtocolsFolderInitializer.writeDateLineToConsole(current);
                    numOfTalksWithoutBreak = 0;
                } else if (numOfTalksWithoutBreak > 1) {
                    ProtocolsFolderInitializer.writeBreakLineToConsole(assignment);
                    numOfTalksWithoutBreak = 0;
                }
                ProtocolsFolderInitializer.writeAnnouncementLineToConsole(assignment);
                numOfTalksWithoutBreak++;
                ProtocolsFolderInitializer.writeLineToSolutionFile(
                    solutionsWriter,
                    assignment,
                    metaFile,
                    numberOfTopics
                );
                ProtocolFileWriter.writeProtocolFile(protocols, meta, place, assignment);
                final String[] nameParts = assignment.topicAssignment().participant().split(" ");
                final String lastName = nameParts[nameParts.length - 1].toLowerCase();
                try (
                    BufferedWriter quizAnswersWriter =
                        new BufferedWriter(new FileWriter(quizAnswers.resolve(lastName + ".csv").toFile()))
                ) {
                    quizAnswersWriter.write(assignment.topicAssignment().participant());
                    quizAnswersWriter.write("\n");
                    for (final TalkAssignment assignment2 : assignments) {
                        if (assignment2.equals(assignment)) {
                            continue;
                        }
                        quizAnswersWriter.write(assignment2.topicAssignment().participant());
                        quizAnswersWriter.write(";\n");
                    }
                }
            }
        }
    }

    static Stream<LocalDateTime> toLocalDateTime(final String dateString) {
        final int multiplicity = Integer.parseInt(dateString.substring(dateString.length() - 1));
        final LocalDateTime start =
            LocalDate.of(
                2000 + Integer.parseInt(dateString.substring(0, 2)),
                Integer.parseInt(dateString.substring(2, 4)),
                Integer.parseInt(dateString.substring(4, 6))
            ).atTime(Integer.parseInt(dateString.substring(6, 8)), Integer.parseInt(dateString.substring(8, 10)));
        final List<LocalDateTime> result = new LinkedList<LocalDateTime>();
        int numOfBreaks = 0;
        for (int i = 0; i < multiplicity; i++) {
            if (i > 0 && i % 2 == 0) {
                numOfBreaks++;
            }
            result.add(start.plusMinutes(i * 45 + numOfBreaks * 15));
        }
        return result.stream();
    }

    private static Optional<String> getSolution(
        final String topic,
        final File metaFile,
        final int numberOfTopics
    ) throws IOException {
        final Path path = metaFile.toPath();
        final Optional<Path> solutionFile =
            Files
            .lines(path)
            .skip(4)
            .limit(numberOfTopics)
            .map(line -> line.split(";"))
            .filter(split -> split.length == 2)
            .filter(split -> topic.equals(split[0]))
            .map(split -> path.getParent().resolve(split[1]))
            .findAny();
        return solutionFile.isEmpty() ?
            Optional.empty() :
                Optional.of(Files.lines(solutionFile.get()).findFirst().get().substring(1));
    }

    private static List<LocalDateTime> toDates(final File classFile) throws IOException {
        return
            Arrays
            .stream(ParticipantsAndDates.fromFile(classFile).dates())
            .flatMap(ProtocolsFolderInitializer::toLocalDateTime)
            .toList();
    }

    private static void writeAnnouncementLineToConsole(final TalkAssignment assignment) {
        System.out.println(
            String.format(
                "%s-%s: %s (%s)",
                assignment.date().format(DateTimeFormatter.ofPattern("HH:mm")),
                assignment.date().plusMinutes(45).format(DateTimeFormatter.ofPattern("HH:mm")),
                assignment.topicAssignment().topic(),
                assignment.topicAssignment().participant()
            )
        );
    }

    private static void writeBreakLineToConsole(final TalkAssignment assignment) {
        System.out.println(
            String.format(
                "%s-%s: Pause",
                assignment.date().minusMinutes(15).format(DateTimeFormatter.ofPattern("HH:mm")),
                assignment.date().format(DateTimeFormatter.ofPattern("HH:mm"))
            )
        );
    }

    private static void writeBuildFile(final Path protocols) throws IOException {
        ScriptWriter.writeExecutableScript(
            protocols,
            "build.sh",
            List.of(
                "#!/bin/bash\n\n",
                "",
                "compile(){",
                "    pdflatex \"$1\"",
                "    pdflatex \"$1\"",
                "}",
                "",
                "for i in *.tex; do",
                "    compile \"$i\" &",
                "done",
                "",
                "wait"
            )
        );
    }

    private static void writeDateLineToConsole(final LocalDate date) {
        System.out.println();
        System.out.println(String.format("%s:", date.format(DateTimeFormatter.ofPattern("dd.MM.uuuu"))));
    }

    private static void writeLineToSolutionFile(
        final BufferedWriter solutionsWriter,
        final TalkAssignment assignment,
        final File metaFile,
        final int numberOfTopics
    ) throws IOException {
        solutionsWriter.write(assignment.topicAssignment().participant());
        solutionsWriter.write(";");
        final Optional<String> solution =
            ProtocolsFolderInitializer.getSolution(assignment.topicAssignment().topic(), metaFile, numberOfTopics);
        solutionsWriter.write(solution.orElseGet(() -> ""));
        solutionsWriter.write("\n");
    }

}
