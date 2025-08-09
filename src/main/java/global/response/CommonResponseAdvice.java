package global.response;

import global.response.CommonResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.*;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class CommonResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        // 파일/이미지/스트림/멀티파트 등은 래핑하지 않음
        if (MediaType.APPLICATION_OCTET_STREAM.includes(selectedContentType)
                || MediaType.IMAGE_JPEG.includes(selectedContentType)
                || MediaType.IMAGE_PNG.includes(selectedContentType)
                || MediaType.MULTIPART_FORM_DATA.includes(selectedContentType)) {
            return body;
        }

        // 이미 CommonResponse라면 그대로 통과 (직접 오버라이드한 케이스)
        if (body instanceof CommonResponse<?> cr) return cr;
        
        // CustomErrorResponse도 그대로 통과 (에러 응답)
        if (body instanceof CustomErrorResponse cer) return cer;

        // 현재 HTTP 상태
        int status = (response instanceof ServletServerHttpResponse s)
                ? s.getServletResponse().getStatus()
                : HttpStatus.OK.value();

        // 204는 바디 없이 반환해야 하므로 통과
        if (status == HttpStatus.NO_CONTENT.value()) {
            return body;
        }

        // 상태별 기본 메시지
        String message = switch (status) {
            case 200 -> "요청이 성공적으로 처리되었습니다.";
            case 201 -> "리소스가 생성되었습니다.";
            case 202 -> "요청이 접수되었습니다.";
            default -> "요청이 성공적으로 처리되었습니다.";
        };

        // 정상 래핑
        return CommonResponse.of(status, message, body);
    }
}