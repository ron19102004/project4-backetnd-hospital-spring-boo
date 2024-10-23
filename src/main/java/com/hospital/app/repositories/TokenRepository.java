package com.hospital.app.repositories;

import com.hospital.app.entities.account.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Token findByAccessToken(String accessToken);
    Token findByAccessTokenOrRefreshToken(String accessToken, String refreshToken);
    List<Token> findAllByUserIdOrderByIdAsc(Long userId);
}
