package akkimi_BE.aja.repository;

import akkimi_BE.aja.entity.RefreshToken;
import akkimi_BE.aja.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String refreshToken);

    void deleteByToken(String refreshToken);

    void deleteAllByUser(User user);
}
