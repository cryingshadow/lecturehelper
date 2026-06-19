package lecturehelper.structures;

import java.util.*;

public record MetaInformation(
    String title,
    String shorttitle,
    ExaminationMode type,
    List<String> sheets,
    List<String> exams,
    List<Topic> topics
) {

}
