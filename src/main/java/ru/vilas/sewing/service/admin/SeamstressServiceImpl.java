package ru.vilas.sewing.service.admin;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.vilas.sewing.config.UsernameExistsException;
import ru.vilas.sewing.model.Role;
import ru.vilas.sewing.model.User;
import ru.vilas.sewing.repository.RoleRepository;
import ru.vilas.sewing.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SeamstressServiceImpl implements SeamstressService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public SeamstressServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Override
    public List<User> getAllSeamstresses() {
        List<User> allUsers = userRepository.findAll();
        //List<User> users = customUserDetailsService.getAllUsers();

        allUsers.forEach(System.out::println);
        return allUsers.stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals("ROLE_USER")))
                .collect(Collectors.toList());
    }

    @Override
    public void saveSeamstress(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UsernameExistsException("Пользователь с логином " + user.getUsername() + " уже существует");
        }

        // Кодирование пароля перед сохранением
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Получение объекта роли "user" из базы данных
        Optional<Role> optionalUserRole = roleRepository.findByName("ROLE_USER");

        // Извлечение объекта роли из Optional
        Role userRole = optionalUserRole.orElseThrow(() -> new RuntimeException("Роль 'USER' не найдена в базе данных"));

        // Присвоение роли "user" пользователю
        user.setRoles(Collections.singleton(userRole));

        // Сохранение пользователя в базе данных
        userRepository.save(user);
    }


    @Override
    public void deleteSeamstress(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        user.getRoles().clear(); // Очищаем коллекцию ролей у пользователя
        userRepository.save(user); // Сохраняем пользователя без связей с ролями
        userRepository.deleteById(id);
    }

}
