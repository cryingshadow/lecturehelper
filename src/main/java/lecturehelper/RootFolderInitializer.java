package lecturehelper;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import lecturehelper.structures.*;

public class RootFolderInitializer {

    public static void initializeRootFolder(final File classFile) throws IOException {
        final Path root = Main.getRootFromClassFile(classFile);
        final ParticipantsAndDates participantsAndDates = ParticipantsAndDates.fromFile(classFile);
        if (!root.toFile().mkdir()) {
            throw new IOException("Could not create directory " + root.toFile().getName() + "!");
        }
        final List<String> dates = new ArrayList<String>();
        for (final String date : participantsAndDates.dates()) {
            dates.add(date);
            try (
                BufferedWriter writer =
                    new BufferedWriter(new FileWriter(root.resolve(date.substring(0, 6) + ".txt").toFile()))
            ) {
                for (final String participant : participantsAndDates.participants()) {
                    writer.write(participant);
                    writer.write("\n");
                }
            }
        }
        Collections.sort(dates);
        ScriptWriter.writeExecutableScript(
            root,
            "groups.sh",
            List.of(
                "#!/bin/bash",
                "",
                "if [[ $# -eq 2 ]]; then",
                "  java -jar ../../../../nameandgrouppicker.jar GROUPS -n $1.txt --count $2",
                " else if [[ $# -eq 3 ]]; then",
                "  java -jar ../../../../nameandgrouppicker.jar GROUPS -n $1.txt --min $2 --max $3",
                "else",
                "  echo \"Illegal number of parameters\" >&2",
                "  exit 2",
                "fi",
                "fi"
            )
        );
        ScriptWriter.writeExecutableScript(
            root,
            "pick.sh",
            List.of(
                "#!/bin/bash",
                "",
                "java -jar ../../../../nameandgrouppicker.jar PICK -n $1.txt -f frequencies.txt"
            )
        );
        final File metaFile = root.getParent().resolve(Main.META_FILE_NAME).toFile();
        final MetaInformation meta = Main.parseMetaInformation(metaFile);
        if (meta.type() == ExaminationMode.EXAM) {
            final Path exercisesPath = root.resolve("exercises");
            exercisesPath.toFile().mkdir();
            ScriptWriter.writeExecutableScript(
                exercisesPath,
                "exgen.sh",
                List.of(
                    "#!/bin/bash",
                    "",
                    "cd ../../../exercises",
                    "",
                    "for d in */ ; do",
                    "  cd $d",
                    "  . build.sh",
                    "  cd ..",
                    "done",
                    "",
                    String.format("cd ../classes/%s/exercises", root.getFileName().toString())
                )
            );
            ScriptWriter.writeExecutableScript(
                exercisesPath,
                "build.sh",
                List.of(
                    "#!/bin/bash",
                    "",
                    ". exgen.sh",
                    "",
                    "compile(){",
                    "    pdflatex \"$1\"",
                    "    pdflatex \"$1\"",
                    "}",
                    "",
                    "for i in exercise*.tex; do",
                    "    compile \"$i\" &",
                    "done",
                    "",
                    "for i in solution*.tex; do",
                    "    compile \"$i\" &",
                    "done",
                    "",
                    "for i in exampleExam*.tex; do",
                    "    compile \"$i\" &",
                    "done",
                    "",
                    "wait"
                )
            );
            final String relativePath = meta.exercisepath() == null ? "../../../exercises/" : meta.exercisepath();
            if (meta.sheets() != null) {
                int number = meta.firstsheetnr() == null ? 1 : meta.firstsheetnr();
                final String firstDate = dates.getFirst();
                for (final Sheet sheet : meta.sheets()) {
                    RootFolderInitializer.initializeSheet(
                        exercisesPath,
                        meta.shorttitle(),
                        firstDate,
                        relativePath,
                        number,
                        sheet
                    );
                    number++;
                }
            }
            if (meta.exams() != null) {
                int number = 1;
                final String lastDate = dates.getLast();
                for (final Exam exam : meta.exams()) {
                    RootFolderInitializer.initializeExam(
                        exercisesPath,
                        meta.shorttitle(),
                        lastDate,
                        relativePath,
                        number,
                        exam.folder(),
                        exam.parameters()
                    );
                    number++;
                }
            }
        } else {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(root.resolve("preferences.txt").toFile()))) {
                writer.write(String.valueOf(meta.topics().size()));
                writer.write("\n");
                for (final Topic topic : meta.topics()) {
                    writer.write(topic.topic());
                    writer.write("\n");
                }
                writer.write(String.valueOf(participantsAndDates.participants().length));
                writer.write("\n");
                for (final String participant : participantsAndDates.participants()) {
                    writer.write(participant);
                    writer.write(";\n");
                }
            }
        }
        final Path slidesPath = root.resolve("slides");
        slidesPath.toFile().mkdir();
        if (meta.slides() != null) {
            for (final Slides slides : meta.slides()) {
                RootFolderInitializer.initializeSlides(
                    slidesPath,
                    RootFolderInitializer.toBeamerDate(dates.getFirst()),
                    slides
                );
            }
        }
    }

    private static void initializeExam(
        final Path exercisesPath,
        final String subject,
        final String lastDate,
        final String relativePath,
        final int number,
        final String folder,
        final List<String> parameters
    ) throws IOException {
        try (
            final BufferedWriter mainWriter =
                new BufferedWriter(
                    new FileWriter(
                        exercisesPath.resolve(
                            String.format("main%sExam%s.tex", subject, String.valueOf(number))
                        ).toFile()
                    )
                );
            final BufferedWriter exerciseWriter =
                new BufferedWriter(
                    new FileWriter(
                        exercisesPath.resolve(
                            String.format("exampleExam%s%s.tex", subject, String.valueOf(number))
                        ).toFile()
                    )
                );
            final BufferedWriter solutionWriter =
                new BufferedWriter(
                    new FileWriter(
                        exercisesPath.resolve(
                            String.format("exampleExam%s%sSolution.tex", subject, String.valueOf(number))
                        ).toFile()
                    )
                );
        ) {
            mainWriter.write("\\usepackage[");
            mainWriter.write(
                Stream.concat(parameters.stream(), Stream.of("noscheme")).collect(Collectors.joining(","))
            );
            mainWriter.write("]{fhdwexam}\n");
            mainWriter.write("\\usepackage{import}\n\n");
            mainWriter.write("\\ExamDate{");
            mainWriter.write(lastDate);
            mainWriter.write("}\n\n");
            mainWriter.write("\\subimport{");
            mainWriter.write(relativePath);
            mainWriter.write(folder);
            mainWriter.write("}{main.tex}\n");
            exerciseWriter.write("\\documentclass[12pt,addpoints]{exam}\n\n");
            exerciseWriter.write("\\input{main");
            exerciseWriter.write(subject);
            exerciseWriter.write("Exam");
            exerciseWriter.write(String.valueOf(number));
            exerciseWriter.write(".tex}\n");
            solutionWriter.write("\\documentclass[12pt,addpoints,answers]{exam}\n\n");
            solutionWriter.write("\\input{main");
            solutionWriter.write(subject);
            exerciseWriter.write("Exam");
            solutionWriter.write(String.valueOf(number));
            solutionWriter.write(".tex}\n");
        }
    }

    private static void initializeSheet(
        final Path exercisesPath,
        final String subject,
        final String firstDate,
        final String relativePath,
        final int number,
        final Sheet sheet
    ) throws IOException {
        final String numberWithPadding = RootFolderInitializer.withPadding(number);
        try (
            final BufferedWriter mainWriter =
                new BufferedWriter(
                    new FileWriter(
                        exercisesPath.resolve(String.format("main%s%s.tex", subject, numberWithPadding)).toFile()
                    )
                );
            final BufferedWriter exerciseWriter =
                new BufferedWriter(
                    new FileWriter(
                        exercisesPath.resolve(String.format("exercise%s%s.tex", subject, numberWithPadding)).toFile()
                    )
                );
            final BufferedWriter solutionWriter =
                new BufferedWriter(
                    new FileWriter(
                        exercisesPath.resolve(String.format("solution%s%s.tex", subject, numberWithPadding)).toFile()
                    )
                );
        ) {
            mainWriter.write("\\usepackage[nocover,nopoints]{fhdwexam}\n");
            mainWriter.write("\\usepackage{import}\n\n");
            mainWriter.write("\\ExamDate{");
            mainWriter.write(firstDate);
            mainWriter.write("}\n\n");
            mainWriter.write("\\newcommand{\\sheetnumber}{");
            mainWriter.write(String.valueOf(number));
            mainWriter.write("}\n\n");
            mainWriter.write("\\subimport{");
            mainWriter.write(relativePath);
            mainWriter.write(sheet.folder());
            mainWriter.write("}{main.tex}\n");
            exerciseWriter.write("\\documentclass[12pt]{exam}\n\n");
            exerciseWriter.write("\\input{main");
            exerciseWriter.write(subject);
            exerciseWriter.write(numberWithPadding);
            exerciseWriter.write(".tex}\n");
            solutionWriter.write("\\documentclass[12pt,answers]{exam}\n\n");
            solutionWriter.write("\\input{main");
            solutionWriter.write(subject);
            solutionWriter.write(numberWithPadding);
            solutionWriter.write(".tex}\n");
        }
    }

    private static void initializeSlides(
        final Path slidesPath,
        final String date,
        final Slides slides
    ) throws IOException {
        try (
            final BufferedWriter writer =
                new BufferedWriter(new FileWriter(slidesPath.resolve(slides.name()).toFile()))
        ) {
            writer.write("\\documentclass{beamer}\n\n");
            writer.write("\\usepackage{fhdwbeamer}\n");
            writer.write("\\usepackage{import}\n\n");
            writer.write("\\colorlet{fhdwgreen}{green!60!black}\n");
            writer.write("\\colorlet{fhdwred}{red}\n\n");
            writer.write("\\date{");
            writer.write(date);
            writer.write("}\n");
            if (slides.commands() != null) {
                for (final String command : slides.commands()) {
                    writer.write(command);
                    writer.write("\n");
                }
            }
            writer.write("\n");
            writer.write("\\subimport{");
            writer.write(slides.folder());
            writer.write("}{");
            writer.write(slides.file());
            writer.write("}\n");
        }
    }

    private static String toBeamerDate(final String date) {
        return String.format("20%s-%s-%s", date.substring(0, 2), date.substring(2, 4), date.substring(4, 6));
    }

    private static String withPadding(final int number) {
        return number < 10 ? "0" + number : String.valueOf(number);
    }

}
