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

> *Assembler.java (Assembler를 사용하는 코드)*

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

- 입력한 문자열이 "new "로 시작하면 processNewCommand()