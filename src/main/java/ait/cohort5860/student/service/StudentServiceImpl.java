package ait.cohort5860.student.service;

import ait.cohort5860.student.dao.StudentRepository;
import ait.cohort5860.student.dto.ScoreDto;
import ait.cohort5860.student.dto.StudentCredentialsDto;
import ait.cohort5860.student.dto.StudentDto;
import ait.cohort5860.student.dto.StudentUpdateDto;
import ait.cohort5860.student.dto.exceptions.NotFoundException;
import ait.cohort5860.student.model.Student;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

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

// ----------------------- Вариант учителя ( подходит для МАР но не для базы данных, т. к. не сохраняет)---------------
//    @Override
//    public StudentCredentialsDto updateStudentById(Long id, StudentUpdateDto studentUpdateDto) {
//        Student student = studentRepository.findById(id).orElseThrow(NotFoundException::new);
//        if (studentUpdateDto.getName() != null) {
//            student.setName(studentUpdateDto.getName());
//        }
//        if (studentUpdateDto.getPassword() != null) {
//            student.setPassword(studentUpdateDto.getPassword());
//        }
//        return new StudentCredentialsDto(student.getId(), student.getName(), student.getPassword());
//    }

    @Override
    public Boolean addScore(Long id, ScoreDto scoreDto) {
        Student student = studentRepository.findById(id).orElseThrow(NotFoundException::new);
        return student.addScore(scoreDto.getExamName(), scoreDto.getScore());
    }

    @Override
    public List<StudentDto> findStudentsByName(String name) {
       return studentRepository.findAll().stream()
                .filter(student -> name.equalsIgnoreCase(student.getName()))
                .map(student -> new StudentDto(student.getId(), student.getName(), student.getScores()))
                .toList();
           }

    @Override
    public Long countStudentsByName(Set<String> names) {
        return studentRepository.findAll().stream()
                .filter(student -> names.contains(student.getName()))
                .count();

    }

    @Override
    public List<StudentDto> findStudentsByExamName(String examName, Integer minScore) {
        return studentRepository.findAll().stream()
                .filter(s -> s.getScores().containsKey(examName)
                        && s.getScores().get(examName) > minScore)
                .map(s -> new StudentDto(s.getId(), s.getName(), s.getScores()))
                .toList();

    }
}
