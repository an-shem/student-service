package ait.cohort5860.student.dao;

import ait.cohort5860.student.model.Student;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Set;
import java.util.stream.Stream;

public interface StudentRepository extends MongoRepository<Student, Long> {
    Stream<Student> findByNameIgnoreCase(String name);
    Long countStudentsByNameIgnoreCaseIn(Set<String> names);

    @Query("{'scores.Math': {'$gt':  90}}")
    Stream<Student> findByExamAndScoresGreaterThan(String examName, Integer score);

}
