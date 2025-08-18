package akkimi_BE.aja.repository;

import akkimi_BE.aja.entity.Maltu;
import akkimi_BE.aja.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaltuRepository extends JpaRepository<Maltu, Long> {
    List<Maltu> findByIsPublicTrueOrderByCreatedAtDesc();

    List<Maltu> findByCreatorOrderByCreatedAtDesc(User creator);
}
