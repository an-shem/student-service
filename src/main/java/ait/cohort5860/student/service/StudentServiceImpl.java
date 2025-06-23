package ait.cohort5860.student.service;

import ait.cohort5860.student.dao.StudentRepository;
import ait.cohort5860.student.dto.ScoreDto;
import ait.cohort5860.student.dto.StudentCredentialsDto;
import ait.cohort5860.student.dto.StudentDto;
import ait.cohort5860.student.dto.StudentUpdateDto;
import ait.cohort5860.student.dto.exceptions.NotFoundException;
import ait.cohort5860.student.model.Student;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
    private final StudentRepository studentRepository;

    @Override
    public Boolean addStudent(StudentCredentialsDto studentCredentialsDto) {
        if (studentRepository.findById(studentCredentialsDto.getId()).isPresent()) {
            return false;
        }
        Student student = new Student(studentCredentialsDto.getId(), studentCredentialsDto.getName(), studentCredentialsDto.getPassword());
        studentRepository.save(student);
        return true;
    }

    @Override
    public StudentDto findStudentById(Long id) {
        Student student = studentRepository.findById(id).orElseThrow(NotFoundException::new);
        return new StudentDto(student.getId(), student.getName(), student.getScores());
    }

    @Override
    public StudentDto removeStudentById(Long id) {
        StudentDto studentDto = findStudentById(id);
        studentRepository.deleteById(studentDto.getId());
        return studentDto;
    }

    @Override
    public StudentCredentialsDto updateStudentById(Long id, StudentUpdateDto studentUpdateDto) {
        Student student = studentRepository.findById(id).orElseThrow(NotFoundException::new);
        Student updateStudent = new Student(id,
                studentUpdateDto.getName() == null ?
                        student.getName() : studentUpdateDto.getName(),
                studentUpdateDto.getPassword() == null ?
                        student.getPassword() : studentUpdateDto.getPassword());
        studentRepository.save(updateStudent);
        return new StudentCredentialsDto(id, updateStudent.getName(), updateStudent.getPassword());
    }

    @Override
    public Boolean addScore(Long id, ScoreDto scoreDto) {
        Student student = studentRepository.findById(id).orElseThrow(NotFoundException::new);
        Boolean res = student.addScore(scoreDto.getExamName(), scoreDto.getScore());
        studentRepository.save(student);
        return res;
    }

    @Override
    public List<StudentDto> findStudentsByName(String name) {
        List<Student> students = studentRepository.findAll();
        List<StudentDto> studentDtoList = students.stream()
                .filter(student -> student.getName().toLowerCase().equals(name))
                .map(student -> new StudentDto(student.getId(), student.getName(), student.getScores()))
                .toList();
        return studentDtoList;
    }

    @Override
    public Long countStudentsByName(Set<String> names) {
        List<Student> students = studentRepository.findAll();
        Long quantity = students.stream()
                .filter(student -> names.contains(student.getName()))
                .count();
        return quantity;
    }

    @Override
    public List<StudentDto> findStudentsByExamName(String examName, Integer minScore) {
        List<Student> students = studentRepository.findAll();
        List<StudentDto> studentDtoList = students.stream()
                .filter(student -> student.getScores().containsKey(examName)
                        && student.getScores().get(examName) >= minScore)
                .map(student -> new StudentDto(student.getId(), student.getName(), student.getScores()))
                .toList();
        return studentDtoList;
    }
}
