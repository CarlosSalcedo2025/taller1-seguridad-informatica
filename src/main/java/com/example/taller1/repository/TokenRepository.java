package com.example.taller1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.taller1.entity.Token;

import java.util.List;
import java.util.Optional;
public interface TokenRepository extends JpaRepository<Token, Integer> {

  @Query(value = """
      select t from Token t inner join User u\s
      on t.user.id = u.id
      where u.id = :id and (t.expired = false or t.revoked = false)
      """)
  List<Token> findAllValidTokenByUser(@Param("id") Long id);

  Optional<Token> findByToken(String token);
}
