package io.wisoft.daewon.cofig;

import io.wisoft.daewon.calculator.Calculator;
import io.wisoft.daewon.calculator.RecCalculator;
import io.wisoft.daewon.aspect.ExeTimeAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AppCtx {

  @Bean
  public ExeTimeAspect exeTimeAspect() {
    return new ExeTimeAspect();
  }

  @Bean
  public Calculator calculator() {
    return new RecCalculator();
  }

}
