package tn.esprit.studentmanagement.services;

import tn.esprit.studentmanagement.entities.Course;

import java.util.List;

public interface ICourseService {
    List<Course> getAllCourses();
    Course getCourseById(Long id);
    Course saveCourse(Course course);
    void deleteCourse(Long id);
}
