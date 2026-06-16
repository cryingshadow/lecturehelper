package lecturehelper;

import clit.*;

public enum Flag implements Parameter {

    ASSIGNMENT("a", "assignment", "File containing the assignment of topics to participants."),

    ATTENDANCE("t", "attendance", "File containing the attendance list for the lecture."),

    CLASSFILE("c", "classfile", "File containing the participants and dates of the lecture."),

    EXCLUDE("x", "exclude", "File containing the participants who should be excluded."),

    EXPORT("e", "export", "CSV-export of the outlook calendar or teams participants."),

    MODE("m", "mode", "Execution mode (ATTENDANCE, CLASS, LIST, QUIZ, REVIEWER, TALK)."),

    OUTPUT("o", "output", "File for output."),

    PARTICIPANTS("p", "participants", "File containing the participants of lectures."),

    QUIZ("q", "quiz", "File containing quiz questions.");

    private final String description;

    private final String longName;

    private final String shortName;

    private Flag(final String shortName, final String longName, final String description) {
        this.shortName = shortName;
        this.longName = longName;
        this.description = description;
    }

    @Override
    public String description() {
        return this.description;
    }

    @Override
    public String longName() {
        return this.longName;
    }

    @Override
    public String shortName() {
        return this.shortName;
    }

}
