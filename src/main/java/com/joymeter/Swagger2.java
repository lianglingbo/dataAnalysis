package com.joymeter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @ClassName Swagger2
 * @Description TODO
 * @Author liang
 * @Date 2018/7/31 17:29
 * @Version 1.0
 **/
@Configuration
@EnableSwagger2
public class Swagger2 {


    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.joymeter.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("数据中心数据接口")
                .description("描述信息.")
                .termsOfServiceUrl("http://blog.didispace.com/")
                .contact("Joymeter")
                .version("1.0")
                .build();
    }


}
