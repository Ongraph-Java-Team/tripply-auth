package com.tripply.Auth.repository;

import com.tripply.Auth.entity.BlackListToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BlackListTokenRepository extends JpaRepository<BlackListToken, UUID> {

    BlackListToken findByTokenValue(String tokenValue);
}
