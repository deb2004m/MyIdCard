package com.example.idcard.repository;

import com.example.idcard.model.UserCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardRepository extends JpaRepository<UserCard, Long> {

    Optional<UserCard> findByGymId(String gymId);

    boolean existsByGymId(String gymId);
}
