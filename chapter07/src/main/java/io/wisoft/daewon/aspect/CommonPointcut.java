package io.wisoft.daewon.aspect;

import org.aspectj.lang.annotation.Pointcut;

public class CommonPointcut {

  @Pointcut("execution(public * io.wisoft.daewon.calculator ..*(..))")
  public void commonTarget() {}

}
