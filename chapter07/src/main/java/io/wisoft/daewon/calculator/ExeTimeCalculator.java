package io.wisoft.daewon.calculator;

public class ExeTimeCalculator implements Calculator {

  private Calculator delegate;

  public ExeTimeCalculator(final Calculator delegate) {
    this.delegate = delegate;
  }

  @Override
  public long factorial(final long num) {
    long start = System.nanoTime();
    long result = delegate.factorial(num);
    long end = System.nanoTime();
    System.out.printf("%s.factorial(%d) 실행 시간 = %d\n",
        delegate.getClass().getSimpleName(), num, (end - start));
    return result;
  }

}
