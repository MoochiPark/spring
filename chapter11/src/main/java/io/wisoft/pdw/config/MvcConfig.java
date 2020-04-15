package io.wisoft.pdw.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

  @Override
  public void configureDefaultServletHandling(
      final DefaultServletHandlerConfigurer configurer) {
    configurer.enable();
  }

  @Override
  public void configureViewResolvers(final ViewResolverRegistry resolverRegistry) {
    resolverRegistry.jsp("/WEB-INF/view/", ".jsp");
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/main").setViewName("main");
  }

}
