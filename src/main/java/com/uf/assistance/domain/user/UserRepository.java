package com.uf.assistance.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserId(String userId);
    Optional<User> findByUsername(String userName); //Jpa NamedQuery 작동

    Optional<User> findByEmail(String usermail); //Jpa NamedQuery 작동
    //save - 이미 만들어져 있음
    boolean existsByUserId(String userId);
}
