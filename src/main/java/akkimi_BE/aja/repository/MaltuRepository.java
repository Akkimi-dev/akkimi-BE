package akkimi_BE.aja.repository;

import akkimi_BE.aja.entity.Maltu;
import akkimi_BE.aja.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MaltuRepository extends JpaRepository<Maltu, Long> {

    //내가 생성한 말투
    List<Maltu> findByCreatorOrderByCreatedAtDesc(User creator);

    //기본 말투 저장되어있는지 확인
    Integer countByIsDefaultTrue();

    //공개된 말투 리스트 조회(기본 말투 포함x)
    List<Maltu> findByIsPublicTrueAndIsDefaultFalseOrderByCreatedAtDesc();

    //기본 말투 리스트 조회
    List<Maltu> findByIsDefaultTrue();
}
