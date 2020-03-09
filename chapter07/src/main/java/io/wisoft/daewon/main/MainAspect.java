package io.wisoft.daewon.main;

import io.wisoft.daewon.calculator.RecCalculator;
import io.wisoft.daewon.cofig.AppCtx;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainAspect {

  public static void main(String... args) {
    AnnotationConfigApplicationContext ctx =
        new AnnotationConfigApplicationContext(AppCtx.class);

    RecCalculator calculator = ctx.getBean("calculator", RecCalculator.class);
    long fiveFact = calculator.factorial(5);
    System.out.println("calculator.factorial(5) = " + fiveFact);
    System.out.println(calculator.getClass().getName());

    ctx.close();
  }

}
