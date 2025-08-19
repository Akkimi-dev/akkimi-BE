package akkimi_BE.aja.service;

import akkimi_BE.aja.dto.response.CharacterResponseDto;
import akkimi_BE.aja.entity.Character;
import akkimi_BE.aja.repository.CharacterRepository;
import akkimi_BE.aja.global.exception.CustomException;
import akkimi_BE.aja.global.exception.HttpErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CharacterService {
    
    private final CharacterRepository characterRepository;

    public List<CharacterResponseDto> getAllCharacters() {
        return characterRepository.findAll().stream()
                .map(CharacterResponseDto::from)
                .collect(Collectors.toList());
    }

    public CharacterResponseDto getCharacter(Long characterId) {
        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new CustomException(HttpErrorCode.CHARACTER_NOT_FOUND));
        return CharacterResponseDto.from(character);
    }
}