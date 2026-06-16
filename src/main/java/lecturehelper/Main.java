package lecturehelper;

import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import clit.*;
import lecturehelper.structures.*;

public class Main {

    public static String escapeForLaTeX(final String text) {
        return text.replaceAll("\\\\", "\\\\textbackslash")
            .replaceAll("([&\\$%\\{\\}_#])", "\\\\$1")
            .replaceAll("~", "\\\\textasciitilde{}")
            .replaceAll("\\^", "\\\\textasciicircum{}")
            .replaceAll("\\\\textbackslash", "\\\\textbackslash{}")
            .replaceAll("([^\\\\])\"", "$1''")
            .replaceAll("^\"", "''");
    }

    public static Path getRootFromClassFile(final File classFile) {
        final String classFileName = classFile.getName();
        final String classIdentifier = classFileName.substring(0, classFileName.length() - 4);
        return classFile.getAbsoluteFile().toPath().getParent().resolve(classIdentifier);
    }

    public static void main(final String[] args)
    throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final CLITamer<Flag> tamer = new CLITamer<Flag>(Flag.class);
        if (args == null || args.length < 1) {
            System.out.println(tamer.getParameterDescriptions());
            System.out.println(Main.helpText());
            return;
        }
        final Parameters<Flag> options = tamer.parse(args);
        switch (Mode.valueOf(options.get(Flag.MODE))) {
        case ARCHIVE:
            ExaminationArchiver.archiveExaminationFiles(
                new File(options.get(Flag.CLASSFILE)),
                Optional.ofNullable(options.get(Flag.EXCLUDE)).map(File::new),
                new File(options.get(Flag.OUTPUT))
            );
            break;
        case ATTENDANCE:
            AttendanceListUpdater.updateAttendanceList(
                new File(options.get(Flag.ATTENDANCE)),
                new File(options.get(Flag.EXPORT))
            );
            break;
        case CLASS:
            CalendarExport.createClassFiles(
                new File(options.get(Flag.PARTICIPANTS)),
                new File(options.get(Flag.EXPORT))
            );
            break;
        case LIST:
            RootFolderInitializer.initializeRootFolder(new File(options.get(Flag.CLASSFILE)));
            break;
        case QUIZ:
            QuizQuestions.transformQuizFile(new File(options.get(Flag.QUIZ)), new File(options.get(Flag.OUTPUT)));
            break;
        case REVIEWER_ASSIGN:
            ReviewerAssignments.writeAssignment(new File(options.get(Flag.PARTICIPANTS)));
            break;
        case REVIEWER_SHOW:
            ReviewerAssignments.showAssignmentsByReviewer(new File(options.get(Flag.ASSIGNMENT)));
            break;
        case TALK:
            ProtocolsFolderInitializer.initializeProtocolsFolder(
                new File(options.get(Flag.ASSIGNMENT)),
                new File(options.get(Flag.CLASSFILE))
            );
            break;
        default:
            throw new IllegalStateException("Unknown Mode detected!");
        }
    }

    private static String helpText() {
        return String.format(
            "Available modes:\n%s",
            Arrays.stream(Mode.values())
            .map(mode ->
                String.format(
                    "%s (%s)",
                    mode.name(),
                    mode.parameters.stream()
                    .map(flag -> "-" + flag.shortName())
                    .collect(Collectors.joining(" and "))
                )
            ).collect(Collectors.joining("\n"))
        );
    }

}
