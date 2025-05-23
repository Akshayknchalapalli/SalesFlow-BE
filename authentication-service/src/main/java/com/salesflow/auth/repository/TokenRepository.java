package com.salesflow.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.salesflow.auth.domain.Token;
import com.salesflow.auth.domain.User;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByRefreshToken(String refreshToken);
    
    @Query("SELECT t FROM Token t WHERE t.user = ?1 AND t.revoked = false")
    List<Token> findAllValidTokensByUser(User user);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Token t WHERE t.user.id = ?1")
    void deleteByUser_Id(Long userId);
    
    @Modifying
    @Transactional
    @Query("UPDATE Token t SET t.revoked = true WHERE t.user.id = ?1")
    void revokeAllUserTokens(Long userId);
    
    @Query("SELECT COUNT(t) > 0 FROM Token t WHERE t.refreshToken = ?1 AND t.revoked = false")
    boolean isTokenValid(String refreshToken);
} 