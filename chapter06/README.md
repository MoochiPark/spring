# Chapter 06. 빈 라이프사이클과 범위

> ***이 장에서 다룰 내용***
>
> - *컨테이너 초기화와 종료*
> - *빈 객체의 라이프사이클*
> - *싱글톤과 프로토타입 범위*



## 컨테이너 초기화와 종료

스프링 컨테이너는 초기화와 종료라는 라이프사이클을 갖는다. 2장에서 작성한 코드를 다시 보자.

> *Main.java*

```java
// 1. 컨테이너 초기화
AnnotaitionConfigApplicationContext ctx = 
  new AnnotationConfigApplicationContext(AppContext.class);

// 2. 컨테이너에서 빈 객체를 구해서 사용
Greeter g = ctx.getBean("greeter", Greeter.class);
String msg = g.greet("스프링");
System.out.println("msg");

// 3. 컨테이너 종료
ctx.close();
```

위 코드를 보면 AnnotationConfigApplicationContext의 생성자를 이용해서 컨텍스트 객체를 생성하는데 이 시점에서 
스프링 컨테이너를 초기화한다. 스프링 컨테이너는 설정 클래스에서 정보를 읽어와 알맞은 빈 객체를 생성하고 각 빈을 연결
(의존 주입)하는 작업을 수행한다.

컨테이너 초기화가 완료되면 컨테이너를 사용할 수 있다. 즉, getBean()과 같은 메서드를 이용해서 컨테이너에 보관된 빈 객체를 구할 수 있다.

컨테이너 사용이 끝나면 컨테이너를 종료한다. 이럴 때 close() 메서드를 사용한다. close() 메서드는 AbstractApplicationContext 클래스에 정의되어 있다. 자바 설정을 사용하는 AnnotationConfigApplicationContext 클래스나 XML 설정을 사용하는 GenericXmlApplicationContext 클래스를 모두 AbstractApplicationContext 클래스를 
상속 받고 있다. 따라서 앞서 코드처럼 close() 메서드를 이용해서 컨테이너를 종료할 수 있다.

컨테이너를 초기화하고 종료할 때에는 다음의 작업도 함께 수행한다.

- **컨테이너 초기화 → 빈 객체의 생성, 의존 주입, 초기화**
- **컨테이너 종료 → 빈 객체의 소멸**

스프링 컨테이너의 라이프사이클에 따라 빈 객체도 자연스럽게 생성과 소멸이라는 라이프사이클을 갖는다.



## 스프링 빈 객체의 라이프사이클

스프링 컨테이너는 빈 객체의 라이프사이클을 다음 그림처럼 관리한다. 

![image](https://user-images.githubusercontent.com/43429667/75136874-5f28b380-5729-11ea-9f1d-1281e29a0b49.png)

스프링 컨테이너를 초기화할 때 스프링 컨테이너는 **가장 먼저 빈 객체를 생성하고 의존을 설정**한다. 의존 자동 주입을 통한
의존 설정이 **이 시점에 수행**된다. 

모든 의존 설정이 완료되면 빈 객체의 초기화를 수행한다. 빈 객체를 초기화하기 위해 스프링은 빈 객체의 지정된 메서드를 
호출한다.

스프링 컨테이너를 종료하면 스프링 컨테이너는 빈 객체의 소멸을 처리한다. 이때에도 지정한 메서드를 호출한다.

### 빈 객체의 초기화와 소멸: 스프링 인터페이스

**스프링 컨테이너는 빈 객체를 초기화하고 소멸하기 위해 빈 객체의 지정한 메서드를 호출**한다.
스프링은 다음의 두 인터페이스에 이 메서드들을 정의하고 있다.

- org.springframework.beans.factory.InitializingBean
- org.springframework.beans.factory.DisposableBean

두 인터페이스는 다음과 같이 정의되어 있다.

```java
public interface InitializingBean {
  void afterPropertiesSet() throws Exception;
}

public interface DisposableBean {
  void destroy() throws Exception;
}
```

빈 객체가 InitializingBean 인터페이스를 구현하면 스프링 컨테이너는 초기화 과정에서 빈 객체의 afterPropertiesSet() 메서드를 실행한다. **빈 객체를 생성한 뒤에 초기화 과정이 필요하면 InitializingBean 인터페이스의afterPropertiesSet() 메서드를 알맞게 구현**하면 된다.

스프링 컨테이너는 빈 객체가 DisposableBean 인터페이스를 구현한 경우 소멸 과정에서 빈 객체의 destroy() 메서드를 
실행한다. **빈 객체의 소멸 과정이 필요하면 DisposableBean 인터페이스의 destroy() 메서드를 알맞게 구현**하면 된다.

초기화와 소멸 과정이 필요한 예로 데이터베이스 커넥션 풀이 있다. 커넥션 풀을 위한 빈 객체는 **초기화 과정에서 데이터베이스 연결을 생성**한다. 컨테이너를 사용하는 동안 연결을 유지하고 빈 객체를 소멸할 때 사용중인 데이터베이스 연결을 끊어야 한다.

InitializingBean 인터페이스와 DisposableBean 인터페이스를 구현한 간단한 클래스를 통해서 실제로 초기화 메서드와 소멸 메서드가 언제 실행되는지 확인해보자.

> *Client.java*

```java
package io.wisoft.daewon.spring;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class Client implements InitializingBean, DisposableBean {
  
  private String host;
  
  public void setHost(final String host) {
    this.host = host;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    System.out.println("Client.afterPropertiesSet() 실행");
  }

  public void send() {
    System.out.println("Client.send() to " + host);
  }
  
  @Override
  public void destroy() throws Exception {
    System.out.println("Client.destroy() 실행");
  }
  
}
```

각 메서드는 실행되는 순서를 확인하기 위해 콘솔에 관련 메시지를 출력하도록 구현했다. 

Client 클래스를 위한 설정클래스는 다음과 같다.

> *AppCtx.java*

```java
package io.wisoft.daewon.config;

import io.wisoft.daewon.spring.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppCtx {
  
  @Bean
  public Client client() {
    Client client = new Client();
    client.setHost("host");
    return client;
  }

}
```

이제 AppCtx를 이용해서 스프링 컨테이너를 생성하고 Client 빈 객체를 구해 사용하는 코드를 다음과 같이 작성하자.

> *Main.java*

```java
package io.wisoft.daewon.main;

import io.wisoft.daewon.config.AppCtx;
import io.wisoft.daewon.spring.Client;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public class Main {

  public static void main(String... args) {
    AbstractApplicationContext ctx = new AnnotationConfigApplicationContext(AppCtx.class);

    Client client = ctx.getBean(Client.class);
    client.send();

    ctx.close();
  }

}
```

실행 결과를 보면 다음과 같다.

```
> Task :Main.main()
Client.afterPropertiesSet() 실행
Client.send() to host
Client.destroy() 실행
```

먼저 afterPropertiesSet() 메서드를 실행했다. 즉 **스프링 컨테이너는 빈 객체 생성을 마무리한 뒤에 초기화 메서드를 실행**한다. 가장 마지막에 destroy() 메서드를 실행했다. 이 메서드는 스프링 컨테이너를 종료하면 호출되는 것을 알 수 있다.
`ctx.close()`코드가 없다면 컨테이너의 종료 과정을 수행하지 않기 때문에 빈 객체의 소멸 과정도 실행되지 않는다.



### 빈 객체의 초기화와 소멸: 커스텀 메서드

모든 클래스가 InitializingBean, DisposableBean 인터페이스를 구현할 수 있는 것은 아니다. 직접 구현한 클래스가 아닌
외부에서 제공받은 클래스를 스프링 빈 객체로 설정하고 싶을  때도 있다. 이 경우 소스 코드를 받지 않았다면 두 인터페이스를
구현하도록 수정할 수 없다. 이렇게 InitializingBean, Disposable 인터페이스를 구현할 수 없거나 이 두 인터페이스를 사용하고 싶지 않은 경우에는 스프링 설정에서 직접 메서드를 지정할 수 있다.

방법은 @Bean 태그에서 initMethod 속성과 destroyMethod 속성을 사용해서 초기화 메서드와 소멸 메서드의 이름을 
지정하면 된다.

> *Client2.java*

```java
package io.wisoft.daewon.spring;

public class Client2 {
  
  private String host;
  
  public void setHost(final String host) {
    this.host = host;
  }
  
  public void connect() {
    System.out.println("Client2.connect() 실행");
  }
  
  public void send() {
    System.out.println("Client2.send to " + host);
  }
  
  public void close() {
    System.out.println("Client2.close() 실행");
  }
  
}
```

Client2 클래스를 빈으로 사용하려면 초기화 과정에서 connect() 메서드를 실행하고 소멸 과정에서 close() 메서드를 실행해야 한다면 다음과 같이 @Bean 애노테이션의 **initMethod**와 **destroyMethod** 속성에 메서드 이름을 지정하면 된다.

```java
  @Bean(initMethod = "connect", destroyMethod = "close")
  public Client2 client2() {
    Client2 client = new Client2();
    client.setHost("host");
    return client;
  }
```

실행 해보면 다음과 같이 Client2 빈 객체를 위한 초기화 메서드와 소멸 메서드가 실행된 것을 알 수 있다.

```java
Client.afterPropertiesSet() 실행
Client2.connect() 실행
Client.send() to host
Client2.close() 실행
Client.destroy() 실행
```

설정 클래스 자체는 자바 코드이므로 initMethod 속성을 사용하는 대신 다음과 같이 빈 설정 메서드에서 직접 초기화를 수행해도 된다.

```java
  @Bean(destroyMethod = "close")
  public Client2 client2() {
    Client2 client = new Client2();
    client.setHost("host");
    client.connect();
    return client;
  }
```

설정 코드에서 초기화를 직접 실행할 때 주의할 점은 초기화 메서드가 두 번 불리지 않도록 하는 것이다. 다음을 보자.

```java
  @Bean
  public Client client() {
    Client client = new Client();
    client.setHost("host");
    client.afterPropertiesSet();
    return client;
  }
```

Clinet 클래스는 InitializingBean 인터페이스를 구현했기 때문에 스프링 컨테이너는 빈 객체 생성 이후 afterPropertiesSet() 메서드를 실행하여 두 번 호출되게 된다. 이런 일이 발생하지 않도록 주의하여야 한다.

> InitMethod 속성과 destroyMethod 속성에 지정한 메서드는 파라미터가 없어야 한다. 이 두 속성에 지정한 메서드에 파라미터가 존재할 경우 스프링 컨테이너는 익셉션을 발생시킨다.



## 빈 객체의 생성과 관리 범위

2장에서 스프링 컨테이너는 빈 객체를 한 개만 생성한다고 했다. 예를  들어 아래 코드와 같이 동일한 이름을 갖는 빈 객체를 구하면 client1과 client2는 동일한 빈 객체를 참조한다고 했었다.

```java
Client1 client1 = ctx.getBean("client", Client.class);
Client2 client2 = ctx.getBean("client", Client.class);
//client1 == client2 ==> true
```

이렇게 한 식별자에 대해 한 개의 객체만 존재하는 빈은 싱글톤<sup>singleton</sup> 범위<sup>scope</sup>를 갖는다. **별도 설정을 하지 않으면 빈은 싱글톤 범위를 갖는다.**

사용 빈도가 낮긴 하지만 프로토 타입 범위의 빈을 설정할 수도 있다. 빈의 범위를 프로토타입으로 지정하면 빈 객체를 구할 때마다 매번 새로운 객체를 생성한다. 예를 들어 위의 예제에서 프로토타입 범위로 빈을 생성했다면 getBean() 메서드는 매번
새로운 객체를 생성하여 리턴하기 때문에 두 객체는 서로 다른 객체가 된다.

특정 빈을 프로토타입 범위로 지정하려면 다음과 같이 값으로 "prototype"을 갖는 **@Scope 애노테이션**을 @Bean 애노테이션과  함께 사용하면 된다.

> *AppCtxWithPrototype.java*

```java
...
  @Bean
  @Scope("prototype")
  public Client client() {
    Client client = new Client();
    client.setHost("host");
    return client;
  }
...
```

만약 싱글톤 범위를 명시적으로 지정하고 싶다면 @Scope 애노테이션 값으로 "singleton"을 주면 된다.

프로토타입 범위를 갖는 빈은 완전한 라이프사이클을 따르지 않는다는 점에 주의해야 한다. 스프링 컨테이너는 프로토타입의 빈 객체를 생성하고 프로퍼티를 설정하고 초기화하는 작업까지는 수행하지만, 컨테이너를 종료한다고 해서 생성한 프로토타입 빈 객체의 소멸 메서드를 실행하지는 않는다. 따라서 프로토타입 범위의 빈을 사용할 때는 빈 객체의 소멸 처리를 직접 해야 한다.