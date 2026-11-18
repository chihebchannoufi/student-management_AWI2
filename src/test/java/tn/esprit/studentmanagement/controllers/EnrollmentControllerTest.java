package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.entities.Status;
import tn.esprit.studentmanagement.services.IEnrollment;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnrollmentController.class)
class EnrollmentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IEnrollment enrollmentService;

    @Test
    void getAllEnrollmentsReturnsJsonList() throws Exception {
        Enrollment enrollment = enrollment();
        enrollment.setIdEnrollment(1L);
        when(enrollmentService.getAllEnrollments()).thenReturn(List.of(enrollment));

        mockMvc.perform(get("/Enrollment/getAllEnrollment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idEnrollment").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void getEnrollmentByIdReturnsJsonEnrollment() throws Exception {
        Enrollment enrollment = enrollment();
        enrollment.setIdEnrollment(2L);
        when(enrollmentService.getEnrollmentById(2L)).thenReturn(enrollment);

        mockMvc.perform(get("/Enrollment/getEnrollment/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEnrollment").value(2))
                .andExpect(jsonPath("$.grade").value(15.5));
    }

    @Test
    void createEnrollmentReturnsJsonEnrollment() throws Exception {
        Enrollment saved = enrollment();
        saved.setIdEnrollment(3L);
        when(enrollmentService.saveEnrollment(any(Enrollment.class))).thenReturn(saved);

        mockMvc.perform(post("/Enrollment/createEnrollment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"enrollmentDate":"2026-09-15","grade":15.5,"status":"ACTIVE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEnrollment").value(3))
                .andExpect(jsonPath("$.grade").value(15.5));
    }

    @Test
    void updateEnrollmentReturnsUpdatedJsonEnrollment() throws Exception {
        Enrollment saved = enrollment();
        saved.setIdEnrollment(4L);
        saved.setStatus(Status.COMPLETED);
        saved.setGrade(18.0);
        when(enrollmentService.saveEnrollment(any(Enrollment.class))).thenReturn(saved);

        mockMvc.perform(put("/Enrollment/updateEnrollment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idEnrollment":4,"enrollmentDate":"2026-09-15","grade":18.0,"status":"COMPLETED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEnrollment").value(4))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void deleteEnrollmentReturnsOkAndCallsService() throws Exception {
        mockMvc.perform(delete("/Enrollment/deleteEnrollment/5"))
                .andExpect(status().isOk());

        verify(enrollmentService).deleteEnrollment(5L);
    }

    private Enrollment enrollment() {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentDate(LocalDate.of(2026, 9, 15));
        enrollment.setGrade(15.5);
        enrollment.setStatus(Status.ACTIVE);
        return enrollment;
    }
}
