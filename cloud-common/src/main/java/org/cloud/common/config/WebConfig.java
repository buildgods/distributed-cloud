package org.cloud.common.config;


import org.cloud.common.interceptor.LoginInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
// 由于网关底层并不是springMvc，所以一旦启动网管就会报错
// 就需要这个注解@ConditionalOnClass只有有springMVC的核心类才会生效
@ConditionalOnClass(DispatcherServlet.class)
public class WebConfig implements WebMvcConfigurer {
     @Bean
     public LoginInterceptor getLoginInterceptor(){
        return new LoginInterceptor();
    }
    /**
     * 注册登录拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(getLoginInterceptor()).excludePathPatterns("/user/login","/user/register","/user/forgetPwd",
                "/images/**","/files/**","/verify/getCode/**","/mail/getCode/**");
    }
}
