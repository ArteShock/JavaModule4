package app.service;

import app.dto.EmailMessage;
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
    @Autowired
    private EmailProducer emailProducer;

    public UserResponseDTO createUser(UserRequestDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email already exists: " + userDTO.getEmail());
        }

        User user = convertToEntity(userDTO);
        User savedUser = userRepository.save(user);
        emailProducer.sendUserCreate(user.getEmail(),user.getName());

        return convertToDTO(savedUser);
    }


    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO getUserById(int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return convertToDTO(user);
    }

    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return convertToDTO(user);
    }

    public boolean userExists(int id) {
        return userRepository.existsById(id);
    }

    public boolean userExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public long getUsersCount() {
        return userRepository.count();
    }

    public UserResponseDTO updateUser(int id, UserRequestDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (userRepository.existsByEmail(userDTO.getEmail()) &&
                !existingUser.getEmail().equals(userDTO.getEmail())) {
            throw new RuntimeException("Email already exists: " + userDTO.getEmail());
        }

        existingUser.setName(userDTO.getName());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setAge(userDTO.getAge());

        if (userDTO.getCreatedAt() != null) {
            existingUser.setCreatedAt(userDTO.getCreatedAt());
        }

        User updatedUser = userRepository.save(existingUser);
        return convertToDTO(updatedUser);
    }

    public UserResponseDTO updateUserName(int id, String name) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        existingUser.setName(name);
        User updatedUser = userRepository.save(existingUser);
        return convertToDTO(updatedUser);
    }


    public UserResponseDTO updateUserEmail(int id, String email) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        if (userRepository.existsByEmail(email) && !existingUser.getEmail().equals(email)) {
            throw new RuntimeException("Email already exists: " + email);
        }

        existingUser.setEmail(email);
        User updatedUser = userRepository.save(existingUser);
        return convertToDTO(updatedUser);
    }


    public UserResponseDTO updateUserAge(int id, int age) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        existingUser.setAge(age);
        User updatedUser = userRepository.save(existingUser);
        return convertToDTO(updatedUser);
    }


    public void deleteUser(UserResponseDTO userDTO) {
        int id = userDTO.getId();
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        emailProducer.sendUserDelete(userDTO.getEmail(),userDTO.getName());
    }

    public void deleteAllUsers() {
        userRepository.deleteAll();
    }

    private UserResponseDTO convertToDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                user.getCreatedAt()
        );
    }


    private User convertToEntity(UserRequestDTO userDTO) {
        User user = new User(
                userDTO.getName(),
                userDTO.getEmail(),
                userDTO.getAge(),
                userDTO.getCreatedAt()
        );

        if (userDTO.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }

        return user;
    }
}