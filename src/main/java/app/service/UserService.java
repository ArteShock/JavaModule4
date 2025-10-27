package app.service;

import app.entity.User;
import app.repository.UserRepository;
import app.dto.UserRequestDTO;
import app.dto.UserResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // === CREATE OPERATIONS ===

    // Создать пользователя
    public UserResponseDTO createUser(UserRequestDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email already exists: " + userDTO.getEmail());
        }

        User user = convertToEntity(userDTO);
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    // Создать нескольких пользователей
    public List<UserResponseDTO> createUsers(List<UserRequestDTO> userDTOs) {
        // Проверка уникальности email
        for (UserRequestDTO userDTO : userDTOs) {
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw new RuntimeException("Email already exists: " + userDTO.getEmail());
            }
        }

        List<User> users = userDTOs.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());

        List<User> savedUsers = userRepository.saveAll(users);
        return savedUsers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // === READ OPERATIONS ===

    // Получить всех пользователей
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Получить пользователя по ID
    public UserResponseDTO getUserById(int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return convertToDTO(user);
    }

    // Получить пользователя по email
    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return convertToDTO(user);
    }

    // Поиск пользователей по имени
    public List<UserResponseDTO> getUsersByName(String name) {
        return userRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Получить пользователей по возрастному диапазону
    public List<UserResponseDTO> getUsersByAgeRange(int minAge, int maxAge) {
        return userRepository.findByAgeBetween(minAge, maxAge)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Проверить существование пользователя по ID
    public boolean userExists(int id) {
        return userRepository.existsById(id);
    }

    // Проверить существование пользователя по email
    public boolean userExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Получить общее количество пользователей
    public long getUsersCount() {
        return userRepository.count();
    }

    // === UPDATE OPERATIONS ===

    // Полное обновление пользователя
    public UserResponseDTO updateUser(int id, UserRequestDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Проверяем email на уникальность (исключая текущего пользователя)
        if (userRepository.existsByEmail(userDTO.getEmail()) &&
                !existingUser.getEmail().equals(userDTO.getEmail())) {
            throw new RuntimeException("Email already exists: " + userDTO.getEmail());
        }

        existingUser.setName(userDTO.getName());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setAge(userDTO.getAge());

        // Обновляем дату создания только если она указана
        if (userDTO.getCreatedAt() != null) {
            existingUser.setCreatedAt(userDTO.getCreatedAt());
        }

        User updatedUser = userRepository.save(existingUser);
        return convertToDTO(updatedUser);
    }

    // Частичное обновление - только имя
    public UserResponseDTO updateUserName(int id, String name) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        existingUser.setName(name);
        User updatedUser = userRepository.save(existingUser);
        return convertToDTO(updatedUser);
    }

    // Частичное обновление - только email
    public UserResponseDTO updateUserEmail(int id, String email) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Проверяем email на уникальность
        if (userRepository.existsByEmail(email) && !existingUser.getEmail().equals(email)) {
            throw new RuntimeException("Email already exists: " + email);
        }

        existingUser.setEmail(email);
        User updatedUser = userRepository.save(existingUser);
        return convertToDTO(updatedUser);
    }

    // Частичное обновление - только возраст
    public UserResponseDTO updateUserAge(int id, int age) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        existingUser.setAge(age);
        User updatedUser = userRepository.save(existingUser);
        return convertToDTO(updatedUser);
    }

    // Обновление возраста через пользовательский запрос
    public void updateAgeCustom(int id, int age) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.updateUserAge(id, age);
    }

    // === DELETE OPERATIONS ===

    // Удалить пользователя по ID
    public void deleteUser(int id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    // Удалить пользователя по email
    public void deleteUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        userRepository.delete(user);
    }

    // Удалить всех пользователей
    public void deleteAllUsers() {
        userRepository.deleteAll();
    }

    // Удалить нескольких пользователей по ID
    public void deleteUsers(List<Integer> ids) {
        List<User> users = userRepository.findAllById(ids);
        if (users.size() != ids.size()) {
            throw new RuntimeException("Some users not found");
        }
        userRepository.deleteAll(users);
    }

    // === UTILITY METHODS ===

    // Конвертация Entity в DTO
    private UserResponseDTO convertToDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                user.getCreatedAt()
        );
    }

    // Конвертация DTO в Entity
    private User convertToEntity(UserRequestDTO userDTO) {
        User user = new User(
                userDTO.getName(),
                userDTO.getEmail(),
                userDTO.getAge(),
                userDTO.getCreatedAt()
        );

        // Если дата не указана, устанавливаем текущую
        if (userDTO.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }

        return user;
    }
}