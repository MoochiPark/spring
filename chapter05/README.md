# Chapter 05. 컴포넌트 스캔

자동 주입과 함께 사용하는 추가 기능이 컴포넌트 스캔이다. 컴포넌트 스캔은 스프링이 직접 **클래스를 검색**해서 **빈으로 등록**해주는 기능이다. 설정 클래스에 빈으로 등록하지 않아도 원하는 클래스를 빈으로 등록할 수 있어 설정 코드가 크게 줄어든다.



## @Component 애노테이션으로 스캔 대상 지정

스프링이 검색해서 빈으로 등록할 수 있으려면 클래스에 **@Component 애노테이션**을 붙여야 한다. @Component 애노테이션은 해당 클래스를 **스캔 대상**으로 표시한다. 다음 클래스에 @Component 애노테이션을 붙이도록 하자.

- ChangePasswordService
- MemberDao
- MemberInfoPrinter 
- MemberListPrinter
- MemberRegisterService

MemberInfoPrinter 클래스에는 다음과 같이 애노테이션에 속성 값을 준다.

```java
...
@Component("infoPrinter")
public class MemberInfoPrinter {
  ...
```

@Component 애노테이션에 값을 주었는지에 따라 빈으로 등록할 때 사용할 이름이 결정된다. MemberDao와 ChangePasswordService처럼 값을 주지 않았을 경우에는 **클래스 이름의 첫 글자를 소문자로 바꾼 값을 빈 이름으로 사용**한다.따라서 각각 `memberDao`와 `changePasswordService`가 빈 이름으로 사용된다. 

MemberInfoPrinter처럼 **애노테이션에 값을 전달한 경우 그 값을 빈 이름으로 사용**한다. 따라서 `infoPrinter`가 빈 이름이 된다. MemberListPrinter도 다음과 같이 `listPrinter`를 빈 이름으로 설정하자.

```java
...
@Component("listPrinter")
public class MemberListPrinter {
  ...
```



## @ComponentScan 애노테이션으로 스캔 설정

@Component 애노테이션을 붙인 클래스를 스캔해서 스프링 빈으로 등록하려면 설정 클래스에 @ComponentScan 애노테이션을 적용해야 한다. 설정 클래스인 AppCtx에 @ComponentScan 애노테이션을 적용해보자.

> *AppCtx.java*

```java
@Configuration
@ComponentScan(basePackages = "io.wisoft.daewon.chapter05.spring")
public class AppCtx {
  
  @Bean
  @Qualifier("printer")
  public MemberPrinter memberPrinter1() {
    return new MemberPrinter();
  }

  @Bean
  @Qualifier("summaryPrinter")
  public MemberSummaryPrinter memberPrinter2() {
    return new MemberSummaryPrinter();
  }

  @Bean
  public VersionPrinter versionPrinter() {
    return new VersionPrinter();
  }

}
```

4장에서 작성한 AppCtx 클래스와 비교하면 스프링 컨테이너가 @Component 애노테이션을 붙인 클래스를 검색해서 빈으로 등록해주기 때문에 설정 코드가 줄어든 것을 알 수 있다.

@ComponentScan 애노테이션의 basePackages 속성값은 스캔 대상 패키지 목록을 지정한다. **스캔 대상에 해당하는 클래스 중에서 @Component 애노테이션이 붙은 클래스의 객체를 생성해서 빈으로 등록**한다.



## 예제 실행

예제 실행에 앞서 MainForSpring 클래스에서 일부 수정할 코드가 있는데, 다음과 같이 이름으로 빈을 검색하는 코드이다.

> *MainForSpring.java*

```java
// processNewCommand() 메서드
MemberRegisterService regSvc =
  ctx.getBean("memberRegSvc", MemberRegisterService.class);

// processChangeCommand() 메서드
ChangePasswordService changePwdSvc =
  ctx.getBean("changePwdSvc", ChangePasswordService.class);

// processListCommand() 메서드
MemberListPrinter listPrinter = 
  ctx.getBean("listPrinter", MemberListPrinter.class);

// processInfoCommand() 메서드
MemberInfoPrinter infoPrinter = 
  ctx.getBean("infoPrinter", MemberInfoPrinter.class);

// processVersionCommand() 메서드
ctx.getBean("versionPrinter", VersionPrinter.class).print();
```

이 중에서 MemberRegisterService 타입 빈과 ChangePasswordService 타입의 빈은 이름이 달라졌다. 따라서 이 두 타입의 빈을 구하는 코드를 다음과 같이 타입만으로 구하도록 변경하자. 

```java
MemberRegisterService regSvc = ctx.getBean(MemberRegisterService.class);
ChangePasswordService changePwdSvc = ctx.getBean(ChangePasswordService.class);
```

MemberPrinter 클래스와 MemberInfoPrinter 클래스는 @Component 애노테이션 속성값으로 빈 이름을 알맞게 지정했으므로 MainForSpring에서 빈을 구하는 코드를 수정할 필요가 없다. 

MainForSpring 클래스를 수정하고 컴포넌트 스캔으로 등록한 MemberRegisterService 타입 빈과 MemberInfoPrinter 타입 빈을 사용하는 new 명령어와 info 명령어를 실행하면 정상 동작하는 것을 알 수 있다.



## 스캔 대상에서 제외하거나 포함하기

**excludeFilters** 속성을 사용하면 스캔할 때 특정 대상을 자동 등록 대상에서 제외할 수 있다. 

> *AppCtxWithExclude.java*

```java
@Configuration
@ComponentScan(basePackages = {"io.wisoft.daewon.chapter05.spring"},
  excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, 
                                         pattern = "spring\\..*Dao"))
public class AppCtxWithExclude {
...
```

위 코드는 @Filter 애노테이션의 type 속성값으로 FilterType.REGEX를 주었다. 이는 정규표현식을 사용해서 제외 대상을 지정한다는 것을 의미한다. pattern 속성은 FilterType에 적용할 값을 설정한다. 위 설정에서는 "spring."으로 시작하고 Dao로 
끝나는 정규표현식을 지정했으므로 spring.MemberDao 클래스를 컴포넌트 스캔 대상에서 제외한다.

FilterType.ASPECTJ를 필터 타입으로 설정할 수도 있다. 이 타입을 사용하면 정규표현식 대신 AspectJ 패턴을 사용해서 대상을 지정한다. 

```java
@Configuration
@ComponentScan(basePackages = {"io.wisoft.daewon.chapter05.spring"},
  excludeFilters = @ComponentScan.Filter(type = FilterType.ASPECTJ, 
                                         pattern = "spring.*Dao"))
public class AppCtxWithExclude {
...
```

AspectJ 패턴은 정규표현식과는 조금 다른데 이는 7장에서 살펴볼 것이다. 일단 지금은 "spring.*Dao"는 spring 패키지의 
Dao로 끝나는 타입을 지정한다는 정도만 알고 넘어가자.

> AspectJ 패턴이 동작하려면 의존 대상에 aspectjweaver 모듈을 추가해야 한다.
>
> ```groovy
> dependencies {
>     testCompile group: 'junit', name: 'junit', version: '4.12'
>     implementation 'org.springframework:spring-context:5.2.3.RELEASE'
>     implementation 'org.aspectj:aspectjweaver:1.9.5'
> 
> ```



pattern 속성은 String[] 타입이므로 배열을 이용해서 패턴을 한 개 이상 지정할 수도 있다.

특정 애노테이션을 붙인 타입을 컴포넌트 대상에서 제외할 수도 있다. 예를들어 다음의 @NoProduct나 @ManualBean 애노테이션을 붙인 클래스는 컴포넌트 스캔 대상에서 제외하고 싶다고 해보자.

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NoProduct {
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ManualBean {
}
```

위 두 애노테이션을 붙인 클래스를 컴포넌트 스캔 대상에서 제외하려면 excludeFilters 속성을 설정한다.

> *AppCtxWithExclude.java*

```java
@Configuration
@ComponentScan(basePackages = {"io.wisoft.daewon.chapter05.spring"},
    excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION,
      classes = {NoProduct.class, ManualBean.class} ))
public class AppCtxWithExclude {
...
```

type 속성값으로 FilterType.ANNOTATION을 사용하면 classes 속성에 필터로 사용할 애노테이션 타입을 값으로 준다.
@ManualBean 애노테이션을 제외 대상에 추가했으므로 다음 클래스를 컴포넌트 스캔 대상에서 제외한다.

```java
@Component
@ManualBean
public class MemberDao {
...
```

특정 타입이나 그 하위 타입을 컴포넌트 스캔 대상에서 제외하려면 ASSIGNABLE_TYPE을 FilterType으로 사용하면 된다.

```java
@Configuration
@ComponentScan(basePackages = {"io.wisoft.daewon.chapter05.spring"},
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
      classes = MemberDao.class ))
public class AppCtxWithExclude {
...
```

classes 속성에는 제외할 타입 목록을 지정한다. 위 설정은 제외할 타입이 한 개이므로 배열 표기<sup>{}</sup>를 하지 않았다.

설정할 필터가 두 개 이상이면 @ComponentScan의 excludeFilters 속성에 배열을 사용해서 목록을 전달하면 된다.

```java
@Configuration
@ComponentScan(basePackages = {"io.wisoft.daewon.chapter05.spring"},
    excludeFilters = {
      @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = ManualBean.class), 
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "spring2\\..*")
    })
public class AppCtxWithExclude {
  ...
```



### 기본 스캔 대상

@Component 애노테이션을 붙인 클래스만 컴포넌트 스캔 대상에 포함되는 것은 아니다.
다음 애노테이션을 붙인 클래스가 컴포넌트 스캔 대상에 포함된다.

- @Component(org.springframework.stereotype 패키지)
- @Controller(org.springframework.stereotype 패키지)
- @Service(org.springframework.stereotype 패키지)
- @Repository(org.springframework.stereotype 패키지)
- @Aspect(org.aspectj.lang.annotation 패키지)
- @Configuration(org.springframework.context.annotation 패키지)

@Aspect 애노테이션을 제외한 나머지 애노테이션은 실제로는 @Component 애노테이션에 대한 특수 애노테이션이다.
예를들어 Controller 애노테이션은 다음과 같다.

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Controller {
  
  @AliasFor(annotation = Component.class)
  String value() default "";
  
}
```

@Component 애노테이션이 붙어 있는데, 스프링은 @Controller 애노테이션을 @Component 애노테이션과 동일하게 
컴포넌트 스캔 대상에 포함한다. @Controller 애노테이션이나 @Repository 애노테이션 등은 컴포넌트 스캔 대상이 될뿐만
아니라 스프링 프레임워크에서 특별한 기능과 연관되어 있다. 예를 들어 @Controller 애노테이션은 웹 MVC와 관련 있고, 
@Repository 애노테이션은 DB 연동과 관련 있다. 각 애노테이션의 용도는 관련 장에서 살펴보도록 하자.



## 컴포넌트 스캔에 따른 충돌 처리

컴포넌트 스캔 기능을 사용해서 자동으로 빈을 등록할 때에는 충돌에 주의해야 한다. 

- 빈 이름 충돌
- 수동 등록에 따른 충돌



### 빈 이름 충돌

spring 패키지와 spring2 패키지에 MemberRegisterService 클래스가 존재하고 두 클래스 모두 @Component 애노테이션을 붙였다고 하자. 이 상태에서 @ComponentScan을 사용하면 어떻게 될까?

```java
@Configuration
@ComponentScan(basePackages = {"spring", "spring2"})
public class AppCtx {
  ...
}
```

에러 메시지를 보면 spring2.MemberRegisterService 클래스르 빈으로 등록할 때 사용한 이름인 memberRegisterService가 타입이 일치하지 않는 spring.MemberRegisterService 타입의 빈 이름과 충돌한다는 것을 알 수 있을 것이다.

이렇게 컴포넌트 스캔 과정에서 서로 다른 타입인데 같은 빈 이름을 사용하는 경우가 있다면 둘 중 하나에 명시적으로 빈 이름을 지정해서 이름 충돌을 피해야 한다.



### 수동 등록한 빈과 충돌

이 장을 진행하면서 MemberDao 클래스에 @Component 애노테이션을 붙였다.

```java
@Component
public class MemberDao {
...
}
```

MemberDao 클래스는 컴포넌트 스캔 대상이다. 자동 등록된 빈의 이름은 "memberDao"이다. 그런데 설정 클래스에 직접 MemberDao 클래스를 같은 이름의 빈으로 등록하면 어떻게 될까?

```java
@Configuration
@ComponentScan(basePackages = {"io.wisoft.daewon.chapter05.spring"})
public class AppCtx {
  
  @Bean
  public MemberDao memberDao() {
    return new MemberDao();
  }
  ...
```

**스캔할 때 사용하는 빈 이름과 수동 등록한 빈 이름이 같은 경우** **수동 등록한 빈이 우선**한다. **즉 MemberDao 타입 빈은 AppCtx에서 정의한 한 개만 존재**한다.

그렇다면 다음과 같이 다른 이름을 사용하면 어떻게 될까?

```java
@Configuration
@ComponentScan(basePackages = {"io.wisoft.daewon.chapter05.spring"})
public class AppCtx {
  
  @Bean
  public MemberDao memberDao2() {
    return new MemberDao();
  }
  ...
```

이 경우 스캔을 통해 등록한 "memberDao" 빈과 수동 등록한 "memberDao2" 빈이 모두 존재한다. MemberDao 타입의 빈이 두 개가 생성되므로 자동 주입하는 코드는 @Qualifier 애노테이션을 사용해서 알맞은 빈을 선택해야 한다.