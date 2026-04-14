package lectureinitializer;

import java.util.stream.*;

public record ReviewerAssignment(
    String participant,
    String reviewer1,
    String reviewer2,
    String reviewer3,
    String pcMember
) {

    @Override
    public String toString() {
        return Stream.of(this.participant(), this.reviewer1(), this.reviewer2(), this.reviewer3(), this.pcMember())
            .collect(Collectors.joining(";"));
    }

}
