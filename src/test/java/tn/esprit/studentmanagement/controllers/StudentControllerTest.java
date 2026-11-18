package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.services.IStudentService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
class StudentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IStudentService studentService;

    @Test
    void getAllStudentsReturnsJsonList() throws Exception {
        Student student = student("Alice");
        student.setIdStudent(1L);
        when(studentService.getAllStudents()).thenReturn(List.of(student));

        mockMvc.perform(get("/students/getAllStudents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idStudent").value(1))
                .andExpect(jsonPath("$[0].firstName").value("Alice"));
    }

    @Test
    void getStudentByIdReturnsJsonStudent() throws Exception {
        Student student = student("Bob");
        student.setIdStudent(2L);
        when(studentService.getStudentById(2L)).thenReturn(student);

        mockMvc.perform(get("/students/getStudent/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idStudent").value(2))
                .andExpect(jsonPath("$.lastName").value("Martin"));
    }

    @Test
    void createStudentReturnsJsonStudent() throws Exception {
        Student saved = student("Carol");
        saved.setIdStudent(3L);
        when(studentService.saveStudent(any(Student.class))).thenReturn(saved);

        mockMvc.perform(post("/students/createStudent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Carol","lastName":"Martin","email":"carol@example.com","phone":"123","dateOfBirth":"2004-03-10","address":"Tunis"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idStudent").value(3))
                .andExpect(jsonPath("$.email").value("carol@example.com"));
    }

    @Test
    void updateStudentReturnsUpdatedJsonStudent() throws Exception {
        Student saved = student("Dana");
        saved.setIdStudent(4L);
        when(studentService.saveStudent(any(Student.class))).thenReturn(saved);

        mockMvc.perform(put("/students/updateStudent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idStudent":4,"firstName":"Dana","lastName":"Martin","email":"dana@example.com","phone":"456","dateOfBirth":"2003-04-11","address":"Sfax"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idStudent").value(4))
                .andExpect(jsonPath("$.firstName").value("Dana"));
    }

    @Test
    void deleteStudentReturnsOkAndCallsService() throws Exception {
        mockMvc.perform(delete("/students/deleteStudent/5"))
                .andExpect(status().isOk());

        verify(studentService).deleteStudent(5L);
    }

    private Student student(String firstName) {
        Student student = new Student();
        student.setFirstName(firstName);
        student.setLastName("Martin");
        student.setEmail(firstName.toLowerCase() + "@example.com");
        student.setPhone("111");
        student.setDateOfBirth(LocalDate.of(2004, 3, 10));
        student.setAddress("Tunis");
        return student;
    }
}
