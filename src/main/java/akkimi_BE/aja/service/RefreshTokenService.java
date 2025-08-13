package akkimi_BE.aja.service;

import akkimi_BE.aja.entity.RefreshToken;
import akkimi_BE.aja.entity.User;

import java.util.Optional;

public interface RefreshTokenService {
    Optional<RefreshToken> find(String refreshToken);
    RefreshToken store(User user, String refreshToken, long ttlSeconds);
    void revoke(String refreshToken);
    void revokeAll(User user);
}
