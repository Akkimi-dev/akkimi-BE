package akkimi_BE.aja.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 비동기 처리(SSE) 시 Spring Security Context를 전파하기 위한 설정
 * 
 * SSE(Server-Sent Events) 사용 시 발생하는 인증 문제 해결:
 * - 비동기 디스패치 시 SecurityContext가 손실되는 문제
 * - DelegatingSecurityContextAsyncTaskExecutor를 사용하여 SecurityContext 전파
 */
@Configuration
public class AsyncConfig implements WebMvcConfigurer {

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(10);
        taskExecutor.setMaxPoolSize(20);
        taskExecutor.setQueueCapacity(100);
        taskExecutor.setThreadNamePrefix("sse-async-");
        // 큐가 가득 찰 때 호출자 스레드에서 실행 (서버 다운 방지)
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.initialize();

        // DelegatingSecurityContextAsyncTaskExecutor로 감싸서 Security Context 전파
        AsyncTaskExecutor securityTaskExecutor = new DelegatingSecurityContextAsyncTaskExecutor(taskExecutor);
        
        configurer.setTaskExecutor(securityTaskExecutor);
        // SSE 타임아웃 설정 (30분)
        configurer.setDefaultTimeout(30 * 60 * 1000L);
    }
}