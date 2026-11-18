package tn.esprit.studentmanagement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tn.esprit.studentmanagement.entities.Course;
import tn.esprit.studentmanagement.exceptions.CourseNotFoundException;
import tn.esprit.studentmanagement.repositories.CourseRepository;
import tn.esprit.studentmanagement.services.CourseService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CourseIntegrationTest {
    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepository courseRepository;

    @BeforeEach
    void cleanDatabase() {
        courseRepository.deleteAll();
    }

    @Test
    void createAndRetrieveCourseUsingRealComponents() {
        Course course = new Course();
        course.setName("Software Engineering");
        course.setCode("SE101");
        course.setCredit(4);
        course.setDescription("Real integration test");

        Course saved = courseService.saveCourse(course);

        assertNotNull(saved.getIdCourse());
        Course retrieved = courseService.getCourseById(saved.getIdCourse());
        assertEquals(saved.getIdCourse(), retrieved.getIdCourse());
        assertEquals("Software Engineering", retrieved.getName());
        assertEquals("SE101", retrieved.getCode());
        assertEquals(4, retrieved.getCredit());
    }

    @Test
    void findAllAndDeleteCourseUsingRealComponents() {
        Course first = courseService.saveCourse(course("Java", "JAVA101"));
        Course second = courseService.saveCourse(course("Spring", "SPRING101"));

        List<Course> courses = courseService.getAllCourses();

        assertEquals(2, courses.size());
        courseService.deleteCourse(first.getIdCourse());
        assertFalse(courseRepository.existsById(first.getIdCourse()));
        assertTrue(courseRepository.existsById(second.getIdCourse()));
    }

    @Test
    void realComponentsThrowForMissingCourse() {
        assertThrows(CourseNotFoundException.class,
                () -> courseService.getCourseById(-1L));
        assertThrows(CourseNotFoundException.class,
                () -> courseService.deleteCourse(-1L));
    }

    private Course course(String name, String code) {
        Course course = new Course();
        course.setName(name);
        course.setCode(code);
        course.setCredit(3);
        return course;
    }
}
