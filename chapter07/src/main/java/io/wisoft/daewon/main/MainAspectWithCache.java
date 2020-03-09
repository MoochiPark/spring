package io.wisoft.daewon.main;

import io.wisoft.daewon.calculator.Calculator;
import io.wisoft.daewon.cofig.AppCtxWithCache;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainAspectWithCache {

  public static void main(String... args) {
    AnnotationConfigApplicationContext ctx =
        new AnnotationConfigApplicationContext(AppCtxWithCache.class);

    Calculator cal = ctx.getBean("calculator", Calculator.class);
    cal.factorial(7);
    cal.factorial(7);
    cal.factorial(5);
    cal.factorial(5);

    ctx.close();
  }

}
