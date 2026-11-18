package tn.esprit.studentmanagement.mappers;

import tn.esprit.studentmanagement.dto.EnrollmentDTO;
import tn.esprit.studentmanagement.entities.Enrollment;

public final class EnrollmentMapper {
    private EnrollmentMapper() {
    }

    public static EnrollmentDTO toDto(Enrollment enrollment) {
        return new EnrollmentDTO(
                enrollment.getIdEnrollment(),
                enrollment.getEnrollmentDate(),
                enrollment.getGrade(),
                enrollment.getStatus());
    }

    public static Enrollment toEntity(EnrollmentDTO dto) {
        Enrollment enrollment = new Enrollment();
        enrollment.setIdEnrollment(dto.idEnrollment());
        enrollment.setEnrollmentDate(dto.enrollmentDate());
        enrollment.setGrade(dto.grade());
        enrollment.setStatus(dto.status());
        return enrollment;
    }
}
