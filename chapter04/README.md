# Chapter 04. 의존 자동 주입

>***이 장에서 다룰 내용***
>
>- Autowired를 이용한 의존 자동 주입

앞서 3장에서 스프링의 DI 설정에 대해 살펴봤다. 설정 클래스는 주입할 의존 대상을 생성자나 메서드를 이용해서 주입했다.

하지만 의존 대상을 설정 코드에서 직접 주입하지 않고 스프링이 자동으로 의존하는 빈 객체를 주입해주는 기능도 있다.
이를 **자동 주입**이라고 한다. 초기의 스프링에서는 의존 자동 주입에 호불호가 있었으나, 스프링 부트가 나오면서 의존 자동 주입을 사용하는 추세로 바뀌었다.

스프링에서 의존 자동 주입을 설정하려면 @Autowired 애노테이션이나 @Resource 애노테이션을 사용하면 되는데
이 책에서는 @Autowired 애노테이션의 사용 방법을 알려준다.



## @Autowired 애노테이션을 이용한 의존 자동 주입

자동 주입 기능을 사용하면 스프링이 알아서 의존 객체를 찾아서 주입한다. 예를 들어 자동 주입을 사용하면 설정에 의존 객체를 명시하지 않아도 스프링이 필요한 의존 빈 객체를 찾아서 주입해준다.

자동 주입 기능을 사용하는 것은 매우 간단한데, 의존을 주입할 대상에 @Autowired 애노테이션을 붙이기만 하면 된다.
ChangepasswordService 클래스에 Autowired 애노테이션을 적용해보자.

> *ChangePasswordService.java*

```java
package chapter04.spring;

import org.springframework.beans.factory.annotation.Autowired;

public class ChangePasswordService {

  @Autowired
  private MemberDao memberDao;

  public void changePassword(final String email, String oldPwd, final String newPwd) {
    Member member = memberDao.selectByEmail(email);
    if (member == null) throw new MemberNotFoundException();
    member.changePassword(oldPwd, newPwd);
    memberDao.update(member);
  }

  public void setMemberDao(final MemberDao memberDao) {
    this.memberDao = memberDao;
  }

}
```

memberDao 필드에 @Autowired 애노테이션을 붙였다. @Autowired 애노테이션을 붙이면 설정 클래스에서 의존을 주입하지 않아도 된다. 필드에 @Autowired 애노테이션이 붙어있으면 스프링이 해당 타입의 빈 객체를 찾아서 필드에 할당해준다.

> *AppCtx.java*

```java
package chapter04.config;

        import chapter04.spring.ChangePasswordService;
        import chapter04.spring.MemberDao;
        import chapter04.spring.MemberRegisterService;
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;

@Configuration
public class AppCtx {

    @Bean
    public MemberDao memberDao() {
        return new MemberDao();
    }

    @Bean
    public MemberRegisterService memberRegSvc() {
        return new MemberRegisterService(memberDao());
    }

    @Bean
    public ChangePasswordService changePwdSvc() {
        return new ChangePasswordService(); // 의존을 주입하지 않아도 @Autowired가 붙은 필드에
        																		// 해당 타입의 빈 객체를 찾아서 주입한다.
    }

}
```

changePwdSvc 메서드를 보면 setMemberDao() 를 하지 않고도 스프링이 MemberDao 타입의 빈 객체를 주입해준다.

만약 @Autowired 애노테이션을 설정한 필드에 알맞은 빈 객체가 주입되지 않았다면 ChangePasswordService의 memberDao 필드는 null일 것이다. 그러면 암호 변경 기능을 실행할 때 NPE<sup>NullPonterException</sup>가 발생하게 된다.


@Autowired 애노테이션은 메서드에도 붙일 수 있다.

> *MemberInfoPrinter.java*

```java
...
    @Autowired
  public void setMemberDao(final MemberDao memberDao) {
    this.memberDao = memberDao;
  }

  @Autowired
  public void setPrinter(final MemberPrinter printer) {
    this.printer = printer;
  }
...
```

MemberInfoPrinter 클래스의 두 세터 메서드에 @Autowired 애노테이션을 붙였다. AppCtx도 마저 수정하도록 하자.

> *AppCtx.java*

```java
...
   @Bean
  public MemberPrinter memberPrinter() {
    return new MemberPrinter();
  }

  @Bean
  public MemberInfoPrinter infoPrinter() {
    return new MemberInfoPrinter();
  }
...
```

빈 객체의 메서드에 @Autowired 애노테이션을 붙이면 스프링은 해당 메서드를 호출한다. 

이때 **메서드 파라미터 타입에 해당하는 빈 객체를 찾아 인자로 주입한다.**



이제 나머지 클래스에도 @Autowired를 설정해보자. 먼저 MemberRegisterService 클래스에 추가해보자.

> *MemberRegisterService.java*

```java
public class MemberRegisterService {

  @Autowired
  private MemberDao memberDao;

  public MemberRegisterService() {
  }
  ...
```

memberDao 필드에 @Autowired 애노테이션을 붙였고 인자 없는 기본 생성자를 추가했다.

다음은 MemberListPrinter 클래스에도 @Autowired 애노테이션을 적용하자. 이번에는 세터 메서드를 추가하고 세터 메서드에 @Autowired 애노테이션을 붙여보자. 인자가 없는 기본 생성자도 추가했다.

> *MemberListPrinter.java*

```java
...
  public MemberListPrinter() {
  }

  @Autowired
  public void setMemberDao(MemberDao memberDao) {
    this.memberDao = memberDao;
  }

  @Autowired
  public void setPrinter(MemberPrinter printer) {
    this.printer = printer;
  }
...
```

이제 스프링 빈으로 생성할 모든 클래스에 @Autowired 애노테이션을 적용했으므로 설정 클래스에서 의존을 주입하는 코드를
변경하자. 다음은 변경한 AppCtx 클래스이다.

> *AppCtx.java*

```java
...
  @Bean
  public MemberDao memberDao() { return new MemberDao(); }

  @Bean
  public MemberRegisterService memberRegSvc() {
    return new MemberRegisterService(memberDao());
  }

  @Bean
  public ChangePasswordService changePwdSvc() { return new ChangePasswordService(); }

  @Bean
  public MemberPrinter memberPrinter() { return new MemberPrinter(); }

  @Bean
  public MemberListPrinter listPrinter() { return new MemberListPrinter(); }
  
  @Bean
  public MemberInfoPrinter infoPrinter() { return new MemberInfoPrinter(); }

  @Bean
  public VersionPrinter versionPrinter() { return new VersionPrinter(); }
...
```

모든 클래스에 @Autowired 애노테이션을 적용하고 설정 클래스에서 의존을 주입하는 코드를 제거했다.
이제 MainForSpring 클래스를 다시 실행해서 모든 것이 제대로 동작하는지 확인해보도록 하자.



### 일치하는 빈이 없는 경우

@Autowired 애노테이션을 적용한 대상에 일치하는 빈이 없으면 어떻게 될까? 콘솔에는 다음과 같은 메시지가 출력된다.

```java
Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'io.wisoft.daewon.chapter04.spring.MemberDao' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {@org.springframework.beans.factory.annotation.Autowired(required=true)}
```

스프링 버전에 따라  메세지가 조금은 다르지만 대충 타입에 맞는 빈을 찾을 수 없다는 내용의 메시지가 출력된다.

반대로 @Autowired 애노테이션을 붙인 주입 대상에 일치하는 빈이 두 개 이상이면 어떻게 될까?

```java
...
@Bean
public MemberPrinter memberPrinter1() {
  return new MemberPrinter();
}

@Bean
public MemberPrinter memberPrinter2() {
  return new MemberPrinter();
}
...
```

다음과 같은 익셉션 메시지가 출력된다.

```java
Caused by: org.springframework.beans.factory.NoUniqueBeanDefinitionException: No qualifying bean of type 'io.wisoft.daewon.chapter04.spring.MemberPrinter' available: expected single matching bean but found 2: memberPrinter,memberPrinter1
```

자세히 보면 NoUniqueBeanDefinitionException인 것을 확인할 수 있는데, 이름 그대로 빈이 중복으로 정의돼 있는 것이다.

자동 주입을 하려면 해당 타입을 가진 빈이 어떤 빈인지 정확히 한정할 수 있어야 한다. 



## @Qualifier 애노테이션을 이용한 의존 객체 선택

자동 주입 가능한 빈이 두 개 이상이면 자동 주입할 빈을 지정할 수 있는 방법이 필요하다. 이 때 **@Qualifier** 애노테이션을 사용한다. @Qualifer 애노테이션을 사용하면 **자동 주입 대상 빈을 한정**할 수 있다.



@Qualifier 애노테이션은 두 위치에서 사용 가능하다.

- @Bean 애노테이션을 붙인 해당 빈의 한정 값을 지정할 때
- @Autowired 애노테이션에서 자동 주입할 빈을 한정할 때

#### 1. @Bean 애노테이션을 붙인 해당 빈의 한정 값을 지정할 때

> *AppCtx.java*

```java
...
  @Bean
  @Qualifier("printer")
  public MemberPrinter memberPrinter1() {
    return new MemberPrinter();
  }
...
```

memberPrinter1 메서드에 "printer" 값을 갖는 @Qualifier 애노테이션을 붙였다. **해당 빈의 한정 값으로 "printer"를 지정한다.**

이렇게 지정한 한정 값은 @Autowired 애노테이션에서 **자동 주입할 빈을 한정할 때 사용**한다. 

> *MemberListPrinter.java*

```java
...
    @Autowired
  @Qualifier("printer")
  public void setPrinter(MemberPrinter printer) {
    this.printer = printer;
  }

}
```

- @Autowired 애노테이션을 붙였을 때 : MemberPrinter 타입의 빈을 자동 주입한다.
- @Qualifier("printer") 애노테이션을 붙였을 때 : 한정 값이 "printer"인 빈을 

