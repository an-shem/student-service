package ait.cohort5860.student.service;

import ait.cohort5860.configuration.ServiceConfiguration;
import ait.cohort5860.student.dao.StudentRepository;
import ait.cohort5860.student.dto.ScoreDto;
import ait.cohort5860.student.dto.StudentCredentialsDto;
import ait.cohort5860.student.dto.StudentDto;
import ait.cohort5860.student.dto.StudentUpdateDto;
import ait.cohort5860.student.dto.exceptions.NotFoundException;
import ait.cohort5860.student.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


//AAA - Arrange, Act, Assert (подготовка, действие, утверждение/проверка)

@ContextConfiguration(classes = {ServiceConfiguration.class})
@SpringBootTest
public class StudentServiceTest {
    private final long studentId = 1000L;
    private final String name = "John";
    private final String password = "1234";
    private Student student;

    @Autowired
    private ModelMapper modelMapper;

    @MockitoBean
    private StudentRepository studentRepository;

    private StudentService studentService;

    @BeforeEach
    public void setUp() {
        student = new Student(studentId, name, password);
        studentService = new StudentServiceImpl(studentRepository, modelMapper);
    }

    @Test
    void testAddStudentWhenStudentDoesNotExist() {
        //Arrange
        StudentCredentialsDto dto = new StudentCredentialsDto(studentId, name, password);
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        //Act
        boolean result = studentService.addStudent(dto);

        //Assert
        assertTrue(result);
    }

    @Test
    void testAddStudentWhenStudentExist() {
        //Arrange
        StudentCredentialsDto dto = new StudentCredentialsDto(studentId, name, password);
        when(studentRepository.existsById(dto.getId())).thenReturn(true);

        //Act
        boolean result = studentService.addStudent(dto);

        //Assert
        assertFalse(result);
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void testFindStudentWhenStudentExist() {
        //Arrange
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

        //Act
        StudentDto studentDto = studentService.findStudent(studentId);

        //Assert
        assertNotNull(studentDto);
        assertEquals(studentId, studentDto.getId());
    }

    @Test
    void testFindStudentWhenStudentNotExist() {
        //Arrange
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        //Act & Assert
        assertThrows(NotFoundException.class, () -> studentService.findStudent(studentId));
    }

    @Test
    void testRemoveStudent() {
        //Arrange
        when(studentRepository.findById(studentId)).thenReturn(Optional.ofNullable(student));

        //Act
        StudentDto studentDto = studentService.removeStudent(studentId);

        //Assert
        assertNotNull(studentDto);
        assertEquals(studentId, studentDto.getId());
        verify(studentRepository, times(1)).deleteById(studentId);
    }

    @Test
    void testUpdateStudent() {
        //Arrage
        String newName = "newName";
        when(studentRepository.findById(studentId)).thenReturn(Optional.ofNullable(student));
        StudentUpdateDto dto = new StudentUpdateDto(newName, null);

        //Act
        StudentCredentialsDto studentCredentialsDto = studentService.updateStudent(studentId, dto);

        //Assert
        assertNotNull(studentCredentialsDto);
        assertEquals(studentId, studentCredentialsDto.getId());
        assertEquals(newName, studentCredentialsDto.getName());
        assertEquals(password, studentCredentialsDto.getPassword());
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void testAddScoreSuccessfully() {
        // Arrange
        ScoreDto scoreDto = new ScoreDto("Math", 95);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student)); // без .ofNullable

        // Act
        Boolean result = studentService.addScore(studentId, scoreDto);

        // Assert
        assertNotNull(result);
        assertTrue(result);
        assertEquals(95, student.getScores().get("Math")); // проверка, что оценка добавилась
        verify(studentRepository, times(1)).save(student);
    }

    @Test
    void testAddScoreWhenScoreAlreadyExists() {
        // Arrange
        ScoreDto scoreDto = new ScoreDto("Math", 100);
        student.getScores().put("Math", 95); // ← оценка уже есть
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

        // Act
        Boolean result = studentService.addScore(studentId, scoreDto);

        // Assert
        assertNotNull(result);
        assertFalse(result); // потому что оценка уже существовала
        assertEquals(100, student.getScores().get("Math")); // значение обновилось
        verify(studentRepository, times(1)).save(student);
    }

    @Test
    void testFindStudentsByName() {
        // Arrange
        String name = "John";
        Student student1 = new Student(1L, name, "pass1");
        Student student2 = new Student(2L, name, "pass2");

        when(studentRepository.findByNameIgnoreCase(name)).thenReturn(Stream.of(student1, student2));

        // Act
        List<StudentDto> result = studentService.findStudentsByName(name);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(name, result.get(0).getName());
        assertEquals(name, result.get(1).getName());

        verify(studentRepository, times(1)).findByNameIgnoreCase(name);
    }

    @Test
    void testFindStudentsByNameReturnsEmpty() {
        // Arrange
        String name = "NonExistingName";

        // Репозиторий возвращает пустой Stream
        when(studentRepository.findByNameIgnoreCase(name)).thenReturn(Stream.empty());

        // Act
        List<StudentDto> result = studentService.findStudentsByName(name);

        // Assert
        assertNotNull(result);           // Список не должен быть null
        assertTrue(result.isEmpty());    // Список должен быть пустым

        verify(studentRepository, times(1)).findByNameIgnoreCase(name);
    }

    @Test
    void testCountStudentsByNames() {
        // Arrange
        Set<String> names = Set.of("John", "Alice");
        long expectedCount = 3L;

        // Мокаем вызов репозитория, чтобы вернуть expectedCount
        when(studentRepository.countByNameIgnoreCaseIn(names)).thenReturn(expectedCount);

        // Act
        Long actualCount = studentService.countStudentsByNames(names);

        // Assert
        assertNotNull(actualCount);
        assertEquals(expectedCount, actualCount);

        verify(studentRepository, times(1)).countByNameIgnoreCaseIn(names);
    }

    @Test
    void testFindStudentsByExamNameMinScore() {
        // Arrange
        String examName = "Math";
        Integer minScore = 80;

        List<Student> students = List.of(
                new Student(1L, "John", "pass"),
                new Student(2L, "Alice", "pass")
        );

        when(studentRepository.findByExamAndScoreGreaterThan(examName, minScore))
                .thenReturn(students.stream());

        // Act
        List<StudentDto> result = studentService.findStudentsByExamNameMinScore(examName, minScore);

        // Assert
        assertNotNull(result);
        assertEquals(students.size(), result.size());

        for (int i = 0; i < students.size(); i++) {
            assertEquals(students.get(i).getId(), result.get(i).getId());
            assertEquals(students.get(i).getName(), result.get(i).getName());
        }

        verify(studentRepository, times(1)).findByExamAndScoreGreaterThan(examName, minScore);
    }

    @Test
    void testFindStudentsByExamNameMinScoreEmpty() {
        // Arrange
        String examName = "Physics";
        Integer minScore = 90;

        when(studentRepository.findByExamAndScoreGreaterThan(examName, minScore))
                .thenReturn(Stream.empty());

        // Act
        List<StudentDto> result = studentService.findStudentsByExamNameMinScore(examName, minScore);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(studentRepository, times(1)).findByExamAndScoreGreaterThan(examName, minScore);
    }

}

