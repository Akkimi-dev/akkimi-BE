package akkimi_BE.aja.repository;

import akkimi_BE.aja.entity.Maltu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaltuRepository extends JpaRepository<Maltu, Long> {
}
