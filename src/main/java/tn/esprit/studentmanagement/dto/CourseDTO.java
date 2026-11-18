package tn.esprit.studentmanagement.dto;

public record CourseDTO(
        Long idCourse,
        String name,
        String code,
        int credit,
        String description) {
}
