package cn.beanbang.aliyunoss;

import cn.beanbang.aliyunoss.service.AsyncExecutorTaskService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;

import javax.annotation.Resource;

@SpringBootApplication
public class AliyunOssApplication {

    @Resource
    private ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(AliyunOssApplication.class, args);
    }

}
