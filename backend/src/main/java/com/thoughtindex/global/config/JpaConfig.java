package com.thoughtindex.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * @CreatedDate / @LastModifiedDate 가 자동으로 채워지게 켠다.
 * 메인 클래스에 붙이지 않고 따로 빼두면 @WebMvcTest 같은 슬라이스 테스트가 JPA 설정에 끌려가지 않는다.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
