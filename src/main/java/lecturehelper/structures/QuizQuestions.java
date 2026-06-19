package lecturehelper.structures;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import lecturehelper.*;

public class QuizQuestions extends ArrayList<QuizQuestion> {

    private static final long serialVersionUID = 1L;

    public static void transformQuizFile(final File quizFile, final File output) throws IOException {
        final Quiz quiz = QuizQuestions.parseQuiz(quizFile);
        final Random random = new Random();
        final List<Character> correctAnswers = new LinkedList<Character>();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
            writer.write("\\documentclass[12pt]{article}\n\n");
            writer.write("\\input{../../../../../../templates/mctests.tex}\n\n");
            writer.write("\\begin{document}\n\n");
            writer.write("\\newtest[");
            writer.write(quiz.student());
            writer.write("]{");
            writer.write(quiz.title());
            writer.write("}\n\n");
            for (final QuizQuestion question : quiz.questions()) {
                writer.write("\\question{");
                writer.write(question.question());
                writer.write("}{%\n");
                int skip = random.nextInt(4);
                for (int i = 0; i < 3; i++) {
                    writer.write("\\item ");
//                    writer.write('A' + i + (skip < 0 ? 1 : 0));
//                    writer.write(") ");
                    if (skip == 0) {
                        correctAnswers.add((char)('a' + i));
                        writer.write(question.correctAnswer());
                        writer.write("\n\\item ");
//                        writer.write('A' + i + 1);
//                        writer.write(") ");
                    }
                    writer.write(question.wrongAnswers().get(i));
                    skip--;
                    writer.write("\n");
                }
                if (skip == 0) {
                    correctAnswers.add('d');
                    writer.write("\\item ");
//                    writer.write("D) ");
                    writer.write(question.correctAnswer());
                    writer.write("\n");
                }
                writer.write("}{}\n\n");
            }
            writer.write("% ");
            writer.write(correctAnswers.stream().map(String::valueOf).collect(Collectors.joining(";")));
            writer.write("\n\n\\end{document}\n\n");
        }
    }

    private static Quiz parseQuiz(final File quizFile) throws IOException {
        if (quizFile.getName().toLowerCase().endsWith("json")) {
            try (BufferedReader reader = new BufferedReader(new FileReader(quizFile))) {
                return Main.GSON.fromJson(reader, Quiz.class);
            }
        }
        final List<String> lines = Files.lines(quizFile.toPath()).filter(line -> !line.isBlank()).toList();
        final Iterator<String> iterator = lines.iterator();
        final String student = iterator.next();
        final String topic = iterator.next();
        final QuizQuestions questions = new QuizQuestions(iterator);
        return new Quiz(topic, student, questions);
    }

    public QuizQuestions() {
        super();
    }

    public QuizQuestions(final Collection<? extends QuizQuestion> questions) {
        super(questions);
    }

    public QuizQuestions(final Iterator<String> iterator) {
        super();
        int counter = 0;
        String question = null;
        String correctAnswer = null;
        List<String> wrongAnswers = new ArrayList<String>();
        while (iterator.hasNext()) {
            final String line = iterator.next();
            if (counter == 0) {
                question =
                    Main.escapeForLaTeX(line.strip().replaceAll("^(Frage\\s*)?\\(*\\s*\\d+(\\.|\\)|:|\\s)+", ""));
            } else {
                final String stripped =
                    Main.escapeForLaTeX(line.strip().replaceAll("^(\\d|[abcdABCD])(\\.|\\)|:|\\s)+", ""));
                if (stripped.toLowerCase().endsWith("(richtig)")) {
                    correctAnswer = stripped.substring(0, stripped.length() - 9).strip();
                } else {
                    wrongAnswers.add(stripped);
                }
            }
            counter++;
            if (counter > 4) {
                Collections.shuffle(wrongAnswers);
                this.add(new QuizQuestion(question, correctAnswer, wrongAnswers));
                wrongAnswers = new ArrayList<String>();
                counter = 0;
            }
        }
        Collections.shuffle(this);
    }

}
