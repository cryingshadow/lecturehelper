package lecturehelper;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import lecturehelper.structures.*;

public class RootFolderInitializer {

    public static void initializeRootFolder(final File classFile) throws IOException {
        final String classFileName = classFile.getName();
        final String classIdentifier = classFileName.substring(0, classFileName.length() - 4);
        final ParticipantsAndDates participantsAndDates = ParticipantsAndDates.fromFile(classFile);
        final Path root = classFile.getAbsoluteFile().toPath().getParent().resolve(classIdentifier);
        if (!root.toFile().mkdir()) {
            throw new IOException("Could not create directory " + root.toFile().getName() + "!");
        }
        for (final String date : participantsAndDates.dates()) {
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
        final File metaFile = root.getParent().resolve("meta.txt").toFile();
        try (BufferedReader reader = new BufferedReader(new FileReader(metaFile))) {
            reader.readLine();
            reader.readLine();
            final String thirdLine = reader.readLine();
            if (thirdLine != null && !thirdLine.isBlank()) {
                if ("EXAM".equals(thirdLine)) {
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
                } else {
                    final int numOfTopics = Integer.parseInt(reader.readLine());
                    final String[] topics = new String[numOfTopics];
                    for (int i = 0; i < numOfTopics; i++) {
                        topics[i] = reader.readLine().split(";")[0];
                    }
                    try (
                        BufferedWriter writer =
                            new BufferedWriter(new FileWriter(root.resolve("preferences.txt").toFile()))
                    ) {
                        writer.write(String.valueOf(numOfTopics));
                        writer.write("\n");
                        for (final String topic : topics) {
                            writer.write(topic);
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
            }
        }
    }

}
