package tn.esprit.studentmanagement.services;

import org.springframework.stereotype.Service;
import tn.esprit.studentmanagement.entities.Course;
import tn.esprit.studentmanagement.exceptions.CourseNotFoundException;
import tn.esprit.studentmanagement.repositories.CourseRepository;

import java.util.List;

@Service
public class CourseService implements ICourseService {
    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));
    }

    @Override
    public Course saveCourse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Course must not be null");
        }
        return courseRepository.save(course);
    }

    @Override
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new CourseNotFoundException(id);
        }
        courseRepository.deleteById(id);
    }
}
