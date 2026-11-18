package tn.esprit.studentmanagement.mappers;

import tn.esprit.studentmanagement.dto.CourseDTO;
import tn.esprit.studentmanagement.entities.Course;

public final class CourseMapper {
    private CourseMapper() {
    }

    public static CourseDTO toDto(Course course) {
        return new CourseDTO(
                course.getIdCourse(),
                course.getName(),
                course.getCode(),
                course.getCredit(),
                course.getDescription());
    }

    public static Course toEntity(CourseDTO dto) {
        Course course = new Course();
        course.setIdCourse(dto.idCourse());
        course.setName(dto.name());
        course.setCode(dto.code());
        course.setCredit(dto.credit());
        course.setDescription(dto.description());
        return course;
    }
}
