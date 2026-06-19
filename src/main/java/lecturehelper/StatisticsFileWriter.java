package lecturehelper;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

import lecturehelper.structures.*;

public class StatisticsFileWriter {

    private static final String[] GRADES =
        new String[] {
            "1.0",
            "1.3",
            "1.7",
            "2.0",
            "2.3",
            "2.7",
            "3.0",
            "3.3",
            "3.7",
            "4.0",
            "5.0"
        };

    public static void writeStatisticsFileFromProtocols(final File classFile) throws IOException {
        final Path root = Main.getRootFromClassFile(classFile);
        final ParticipantsAndDates participantsAndDates = ParticipantsAndDates.fromFile(classFile);
        final String group = StatisticsFileWriter.parseGroupFromClassFile(classFile);
        final File metaFile = root.getParent().resolve("meta.txt").toFile();
        final Subject subject = Subject.fromFile(metaFile);
        final TalkMode talkMode = TalkMode.fromFile(metaFile);
        final String fileName = String.format("statistics%s%s.tex", subject.shortName(), group);
        final String date =
            Arrays.stream(participantsAndDates.dates()).sorted(Comparator.reverseOrder()).findFirst().get();
        final Path protocols = root.resolve("protocols");
        final int[] gradeCounts = StatisticsFileWriter.countGrades(protocols);
        try (
            BufferedWriter writer =
                new BufferedWriter(new FileWriter(protocols.resolve(fileName).toFile()))
        ) {
            writer.write("\\documentclass[12pt]{article}\n\n");
            writer.write("\\usepackage[a4paper,landscape,margin=1cm]{geometry}\n");
            writer.write("\\usepackage{pgfplots}\n\n");
            writer.write("\\pagestyle{empty}\n\n");
            writer.write("\\begin{document}\n\n");
            writer.write("\\pgfplotstableread[row sep=\\\\,col sep=&]{\n");
            writer.write("    grade & number \\\\\n");
            for (int i = 0; i < StatisticsFileWriter.GRADES.length; i++) {
                writer.write("    ");
                writer.write(StatisticsFileWriter.GRADES[i]);
                writer.write("   & ");
                final String gradeCount = String.valueOf(gradeCounts[i]);
                writer.write(" ".repeat(6 - gradeCount.length()));
                writer.write(gradeCount);
                writer.write(" \\\\\n");
            }
            writer.write("}\\mydata\n\n");
            writer.write("\\vspace*{2cm}\n\n");
            writer.write("\\begin{center}\n\n");
            writer.write("{\\Huge \\textbf{Notenspiegel ");
            writer.write(talkMode == TalkMode.PRACTICAL ? "Praktische Prüfung" : "Referat");
            writer.write(" ");
            writer.write(subject.shortName());
            writer.write(" vom ");
            writer.write(String.format("%s.%s.20%s", date.substring(4, 6), date.substring(2, 4), date.substring(0, 2)));
            writer.write("}}\n\n");
            writer.write("\\vspace*{1cm}\n\n");
            writer.write("{\\large\n");
            writer.write("\\begin{tikzpicture}\n");
            writer.write("    \\begin{axis}[\n");
            writer.write("            ybar,\n");
            writer.write("            bar width=.5cm,\n");
            writer.write("            width=0.8\\paperwidth,\n");
            writer.write("            height=0.5\\paperheight,\n");
            writer.write("            legend style={");
            writer.write("                at={(0.5,1)},\n");
            writer.write("                anchor=north,\n");
            writer.write("                legend columns=-1\n");
            writer.write("            },\n");
            writer.write("            symbolic x coords={1.0,1.3,1.7,2.0,2.3,2.7,3.0,3.3,3.7,4.0,5.0},\n");
            writer.write("            xtick=data,\n");
            writer.write("            nodes near coords,\n");
            writer.write("            nodes near coords align={vertical},\n");
            writer.write("            ymin=0,ymax=6,\n");
            writer.write("            ylabel={Anzahl},\n");
            writer.write("            x label style={at={(axis description cs:0.5,-0.05)},anchor=north},\n");
            writer.write("            xlabel={Note}\n");
            writer.write("        ]\n");
            writer.write("        \\addplot table[x=grade,y=number]{\\mydata};\n");
            writer.write("    \\end{axis}\n");
            writer.write("\\end{tikzpicture}\n");
            writer.write("}\n\n");
            writer.write("\\vspace*{8mm}\n\n");
            writer.write("Notendurchschnitt: ");
            writer.write(StatisticsFileWriter.averageGrade(gradeCounts));
            writer.write("\n\n");
            writer.write("\\end{center}\n\n");
            writer.write("\\end{document}\n");
        }
    }

    private static String averageGrade(final int[] gradeCounts) {
        final int sum =
            gradeCounts[0] * 100
            + gradeCounts[1] * 130
            + gradeCounts[2] * 170
            + gradeCounts[3] * 200
            + gradeCounts[4] * 230
            + gradeCounts[5] * 270
            + gradeCounts[6] * 300
            + gradeCounts[7] * 330
            + gradeCounts[8] * 370
            + gradeCounts[9] * 400
            + gradeCounts[10] * 500;
        final int average = sum / Arrays.stream(gradeCounts).sum();
        final int grade = average / 100;
        final int last = average % 10;
        final int butlast = (average / 10) % 10;
        return String.format(
            "$%d{,}%d$",
            last >= 5 && butlast == 9 ? grade + 1 : grade,
            last >= 5 ? (butlast + 1) % 10 : butlast
        );
    }

    private static int[] countGrades(final Path protocols) throws IOException {
        final int[] result = new int[11];
        for (final File protocol : protocols.toFile().listFiles()) {
            if (!protocol.getName().toLowerCase().endsWith(".pdf")) {
                continue;
            }
            final String grade = StatisticsFileWriter.extractGrade(protocol);
            switch (grade) {
            case "1,0":
                result[0]++;
                break;
            case "1,3":
                result[1]++;
                break;
            case "1,7":
                result[2]++;
                break;
            case "2,0":
                result[3]++;
                break;
            case "2,3":
                result[4]++;
                break;
            case "2,7":
                result[5]++;
                break;
            case "3,0":
                result[6]++;
                break;
            case "3,3":
                result[7]++;
                break;
            case "3,7":
                result[8]++;
                break;
            case "4,0":
                result[9]++;
                break;
            case "5,0":
                result[10]++;
                break;
            default:
                throw new IOException("Could not parse grade!");
            }
        }
        return result;
    }

    private static String extractGrade(final File protocol) throws IOException {
        final File tmp = File.createTempFile(protocol.getName().substring(0, protocol.getName().length() - 4), ".txt");
        tmp.deleteOnExit();
        try {
            new ProcessBuilder(
                "pdftotext",
                protocol.getName(),
                tmp.getAbsolutePath()
            ).inheritIO().directory(protocol.getAbsoluteFile().getParentFile()).start().waitFor(30, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            throw new IOException(e);
        };
        boolean skip = true;
        for (final String line : Files.lines(tmp.toPath()).toList()) {
            if (line.endsWith("Gesamtbeurteilung") || line.endsWith("Overall Assessment")) {
                skip = false;
                continue;
            }
            if (skip) {
                continue;
            }
            if (
                Arrays
                .stream(StatisticsFileWriter.GRADES)
                .map(grade -> grade.replace('.', ','))
                .anyMatch(grade -> grade.equals(line))
            ) {
                return line;
            }
        }
        throw new IOException("Could not parse grade!");
    }

    private static String parseGroupFromClassFile(final File classFile) {
        return classFile.getName().substring(3, classFile.getName().length() - 4);
    }

}
