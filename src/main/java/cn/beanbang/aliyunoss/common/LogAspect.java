package cn.beanbang.aliyunoss.common;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LogAspect {

    @Pointcut("execution(public * cn.beanbang.aliyunoss.util.AliyunOSSUtil.*(..))")
    public void pointcut() {}

    /**
     * 日志切面，计算下载和上传的时间
     */
    @Around("pointcut()")
    public Object log(ProceedingJoinPoint joinPoint) {
        long startTime = System.currentTimeMillis();

        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        long respTime = System.currentTimeMillis() - startTime;

        log.info("Total time: {} s", (float) respTime / 1000);

        return result;
    }
}
