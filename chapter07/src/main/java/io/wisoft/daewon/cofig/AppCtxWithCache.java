package io.wisoft.daewon.cofig;

import io.wisoft.daewon.aspect.CacheAspect;
import io.wisoft.daewon.aspect.ExeTimeAspect;
import io.wisoft.daewon.calculator.Calculator;
import io.wisoft.daewon.calculator.RecCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class AppCtxWithCache {

  @Bean
  public CacheAspect cacheAspect() {
    return new CacheAspect();
  }

  @Bean
  public ExeTimeAspect exeTimeAspect() {
    return new ExeTimeAspect();
  }

  @Bean
  public Calculator calculator() {
    return new RecCalculator();
  }

}
