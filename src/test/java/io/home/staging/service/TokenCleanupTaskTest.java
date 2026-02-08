package io.home.staging.service;

import io.home.staging.repository.TokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TokenCleanupTaskTest {

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private TokenCleanupTask tokenCleanupTask;

    @Test
    void cleanupExpiredTokens_ShouldCallRepositoryMethod() {
        // When
        tokenCleanupTask.cleanupExpiredTokens();

        // Then
        verify(tokenRepository, times(1)).deleteExpiredOrRevokedTokens();
    }
}
