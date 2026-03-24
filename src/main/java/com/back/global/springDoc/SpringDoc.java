package com.back.global.springDoc;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "커피 주문 API",
                version = "v1",
                description = "team 9 커피 주문 REST API문서입니다."
        )
)
public class SpringDoc {
}
