package com.example.springbootfileupload.repository;

import com.example.springbootfileupload.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    Users findByUsername(String username);

    Users findByLocation(String location);
}