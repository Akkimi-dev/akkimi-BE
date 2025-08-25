package akkimi_BE.aja.repository;

import akkimi_BE.aja.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySocialId(String socialId);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.character WHERE u.socialId = :socialId")
    Optional<User> findBySocialIdWithCharacter(@Param("socialId") String socialId);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phone);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    // characterName만 바로 뽑기: Lazy 프록시 건드리지 않음
    @Query("""
           select c.characterName
           from User u
           join u.character c
           where u.userId = :userId
           """)
    Optional<String> findCharacterNameByUserId(@Param("userId") Long userId);
}
