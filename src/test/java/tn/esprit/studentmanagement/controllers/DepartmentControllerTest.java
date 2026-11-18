package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.services.IDepartmentService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentController.class)
class DepartmentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IDepartmentService departmentService;

    @Test
    void getAllDepartmentsReturnsJsonList() throws Exception {
        Department department = department("Computer Science");
        department.setIdDepartment(1L);
        when(departmentService.getAllDepartments()).thenReturn(List.of(department));

        mockMvc.perform(get("/Depatment/getAllDepartment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idDepartment").value(1))
                .andExpect(jsonPath("$[0].name").value("Computer Science"));
    }

    @Test
    void getDepartmentByIdReturnsJsonDepartment() throws Exception {
        Department department = department("Mathematics");
        department.setIdDepartment(2L);
        when(departmentService.getDepartmentById(2L)).thenReturn(department);

        mockMvc.perform(get("/Depatment/getDepartment/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDepartment").value(2))
                .andExpect(jsonPath("$.name").value("Mathematics"));
    }

    @Test
    void createDepartmentReturnsJsonDepartment() throws Exception {
        Department saved = department("Physics");
        saved.setIdDepartment(3L);
        saved.setLocation("B2");
        when(departmentService.saveDepartment(any(Department.class))).thenReturn(saved);

        mockMvc.perform(post("/Depatment/createDepartment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Physics","location":"B2","phone":"123","head":"Dr Smith"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDepartment").value(3))
                .andExpect(jsonPath("$.location").value("B2"));
    }

    @Test
    void updateDepartmentReturnsUpdatedJsonDepartment() throws Exception {
        Department saved = department("Updated Department");
        saved.setIdDepartment(4L);
        when(departmentService.saveDepartment(any(Department.class))).thenReturn(saved);

        mockMvc.perform(put("/Depatment/updateDepartment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idDepartment":4,"name":"Updated Department","location":"C1","phone":"456","head":"Dr Jones"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDepartment").value(4))
                .andExpect(jsonPath("$.name").value("Updated Department"));
    }

    @Test
    void deleteDepartmentReturnsOkAndCallsService() throws Exception {
        mockMvc.perform(delete("/Depatment/deleteDepartment/5"))
                .andExpect(status().isOk());

        verify(departmentService).deleteDepartment(5L);
    }

    private Department department(String name) {
        Department department = new Department();
        department.setName(name);
        department.setLocation("A1");
        department.setPhone("111");
        department.setHead("Head");
        return department;
    }
}
