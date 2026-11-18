package tn.esprit.studentmanagement.mappers;

import tn.esprit.studentmanagement.dto.StudentDTO;
import tn.esprit.studentmanagement.entities.Student;

public final class StudentMapper {
    private StudentMapper() {
    }

    public static StudentDTO toDto(Student student) {
        return new StudentDTO(
                student.getIdStudent(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getPhone(),
                student.getDateOfBirth(),
                student.getAddress());
    }

    public static Student toEntity(StudentDTO dto) {
        Student student = new Student();
        student.setIdStudent(dto.idStudent());
        student.setFirstName(dto.firstName());
        student.setLastName(dto.lastName());
        student.setEmail(dto.email());
        student.setPhone(dto.phone());
        student.setDateOfBirth(dto.dateOfBirth());
        student.setAddress(dto.address());
        return student;
    }
}
