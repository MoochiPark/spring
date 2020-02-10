# Chapter 02. 스프링 시작하기

> ***이 장에서 다룰 내용***
>
> - 스프링 프로젝트 생성
> - 간단한 스프링 예제
> - 스프링 컨테이너

*이 자료는 Jetbrain intelliJ IDE와 Gradle 기준으로 작성되었습니다.*



## 스프링 프로젝트 시작하기

스프링을 이용한 자바 프로젝트를 진행하는 과정은 다음과 같다.

- 그레이들<sup>Gradle</sup> 프로젝트 생성
- intellJ에서 그레이들 프로젝트 임포트
- 스프링에 맞는 자바 코드와 설정 파일 작성
- 실행



### 그레이들 프로젝트 생성

build.gradle 파일에 의존을 설정해주기만 하면 된다. 

- https://search.maven.org 접속
- org.springframework:spring-context 검색
- Gradle Groovy DSL 복사
- build.gradle 파일의 dependencies 항목에 추가

```javascript
plugins {
    id 'java'
}

group 'org.example'
version '1.0-SNAPSHOT'

sourceCompatibility = 1.8
compileJava.options.encoding = "UTF-8"

repositories {
    mavenCentral()
}

dependencies {
    testCompile group: 'junit', name: 'junit', version: '4.12'
    implementation 'org.springframework:spring-context:5.2.3.RELEASE'
}

```

*20.02.11 기준 최신 버전인 5.2.3 버전을 사용 했다.*



### 예제 코드 작성

이제 스프링을 이용한 프로그램을 작성해보자.  다음은 작성할 파일이다. src/main/java 디렉토리에 작성하자.

- **Greeter.java**: 콘솔에 간단한 메시지를 출력하는 자바 클래스
- **AppContext.java**: 스프링 설정 파일
- **Main.java**: main() 메서드를 통해 스프링과 Greeter를 실행하는 자바 클래스



> *Greeter.java*

```java
package chapter02;

public class Greeter {
  
  private String format;
  
  public String greet(final String guest) {
    return String.format(this.format, guest);
  }
  
  public void setFormat(String format) {
    this.format = format;
  }
  
}
```

> *Ex*

```java
Greeter = new Greeter();
greeter.setFormat("%s, 안녕하세요!");
String msg = greeter.greet("스프링"); // msg는 "스프링, 안녕하세요!"
```



> *AppContext.java*

```java
package chapter02;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppContext {
  
  @Bean
  public Greeter greeter() {
    Greeter g = new Greeter();
    g.setFormat("%s, 안녕하세요!");
    return g;
  }
  
}
```

스프링은 객체를 생성하고 초기화하는 기능을 제공하는데, 09~14행 코드가 한 개 객체를 생성하고 초기화하는
설정을 담고 있다. 스프링이 생성하는 객체를 **빈<sup>Bean</sup> 객체**라고 부르는데, 이 빈 객체에 대한 정보를 담고 있는 
메서드가 greeter() 메서드이다. 이 메서드에는 **@Bean 애노테이션**이 붙어 있다. @Bean 애노테이션을 메서드에 붙이면 **해당 메서드가 생성한 객체를 스프링이 관리하는 빈 객체로 등록한다.**

또한 @Bean **애노테이션을 붙인 메서드의 이름은** **빈 객체를 구분**할 때 사용한다. 예를 들어 10~14행에서 생성한
객체를 구분할 때 greeter라는 이름을 사용한다. **이 이름은 빈 객체를 참조할 때 사용된다.**

@Bean 애노테이션을 붙인 메서드는 객체를 생성하고 알맞게 초기화해야 한다.

이제 남은 건 스프링이 제공하는 클래스를 이용해서 AppContext를 읽어와 사용하는 것이다.

> *Main.java*

```java
package chapter02;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {

  public static void main(String... args) {
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppContext.class);
    Greeter g = ctx.getBean("greeter", Greeter.class);
    String msg = g.greet("스프링");
    System.out.println(msg);
    ctx.close();
  }
  
}
```

- 03 행: AnnotationConfigApplicationContext 클래스는 자바 설정에서 정보를 읽어와 빈 객체를 생성하고 관리한다.
- 08~09행: AnnotationConfigApplicationContext 객체를 생성할 때 앞서 작성한 AppContext 클래스를 생성자 파라미터로 전달한다. AnntationConfigApplicationContext는 AppContext에서 정의한 @Bean 설정 정보를 읽어와 Greeter 객체를 생성하고 초기화한다.
- 10행: getBean() 메서드는 AnnotationConfigApplicationContext가 자바 설정을 읽어와 생성한 빈 객체를 검색할 때 사용된다. getBean() 메서드의 **첫 번째 파라미터**는 @Bean 애노테이션 메서드 이름인 빈 객체의 이름이며, **두 번째 파라미터**는 검색할 빈 객체의 타입이다. 앞서 작성한 자바 설정<sup>AppContext</sup>을 보면 @Bean 메서드의 이름이 `greeter`이고 생성한 객체의 리턴 타입이 Greeter이므로, 10행의 getBean() 메서드는 greeter() 메서드가 생성한 Greeter 객체를 리턴한다.



이제는 Main 클래스를 실행해보자.

```java
오전 2:26:33: Executing task 'Main.main()'...

Starting Gradle Daemon...
Gradle Daemon started in 4 s 542 ms
> Task :compileJava UP-TO-DATE
> Task :processResources NO-SOURCE
> Task :classes UP-TO-DATE

> Task :Main.main()
스프링, 안녕하세요!

BUILD SUCCESSFUL in 10s
2 actionable tasks: 1 executed, 1 up-to-date
오전 2:26:48: Task execution finished 'Main.main()'.
```

*만약 한글 깨짐이 발생하는 경우 상단의 <u>H</u>elp -> Edit Custom VM Options... -> `-Dfile.encoding=UTF-8`를 추가.*



## 스프링은 객체 컨테이너

간단한 코드를 작성하고 실행해봤다. 위 코드에서 핵심은 AnnotationConfigApplicationContext 클래스이다. 
스프링의 핵심 기능은 **객체를 생성하고 초기화 하는 것이다.** 이와 관련된 기능은 ApplicationContext라는 인터페이스에 정의되어 있다. AnnotationConfigApplicationContext 클래스는 자바 클래스에서 정보를 읽어와 객체 생성과 초기화를 수행한다. *XML 파일이나 그루비 설정 코드를 이용해서 객체 생성/초기화를 할 수도 있다.*

> *그레이들의 의존 다이어그램 일부*

![image](https://user-images.githubusercontent.com/43429667/74174306-09f39900-4c77-11ea-83aa-222451b5af30.png)

계층도를 보면 최상위에 *BeanFactory* 인터페이스가 위치하고, 위에서 세 번째에 *ApplicationContext* 인터페이스,
그리고 가장 하단에 AnntationConfigApplicationContext 등의 구현 클래스가 위치한다. 

*BeanFactory* 인터페이스는 객체 생성과 검색에 대한 기능을 정의한다. 예를 들어 생성된 객체를 검색하는데 필요한 getBean() 메서드가 *BeanFactory*에 정의되어 있다. 객체를 검색하는 것 이외에 싱글톤/프로토타입 빈인지 확인하는 기능도 제공한다. (6장에서 설명)

*ApplcationContext* 인터페이스는 메시지, 프로필/환경 변수 등을 처리할 수 있는 기능을 추가로 정의한다. 이에 관한 내용은 진행하면서 살펴볼 예정이다.

앞서 예제에서 사용한 AnntationConfigApplicationContext를 비롯해 가장 하단에 위치한 세 개의 클래스는 *BeanFactory*와 *ApplicationContext*에 정의된 기능의 구현을 제공한다. 차이점을 비교해보면,

- **AnnotationConfigApplicationContext**: 자바 애노테이션을 이용해 클래스에서 객체 설정 정보를 가져온다.
- **GenericXmlApplicationContext**: XML로부터 객체 설정 정보를 가져온다.
- **GenericGroovyApplicationContext**: 그루비 코드를 이용해 설정 정보를 가져온다.



어떤 구현 클래스를 사용하든, 각 구현 클래스는 설정 정보로부터 빈<sup>Bean</sup>이라고 불리는 객체를 생성하고 그 객체를 내부에 보관한다. 그리고 getBean() 메서드를 실행하면 해당하는 빈 객체를 제공한다. 예를 들어 앞서 작성한 Main.java 코드를 보면 다음과 같이 빈 객체를 생성하고 제공한다.

```java
//1. 설정 정보를 이용해서 빈 객체를 생성한다.
AnnotationConfigApplicationContext ctx =
  new AnnotaitionConfigApplicationContext(AppContext.class);

//2. 빈 객체를 제공한다.
Greeter g = ctx.getBean("greeter", Greeter.class);
```

ApplicationContext(또는 BeanFactory)는 빈 객체의 생성, 초기화, 보관, 제거 등을 관리하고 있어서 ApplicationContext를 **컨테이너<sup>Container</sup>**라고도 부른다.  

이 책에서도 **ApplicationContext나 BeanFactory 등을 스프링 컨테이너**라고 표현할 것이다.



> **스프링 컨테이너의 빈 객체 관리**
>
> 스프링 컨테이너는 내부적으로 빈 객체와 빈 이름을 연결하는 정보를 갖는다. 예를 들어 위의 예제처럼
> chapter02.Greeter 타입의 객체를 greeter라는 이름의 빈으로 설정했다고 하자. 이 경우 컨테이너는 
> 다음 그림처럼 greeter 이름과 Greeter 객체를 연결한 정보를 관리한다.
>
> ![image](https://user-images.githubusercontent.com/43429667/74175740-be8eba00-4c79-11ea-8fec-c6a2e0c7bc93.png)이름과 실제 객체의 관계뿐만 아니라 실제 객체의 생성, 초기화, 의존 주입 등 스프링 컨테이너는 
> 객체 관리를 위한 다양한 기능을 제공한다. 



### 싱글톤<sup>Singleton</sup> 객체

> *Main2.java*

```java
package chapter02;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main2 {

  public static void main(String... args) {
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppContext.class);
    Greeter g1 = ctx.getBean("greeter", Greeter.class);
    Greeter g2 = ctx.getBean("greeter", Greeter.class);

    System.out.println("(g1 == g2) = " + (g1 == g2));
    ctx.close();
  }
  
}
```

g1과 g2가 같은 객체인지 여부를 콘솔에 출력해보자. 결과가 true로 출력되는 것을 확인할 수 있다.

**즉 getBean() 메서드는 같은 객체를 리턴하는 것이다.**

별도 설정을 하지 않을 경우 스프링은 한 개의 빈 객체만을 생성하며, 이때 빈 객체는 '싱글톤 범위를 갖는다'고 표현한다. 스프링은 기본적으로 한 개의 @Bean 애노테이션에 대해 한 개의 빈 객체를 생성한다. 

따라서 다음과 같은 코드에서는 두 개의 빈 객체가 생성된다.

```java
@Bean
public Greeter greeter() {
  Greeter g = new Greeter();
  g.setFormat("%s, 안녕하세요!");
  return g;
}
```

```java
@Bean
public Greeter greeter1() {
  Greeter g= new Greeter();
   g.setFormat("안녕하세요, %s님!");
  return g;
}
```

싱글톤 범위 이외에 프로토타입 범위도 존재한다. 이에 관한 내용은 6장에서 배워보자.