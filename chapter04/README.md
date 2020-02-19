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
- @Qualifier("printer") 애노테이션을 붙였을 때 : 한정 값이 "printer"인 빈을 의존 주입 후보로 사용한다.

@Autowired 애노테이션을 필드와 메서드에 모두 적용할 수 있으므로 @Qualifier 애노테이션도 필드와 메서드에 적용할 수 있다.



### 빈 이름과 기본 한정자

빈 설정에 @Qualifier 애노테이션이 없으면 **빈의 이름을 한정자로 지정**한다.

```java
@Configuration
public class AppCtx2 {
  
  @Bean
  public MemberPrinter printer() {
    return new MemberPrinter();
  }
  
  @Bean
  @Qualifier("mprinter")
  public MemberPrinter printer2() {
    return new MemberPrinter();
  }
  
  @Bean
  public MemberInfoPrinter2 infoPrinter() {
		return new MemberInfoPrinter2();
  }
  
}
```

여기서 printer() 메서드로 정의한 빈의 한정자는 빈 이름인 "printer"가 된다. printer2 빈은 @Qualifier 애노테이션 값인
"mprinter"가 한정자가 된다.

@Autowired 애노테이션도 @Qualifier 애노테이션이 없으면 **필드나 파라미터 이름을 한정자로 사용**한다. 

```java
public class MemberInfoPrinter2 {
  
  @Autowired
  private MemberPrinter printer;
...
```

여기선 필드 이름인 printer를 한정자로 사용한다.

|   빈 이름   | @Qualifier |   한정자    |
| :---------: | :--------: | :---------: |
|   printer   |            |   printer   |
|  printer2   |  mprinter  |  mprinter   |
| infoPrinter |            | infoPrinter |



## 상위/하위 타입 관계와 자동 주입

다음 클래스는 MemberPrinter 클래스를 상속한 MemberSummaryPrinter 클래스이다.

> *MemberSummaryPrinter.java*

```java
package io.wisoft.daewon.chapter04.spring;

public class MemberSummaryPrinter extends MemberPrinter {

  @Override
  public void print(Member member) {
    System.out.printf("화원 정보: 이메일=%s, 이름=%s\n",
        member.getEmail(), member.getName());
  }
  
}
```

AppCtx 클래스 설정에서 memberPrinter2() 메서드가  MemberSummaryPritner 타입의 빈 객체를 설정하도록
변경하자. 그리고 @Qualifier 애노테이션도 삭제한다.

> *AppCtx.java*

```java
  ...
	@Bean
  public MemberPrinter memberPrinter1() {
    return new MemberPrinter();
  }

  @Bean
  public MemberSummaryPrinter memberPrinter2() {
    return new MemberSummaryPrinter();
  }
...
```

그리고 Member(List, Info)Printer 클래스의 세터 메서드에 붙인 @Qualifier 애노테이션도 삭제한다. 

그런 다음 MainForSpring을 실행하면 MemberPrinter 타입 빈을 두 개 설정하고 @Qualifier 애노테이션을 붙이지 
않았을 때와 동일한 익셉션이 발생할 것이다.

그 이유는 MemberSummaryPrinter 클래스가 MemberPrinter 클래스를 상속하여 MemberPrinter 타입에도 할당 당할 수 있기 때문에 스프링 컨테이너는 MemberPrinter 타입 빈을 자동 주입해야 하는 @Autowired를 만나게 되면 memberPrinter1 빈과 memberPrinter2 타입 빈 중에서 어떤 빈을 주입해야 할지 알 수 없다.

때문에 어떤 타입의 빈을 주입할지 결정해야 한다. 이 경우 두 가지 방법이 있다. 

1. @Qualifier 애노테이션을 사용하는 방법
2. MemberSummaryPrinter를 사용하는 프린터의 세터 메서드의 파라미터 타입을 변경하는 방법

> *1. AppCtx.java*

```java
...
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
...
```

> *1. MemberInfoPrinter.java*

```java
...
  @Autowired
  @Qualifier("printer")
  public void setPrinter(final MemberPrinter printer) {
    this.printer = printer;
  }
...
```

> *1. MemberListPrinter.java*

```java
...
  @Autowired
  @Qualifier("summaryPrinter")
  public void setPrinter(MemberPrinter printer) {
    this.printer = printer;
  }
...
```



두 번째 방법은 MemberListPrinter가 MemberSummaryPrinter를 사용하도록 수정하는 것이다. MemberSummaryPrinter 타입 빈은 한 개만 존재하므로 MemberSummaryPrinter 빈을 자동 주입 받도록 코드를 
수정하면 자동 주입할 대상이 두 개 이상일 때 발생하는 문제를 피할 수 있다.

> *2. MemberListPrinter.java*

```java
...
  @Autowired
  public void setPrinter(MemberSummaryPrinter printer) {
    this.printer = printer;
  }
...
```



## @Autowired 애노테이션의 필수 여부

우선 MemberPrinter 코드를 다음과 같이 바꿔보자.

> *MemberPrinter.java*

```java
public class MemberPrinter {
  
  private DateTimeFormatter dateTimeFormatter;

  public void print(final Member member) {
    if (dateTimeFormatter == null) {
      System.out.printf(
          "회원 정보: 아이디=%d, 이메일=%s, 이름=%s, 등록일=%tF\n",
          member.getId(), member.getEmail(),
          member.getName(), member.getRegisterDateTime());
    } else {
      System.out.printf(
          "회원 정보: 아이디=%d, 이메일=%s, 이름=%s, 등록일=%s\n",
          member.getId(), member.getEmail(), member.getName(), 	
        	dateTimeFormatter.format(member.getRegisterDateTime()));
    }
  }
  
  @Autowired
  public void setDateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
    this.dateTimeFormatter = dateTimeFormatter;
  }
  
}
```

 print() 메서드는 dateTimeFormatter가 null인 경우에도 알맞게 동작한다. 즉 반드시 setDateFormatter()를 통해서
의존 객체를 주입할 필요는 없다. 

그런데 @Autowired 애노테이션은 기본적으로 @Autowired 애노테이션을 붙인 타입에 해당하는 빈이 존재하지 않으면
익셉션을 발생시킨다. 

이렇게 자동 주입할 대상이 필수가 아닌 경우에는 @Autowired 애노테이션의 required 속성을 다음과 같이 false로 지정하면 된다.

> *MemberPrinter.java*

```java
...
  @Autowired(required = false)
  public void setDateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
    this.dateTimeFormatter = dateTimeFormatter;
  }
...
```

@Autowired 애노테이션의 required 속성을 false로 지정하면 매칭되는 빈이 없어도 익셉션이 발생하지 않으며 자동 주입을 수행하지 않는다. **즉 맞는 타입의 빈이 존재하지 않으면 익셉션을 발생하지 않고** **메서드를 실행하지 않는다**.



스프링 5 버전부터는 @Autowired 애노테이션의 required 속성을 false로 하는 대신에 다음과 같이 의존 주입 대상에 
자바 8의 Optional을 사용해도 된다.

```java
...
  @Autowired
  public void setDateTimeFormatter(Optional<DateTimeFormatter> formatterOpt) {
    this.dateTimeFormatter = formatterOpt.orElse(null);
  }
...
```

자동 주입 대상 타입이 Optional인 경우, 일치하는 빈이 존재하지 않으면 값이 없는 Optional<sup>Optional.empty()</sup>을 인자로 
전달하고(익셉션이 발생하지 않음), 일치하는 빈이 존재하면 해당 빈을 값으로 갖는 Optional을 인자로 전달한다.

필수 여부를 지정하는 세 번째 방법은 @Nullable 애노테이션을 사용하는 것이다.

```java
...
  @Autowired
  public void setDateTimeFormatter(@Nullable final DateTimeFormatter dateTimeFormatter) {
    this.dateTimeFormatter = dateTimeFormatter;
  }
...
```

@Autowired 애노테이션을 붙인 세터 메서드에서 @Nullable 애노테이션을 의존 주입 대상 파라미터에 붙이면, 스프링 컨테이너는 세터 메서드를 호출할 때 자동 주입할 빈이 **존재하면 해당 빈을 인자로 전달하고, 존재하지 않으면 인자로 null**을 전달한다.

@Autowired 애노테이션의 required 속성을 false로 할 때와 차이점은 **@Nullable 애노테이션을 사용하면 자동 주입할**
**빈이 존재하지 않아도 메서드가 호출된다는 점**이다. @Autowired 애노테이션의 경우 **required 속성이 false인데 대상 빈이 존재하지 않으면 메서드를 호출하지 않는다**.

앞서 설명한 세 가지 방식은 필드에도 그대로 적용된다. 다음은 세 가지 방식을 사용한 예이다.

> *1. required 속성 사용*

```java
...
  @Autowired(required = false)
  private DateTimeFormatter dateTimeFormatter;
...
```

> *2. Optional 사용*

```java
...
  @Autowired
  private Optional<DateTimeFormatter> formatterOpt;
...
```

> *@Nullable 애노테이션 사용*

```java
...
  @Autowired
  @Nullable
  private DateTimeFormatter dateTimeFormatter;
...
```



### 생성자 초기화와 필수 여부 지정 방식 동작 이해

자동 주입 대상 필드를 기본 생성자에서 초기화한 예를 살펴보자.

> *MemberPrinter.java*

```java
...
  public MemberPrinter() {
    dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
  }
...
  @Autowired(required = false)
  public void setDateTimeFormatter(final DateTimeFormatter dateTimeFormatter) {
    this.dateTimeFormatter = dateTimeFormatter;
  }
...
```

DateTimeFormatter 타입의 빈이 존재하지 않은 상태에서 MainForSpring을 실행한 뒤 info 명령어를 사용해보자.

```java
명령어를 입력하세요:
info a
회원 정보: 아이디=1, 이메일=a, 이름=a, 등록일=2020년 02월 19일
```

기본 생성자에서 초기화한 DateTimeFormatter를 사용해서 회원의 가입 일자를 출력하는 것을 확인할 수 있다.
이를 통해 @Autowired 애노테이션의 required 속성이 false면, 매칭되는 빈이 존재하지 않을 때 기본 생성자에서 초기화한 값을 null로 바꾸지 않는다는 것을 알 수 있다.

만약 @Nullable을 사용한다면 null을 전달하므로 다음과 같이 출력된다.

```java
명령어를 입력하세요:
info a
회원 정보: 아이디=1, 이메일=a, 이름=a, 등록일=2020-02-19
```

다음과 같이 dataTimeFormatter가 null일 때의 결과가 출력된다.

일치하는 빈이 없으면 값 할당 자체를 하지 않는 @Autowired(required = false)와 달리 @Nullable 애노테이션을 사용
하면 일치하는 빈이 없을 때 null을 할당한다. 유사하게 Optional 타입은 매칭되는 빈이 없으면 값이 없는 Optional을 할당한다. 기본 생성자에서 자동 주입 대상이 되는 필드를 초기화할 때는 이 점에 유의하도록 하자.



## 자동 주입과 명시적 의존 주입 간의 관계

설정 클래스에서 의존을 주입했는데 자동 주입 대상이면 어떻게 될까? AppCtx 설정 클래스의 infoPrinter() 메서드를 변경해보자.

> *AppCtx.java*

```java
...
  @Bean
  public MemberInfoPrinter infoPrinter() {
    MemberInfoPrinter infoPrinter = new MemberInfoPrinter();
    infoPrinter.setPrinter(memberPrinter2());
    return infoPrinter;
  }
...
```

infoPrinter() 메서드는 MemberInfoPrinter.setPritner() 메서드를 호출해서 memberPrinter2 빈을 주입하고 있다.
memberPrinter2 빈은 MemberSummaryPrinter 객체이므로 이메일과 이름만 출력한다.

```java
...
  @Autowired
  @Qualifier("printer")
  public void setPrinter(final MemberPrinter printer) {
    this.printer = printer;
  }
...
```

MemberInfoPrinter.setPrinter()는 다음과 같이 @Autowired 애노테이션이 붙어 있다.

이 상태에서 MainForSpring을 실행하고 info 명령어를 실행해보자. 출력 결과를 보면 아래와 같이 회원의 전체 정보를 보여준다. 

```java
명령어를 입력하세요:
info a
회원 정보: 아이디=1, 이메일=a, 이름=a, 등록일=2020년 02월 20일
```

위와 같이 MemberInfoPrinter는 설정 클래스에서 이메일과 이름만을 출력하는 memberPrinter2 빈을 주입했지만
결과를 보면 전체 결과를 출력하는 memberPrinter1 빈을 사용해서 출력한 것을 볼 수 있다.

즉 설정 클래스에서 세터 메서드를 통해 의존을 주입해도 해당 세터 메서드에 @Autowired 애노테이션이 붙어 있으면 자동 주입을 통해 일치하는 빈을 주입한다. **따라서 @Autowired 애노테이션을 사용했다면 설정 클래스에서 객체를 주입하기보다는 스프링이 제공하는 자동 주입 기능을 사용하는 편이 좋다.**

> 세터 메서드를 통한 자동 주입이 설정 클래스에서의 주입보다 더 늦게 실행된다.