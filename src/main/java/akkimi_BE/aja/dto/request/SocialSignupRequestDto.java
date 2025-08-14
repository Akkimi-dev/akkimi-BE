package akkimi_BE.aja.dto.request;

import akkimi_BE.aja.entity.SocialType;

public record SocialSignupRequestDto(
        SocialType socialType,
        String socialId,
        String nickname
) {}
