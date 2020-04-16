# Chapter 15. 간단한 웹 어플리케이션의 구조

> ***이 장의 내용***
>
> - *구성 요소*
> - *서비스 구현*
> - *패키지 구성*





## 간단한 웹 어플리케이션의 구성 요소

간단한 웹 어플리케이션을 개발할 때 사용하는 전형적인 구조는 다음 요소를 포함한다.

- 프론트 서블릿(=DispatcherServlet)

  웹 브라우저의 모든 요청을 받는 창구 역할. 요청을 분석해서 알맞은 컨트롤러에 전달한다.

- 컨트롤러 + 뷰

  실제 웹 브라우저의 요청을 처리한다. 클라이언트의 요청을 처리하기 위해 알맞은 기능 실행, 뷰에 결과 전달.

- 서비스

  기능의 로직 구현. DB 연동이 필요하면 DAO를 사용.

- DAO(⩬Repository)

  DB와 웹 어플리케이션 간에 데이터를 이동시켜주는 역할.





## 서비스의 구현

서비스는 핵심이 되는 기능의 로직을 제공한다.

예

- DB에서 비밀번호를 변경할 회원의 데이터를 구한다.
- 존재하지 않으면 익셉션 발생.
- 회원 데이터의 비밀번호를 변경한다.
- 변경 내역을 DB에 반영한다.



위처럼 로직들은 한 번의 과정으로 끝나기보다는 몇 단계의 과정을 거치는데, 중간에 실패하면 이전 것들을 모두 되돌려야 한다.
이런 이유로 서비스 메서드를 트랜잭션 범위에서 실행한다.

```java
  @Transactional
  public void changePassword(String email, String oldPwd, String newPwd) {
    Member member = memberDao.selectByEmail(email);
    if (member == null)
      throw new MemberNotFoundException();

    member.changePassword(oldPwd, newPwd);

    memberDao.update(member);
  }
```



같은 데이터를 사용하는 기능들<sup>회원가입, 비밀번호 변경</sup>을 한 개의 서비스 클래스로 모아서 구현할 수도 있지만,
코드 길이가 길어지면 수정이나 확장이 어려워지므로 기능마다 서비스 클래스를 만드는 편을 추천한다.



서비스 메서드는 기능을 실행한 후에 결과를 알려주어야 한다. 결과는 크게 두가지가 있다.

- 리턴 값을 이용한 정상 결과
- 익셉션을 이용한 비정상 결과



위를 잘 나타내는 예를 보자.

<script src="https://gist.github.com/389912b6fd68681fd77acf303c770baf.js"></script>

- 인증에 성공할 경우 인증 정보를 담고있는 객체를 리턴.
- 실패할 경우 익셉션을 발생시킨다.

따라서 위 메서드가 익셉션을 발생시키면 인증에 실패했다는 것을 알 수 있다. 실제로 LoginController는 다음처럼 처리한다.

```java
  @PostMapping
    public String submit(
    		LoginCommand loginCommand, Errors errors, HttpSession session,
    		HttpServletResponse response) {
      ...
                } catch (WrongIdPasswordException e) {
            errors.reject("idPasswordNotMatching");
            return "login/loginForm";
        }
    }
```





## 컨트롤러에서의 DAO 접근

서비스에서 어떤 로직도 수행하지 않고 단순히 DAO의 메서드만 호출하고 끝나는 코드도 있다.

```java
public class MemberService {
  ...
    public Member getMember(final Long id) {
    return memberDao.selectById(id);
  }
}
```

위 코드는 사실상 DAO의 메서드를 직접 호출하는 것과 같다. 이 경우 DAO에 직접 접근하는 방법도 유효하지만 
개발자마다 호불호가 갈린다. 어떤 방식도 정답은 아니니 각자의 선호하는 방식을 정립하면 된다.



## 패키지 구성

패키지 구성에는 정답은 없다. 중요한 점은 팀 구성원 모두가 동일한 규칙에 따라 일관되게 패키지를 구성하면 된다.

> 웹 어플리케이션이 복잡해지고 커지면서 코드도 함께 복합해지는 문제를 완화하는 방법으로
> 도메인 주도 설계<sup>Domain-Driven Design: DDD</sup>를 적용하는 것이다. DDD는 컨트롤러-서비스-DAO 구조 대신에
> UI-서비스-도메인-인프라의 네 영역으로 어플리케이션을 구성한다. 여기서 UI는 컨트롤러 영역에 대응하고
> 인프라는 DAO 영역에 대응한다. 중요한 점은 주요한 도메인 모델과 업무 로직이 서비스 영역이 아닌 도메인 영역에
> 위치하는 것이다.  또한 도메인 영역은 정해진 패턴에 따라 모델을 구현한다.