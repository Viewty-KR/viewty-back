package com.viewty.viewtyback.repository;

import com.viewty.viewtyback.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUserId(String UserId);

    boolean existsByEmail(String email);
}
