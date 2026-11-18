package tn.esprit.studentmanagement.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tn.esprit.studentmanagement.dtos.CourseCreateDto;
import tn.esprit.studentmanagement.dtos.CourseUpdateDto;
import tn.esprit.studentmanagement.entities.Course;
import tn.esprit.studentmanagement.services.ICourseService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {
    @Mock
    private ICourseService courseService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CourseController(courseService)).build();
    }

    @Test
    void getAllCoursesReturnsList() throws Exception {
        Course course = new Course();
        course.setName("Java");
        when(courseService.getAllCourses()).thenReturn(List.of(course));

        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Java"));
    }

    @Test
    void getCourseByIdReturnsCourse() throws Exception {
        Course course = new Course();
        course.setIdCourse(5L);
        course.setName("Databases");
        when(courseService.getCourseById(5L)).thenReturn(course);

        mockMvc.perform(get("/courses/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Databases"));
    }

    @Test
    void createCourseReturnsCreatedCourse() throws Exception {
        CourseCreateDto dto = new CourseCreateDto();
        dto.setName("Algorithms");
        Course saved = new Course();
        saved.setIdCourse(8L);
        saved.setName("Algorithms");
        when(courseService.saveCourse(any(Course.class))).thenReturn(saved);

        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCourse").value(8))
                .andExpect(jsonPath("$.name").value("Algorithms"));
    }

    @Test
    void updateCourseSetsPathIdAndSaves() throws Exception {
        CourseUpdateDto dto = new CourseUpdateDto();
        dto.setName("Updated");
        Course saved = new Course();
        saved.setIdCourse(10L);
        saved.setName("Updated");
        when(courseService.saveCourse(argThat(value -> value.getIdCourse() == 10L)))
                .thenReturn(saved);

        mockMvc.perform(put("/courses/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void deleteCourseReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/courses/12"))
                .andExpect(status().isNoContent());

        verify(courseService).deleteCourse(12L);
    }
}
