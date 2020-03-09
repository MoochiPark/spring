package io.wisoft.daewon.main;

import io.wisoft.daewon.calculator.ExeTimeCalculator;
import io.wisoft.daewon.calculator.ImpeCalculator;
import io.wisoft.daewon.calculator.RecCalculator;

public class MainProxy {

  public static void main(String... args) {
    ExeTimeCalculator ttCal1 = new ExeTimeCalculator(new ImpeCalculator());
    System.out.println(ttCal1.factorial(20));

    ExeTimeCalculator ttCal2 = new ExeTimeCalculator(new RecCalculator());
    System.out.println(ttCal2.factorial(20));
  }

}
