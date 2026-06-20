package lecturehelper.structures;

import java.util.*;

public record MetaInformation(
    String title,
    String shorttitle,
    ExaminationMode type,
    Integer firstsheetnr,
    List<Sheet> sheets,
    String exercisepath,
    List<HelperFile> exerciseextra,
    List<Exam> exams,
    List<Topic> topics,
    List<Slides> slides
) {

}
