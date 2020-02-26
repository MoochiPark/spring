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

RecCalculator 클래스는 재귀호출이므로 약간 복잡해진다. 또 시간이 3번 출력되는 문제가 있다.

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
  변경 없이 이 두 클래스의 factorial() 메œ서드 실행 시간을 출력할 수 있게 되었다.
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

