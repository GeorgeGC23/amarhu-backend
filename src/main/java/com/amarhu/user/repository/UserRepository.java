package com.amarhu.user.repository;

import com.amarhu.user.entity.User;
import com.amarhu.user.entity.Role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    List<User> findByRoleAndCodigo(Role role, String codigo);

    boolean existsByCodigo(String codigo);
    Optional<User> findByCodigo(String codigo); // Este método busca usuarios por su código

    List<User> findByRole(Role role);
}
