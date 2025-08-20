package akkimi_BE.aja.repository;

import akkimi_BE.aja.entity.ChatMessage;
import akkimi_BE.aja.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("""
        select c
        from ChatMessage c
        where c.user.userId = :userId
          and (:beforeId is null or c.chatId < :beforeId)
        order by c.chatId desc
    """)
    List<ChatMessage> findSlice(
            @Param("userId") Long userId,
            @Param("beforeId") Long beforeId,
            Pageable pageable);
    
    void deleteAllByUser(User user);
}
