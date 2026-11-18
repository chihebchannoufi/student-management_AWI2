package tn.esprit.studentmanagement.dto;

import java.time.LocalDate;

public record StudentDTO(
        Long idStudent,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate dateOfBirth,
        String address) {
}
