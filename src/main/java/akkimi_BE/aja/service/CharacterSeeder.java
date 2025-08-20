package akkimi_BE.aja.service;

import akkimi_BE.aja.entity.Character;
import akkimi_BE.aja.repository.CharacterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * Character 초기 데이터를 관리하고 DB에 시딩하는 클래스
 * AdminAndMaltuSeeder와 동일한 패턴으로 구현
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CharacterSeeder implements ApplicationRunner {
    
    private final CharacterRepository characterRepository;
    
    private static final List<String> CHARACTER_NAMES = Arrays.asList(
        // 미식파
        "실속형 미식파",
        "감정형 미식파",
        "무의식형 미식파",
        
        // 스타일파
        "실속형 스타일파",
        "감정형 스타일파",
        "무의식형 스타일파",
        
        // 취미파
        "실속형 취미파",
        "감정형 취미파",
        "무의식형 취미파",
        
        // 생활파
        "실속형 생활파",
        "감정형 생활파",
        "무의식형 생활파"
    );
    
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 이미 캐릭터 데이터가 있으면 스킵
        if (characterRepository.count() > 0) {
            return;
        }
        
        // 12개 캐릭터 생성
        CHARACTER_NAMES.forEach(name -> {
            Character character = Character.builder()
                    .characterName(name)
                    .build();
            characterRepository.save(character);
        });
    }
}