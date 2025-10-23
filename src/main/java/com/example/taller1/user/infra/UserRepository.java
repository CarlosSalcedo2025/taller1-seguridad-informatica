package com.example.taller1.user.infra;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taller1.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
	
	Optional<User> findByEmail(String email);
    
}
