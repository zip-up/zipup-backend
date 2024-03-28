package com.zipup.server.global.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.BiConsumer;

@Aspect
@Slf4j
@Component
public class LogAspect {

    private final String LOG_METHOD_NAME = "메서드 이름 :: {}";
    private final String LOG_PARAMETER_TYPE = "파라미터 타입 :: {}";
    private final String LOG_PARAMETER_VALUE = "파라미터 값 :: {}";
    private final String LOG_TIME_FORMAT = "요청 시간 :: {}";
    private final String LOG_ERROR_MESSAGE = "에러 메시지 :: {}";
    private final String LOG_ERROR_CLASS = "에러 클래스 :: {}";

    private void info(final JoinPoint joinPoint, final BiConsumer<String, String> consumer) {
        consumer.accept(LOG_METHOD_NAME, joinPoint.getSignature().toShortString());
        consumer.accept(LOG_TIME_FORMAT, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        for (Object object : joinPoint.getArgs()) {
            if (Objects.nonNull(object)) {
                consumer.accept(LOG_PARAMETER_TYPE, object.getClass().getSimpleName());
                consumer.accept(LOG_PARAMETER_VALUE, object.toString());
            }
        }
    }

    private void err(final JoinPoint joinPoint, Exception exception, final BiConsumer<String, String> consumer) {
        consumer.accept(LOG_METHOD_NAME, joinPoint.getSignature().toShortString());
        consumer.accept(LOG_TIME_FORMAT, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        consumer.accept(LOG_ERROR_MESSAGE, exception.getMessage());
        consumer.accept(LOG_ERROR_CLASS, exception.getClass().getSimpleName());
    }

    @Pointcut("execution(* com.zipup.server.*.controller..*.*(..))")
    public void beforeExecute() {}

    @Before("beforeExecute()")
    public void requestLogging(final JoinPoint joinPoint) {
        info(joinPoint, log::info);
    }

    @AfterReturning("beforeExecute()")
    public void afterRequesting(JoinPoint joinPoint) {
        info(joinPoint, log::debug);
    }

    @AfterThrowing(pointcut = "beforeExecute()", throwing = "exception")
    public void exceptionLogging(JoinPoint joinPoint, Exception exception) {
        err(joinPoint, exception, log::error);
    }

}
