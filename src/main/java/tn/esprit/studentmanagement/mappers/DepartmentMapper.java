package tn.esprit.studentmanagement.mappers;

import tn.esprit.studentmanagement.dto.DepartmentDTO;
import tn.esprit.studentmanagement.entities.Department;

public final class DepartmentMapper {
    private DepartmentMapper() {
    }

    public static DepartmentDTO toDto(Department department) {
        return new DepartmentDTO(
                department.getIdDepartment(),
                department.getName(),
                department.getLocation(),
                department.getPhone(),
                department.getHead());
    }

    public static Department toEntity(DepartmentDTO dto) {
        Department department = new Department();
        department.setIdDepartment(dto.idDepartment());
        department.setName(dto.name());
        department.setLocation(dto.location());
        department.setPhone(dto.phone());
        department.setHead(dto.head());
        return department;
    }
}
