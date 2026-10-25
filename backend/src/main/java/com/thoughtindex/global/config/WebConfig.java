package com.thoughtindex.global.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.thoughtindex.global.security.CurrentMemberArgumentResolver;

/**
 * CORS 는 시큐리티가 먼저 처리해야 해서 SecurityConfig 로 옮겼다.
 * 여기서는 @CurrentMember 파라미터 해석기만 등록한다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentMemberArgumentResolver());
    }
}
