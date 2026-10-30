package tn.esprit.studentmanagement.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.dto.StudentDTO;
import tn.esprit.studentmanagement.services.IDepartmentService;
import tn.esprit.studentmanagement.services.IStudentService;

import java.util.List;

@RestController
@RequestMapping("/students")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class StudentController {
    private final IStudentService studentService;
    private final IDepartmentService departmentService;

    @GetMapping("/getAllStudents")
    public List<Student> getAllStudents() { return studentService.getAllStudents(); }

    @GetMapping("/getStudent/{id}")
    public Student getStudent(@PathVariable Long id) { return studentService.getStudentById(id); }

    @PostMapping("/createStudent")
    public Student createStudent(@RequestBody StudentDTO studentDTO) {
        return studentService.saveStudent(toEntity(studentDTO));
    }

    @PutMapping("/updateStudent/{id}")
    public Student updateStudent(@PathVariable Long id, @RequestBody StudentDTO studentDTO) {
        Student student = studentService.getStudentById(id);
        copyFields(studentDTO, student);
        return studentService.saveStudent(student);
    }

    @DeleteMapping("/deleteStudent/{id}")
    public void deleteStudent(@PathVariable Long id) { studentService.deleteStudent(id); }

    private Student toEntity(StudentDTO studentDTO) {
        Student student = new Student();
        copyFields(studentDTO, student);
        return student;
    }

    private void copyFields(StudentDTO studentDTO, Student student) {
        student.setFirstName(studentDTO.getFirstName());
        student.setLastName(studentDTO.getLastName());
        student.setEmail(studentDTO.getEmail());
        student.setPhone(studentDTO.getPhone());
        student.setDateOfBirth(studentDTO.getDateOfBirth());
        student.setAddress(studentDTO.getAddress());
        if (studentDTO.getDepartmentId() != null) {
            Department department = departmentService.getDepartmentById(studentDTO.getDepartmentId());
            student.setDepartment(department);
        } else {
            student.setDepartment(null);
        }
    }
}
