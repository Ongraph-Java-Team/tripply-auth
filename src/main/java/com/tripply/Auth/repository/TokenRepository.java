package com.tripply.Auth.repository;

import com.tripply.Auth.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<Token, UUID> {

    Token findByBlockedToken(String blockedToken);
}
