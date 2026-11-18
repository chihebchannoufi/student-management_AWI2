package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.studentmanagement.entities.Course;
import tn.esprit.studentmanagement.services.ICourseService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
class CourseControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ICourseService courseService;

    @Test
    void getAllCoursesReturnsJsonList() throws Exception {
        Course course = course("Java", "JAVA101", 5);
        course.setIdCourse(1L);
        when(courseService.getAllCourses()).thenReturn(List.of(course));

        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idCourse").value(1))
                .andExpect(jsonPath("$[0].name").value("Java"));
    }

    @Test
    void getCourseByIdReturnsJsonCourse() throws Exception {
        Course course = course("Databases", "DB101", 4);
        course.setIdCourse(5L);
        when(courseService.getCourseById(5L)).thenReturn(course);

        mockMvc.perform(get("/courses/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCourse").value(5))
                .andExpect(jsonPath("$.code").value("DB101"));
    }

    @Test
    void createCourseReturnsCreatedJsonCourse() throws Exception {
        Course saved = course("Algorithms", "ALG101", 3);
        saved.setIdCourse(8L);
        when(courseService.saveCourse(any(Course.class))).thenReturn(saved);

        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Algorithms","code":"ALG101","credit":3,"description":"Algorithms"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCourse").value(8))
                .andExpect(jsonPath("$.name").value("Algorithms"));
    }

    @Test
    void updateCourseUsesPathIdAndReturnsJsonCourse() throws Exception {
        Course saved = course("Updated", "UPD101", 2);
        saved.setIdCourse(10L);
        when(courseService.saveCourse(argThat(value -> value.getIdCourse() == 10L))).thenReturn(saved);

        mockMvc.perform(put("/courses/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Updated","code":"UPD101","credit":2,"description":"Updated"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCourse").value(10))
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void deleteCourseReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/courses/12"))
                .andExpect(status().isNoContent());

        verify(courseService).deleteCourse(12L);
    }

    private Course course(String name, String code, int credit) {
        Course course = new Course();
        course.setName(name);
        course.setCode(code);
        course.setCredit(credit);
        course.setDescription(name);
        return course;
    }
}
