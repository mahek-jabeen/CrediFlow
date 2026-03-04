package com.loanmanagement.repository;

import com.loanmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByPanNumber(String panNumber);

    Optional<User> findByAadharNumber(String aadharNumber);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByPanNumber(String panNumber);

    boolean existsByAadharNumber(String aadharNumber);
}
