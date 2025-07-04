package com.amarhu.user.repository;

import com.amarhu.user.entity.UserRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRequestRepository extends JpaRepository<UserRequest, Long> {
    boolean existsByEmail(String email);
}
