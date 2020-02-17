# Chapter 03. 스프링 DI

> ***이 장에서 다룰 내용***
>
> - 객체 의존과 의존 주입(DI)
> - 객체 조립
> - 스프링 DI 설정



## 의존이란?

DI는 'Dependency Injection'의 약자로 우리말로는 '의존 주입'이라고 번역한다. 이 단어의 의미를 이해하려면 먼저 '의존<sup>Dependency</sup>'이 뭔지 알아야 한다. 여기서 말하는 의존은 **객체 간의 의존**을 의미한다. 다음의 코드를 보자.

> *MemberRegisterService.java*

```java
package chapter03;

import java.time.LocalDateTime;

public class MemberRegisterService {

  private MemberDao memberDao = new MemberDao();

  public void register(RegisterRequest req) {
    // 이메일로 회원 데이터(Member) 조회
    Member member = memberDao.selectByEmail(req.getEmail());
    if (member != null) {
      // 같은 이메일을 가진 회원이 이미 존재하면 익셉션 발생
      throw new DuplicateMemberException("dup email " + req.getEmail());
    }
    // 같은 이메일을 가진 회원이 존재하지 않으면 DB에 삽입
    Member newMember = new Member(
        req.getEmail(), req.getPassword(), req.getName(), LocalDateTime.now());
    memberDao.insert(newMember);
  }

}
```

서로 다른 회원은 동일한 이메일 주소를 사용할 수 없다는 요구사항하에 이 제약사항을 처리하기 위해 이 클래스는
MemberDao 객체의 selectByEmail() 메서드를 이용해 동일한 이메일을 가진 회원 데이터가 존재하는지 확인한다.
같은 이메일을 가진 회원 데이터가 존재한다면 익셉션을 발생시키고, 존재하지 않으면 회원 정보를 담은 Member 객체를 생성하고 MemberDao 객체의 insert() 메서드를 이용해서 DB에 회원 데이터를 삽입한다.

여기서 눈여겨볼 점은 MemberRegisterService 클래스가 DB 처리를 위해 MemberDao 클래스의 메서드를 사용한다는 점이다. 회원 데이터가 존재하는지 확인하기 위해 selectByEmail() 메서드를 실행하고, 회원 데이터를 DB에  삽입하기 위해 insert() 메서드를 실행한다.

이렇게 한 클래스가 다른 클래스의 메서드를 실행할 때 이를 **'의존'**한다고 표현한다. 따라서 위의 코드에서는
**"MemberRegisterService 클래스가 MemberDao 클래스에 의존한다"**고 표현할 수 있다.

> **의존**
>
> 의존은 변경에 의해 영향을 받는 관계를 의미한다. 예를 들어 MemberDao의 insert() 메서드의 이름을 insertMember()로 변경하면 이 메서드를 사용하는 MemberRegisterService 클래스의 소스 코드도 함께 
> 변경된다. 이렇게 변경에 따른 영향이 전파되는 관계를 '의존'한다고 표현한다.



의존하는 대상이 있으면 그 대상을 구하는 방법이 필요하다. 가장 쉬운 방법은 위 코드처럼 의존 대상 객체를 직접 생성하는 것이다.

```java
public class MemberRegisterService {
  // 의존 객체를 직접 생성
  private MemberDao memberDao = new MemberDao();
  ...
}
```

MemberregisterService 클래스에서 의존하는 MemberDao 객체를 직접 생성하기 때문에 MemberRegisterService 객체를 생성하는 순간에 MemberDao 객체도 함께 생성된다.

```java
// 의존하는 MemberDao의 객체도 함께 생성
MemeberRegisterService svc = new MemberRegisterService();
```

클래스 내부에서 직접 의존 객체를 생성하는 것이 쉽긴 하지만 **유지보수 관점에서 문제점을 유발할 수 있다.**

의존 객체를 구하는 방법에는 DI와 서비스 로케이터가 있는데, 이 중 스프링과 관련된 것은 DI로서 DI를 이용해서
의존 객체를 구하는 방법에 관해 살펴보자.



## DI를 통한 의존 처리

DI(Dependency Injection, 의존 주입)는 의존하는 객체를 직접 생성하는 대신 의존 객체를 전달받는 방식을 사용한다. 예를 들어 앞서 의존 객체를 직접 생성했던 MemberRegisterService 클래스에 DI 방식을 적용해보자.

> MemberRegisterService.java

```java
package chapter03;

import java.time.LocalDateTime;

public class MemberRegisterService {

  private MemberDao memberDao;

  public MemberRegisterService(final MemberDao memberDao) {
    this.memberDao = memberDao;
  }

  public Long register(final RegisterRequest req) {
    // 이메일로 회원 데이터(Member) 조회
    Member member = memberDao.selectByEmail(req.getEmail());
    if (member != null) {
      // 같은 이메일을 가진 회원이 이미 존재하면 익셉션 발생
      throw new DuplicateMemberException("dup email " + req.getEmail());
    }
    // 같은 이메일을 가진 회원이 존재하지 않으면 DB에 삽입
    Member newMember = new Member(
        req.getEmail(), req.getPassword(), req.getName(), LocalDateTime.now());
    memberDao.insert(newMember);
    return newMember.getId();
  }

}
```

직접 의존 객체를 생성했던 코드와 달리 바뀐 코드는 의존 객체를 직접 생성하지 않는다. 대신 8~10행과 같이 생성자를 통해서 의존 객체를 전달받는다. 즉 **생성자를 통해** MemberRegisterService가 **의존<sup>Dependency</sup>**하고 있는 MemberDao **객체를 주입<sup>Injection</sup>** 받은 것이다. 의존 객체를 직접 구하지 않고 생성자를 통해서 전달받기 때문에 이 코드는 **DI<sup>의존 주입</sup> 패턴**을 따르고 있다.

DI를 적용한 MemberRegisterService 클래스를 사용하는 코드는 다음과 같이 생성자에 객체를 전달해야 한다.

```java
MemberDao dao = new MemberDao();
// 의존 객체를 생성자를 통해 주입한다.
MemberRegisterService svc = new MemberRegisterService(dao);
```

객체를 생성하는 부분의 코드가 길어지고 복잡해짐에도 DI를 하는 이유는 **변경의 유연함**에 있다.



## DI와 의존 객체 변경의 유연함

의존 객체를 직접 생성하는 방식은 필드나 생성자에서 new 연산자를 이용해서 객체를 생성한다.  
회원 등록 기능을 제공하는 MemberRegisterService 클래스에서 다음 코드처럼 의존 객체를 직접 생성할 수 있다.

```java
public class MemberRegisterService {
  private MemberDao memberDao = new MemberDao();
  ...
}
```

회원의 암호 변경 기능을 제공하는 ChangePasswordService 클래스도 다음과 같이 의존 객체를 생성한다고 하자.

```java
public class ChangePasswordService {
  private MemberDao memberDao = new MemberDao();
  ...
}
```



MemberDao 클래스는 회원 데이터를 데이터베이스에 저장한다고 가정하고, 이 상태에서 회원 데이터의 빠른 조회를 위해 캐시<sup>Cache</sup>를 적용하기위해 MemberDao 클래스를 상속받은 CachedMemberDao 클래스를 만들었다.

```java
public class CachedMemberDao extends MemberDao {
  ...
}
```

> **캐시<sup>Cache</sup>**
>
> 캐시는 데이터 값을 복사해 놓는 임시 장소를 가리킨다. 보통 조회 속도 향상을 위해 캐시를 사용한다.
> DB에 있는 데이터 중 자주 조회하는 데이터를 메모리를 사용하는 캐시에 보관하면 속도를 향상 시킬 수 있다.

이제 캐시 기능을 적용한 CachedMemberDao를 사용하려면 각 클래스의 코드를 변경해주어야 한다.



```java
public class MemberRegisterService {
  private MemberDao memberDao = new CachedMemberDao();
  ...
}
```

```java
public class ChangePasswordService {
  private MemberDao memberDao = new CachedMemberDao();
  ...
}
```

동일한 상황에서 DI를 사용하면 수정할 코드가 줄어든다. 예를들어 생성자를 통해서 의존 객체를 주입 받도록 구현했다고 한다면,

```java
MemberDao memberDao = new MemberDao();
MemberRegisterService regSvc = new MemberRegisterService(memberDao);
ChangePasswordService pwdSvc = new ChangePasswordService(memberDao);
```

이제 MemberDao 대신 CachedMemberDao를 사용하도록 수정한다면 수정해야 할 소스 코드는 한 곳뿐이다.

```java
MemberDao memberDao = new CachedMemberDao();
MemberRegisterService regSvc = new MemberRegisterService(memberDao);
ChangePasswordService pwdSvc = new ChangePasswordService(memberDao);
```

이처럼 DI를 사용하면 MemberDao 객체를 사용하는 클래스가 여러 개여도 변경할 곳은 의존 주입 대상이 되는 객체를 생성하는 코드 한 곳뿐이다. 변경할 코드가 한 곳으로 집중되는 것을 알 수 있다.



## 예제 프로젝트 만들기

- 회원 데이터 관련 클래스
  - Member
  - WrongIdPasswordException
  - MemberDao
- 회원 가입 처리 관련 클래스
  - DuplicateMemberException
  - RegisterRequest
  - MemberRegisterService
- 암호 변경 관련 클래스
  - MemberNotFoundException
  - ChangePasswordService



### 회원 데이터 관련 클래스

> *Member.java*

```java
package chapter03;

import java.time.LocalDateTime;

public class Member {

  private Long id;
  private String email;
  private String password;
  private String name;
  private LocalDateTime registerDateTime;

  public Member(final String email, final String password, final String name,
                final LocalDateTime registerDateTime) {
    this.email = email;
    this.password = password;
    this.name = name;
    this.registerDateTime = registerDateTime;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public String getName() {
    return name;
  }

  public LocalDateTime getRegisterDateTime() {
    return registerDateTime;
  }

  public void changePassword(final String oldPassword, final String newPassword) {
    if (!password.equals(oldPassword)) throw new WrongIdPasswordException();
    this.password = newPassword;
  }
  
}
```



> *WrongIdPasswordException.java*

```java
package chapter03;

public class WrongIdPasswordException extends RuntimeException {
}
```



*MemberDao 클래스는 아직 스프링을 이용해 DB를 연동하는 방법을 배우지 않았기 때문에 Map을 이용해 구현.*

> MemberDao.java

```java
package chapter03;

import java.util.HashMap;
import java.util.Map;

public class MemberDao {
  
  private static long nextId = 0;
  
  private Map<String, Member> map = new HashMap<>();
  
  public Member selectByEmail(final String email) {
    return map.get(email);
  }
  
  public void insert(final Member member) {
    member.setId(++nextId);
    map.put(member.getEmail(), member);
  }
  
  public void update(final Member member) {
    map.put(member.getEmail(), member);
  }
  
}
```



### 회원 가입 처리 관련 클래스

> *DuplbicateMemberException.java*

```java
package chapter03;

public class DuplicateMemberException extends RuntimeException {
  
  public DuplicateMemberException(final String message) {
    super(message);
  }
  
}
```



> *RegisterRequest.java*

```java
package chapter03;

public class RegisterRequest {

  private String email;
  private String password;
  private String confirmPassword;
  private String name;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getConfirmPassword() {
    return confirmPassword;
  }

  public void setConfirmPassword(String confirmPassword) {
    this.confirmPassword = confirmPassword;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isPasswordEqualTOConfirmPassword() {
    return password.equals(confirmPassword);
  }

}
```



MemberRegisterService 클래스의 소스 코드는 이미 작성했었다.

> *MemberRegisterService.java*

```java
package chapter03;

    import java.time.LocalDateTime;

public class MemberRegisterService {

  private MemberDao memberDao;

  public MemberRegisterService(final MemberDao memberDao) {
    this.memberDao = memberDao;
  }

  public Long register(final RegisterRequest req) {
    // 이메일로 회원 데이터(Member) 조회
    Member member = memberDao.selectByEmail(req.getEmail());
    if (member != null) {
      // 같은 이메일을 가진 회원이 이미 존재하면 익셉션 발생
      throw new DuplicateMemberException("dup email " + req.getEmail());
    }
    // 같은 이메일을 가진 회원이 존재하지 않으면 DB에 삽입
    Member newMember = new Member(
        req.getEmail(), req.getPassword(), req.getName(), LocalDateTime.now());
    memberDao.insert(newMember);
    return newMember.getId();
  }

}
```



### 암호 변경 관련 클래스

> *ChangePasswordService.java*

```java
package chapter03;

public class ChangePasswordService {

  private MemberDao memberDao;

  public void chagnePassword(final String email, String oldPwd, final String newPwd) {
    Member member = memberDao.selectByEmail(email);
    if (member == null) throw new MemberNotFoundException();
    member.changePassword(oldPwd, newPwd);
    memberDao.update(member);
  }

  public void setMemberDao(MemberDao memberDao) {
    this.memberDao = memberDao;
  }

}
```

email에 해당하는 Member가 존재하지 않으면 익셉션을 발생시키고 존재하면 member.chagePassword()를 이용해서 암호를 변경하고 memberDao.update()를 이용해 변경된 데이터를 보관한다.

여기선 setter 메서드로 의존하는 MemberDao를 전달받는다. 즉 의존 객체를 주입받는다.

> *MemberNotFoundException.java*

```java
package chapter03;

public class MemberNotFoundException extends RuntimeException {
}
```



## 객체 조립기

실행에 필요한 코드는 작성했지만 스프링으로 넘어갈 단계는 아니다. 그 전에 **조립기<sup>assembler</sup>**에 대해 알아보자.

앞서 DI를 설명할 때 객체 생성에 사용할 클래스를 변경하기 위해 그 객체를 사용하는 코드를 변경하지 않고 객체를 주입하는 코드 한 곳만 변경하면 된다고 했다. 쉽게 생각하면 다음과 같이 메인 메서드에서 객체를 생성하면 될 것  같다.

> *Main.java*

```java
package chapter03;

public class Main {

  public static void main(String... args) {
    MemberDao memberDao = new MemberDao();
    MemberRegisterService regSvc = new MemberRegisterService(memberDao);
    ChangePasswordService pwdSvc = new ChangePasswordService();
    pwdSvc.setMemberDao(memberDao);
    ... // regSvc와 pwdSvc를 사용하는 코드
  }
  
}
```

main 메서드에서 의존 대상 객체를 생성하고 주입하는 방법이 나쁜 것은 아니다. 이 방법보다 좀 더 나은 방법은
객체를 생성하고 의존 객체를 주입해주는 클래스를 따로 작성하는 것이다. 의존 객체를 주입한다는 것은 서로 다른 두 객체를 조립한다고 생각할 수 있는데, 이런 의미에서 이 클래스를 **조립기**라고도 표현한다.

> *Assembler.java*

```java
package chapter03.assembler;

import chapter03.spring.ChangePasswordService;
import chapter03.spring.MemberDao;
import chapter03.spring.MemberRegisterService;

public class Assembler {
  
  private MemberDao memberDao;
  private MemberRegisterService regSvc;
  private ChangePasswordService pwdSvc;
  
  public Assembler() {
    memberDao = new MemberDao();
    regSvc = new MemberRegisterService(memberDao);
    pwdSvc = new ChangePasswordService();
    pwdSvc.setMemberDao(memberDao);
  }
  
  public MemberDao getMemberDao() {
    return memberDao;
  }
  
  public MemberRegisterService getMemberRegisterService() {
    return regSvc;
  }
  
  public ChangePasswordService getChangePasswordService() {
    return pwdSvc;
  }
  
}
```

생성자 부분에서 MemberRegisterService는 생성자를 통해 MemberDao 객체를 주입받고, ChangePassword는 세터를 통해 주입받는다. 

Assembler 클래스를 사용하는 코드는 다음처럼 Assembler 객체를 생성한 뒤, get 메서드를 이용해 필요한 객체를 구하여 사용한다.

```java
Assembler assembler = new Assembler();
ChangePasswordService changePwdSvc = 
  											assembler.getChangePasswordService();
changePwdSvc.changePassword("daewon@wisoft.io", "oldpwd", "newpwd")
```

MemberDao 클래스가 아니라 CachedMemberDao 클래스를 사용해야 한다면 Assembler에서 객체를 초기화하는 코드만 변경하면 된다.

```java
  public Assembler() {
    memberDao = new CachedMemberDao();
    regSvc = new MemberRegisterService(memberDao);
    pwdSvc = new ChangePasswordService();
    pwdSvc.setMemberDao(memberDao);
  }
```

정리하자면 조립기는 객체를 생성하고 의존 객체를 주입하는 기능을 제공한다. 또한 특정 객체가 필요한 곳에 객체를 제공한다. 



### 조립기 사용 예제

이제 조립기를 사용하는 메인 클래스를 작성해보자. 콘솔에서 명령어를 입력받고 각 명령에 알맞은 기능을 수행하도록 구현할 것이다. 처리할 명령어는 다음 두 가지이다.

- **new**: 새로운 회원 데이터를 추가한다.

- **change**: 회원 데이터의 암호를 변경한다.

  각 명령어는 앞에서 만든 두 클래스<sup>MemberRegisterService, ChangePasswordService</sup>를 이용해서 처리할 것이다.																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																					

작성할 메인 클래스의 코드가 다소 길기 때문에 나눠서 살펴보겠다. 처음 살펴볼 코드는 콘솔에서 명령어를 입력받아 알맞은 기능을 실행하는 부분이다. 

> *MainForAssembler.java (메인 메서드 부분)*

```java
package chapter03.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import chapter03.assembler.Assembler;
import chapter03.spring.*;

public class MainForAssembler {

  public static void main(String... args) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    while (true) {
      System.out.println("명령어를 입력하세요:");
      String command = reader.readLine();
      if (command.equalsIgnoreCase("exit")) {
        System.out.println("종료합니다.");
        break;
      }
      if (command.startsWith("new ")) {
        processNewCommand(command.split(" "));
        continue;
      } else if (command.startsWith("change ")) {
        processChangeCommand(command.split(" "));
        continue;
      }
      printHelp();
    }
  }
```

- 입력한 문자열이 "new "로 시작하면 processNewCommand() 메서드를 실행한다.
- 입력한 문자열이 "change "로 시작하면 processChangeCommand() 메서드를 실행한다.
- 명령어를 잘못 입력한 경우 도움말을 출력해주는 printHelp() 메서드를 실행한다.

- 22, 25행: "new a@a.com name pwd pwd" -> {"new", "a@a.com", "name",  "pwd", "pwd"}



> *MainForAssembler.java (Assembler를 사용하는 코드)*

```java

  private static Assembler assembler = new Assembler();

  private static void processNewCommand(final String... arg) {
    if (arg.length != 5) {
      printHelp();
      return;
    }
    MemberRegisterService regSvc = assembler.getMemberRegisterService();
    RegisterRequest req = new RegisterRequest();
    req.setEmail(arg[1]);
    req.setName(arg[2]);
    req.setPassword(arg[3]);
    req.setConfirmPassword(arg[4]);

    if (!req.isPasswordEqualTOConfirmPassword()) {
      System.out.println("암호의 확인이 일치하지 않습니다.\n");
      return;
    }
    try {
      regSvc.regist(req);
      System.out.println("등록했습니다.\n");
    } catch (DuplicateMemberException e) {
      System.out.println("이미 존재하는 이메일입니다.\n");
    }
  }

  private static void processChangeCommand(final String... arg) {
    if (arg.length != 4) {
      printHelp();
      return;
    }
    ChangePasswordService changePwdSvc = assembler.getChangePasswordService();
    try {
      changePwdSvc.chagnePassword(arg[1], arg[2], arg[3]);
      System.out.println("암호를 변경했습니다.\n");
    } catch (MemberNotFoundException e) {
      System.out.println("존재하지 않는 이메일입니다.\n");
    } catch (WrongIdPasswordException e) {
      System.out.println("이메일과 암호가 일치하지 않습니다.\n");
    }
  }

  private static void printHelp() {
    System.out.println();
    System.out.println("잘못된 명령입니다. 아래 명령어 사용법을 확인하세요.");
    System.out.println("명령어 사용법:");
    System.out.println("new 이메일 이름 암호 암호확인");
    System.out.println("change 이메일 현재비번 변경비번");
    System.out.println();
  }

}
```

- Assembler 클래스의 생성자에서 필요한 객체를 생성하고 의존을 주입한다.

  2행에서 객체를 생성하는 시점에 사용할 객체가 모두 생성된다.



모두 작성했으니 MainForAssembler에서 Run하여 new, change 명령어들을 테스트 해보자.



## 스프링의 DI 설정

지금까지 의존이 무엇이고 DI를 이용해 의존 객체를 주입하는 방법에 대해 알아봤다. 그리고 객체를 생성하고 의존 주입을 이용해서 객체를 서로 연결해주는 조립기에 대해서 살펴봤다. 이 이유는 스프링이 DI를 지원하는 조립기이기 때문이다. 

실제로 스프링은 앞서 구현한 조립기와 유사한 기능을 제공한다. 

- 스프링은 Assembler 클래스의 생성자 코드처럼 필요한 객체를 생성하고 생성한 객체에 의존을 주입한다.
- 또한 Assembler.getMemberRegisterService() 메서드처럼 객체를 제공하는 기능을 정의하고 있다.
- 둘의 차이점은 Assembler는 특정 타입의 클래스만 생성한 반면 스프링은 범용 조립기이다.



### 스프링을 이용한 객체 조립과 사용

앞서 구현했던 Assembler 대신 스프링을 사용하는 코드를 작성해보자. 

스프링을 사용하려면 먼저 어떤 객체를 생성하고, 의존을 어떻게 주입할지를 정의한 설정 정보를 작성해야 한다.

> *AppCtx.java*

```java
package chapter03.config;

import chapter03.spring.ChangePasswordService;
import chapter03.spring.MemberDao;
import chapter03.spring.MemberRegisterService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppCtx {

  @Bean
  public MemberDao memberDao() {
    return new MemberDao();
  }

  @Bean
  public MemberRegisterService memberRegisterService() {
    return new MemberRegisterService(memberDao());
  }

  @Bean
  ChangePasswordService changePasswordService() {
    ChangePasswordService changePasswordService = new ChangePasswordService();
    changePasswordService.setMemberDao(memberDao());
    return changePasswordService;
  }

}
```

- **@Configuration:** 스프링 설정 클래스를 의미한다. 이 애노테이션을 붙여야 스프링 설정 클래스로 사용한다.
- **@Bean:** 해당 메서드가 생성한 객체를 스프링 빈이라고 설정한다. 



이제 설정 클래스를 이용해서 컨테이너를 생성해야 한다. 2장에서 배운 AnnotationConfigApplicationContext 
클래스를 이용해서 스프링 컨테이너를 생성할 수 있다.

​		`ApplicationContext ctx = new AnnotationConfigApplicationContext(AppCtx.class);`

컨테이너를 생성하면 getBean() 메서드를 이용해서 사용할 객체를 구할 수 있다.

```java
MemberRegisterService regSvc = 
  ctx.getBean("memberRegSvc", MemberRegisterService.class)
```

위 코드는 스프링 컨테이너(ctx)로부터 이름이 "memberRegSvc"인 빈 객체를 구한다.

이제 Assembler 클래스를 이용해서 작성한 MainForAssembler를 스프링 컨테이너를 사용하도록 변경하자.

> *MainForSpring.java*

```java
package chapter03.main;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import chapter03.config.AppCtx;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import chapter03.spring.*;

public class MainForSpring {

  private static ApplicationContext ctx = null;

  public static void main(String... args) throws IOException {
    ctx = new AnnotationConfigApplicationContext(AppCtx.class);

    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    while (true) {
      System.out.println("명령어를 입력하세요:");
      String command = reader.readLine();
      if (command.equalsIgnoreCase("exit")) {
        System.out.println("종료합니다.");
        break;
      }
      if (command.startsWith("new ")) {
        processNewCommand(command.split(" "));
        continue;
      } else if (command.startsWith("change ")) {
        processChangeCommand(command.split(" "));
        continue;
      }
      printHelp();
    }
  }

  private static void processNewCommand(String... arg) {
    if (arg.length != 5) {
      printHelp();
      return;
    }
    MemberRegisterService regSvc =
        ctx.getBean("memberRegSvc", MemberRegisterService.class);
    RegisterRequest req = new RegisterRequest();
    req.setEmail(arg[1]);
    req.setName(arg[2]);
    req.setPassword(arg[3]);
    req.setConfirmPassword(arg[4]);

    if (!req.isPasswordEqualToConfirmPassword()) {
      System.out.println("암호와 확인이 일치하지 않습니다.\n");
      return;
    }
    try {
      regSvc.regist(req);
      System.out.println("등록했습니다.\n");
    } catch (DuplicateMemberException e) {
      System.out.println("이미 존재하는 이메일입니다.\n");
    }
  }

  private static void processChangeCommand(String... arg) {
    if (arg.length != 4) {
      printHelp();
      return;
    }
    ChangePasswordService changePwdSvc =
        ctx.getBean("changePwdSvc", ChangePasswordService.class);
    try {
      changePwdSvc.changePassword(arg[1], arg[2], arg[3]);
      System.out.println("암호를 변경했습니다.\n");
    } catch (MemberNotFoundException e) {
      System.out.println("존재하지 않는 이메일입니다.\n");
    } catch (WrongIdPasswordException e) {
      System.out.println("이메일과 암호가 일치하지 않습니다.\n");
    }
  }

  private static void printHelp() {
    System.out.println();
    System.out.println("잘못된 명령입니다. 아래 명령어 사용법을 확인하세요.");
    System.out.println("명령어 사용법:");
    System.out.println("new 이메일 이름 암호 암호확인");
    System.out.println("change 이메일 현재비번 변경비번");
    System.out.println();
  }

}
```

MainForSpring 클래스가 MainForAssembler 클래스와 다른 점은 Assembler 클래스 대신 스프링 컨테이너인
ApplicationContext를 사용했다는 것뿐이다.



### DI 방식 1: 생성자 방식

MemberRegisterService 클래스를 보면 생성자를 통해 의존 객체를 주입받아 필드<sup>this.memberDao</sup>에 할당했다.

```java
public MemberRegisterService(final MemberDao memberDao) {
  this.memberDao = memberDao;
}
```

스프링 자바 설정에서는 생성자를 이용해서 의존 객체를 주입하기 위해 해당 설정을 담은 메서드를 호출했다.

```java
@Bean
public MemberDao memberDao() {
  return new MemberDao();
}

@Bean
public MemberRegisterService memberRegSvc() {
  return new MemberRegisterService(memberDao());
}
```

생성자에 전달할 의존 객체가 두 개 이상이어도 동일한 방식으로 주입하면 된다. 생성자 파라미터가 두 개인 예제를 살펴보기 전에 예제를 실행하는데 필요한 코드를 추가하자.

> *MemberDao.java*

```java
public Collection<Member> selectAll() {
  return map.values();
}
```



다음 추가할 코드는 MemberPrinter 클래스이다.

> *MemberPrinter.java*

```java
package chapter03.spring;

public class MemberPrinter {
  
  public void print(Member member) {
    System.out.printf(
        "회원 정보: 아이디=%d, 이메일=%s, 이름=%s, 등록일=%tF\n",
        member.getId(), member.getEmail(),
        member.getName(), member.getRegisterDateTime());
  }
  
}
```

이제 생성자로 두 개의 파라미터를 전달받는 클래스를 작성해보자. 

> *MemberListPrinter.java*

```java
package chapter03.spring;

import java.util.Collection;

public class MemberListPrinter {
  
  private MemberDao memberDao;
  private MemberPrinter printer;
  
  public MemberListPrinter(MemberDao memberDao, MemberPrinter printer) {
    this.memberDao = memberDao;
    this.printer = printer;
  }
  
  public void printAll() {
    memberDao.selectAll().forEach(m -> printer.print(m));
  }
  
}
```

생성자가 두 개인 경우에도 동일하게 각 파라미터에 해당하는 메서드를 호출해서 의존 객체를 주입한다.

> *AppCtx.java*

```java
...생략
    @Bean
  public MemberPrinter memberPrinter() {
    return new MemberPrinter();
  }
  
  @Bean
  public MemberListPrinter listPrinter() {
    return new MemberListPrinter(memberDao(), memberPrinter());
  }
...생략
```



위 설정이 올바르게 동작하는지 확인하기 위해 MainForSpring에 추가하자.

> *MainForSpring.java*

```java
...생략
  if (command.startsWith("new ")) {
        processNewCommand(command.split(" "));
        continue;
      } else if (command.startsWith("change ")) {
        processChangeCommand(command.split(" "));
        continue;
      } else if (command.equals("list")) {
        processListCommand();
        continue;
      }
...생략
    private static void processListCommand() {
    MemberListPrinter listPrinter = 
      ctx.getBean("listPrinter", MemberListPrinter.class);
    listPrinter.printAll();
  }
...생략
```



### DI 방식 2: 세터 메서드 방식

생성자 외에 세터 메서드를 이용해서 객체를 주입받기도 한다. 일반적인 세터<sup>setter</sup> 메서드는 자바빈 규칙에 따라 
다음과 같이 작성한다.

- 메서드 이름이 set으로 시작한다.
- set 뒤에 첫 글자는 대문자로 시작한다.
- 파라미터가 1개이다.
- 리턴 타입이 void이다.



세터 메서드를 이용해서 의존 객체를 주입받는 코드를 작성해보자.

> *MemberInfoPrinter.java*

```java
package chapter03.spring;

public class MemberInfoPrinter {
  
  private MemberDao memberDao;
  private MemberPrinter printer;
  
  public void printMemberInfo(final String email) {
    Member member = memberDao.selectByEmail(email);
    if (member == null) {
      System.out.println("데이터 없음\n");
      return;
    }
    printer.print(member);
    System.out.println();
  }
  
  public void setMemberDao(final MemberDao memberDao) {
    this.memberDao = memberDao;
  }
  
  public void setPrinter(final MemberPrinter printer) {
    this.printer = printer;
  }
  
}
```

세터 메서드를 이용해서 의존을 주입하는 설정 코드를 AppCtx 클래스에 추가하자. 

> *AppCtx.java*

```java
...생략
  @Bean
  public MemberInfoPrinter infoPrinter() {
    MemberInfoPrinter infoPrinter = new MemberInfoPrinter();
    infoPrinter.setMemberDao(memberDao());
    infoPrinter.setPrinter(memberPrinter());
    return infoPrinter;
  }
...생략  
```



이제 MainForSpring 코드에 MemberInfoPrinter 클래스를 사용하는 코드를 추가해보자.

> *MainForSpring.java*

```java
...생략
    private static void processListCommand() {
    MemberListPrinter listPrinter = 
      ctx.getBean("listPrinter", MemberListPrinter.class);
    listPrinter.printAll();
  }

  private static void processInfoCommand(final String... arg) {
    if (arg.length != 2) {
      printHelp();
      return;
    }
    MemberInfoPrinter infoPrinter = 
      ctx.getBean("infoPrinter", MemberInfoPrinter.class);
    infoPrinter.printMemberInfo(arg[1]);
  }
...생략
```



> **생성자 vs 세터 메서드**
>
> 두 방식은 상황에 따라 두 방식을 혼용해서 사용한다고 한다. 두 방식은 각자 장점이 있다.
>
> - 생성자 방식: 빈 객체를 생성하는 시점에 모든 의존 객체가 주입된다.
> - 세터 메서드 방식: 세터 메서드 이름을 통해 어떤 의존 객체가 주입되는지 알 수 있다.
>
> 각 방식의 장점은 곧 다른 방식의 단점이다. 예를 들어 생성자의 파라미터 개수가 많을 경우 각 인자가 어떤 
> 의존 객체를 설정하는지 알아내려면 생성자의 코드를 확인해야 한다. 하지만 세터 메서드 방식은 메서드
> 이름만으로도 어떤 의존 객체를 설정하는지 쉽게 유추할 수 있다. 
>
> 반면에 생성자 방식은 빈 객체를 생성하는 시점에 필요한 모든 의존 객체를 주입받기 때문에 객체를 사용할 때 완전한 상태로 사용할 수 있다. 하지만 세터 메서드 방식은 세터 메서드를 사용해서 의존 객체를 전달하지 않아도 빈 객체를 때문에 객체를 사용하는 시점에 NullPointerException이 발생할 수 있다.



### 기본 데이터 타입 값 설정

다음 코드는 두 개의 int 타입 값을 세터 메서드로 전달받는다.

> *VersionPrinter.java*

```java
package chapter03.spring;

public class VersionPrinter {

  private int majorVersion;
  private int minorVersion;

  public void print() {
    System.out.printf("이 프로그램의 버전은 %d.%d입니다. \n\n", majorVersion, minorVersion);
  }

  public void setMajorVersion(final int majorVersion) {
    this.majorVersion = majorVersion;
  }

  public void setMinorVersion(final int minorVersion) {
    this.minorVersion = minorVersion;
  }

}
```

int, long과 같은 기본 데이터 타입과 String 타입의 값은 일반 코드처럼 값을 설정하면 된다.

> *AppCtx.java*

```java
...생략
	@Bean
  public VersionPrinter versionPrinter() {
    VersionPrinter versionPrinter = new VersionPrinter();
    versionPrinter.setMajorVersion(5);
    versionPrinter.setMinorVersion(0);
    return versionPrinter;
  }
...생략
```

빈 객체를 하나 더 추가했으니 실제로 동작하는지 확인하기 위한 코드를 추가해보자.

> *MainForSpring.java*

```java
...생략
        } else if (command.equals("version")) {
        processVersionCommand();
        continue;
      }
...생략
  private static void processVersionCommand() {
    ctx.getBean("versionPrinter", VersionPrinter.class).print();
  }
...생략
```

위 코드를 실행하고 version 명령어를 입력하면 versionPrinter 빈 객체의 print() 메서드가 실행된다.

콘솔에 출력된 메시지를 보면 빈을 설정할 때 사용한 majorVersion 프로퍼티와 minorVersion 프로퍼티의 값이 출력된 것을 확인할 수 있다.



## @Configuration 설정 클래스의 @Bean 설정과 싱글톤

앞서 작성했던 AppCtx 클래스의 일부 코드를 다시 보자.

```java
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
  ChangePasswordService changePwdSvc() {
    ChangePasswordService changePwdSvc = new ChangePasswordService();
    changePwdSvc.setMemberDao(memberDao());
    return changePwdSvc;
  }
```

memberRegSvc() 메서드와 changePwdSvc() 메서드는 둘 다 memberDao() 메서드를 실행하고 있다. 그리고 memberDao() 메서드는 매번 새로운 MemberDao 객체를 생성해서 리턴한다. 여기서 궁금증이 생긴다.

- memberDao()가 새로운 MemberDao 객체를 생성해서 리턴하므로
  - memberRegSvc()에서 생성한 MemberRegisterService 객체와 changePwdSvc()에서 생성한 ChangePasswordService 객체는 서로 다른 MemberDao 객체를 사용하는 것이 아닌가?
  - 서로 다른 객체를 사용한다면 MainForSpring에서 new 명령어로 등록한 회원 정보를 저장할 때 사용하는 MemberDao와 change 명령어로 수정할 회원 정보를 찾을 때 사용하는 MemberDao는 다른 객체 아닌가?

그런데 앞서 2장에서 스프링 컨테이너가 생성한 빈은 싱글톤 객체라고 한 것을 기억할 것이다. 스프링 컨테이너는 @Bean이 붙은 메서드에 대해 한 개의 객체만 생성한다. 이는 memberDao()를 몇 번을 호출하더라도 항상 같은 객체를 리턴한다는 것을 의미한다.

이게 어떻게 가능할까? 스프링은 설정 클래스를 그대로 사용하지 않는다. 대신 설정 클래스를 상속한 새로운 설정 클래스를 만들어서 사용한다. 스프링이 런타임에 생성한 설정 클래스는 다음과 유사한 방식으로 동작한다.

> *AppCtxExt.java (이 코드는 가상의 코드일 뿐 실제 스프링 코드는 이보다 훨씬 복잡하다)*

```java
public class AppCtxExt extends AppCtx {
  private Map<String, Object> beans = ...;
  
  @Override
  public MemberDao memberDao() {
    if (!beans.containsKey("memberDao"))
      beans.put("memberDao", super.memberDao());
    
    return (MemberDao) beans.get("memberDao");
  }
}
```

스프링이 런타임에 생성한 설정 클래스의 memberDao() 메서드는 매번 새로운 객체를 생성하지 않는다. 대신 한 번 생성한 객체를 보관했다가 이후에는 동일한 객체를 리턴한다. 따라서 memberRegSvc() 메서드와 changePwdSvc() 메서드에서 memberDao() 메서드를 각각 실행해도 동일한 MemberDao 객체를 사용한다.



## 두 개 이상의 설정 파일 사용하기

스프링을 이용해서 어플리케이션을 개발하다보면 수십, 수백 개 이상의 빈을 설정하게 된다. 설정하는 빈의 개수가 증가하면 한 개의 클래스 파일에 설정하는 것보다 영역별로 설정 파일을 나누면 편해진다.

스프링은 한 개 이상의 설정 파일을 이용해서 컨테이너를 생성할 수 있다. 다음 두 파일은 지금까지 작성한 AppCtx.java의 빈 설정을 나눠서 설정한 것이다.

> *AppConf1.java*

```java
package chapter03.config;

import chapter03.spring.MemberDao;
import chapter03.spring.MemberPrinter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConf1 {
  
  @Bean
  public MemberDao memberDao() {
    return new MemberDao();
  }
  
  @Bean
  public MemberPrinter memberPrinter() {
    return new MemberPrinter();
  }
  
}
```

> *AppConf2.java*

```java
package chapter03.config;

import chapter03.spring.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConf2 {
  
  @Autowired
  private MemberDao memberDao;
  
  @Autowired
  private MemberPrinter memberPrinter;
  
  @Bean
  public MemberRegisterService memberRegSvc() {
    return new MemberRegisterService(memberDao);
  }
  
  @Bean
  public ChangePasswordService changePwdSvc() {
    ChangePasswordService pwdSvc = new ChangePasswordService();
    pwdSvc.setMemberDao(memberDao);
    return pwdSvc;
  }
  
  @Bean
  public MemberListPrinter listPrinter() {
    return new MemberListPrinter(memberDao, memberPrinter);
  }
  
  @Bean
  public MemberInfoPrinter infoPrinter() {
    MemberInfoPrinter infoPrinter = new MemberInfoPrinter();
    infoPrinter.setMemberDao(memberDao);
    infoPrinter.setPrinter(memberPrinter);
    return infoPrinter;
  }
  
  @Bean
  public VersionPrinter versionPrinter() {
    VersionPrinter versionPrinter = new VersionPrinter();
    versionPrinter.setMajorVersion(5);
    versionPrinter.setMinorVersion(0);
    return versionPrinter;
  }
  
}
```

여기서 @Autowired 애노테이션은 스프링의 자동 주입 기능을 위한 것이다. 이 설정은 의존 주입과 관련이 있다.
스프링 설정 클래스의 필드에 @Autowired 애노테이션을 붙이면 해당 타입의 빈을 찾아서 필드에 할당한다.
위 설정의 경우 스프링 컨테이너는 MemberDao 타입의 빈을 memberDao 필드에 할당한다.

AppConf1 클래스에서 MemberDao 타입의 빈을 설정했으므로 AppConf2 클래스의 memberDao 필드에는 AppConf1 클래스에서 설정한 빈이 할당된다.

@Autowired 애노테이션을 이용해서 다른 설정 파일에 정의한 빈을 필드에 할당했다면 설정 메서드에서 이 필드를 사용해서 필요한 빈을 주입하면 된다. 

```java
@Autowired
private MemberDao memberDao;

@Autowired
private MemberPrinter memberPrinter;


@Bean 
public MemberListPrinter listPrinter() {
  return new MemberListPrinter(memberDao, memberPrinter);
}
```

설정 클래스가 두 개 이상이어도 스프링 컨테이너를 생성하는 코드는 크게 다르지 않다.
다음과 같이 파라미터로 설정 클래스를 추가로 전달하면 된다.

```java
ctx = new AnnotaionConfigApplicationContext(AppConf1.class, AppConf2.class);
```

MainForSpring 클래스의 코드를 변경한 뒤 실행하면 동일하게 동작하는 것을 확인할 수 있다.



### @Configuration 애노테이션, 빈, @Autowired 애노테이션

@Autowired 애노테이션이 출현했으니 짧게 알아보고 가자. 
이를 포함한 자동 주입에 대한 내용은 4장에서 더 자세히 살펴본다.

@Autowired 애노테이션은 스프링 빈에 의존하는 다른 빈을 **자동으로 주입**하고 싶을 때 사용한다.
예를 들어 MemberInfoPrinter 클래스에 다음과 같이 @Autowired 애노테이션을 사용했다고 하자.

> *MemberInfoPrinter.java*

```java
package chapter03.spring;

import org.springframework.beans.factory.annotation.Autowired;

public class MemberInfoPrinter {

  @Autowired
  private MemberDao memberDao;
  @Autowired
  private MemberPrinter printer;

  public void printMemberInfo(final String email) {
    Member member = memberDao.selectByEmail(email);
    if (member == null) {
      System.out.println("데이터 없음\n");
      return;
    }
    printer.print(member);
    System.out.println();
  }
  ...세터 생략
```

두 필드에 @Autowired 애노테이션을 붙였다. 이렇게 @Autowired 애노테이션을 의존 주입 대상에 붙이면 다음 
코드처럼 스프링 설정 클래스의 @Bean 메서드에서 의존 주입을 위한 코드를 작성하지 않아도 된다.

> *AppCtx.java*

```java
  @Bean
  public MemberInfoPrinter infoPrinter() {
    //    infoPrinter.setMemberDao(memberDao());
    //    infoPrinter.setPrinter(memberPrinter());
    // 세터 메서드를 사용해서 의존 주입을 하지 않아도
    // 스프링 컨테이너가 @Autowired를 붙인 필드에
    // 자동으로 해당 타입의 빈 객체를 주입
    return new MemberInfoPrinter();
  }
```



앞서 AppConf2.java 클래스를 다시 보자. 여기서는 설정 클래스에 @Autowired 애노테이션을 사용했다.

```java
@Configuration
public class AppConf2 {

  @Autowired
  private MemberDao memberDao;          // 스프링 오류로 인해 붉은 라인이 나온다.

  @Autowired
  private MemberPrinter memberPrinter;

  ...생략
```

스프링 컨테이너는 설정 클래스에서 사용한 @Autowired에 대해서도 자동 주입을 처리한다. 실제로 스프링은 
@Configuration 애노테이션이 붙은 설정 클래스를 내부적으로 스프링 빈으로 등록한다. 그리고 다른 빈과 마찬가지로 @Autowired가 붙은 대상에 대해 알맞은 빈을 자동으로 주입한다.

즉 스프링 컨테이너는 AppConf2 객체를 빈으로 등록하고, @Autowired 애노테이션이 붙은 두 필드<sup>memberDao와 memberPrinter</sup>에 해당 타입의 빈 객체를 주입한다. 