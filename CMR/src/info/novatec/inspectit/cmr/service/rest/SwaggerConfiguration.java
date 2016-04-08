/**
 *
 */
package info.novatec.inspectit.cmr.service.rest;

import static springfox.documentation.builders.PathSelectors.regex;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author Mario Mann
 *
 */
@Configuration
@EnableWebMvc
@EnableSwagger2
// @ComponentScan("info.novatec.inspectit.cmr.service.rest")
public class SwaggerConfiguration {
	@Bean
	public Docket newsApi() {
		return new Docket(DocumentationType.SWAGGER_2).groupName("rest").apiInfo(apiInfo()).select().paths(regex("/rest.*")).build();
	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder().title("Spring REST Sample with Swagger").description("Spring REST Sample with Swagger").license("Apache License Version 2.0").version("1.0").build();
	}
}
