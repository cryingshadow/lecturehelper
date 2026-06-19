package lecturehelper;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.zip.*;

import lecturehelper.structures.*;

public class ExaminationArchiver {

    private static final Set<String> EXCLUDED_FILE_TYPES = Set.of("log", "tex", "out", "aux");

    public static void archiveExaminationFiles(
        final File classFile,
        final Optional<File> exclude,
        final File outputFile
    ) throws IOException {
        final Path root = Main.getRootFromClassFile(classFile);
        final ParticipantsAndDates participantsAndDates = ParticipantsAndDates.fromFile(classFile);
        final Map<String, List<Path>> filesByParticipant = new TreeMap<String, List<Path>>();
        final List<String> excludedParticipants = ExaminationArchiver.parseExclusion(exclude);
        for (final String participant : participantsAndDates.participants()) {
            if (excludedParticipants.contains(participant)) {
                continue;
            }
            filesByParticipant.put(
                NameTransformer.decodeNameFromFile(NameTransformer.encodeNameForFile(participant)).get(),
                new LinkedList<Path>()
            );
        }
        Files.walkFileTree(
            root,
            new FileVisitor<Path>() {

                @Override
                public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                    if (exc != null) {
                        exc.printStackTrace(System.err);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(
                    final Path dir,
                    final BasicFileAttributes attrs
                ) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(
                    final Path file,
                    final BasicFileAttributes attrs
                ) throws IOException {
                    final String fileName = file.getFileName().toString();
                    if (ExaminationArchiver.hasAllowedExtension(fileName)) {
                        final Optional<String> name = NameTransformer.decodeNameFromFile(fileName);
                        if (name.isPresent() && filesByParticipant.containsKey(name.get())) {
                            filesByParticipant.get(name.get()).add(file);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
                    if (exc != null) {
                        exc.printStackTrace(System.err);
                    }
                    return FileVisitResult.CONTINUE;
                }

            }
        );
        final List<String> commonFolders = ExaminationArchiver.parseCommonFolders(classFile);
        try (
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            ZipOutputStream zip = new ZipOutputStream(outputStream);
        ) {
            for (final Map.Entry<String, List<Path>> entry : filesByParticipant.entrySet()) {
                final String folderName = entry.getKey().replaceAll(" ", "_");
                zip.putNextEntry(new ZipEntry(folderName + "/"));
                zip.closeEntry();
                for (final Path file : entry.getValue()) {
                    ExaminationArchiver.addToZipFile(file, folderName, zip);
                }
            }
            for (final String folderName : commonFolders) {
                zip.putNextEntry(new ZipEntry(folderName + "/"));
                zip.closeEntry();
                for (final Path file : Files.list(root.resolve(folderName)).toList()) {
                    ExaminationArchiver.addToZipFile(file, folderName, zip);
                }
            }
        }
    }

    private static void addToZipFile(
        final Path file,
        final String folder,
        final ZipOutputStream zip
    ) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file.toFile())) {
            final String zipEntryName = String.format("%s/%s", folder, file.getFileName().toString());
            zip.putNextEntry(new ZipEntry(zipEntryName));
            final byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) >= 0) {
                zip.write(buffer, 0, len);
            }
            zip.closeEntry();
        }
    }

    private static boolean hasAllowedExtension(final String fileName) {
        return !ExaminationArchiver.EXCLUDED_FILE_TYPES.contains(
            fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase()
        );
    }

    private static List<String> parseCommonFolders(final File classFile) throws IOException {
        final List<String> result = new LinkedList<String>();
        boolean skip = true;
        for (final String line : Files.readAllLines(classFile.toPath())) {
            if (line.isBlank()) {
                continue;
            }
            if ("common:".equals(line.strip())) {
                skip = false;
                continue;
            }
            if (skip) {
                continue;
            }
            result.add(line);
        }
        return result;
    }

    private static List<String> parseExclusion(final Optional<File> exclude) throws IOException {
        if (exclude.isEmpty()) {
            return Collections.emptyList();
        }
        return Files.lines(exclude.get().toPath()).filter(line -> !line.isBlank()).toList();
    }

}
