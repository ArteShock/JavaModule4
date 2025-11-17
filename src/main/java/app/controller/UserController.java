package app.controller;

import app.dto.UserRequestDTO;
import app.dto.UserResponseDTO;
import app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "User Management", description = "API для управления пользователями")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    @Operation(summary = "Получить всех пользователей", description = "Возвращает список всех пользователей в системе")
    @ApiResponse(responseCode = "200", description = "Успешный ответ")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        try {
            List<UserResponseDTO> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/users")
    @Operation(summary = "Создать нового пользователя", description = "Создает нового пользователя и отправляет email уведомление")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь создан"),
            @ApiResponse(responseCode = "400", description = "Неверные данные или email уже существует")
    })
    public ResponseEntity<?> createUser(@RequestBody UserRequestDTO userDTO) {
        try {
            UserResponseDTO createdUser = userService.createUser(userDTO);
            return ResponseEntity.ok(createdUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при создании пользователя");
        }
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя по ID и отправляет email уведомление")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь удален"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<?> deleteUser(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable int id) {
        try {
            UserResponseDTO user = userService.getUserById(id);
            userService.deleteUser(user);
            return ResponseEntity.ok().body("Пользователь успешно удален");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при удалении пользователя");
        }
    }

    @PatchMapping("/users/{id}/name")
    @Operation(summary = "Обновить имя пользователя", description = "Обновляет только имя пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Имя обновлено"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<?> updateUserName(
            @Parameter(description = "ID пользователя") @PathVariable int id,
            @Parameter(description = "Новое имя", example = "Иван Иванов")
            @RequestParam String name) {
        try {
            UserResponseDTO updatedUser = userService.updateUserName(id, name);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при обновлении имени");
        }
    }
}