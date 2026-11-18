package tn.esprit.studentmanagement.controllers;

import org.springframework.web.bind.annotation.*;
import tn.esprit.studentmanagement.dto.StudentDTO;
import tn.esprit.studentmanagement.mappers.StudentMapper;
import tn.esprit.studentmanagement.services.IStudentService;

import java.util.List;

@RestController
@RequestMapping("/students")
@CrossOrigin(origins = "http://localhost:4200")
public class StudentController {
    private final IStudentService studentService;

    public StudentController(IStudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/getAllStudents")
    public List<StudentDTO> getAllStudents() {
        return studentService.getAllStudents().stream().map(StudentMapper::toDto).toList();
    }

    @GetMapping("/getStudent/{id}")
    public StudentDTO getStudent(@PathVariable Long id) {
        return StudentMapper.toDto(studentService.getStudentById(id));
    }

    @PostMapping("/createStudent")
    public StudentDTO createStudent(@RequestBody StudentDTO dto) {
        return StudentMapper.toDto(studentService.saveStudent(StudentMapper.toEntity(dto)));
    }

    @PutMapping("/updateStudent")
    public StudentDTO updateStudent(@RequestBody StudentDTO dto) {
        return StudentMapper.toDto(studentService.saveStudent(StudentMapper.toEntity(dto)));
    }

    @DeleteMapping("/deleteStudent/{id}")
    public void deleteStudent(@PathVariable Long id) { studentService.deleteStudent(id); }
}
