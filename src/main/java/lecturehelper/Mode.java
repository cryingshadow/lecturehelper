package lecturehelper;

import java.util.*;

public enum Mode {

    ARCHIVE("Zip all examination files inot one zip archive.", Set.of(Flag.CLASSFILE, Flag.OUTPUT)),

    ATTENDANCE("Update attendance list based on Teams export.", Set.of(Flag.ATTENDANCE, Flag.EXPORT)),

    CLASS(
        "Create meta class files based on participants lists and calendar export.",
        Set.of(Flag.PARTICIPANTS, Flag.EXPORT)
    ),

    LIST("Write participants lists based on meta class file.", Set.of(Flag.CLASSFILE)),

    QUIZ("Transform quiz questions to random order.", Set.of(Flag.QUIZ, Flag.OUTPUT)),

    REVIEWER_ASSIGN("Compute reviewer distribution.", Set.of(Flag.PARTICIPANTS)),

    REVIEWER_SHOW("Show reviewer distribution by reviewer.", Set.of(Flag.ASSIGNMENT)),

    STATISTICS("Compile statistics file from protocols.", Set.of(Flag.CLASSFILE)),

    TALK("Prepare talk protocols.", Set.of(Flag.CLASSFILE, Flag.ASSIGNMENT));

    public final String description;

    public final Set<Flag> parameters;

    private Mode(final String description, final Set<Flag> parameters) {
        this.description = description;
        this.parameters = parameters;
    }

}
