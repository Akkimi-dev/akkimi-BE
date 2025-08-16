package akkimi_BE.aja.service.auth;

import akkimi_BE.aja.entity.RefreshToken;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<RefreshToken> find(String refreshToken) {
        return refreshTokenRepository.findByToken(refreshToken);
    }

    @Override
    @Transactional
    public RefreshToken store(User user, String refreshToken, long ttlSeconds) {
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(ttlSeconds);
        RefreshToken rt = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiresAt(expiresAt)
                .build();
        return refreshTokenRepository.save(rt);
    }

    @Override
    @Transactional
    public void revoke(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }

    @Override
    @Transactional
    public void revokeAll(User user) {
        refreshTokenRepository.deleteAllByUser(user);
    }
}