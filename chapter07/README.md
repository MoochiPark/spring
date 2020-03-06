# Chapter 07. AOP 프로그래밍

> ***이 장에서 다룰 내용***
>
> - 프록시와 AOP
> - 스프링 AOP 구현

뒤에서 배우게될 트랜잭션의 처리 방식을 이해하려면 **AOP<sup>Aspect Oriented Programming: 관심 지향 프로그래밍</sup>**를 알아야 한다.
이 장은 어쩌면 이 책에서 **가장 어려운 내용**을 담고 있을지도 모른다고 한다. 일단은 시작해보도록 하자.



## 프로젝트 준비

- aspectj.weaver 모듈 의존 추가 - 스프링이 AOP를 구현할 때 사용하는 모듈

스프링 프레임워크의 AOP 기능은 spring-aop 모듈이 제공하는데 spring-context 모듈을 의존 대상에 추가하면 따로 추가해주지 않아도 된다. 

준비가 되었다면 다음과 같은 임의의 정수의 팩토리얼<sup>n!</sup>을 구하는 코드를 작성해보자.

> *Calculator.java*

```java
package io.wisoft.daewon;

public interface Calculator {
  
  public long factorial(final long num);
  
}
```

Calculator 인터페이스를 구현한 첫 번째 클래스는 for 문을 이용해서 팩토리얼을 구하는 코드이다.

> *ImpeCalculator.java*

```java
package io.wisoft.daewon;

public class ImpeCalculator implements Calculator {
  
  @Override
  public long factorial(long num) {
    long result = 1;
    for (long i = 1; i <= num; i++) {
      result *= i;
    }
    return result;
  }
  
}
```

두 번째 클래스는 재귀호출을 이용해서 팩토리얼을 구하는 코드이다.

> *RecCalculator.java*

```java
package io.wisoft.daewon;

public class RecCalculator implements Calculator {
  
  @Override
  public long factorial(long num) {
    if (num == 0) return 1;
    else return num * factorial(num - 1);
  }
  
}
```



## 프록시와 AOP

앞에서 구현한 팩토리얼 구현 클래스의 실행 시간을 출력하려면 어떻게 해야할까? 쉬운 방법은 메서드의 시작과 끝에서 시간을 구하고 이 두 시간의 차이를 출력하는 것이다. 

> *ImpeCalculator.java*

```java
package io.wisoft.daewon;

public class ImpeCalculator implements Calculator {

  @Override
  public long factorial(long num) {
    long start = System.currentTimeMillis();
    long result = 1;
    for (long i = 1; i <= num; i++) {
      result *= i;
    }
    long end = System.currentTimeMillis();
    System.out.printf("ImpeCalculator.factorial(%d) 실행 시간 = %d\n",
                      num, (end - start));
    return result;
  }

}
```

RecCalculator 클래스는 재귀호출이므로 다음 코드처럼 약간 복잡해진다. 또 시간이 3번 출력되는 문제가 있다.

> *RecCalculator.java*

```java
package io.wisoft.daewon;

public class RecCalculator implements Calculator {

  @Override
  public long factorial(long num) {
    long start = System.currentTimeMillis();
    try {
      if (num == 0) return 1;
      else return num * factorial(num - 1);
    } finally {
      long end = System.currentTimeMillis();
      System.out.printf("RecCalculator.factorial(%d) 실행 시간 = %d\n",
                        num, (end - start));
    }
  }

}
```

재귀호출을 고려하면 실행 시간을 출력하기 위해 기존 코드를 변경하는 것보다는 차라리 메서드 실행 전후에 값을 구하는게
나을지도 모른다.

```java
ImpeCalculator impeCal = new ImpeCalculator();
long start1 = System.currentTimeMills();
long fourFactorial1 = impeCal.factorial(4);
long end1 = System.currentTimeMills();
System.out.printf("ImpeCalculator.factorial(%d) 실행 시간 = %d\n", num, (end - start));

RecCalculator recCal = new RecCalculator();
long start2 = System.currentTimeMills();
long fourFactorial2 = recCal.factorial(4);
long end2 = System.currentTimeMills();
System.out.printf("RecCalculator.factorial(%d) 실행 시간 = %d\n", num, (end - start));
```

그렇지만 위 방식도 실행 시간을 밀리초가 아니라 나노초로 변경한다면 위 시간을 구하고 출력하는 코드가 중복되어 있어
두 곳을 모두 변경해야한다는 문제점이 있다.

이때 기존 코드를 수정하지 않고 코드 중복도 피할 수 있는 방법이 바로 **프록시 객체**이다. 코드를 살펴보자.

> *ExeTimeCalculator.java*

```java
package io.wisoft.daewon;

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
```

ExeTimeCalculator 클래스는 Calculator 인터페이스를 구현하고 있다. 이 클래스는 생성자를 통해 다른 Calculator 객체를 전달받아 delegate<sup>대리자</sup> 필드에 할당하고 delegate.factorial() 처럼 메서드를 실행한다. 

다음과 같은 방법으로 ImpeCalculator의 실행 시간을 측정할 수 있다. 

```java
ImpeCalculator impeCal = new ImpeCalculator();
ExeTimeCalculator calculator = new ExeTimeCalculator(impeCal);
System.out.println(calculator.factorial(4));
```

실행 흐름을 보면 ExeTimeCalculator 클래스의 factorial() 메서드는 결과적으로 ImpeCalculator의 factorial() 메서드의 실행 시간을 구해서 콘솔에 출력한다. 

> *MainProxy.java*

```java
package io.wisoft.daewon;

public class MainProxy {

  public static void main(String... args) {
    ExeTimeCalculator ttCal1 = new ExeTimeCalculator(new ImpeCalculator());
    System.out.println(ttCal1.factorial(20));

    ExeTimeCalculator ttCal2 = new ExeTimeCalculator(new RecCalculator());
    System.out.println(ttCal2.factorial(20));
  }

}
```

실행 결과는 다음과 같다.

```java
ImpeCalculator.factorial(20) 실행 시간 = 2268
2432902008176640000
RecCalculator.factorial(20) 실행 시간 = 3254
2432902008176640000
```

위 결과에서 다음을 알 수 있다.

- 기존 코드를 변경하지 않고 실행 시간을 출력할 수 있다. ImpeCalculator 클래스나 RecCalculator 클래스의 코드 
  변경 없이 이 두 클래스의 factorial() 메서드 실행 시간을 출력할 수 있게 되었다.
- 실행 시간을 구하는 코드의 중복을 제거했다. 나노초 대신 밀리초를 사용해서 실행 시간을 구하고 싶다면 ExeTimeCalculator 클래스만 변경하면 된다.

이것이 가능한 이유는 ExeTimeCalculator 클래스를 다음과 같이 구현했기 때문이다.

- factorial() 기능 자체를 직접 구현하기보다는 다른 객체에 factorial()의 실행을 위임한다.
- 계산 기능 외에 다른 부가적인 기능을 실행한다. 여기서 부가적인 기능은 실행 시간 측정이다.

이렇게 **핵심 기능의 실행은 다른 객체에 위임하고 부가적인 기능을 제공하는 객체를 프록시<sup>proxy</sup>라고 부른다.** 
ExeTimeCalculator가 프록시이고 ImpeCalculator 객체가 프록시의 대상 객체가 된다.

> 엄밀히 말하면 지금 작성한 예제는 프록시보다는 데코레이터<sup>decorator</sup> 객체에 가깝다. **프록시는 접근 제어 관점**에 초점이 맞춰져 있다면, **데코레이터는 기능 추가와 확장에 초점**이 맞춰져 있기 때문이다. 예제에서는 기존 기능에 시간 측정 기능을 추가하고 있기 때문에 데코레이터에 가깝지만 스프링 레퍼런스 문서에 AOP를 설명할 때 프록시란 용어를 사용하고 있어 이 책에서도 프록시를 사용했다고 한다.

프록시의 특징은 **핵심 기능은 구현하지 않는다**는 점이다. ImpeCalculator와 RecCalculator와는 다르게 ExeTimeCalculator 클래스는 팩토리얼 연산 자체를 구현하고 있지 않다.

프록시는 핵심 기능을 구현하지 않는 대신 **여러 객체에 공통으로 적용할 수 있는 기능을 구현**한다.

정리하면 ImpeCalculator, RecCalculator는 팩토리얼을 구한다는 핵심 기능 구현에 집중하고 프록시인 ExeTimeCalculator는 실행 시간 측정이라는 공통 기능 구현에 집중한다.

이렇게 **공통 기능 구현과 핵심 기능 구현을 분리하는 것이 AOP의 핵심**이다.



### AOP

AOP는 Aspect Oriented Programming의 약자로, **여러 객체에 공통으로 적용할 수 있는 기능을 분리해서 재사용성을 높여주는 프로그래밍 기법**이다. AOP는 핵심 기능과 공통 기능의 구현을 분리함으로써 핵심 기능을 구현한 코드의 수정 없이
공통 기능을 적용 할 수 있게 만들어준다.

팩토리얼 구현을 통해 핵심 기능과 공통 기능을 구분해서 구현하는 방법을 살펴봤다. 스프링도 이처럼 프록시를 이용해서
AOP를 구현하고 있다.

AOP의 기본 개념은 **핵심 기능에 공통 기능을 삽입**하는 것이다. 즉 **핵심 기능의 코드를 수정하지 않으면서 공통 기능의    **
**구현을 추가하는 것이 AOP**이다. 그 방법에는 세 가지가 있다.

- 컴파일 시점에 코드에 공통 기능을 삽입하는 방법

- 클래스 로딩 시점에 바이트 코드에 공통 기능을 삽입하는 방법

- 런타임에 프록시 객체를 생성해서 공통 기능을 삽입하는 방법

  첫 번째 방법은 AOP 개발 도구가 소스 코드를 컴파일 하기 전에 공통 구현 코드를 소스에 삽입하는 방식으로 동작한다.
  두 번째 방법은 클래스를 로딩할 때 바이트 코드에 공통 기능을 클래스에 삽입하는 방식으로 동작한다.
  이 두가지는 스프링 AOP에서는 지원하지 않으며 AspectJ와 같이 AOP 전용 도구를 사용해서 적용할 수 있다.

  스프링이 제공하는 AOP 방식은 프록시를 이용한 세 번째 방식이다. 두 번째 방식을 일부 지원하지만 널리 사용되는 방법은
  프록시를 이용한 방식이다. 프록시 방식은 이미 살펴본 것처럼 중간에 프록시 객체를 생성한다.

  다음 그림처럼 실제 객체의 기능을 실행하기 전, 후에 공통 기능을 호출한다.

  > *프록시 기반의 AOP*

  ![image](https://user-images.githubusercontent.com/43429667/75655081-71b06900-5ca4-11ea-8c7e-3060a9ba22be.png)

스프링 AOP는 프록시 객체를 자동으로 만들어준다. 따라서 ExeTimeCalculator 클래스처럼 상위 타입의 인터페이스를 상속받은 **프록시 클래스를 직접 구현할 필요가 없다**. 단지 **공통 기능을 구현한 클래스만 알맞게 구현하면 된다.**

AOP에서 공통 기능을 Aspect라 하는데 Aspect 외에 알아두어야 할 용어를 정리해보자.

> *AOP 주요 용어*

| 용어      | 의미                                                         |
| --------- | ------------------------------------------------------------ |
| Advice    | 언제 공통 관심 기능을 핵심 로직에 적용할 지를 정의하고 있다. <br />예를 들어 '메서드를 호출하기 전'(언제)에 '트랜잭션 시작'(공통 기능) 기능을 적용한다는 것을 정의한다. |
| Joinpoint | Advice를 적용 가능한 지점을 의미한다. 메서드 호출, 필드 값 변경 등이 Joinpoint에 해당한다. <br />스프링은 프록시를 이용해서 AOP를 구현하기 때문에 메서드 호출에 대한 Joinpoint만 지원한다. |
| Pointcut  | Joinpoint의 부분 집합으로서 실제 Advice가 적용되는  Joinpoint를 나타낸다.<br />스프링에서는 정규 표현식이나 AspectJ의 문법을 이용하여 Pointcut을 정의할 수 있다. |
| Weaving   | Advice를 핵심 로직 코드에 적용하는 것을 weaving이라고 한다.  |
| Aspect    | 여러 객체에 공통으로 적용되는 기능을 Aspect라고 한다. 트랜잭션이나 보안 등이 Aspect의 좋은 예이다. |



### Advice의 종류

스프링은 프록시를 이용해서 메서드 호출 시점에 Aspect를 적용하기 때문에 구현 가능한 Advice의 종류는 다음과 같다.

> *스프링에서 구현 가능한 Advice 종류*

| 종류                   | 설명                                                         |
| ---------------------- | ------------------------------------------------------------ |
| Before Advice          | 대상 객체의 메서드 호출 전에 공통 기능을 실행한다.           |
| After Returning Advice | 대상 객체의 메서드가 익셉션 없이 실행된 후에 공통 기능을 실행한다. |
| After Throwing Advice  | 대상 객체의 메서드를 실행하는 도중 익셉션이 발생한 경우에 공통 기능을 실행한다. |
| After Advice           | 익셉션 발생 여부에 상관 없이 대상 객체의 메서드 실행 후 공통 기능을 실행한다.<br />(try-catch-finally의 finally 블록과 비슷하다.) |
| Around Advice          | 대상 객체의 메서드 실행 전, 후 또는 익셉션 발생 시점에 공통 기능을 실행하는데 사용된다. |

이 중에서 널리 사용되는 것은 **Around Advice**이다. 이유는 대상 객체의 메서드를 실행하기 전/후, 익셉션 발생 시점 등 다양한 시점에 원하는 기능을 삽입할 수 있기 때문이다. 캐시 기능, 성능 모니터링 기능과 같은 Aspect를 구현할 때에는 Around Advice를 주로 이용한다. 이 책에서도 Around Advice의 구현 방법에 대해서만 살펴본다.



## 스프링 AOP 구현

스프링 AOP를 이용해서 공통 기능을 구현하고 적용하는 방법은 단순하다. 다음과 같은 절차만 따르면 된다.

- Aspect<sup>공통 기능</sup>로 사용할 클래스에 @Aspect 애노테이션을 붙인다.
- @Pointcut 애노테이션으로 공통 기능을 적용할 Pointcut을 정의한다.
- 공통 기능을 구현한 메서드에 @Around 애노테이션을 적용한다.



### @Aspect, @Pointcut, @Around를 이용한 AOP 구현

개발자는 공통 기능을 제공하는 Aspect 구현 클래스를 만들고 자바 설정을 이용해서 Aspect를 어디에 적용할지 설정하면 된다.
Aspect는 @Aspect 애노테이션을 이용해서 구현한다. 프록시는 스프링 프레임워크가 알아서 만들어준다. 
일단 실행 시간을 측정하는 Aspect를 구현해보자. 다음 코드는 Around Advice에서 사용할 Aspect이다. 쉽게 풀어서 말하자면
"다음 코드는 메서드 실행 전/후<sup>Around Advice</sup>에 사용할 공통 기능<sup>Aspect</sup>이다."

> *ExeTimeAspect.java*

```java
package io.wisoft.daewon.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.Arrays;

@Aspect
public class ExeTimeAspect {

  @Pointcut("execution(public * io.wisoft.daewon.calculator ..*(..))")
  private void publicTarget() {
  }
  
  @Around("publicTarget()")
  public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.nanoTime();
    try {
      Object result = joinPoint.proceed();
      return result;
    } finally {
      long finish = System.nanoTime();
      Signature sig = joinPoint.getSignature();
      System.out.printf("%s.%s(%s) 실행 시간 : %d ns\n",
          joinPoint.getTarget().getClass().getSimpleName(),
          sig.getName(), Arrays.toString(joinPoint.getArgs()));
    }
  }

}
```

각 애노테이션과 메서드에 대해 알아보자. 먼저 @Aspect 애노테이션을 적용한 클래스는  Advice와 Pointcut을 함께 제공한다.

@Pointcut은 공통 기능을 적용할 대상을 설정한다. @Pointcut 애노테이션의 값으로 사용할  수 있는 execution 명시자에 대해서는 뒤에서 배울 것이다. 일단 지금은 **io.wisoft.daewon.calculator 패키지와 그 하위 패키지에 위치한 타입의 pulbic 메서드를 Pointcut으로 설정**한다는 정도만 이해하고 넘어가자.

@Around 애노테이션은  Around Advice를 설정한다. **@Around 애노테이션의 값이 "publicTarget()"인데 이는 publicTarget() 메서드에 정의한 Pointcut에 공통 기능을 적용**한다는 것을 의미한다. publicTarget() 메서드는 패키지와 그 하위 패키지에 위치한 public 메서드를 Pointcut으로 설정하고 있으므로, **패키지나 그 하위 패키지에 속한 빈 객체의 public 메서드에 @Around가 붙은 measure 메서드를 적용**한다.

measure() 메서드의 ProceedingJoinPoint 타입 파라미터는 프록시 대상 객체의 메서드를 호출할 때 사용한다. **proceed() 메서드를 사용해서 실제 대상 객체의 메서드를 호출**한다. 이 메서드를 호출하면 대상 객체의 메서드가 실행되므로 이 코드 이전과 이후에 공통 기능을 위한 코드를 위치시키면 된다.

ProceedingJoinPoint의 getSignature(), getTarget(), getArgs() 등의 메서드를 사용하고 있는데, 각 메서드는 호출한 메서드의 시그너처, 대상 객체, 인자 목록을 구하는데 사용된다. 이 메서드를 사용해서 대상 객체의 클래스 이름과 메서드 이름을 출력한다.

> 자바에서 메서드 이름과 파라미터를 합쳐서 메서드 시그너처라 한다.



공통 기능을 적용하는데 필요한 코드를 구현했으므로 스프링 설정 클래스를 작성할 차례이다.

> *AppCtx.java*

```java
package io.wisoft.daewon.cofig;

import io.wisoft.daewon.Calculator;
import io.wisoft.daewon.RecCalculator;
import io.wisoft.daewon.aspect.ExeTimeAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
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
```

@Aspect 애노테이션을 붙인 클래스를 공통 기능으로 적용하려면 @EnableAspectJAutoProxy 애노테이션을 설정 클래스에
붙여야 한다. 이 애노테이션을 추가하면 스프링은 @Aspect 애노테이션이 붙은 빈 객체를 찾아서 빈 객체의 @Pointcut 설정과
@Around 설정을 사용한다.

ExeTimeAspect 클래스에 설정한 코드를 다시보면,

```java
  @Pointcut("execution(public * io.wisoft.daewon.calculator ..*(..))")
  private void publicTarget() {
  }

  @Around("publicTarget()")
  public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
    ...
  }
```

`@Around` 애노테이션은 Pointcut으로 `publicTarget()` 메서드를 설정했다. `publicTarget()` 메서드의 `@Pointcut`은 `io.wisoft.daewon.calculator` 패키지나 그 하위 패키지에 속한 빈 객체의 public 메서드를 설정한다. 
`AppCtx.java`에서 설정한 `Calculator` 타입이 `io.wisoft.daewon.calculator` 패키지에 속하므로 `calculator` 빈에 `ExeTimeAspect` 클래스에 정의한 공통 기능인 `measure()`를 적용한다.

> ***@Enable 류 애노테이션***
>
> 스프링은 @EnableAspectJAutoProxy와 같이 이름이 Enable로 시작하는 다양한 애노테이션을 제공한다.
> @Enable로 시작하는 애노테이션은 관련 기능을 적용하는데 필요한 다양한 스프링 설정을 대신 처리한다. 예를 들어
> @EnableAspectJAutoProxy 애노테이션은 프록시 생성과 관련된 AnnotationAwareAspectJAutoProxyCreator 객체를 빈으로 등록한다. 웹 개발과 관련된 @EnableWebMvc 애노테이션 역시 웹 개발과 관련된 다양한 설정을 등록한다.
>
> @Enable 류의 애노테이션은 복잡한 스프링 설정을 대신하기 때문에 개발자가 쉽게 스프링을 사용할 수 있도록 해준다.

calculator 빈에 공통 기능이 적용되는지 확인해보자.

> *MainAspect.java*

```java
public class MainAspect {

  public static void main(String... args) {
    AnnotationConfigApplicationContext ctx =
        new AnnotationConfigApplicationContext(AppCtx.class);

    Calculator calculator = ctx.getBean("calculator", Calculator.class);
    long fiveFact = calculator.factorial(5);
    System.out.println("calculator.factorial(5) = " + fiveFact);
    System.out.println(calculator.getClass().getName());

    ctx.close();
  }

}
```



**실행 결과**

```java
RecCalculator.factorial([5]) 실행 시간 : 25290 ns
calculator.factorial(5) = 120
com.sun.proxy.$Proxy21
```

- 1행: ExeTimeAspect 클래스의 measure() 메서드가 출력한 것이다.
- 3행: `System.out.println(calculator.getClass().getName());`에서 출력한 코드이다. 결과를 보면 Calculator의 타입이 RecCalculator 클래스가 아니고 $Proxy21이다. 이 타입은 스프링이 생성한 프록시 타입이다.

실제 calculator.factorial(5) 코드를 호출할 때 실행되는 과정은 다음과 같다.

![image](https://user-images.githubusercontent.com/43429667/75769131-f379c280-5d88-11ea-984e-f0a4cff88ae5.png)

AOP를 적용하지 않았다면 리턴한 객체는 RecCalculator 였을 것이다. 실제로 확인 해보자.

```java
calculator.factorial(5) = 120
io.wisoft.daewon.calculator.RecCalculator
```

실행 결과를 보면 타입이 RecCalculator 클래스 임을 알 수 있다.



### ProceedingJoinPoint의 메서드

Around Advice에서 사용할 공통 기능 메서드는 대부분 파라미터로 전달받은 ProceedingJoinPoint의 proceed() 메서드만 호출하면 된다. 예를 들어 다음 처럼  ExeTimeAspect 클래스도 다음처럼 proceed() 메서드를 호출했다.

```java
public class ExeTimeAspect {

  public Object measure(final ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.nanoTime();
    try {
      return joinPoint.proceed();
    } finally {
      ...
    }
  }
```

물론 호출되는 대상 객체에 대한 정보, 실행되는 메서드에 대한 정보, 메서드를 호출할 때 전달된 인자에 대한 정보가 필요할 때가 있다. 이들 정보에 접근할 수 있도록 ProceedingJoinPoint 인터페이스는 다음 메서드를 제공한다. 

- **Signature getSignature()**: 호출되는 메서드에 대한 정보를 구한다.
- **Object getTarget()**: 대상 객체를 구한다.
- **Object[] getArgs()**: 파라미터 목록을 구한다.



org.aspectj.lang.Signature 인터페이스는 다음 메서드를 제공한다. 각 메서드는 호출되는 메서드의 정보를 제공한다.

- **String getName()**: 호출되는 메서드의 이름을 구한다.
- **String toLongString()**: 호출되는 메서드를 완전하게 표현한 문장을 구한다(메서드의 리턴 타입, 파라미터 타입이 모두 
  표시된다).
- **String toShortString()**: 호출되는 메서드를 축약해서 표현한 문장을 구한다(기본 구현은 메서드의 이름만을 구한다).



## 프록시 생성 방식

MainAspect 클래스의 코드를 다음처럼 변경해보자.

```java
// 수정 전
Calculator calculator = ctx.getBean("calculator", Calculator.class);

//수정 후
RecCalculator calculator = ctx.getBean("calculator", RecCalculator.class);
```

getBean() 메서드에 Calculator 타입 대신에 RecCalculator 타입을 사용하도록 수정했다.
자바 설정 파일을 열어보면 "calculator" 빈을 생성할 때 사용한 타입이 RecCalculator 클래스이므로 문제가 없어 보인다.

```java
  @Bean
  public Calculator calculator() {
    return new RecCalculator();
  }
```

하지만 코드를 실행하게 되면 예상과 달리 익셉션이 발생한다.

```java
Exception in thread "main" org.springframework.beans.factory.BeanNotOfRequiredTypeException: Bean named 'calculator' is expected to be of type 'io.wisoft.daewon.calculator.RecCalculator' 
but was actually of type 'com.sun.proxy.$Proxy21'
```

메시지를 보면 getBean() 메서드에 사용한 타입이 RecCaclulator인데 반해 실제 타입은 $Proxy21이라는 메시지가 나온다.

$Proxy21은 스프링이 런타임에 생성한 프록시 객체의 클래스 이름인데, 이 클래스는 RecCalculator 클래스가 상속받은
Calculator 인터페이스를 상속받게 된다.

