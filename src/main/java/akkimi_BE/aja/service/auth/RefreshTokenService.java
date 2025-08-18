package akkimi_BE.aja.service.auth;

import akkimi_BE.aja.entity.auth.RefreshToken;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(readOnly = true)
    public Optional<RefreshToken> find(String refreshToken) {
        return refreshTokenRepository.findByToken(refreshToken);
    }

    @Transactional
    public RefreshToken store(User user, String refreshToken, long ttlSeconds) { //멀티 디바이스 로그인 가능
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(ttlSeconds);
        RefreshToken rt = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiresAt(expiresAt)
                .build();
        return refreshTokenRepository.save(rt);
    }

    @Transactional
    public void revoke(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }

    @Transactional
    public void revokeAll(User user) {
        refreshTokenRepository.deleteAllByUser(user);
    }
}