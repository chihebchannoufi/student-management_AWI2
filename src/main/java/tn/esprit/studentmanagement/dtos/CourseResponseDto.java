package tn.esprit.studentmanagement.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.esprit.studentmanagement.entities.Course;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponseDto {
    private Long idCourse;
    private String name;
    private String code;
    private int credit;
    private String description;

    public static CourseResponseDto fromEntity(Course course) {
        return new CourseResponseDto(
                course.getIdCourse(),
                course.getName(),
                course.getCode(),
                course.getCredit(),
                course.getDescription()
        );
    }
}
