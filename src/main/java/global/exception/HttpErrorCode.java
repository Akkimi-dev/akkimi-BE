package global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum HttpErrorCode implements ErrorCode {

    INTERNAL_SERVER_ERROR("요청 처리 중 알 수 없는 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED("인증이 필요한 요청입니다.", HttpStatus.UNAUTHORIZED),
    SOCIALID_NOT_FOUND("해당 socialId로 회원을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    REFRESH_TOKEN_NULL("Refresh Token의 값이 없습니다.",HttpStatus.BAD_REQUEST),

    // 로그인/회원가입
    LOGIN_BAD_CREDENTIALS("아이디(전화번호/이메일) 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    LOGIN_SOCIAL_TYPE_MISMATCH("로컬 계정이 아닙니다.", HttpStatus.UNAUTHORIZED),
    VALIDATE_EXISTED_EMAIL("이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),
    VALIDATE_EXISTED_PHONE("이미 사용 중인 전화번호입니다.", HttpStatus.CONFLICT),

    //유저
    USER_NOT_FOUND("해당 유저를 찾을 수 없습니다.",HttpStatus.NOT_FOUND),

    //말투
    MALTU_NOT_FOUND("해당 말투를 찾을 수 없습니다.",HttpStatus.NOT_FOUND),
    MALTU_NOT_PUBLIC("해당 말투는 현재 공유되어 있지 않습니다.",HttpStatus.BAD_REQUEST),
    USER_MALTU_NOT_SETTED("유저에 말투가 설정되어 있지 않습니다.",HttpStatus.NOT_FOUND);


    private final String errorMessage; // 에러 메시지
    private final HttpStatus httpStatus; // 매핑할 HTTP 상태
}
