package com.example.taller1.admin.web;

import com.example.taller1.user.infra.UserRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository users;

    public AdminController(UserRepository users) {
        this.users = users;
    }

    @GetMapping("/users")
    public List<Map<String, Object>> users() {
        return users.findAll().stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getId(),
                        "email", u.getEmail(),
                        "roles", u.getRoles()
                ))
                .toList();
    }
}
