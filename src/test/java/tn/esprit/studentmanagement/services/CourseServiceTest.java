package tn.esprit.studentmanagement.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.studentmanagement.entities.Course;
import tn.esprit.studentmanagement.exceptions.CourseNotFoundException;
import tn.esprit.studentmanagement.repositories.CourseRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {
    @Mock
    private CourseRepository courseRepository;

    private CourseService courseService;

    @BeforeEach
    void setUp() {
        courseService = new CourseService(courseRepository);
    }

    @Test
    void getAllCoursesReturnsRepositoryValues() {
        Course course = new Course();
        course.setName("Java");
        when(courseRepository.findAll()).thenReturn(List.of(course));

        assertEquals(List.of(course), courseService.getAllCourses());
        verify(courseRepository).findAll();
    }

    @Test
    void getCourseByIdReturnsFoundCourse() {
        Course course = new Course();
        course.setIdCourse(7L);
        when(courseRepository.findById(7L)).thenReturn(Optional.of(course));

        assertEquals(course, courseService.getCourseById(7L));
    }

    @Test
    void getCourseByIdThrowsWhenMissing() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CourseNotFoundException.class,
                () -> courseService.getCourseById(99L));
    }

    @Test
    void saveCoursePersistsCourse() {
        Course course = new Course();
        course.setName("Spring");
        when(courseRepository.save(course)).thenReturn(course);

        assertEquals(course, courseService.saveCourse(course));
        verify(courseRepository).save(course);
    }

    @Test
    void saveCourseRejectsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> courseService.saveCourse(null));
        verify(courseRepository, never()).save(any());
    }

    @Test
    void deleteCourseDeletesExistingCourse() {
        when(courseRepository.existsById(3L)).thenReturn(true);

        courseService.deleteCourse(3L);

        verify(courseRepository).deleteById(3L);
    }

    @Test
    void deleteCourseThrowsWhenMissing() {
        when(courseRepository.existsById(3L)).thenReturn(false);

        assertThrows(CourseNotFoundException.class,
                () -> courseService.deleteCourse(3L));
        verify(courseRepository, never()).deleteById(any());
    }
}
