package akkimi_BE.aja.repository;

import akkimi_BE.aja.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySocialId(String socialId);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phone);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);
}
