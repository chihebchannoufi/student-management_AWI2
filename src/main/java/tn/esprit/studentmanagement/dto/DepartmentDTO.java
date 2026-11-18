package tn.esprit.studentmanagement.dto;

public record DepartmentDTO(
        Long idDepartment,
        String name,
        String location,
        String phone,
        String head) {
}
