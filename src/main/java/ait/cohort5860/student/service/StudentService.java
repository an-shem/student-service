package ait.cohort5860.student.service;

import ait.cohort5860.student.dto.ScoreDto;
import ait.cohort5860.student.dto.StudentCredentialsDto;
import ait.cohort5860.student.dto.StudentDto;
import ait.cohort5860.student.dto.StudentUpdateDto;

import java.util.List;
import java.util.Set;

public interface StudentService {
    Boolean addStudent(StudentCredentialsDto studentCredentialsDto);

    StudentDto findStudentById(Long id);

    StudentDto removeStudentById(Long id);

    StudentCredentialsDto updateStudentById(Long id, StudentUpdateDto studentUpdateDto);

    Boolean addScore(Long id, ScoreDto scoreDto);

    List<StudentDto> findStudentsByName(String name);

    Long countStudentsByName(Set<String> names);

    List<StudentDto> findStudentsByExamName(String examName, Integer minScore);


}
