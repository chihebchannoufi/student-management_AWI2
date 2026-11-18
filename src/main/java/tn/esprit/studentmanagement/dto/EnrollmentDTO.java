package tn.esprit.studentmanagement.dto;

import tn.esprit.studentmanagement.entities.Status;

import java.time.LocalDate;

public record EnrollmentDTO(
        Long idEnrollment,
        LocalDate enrollmentDate,
        Double grade,
        Status status) {
}
