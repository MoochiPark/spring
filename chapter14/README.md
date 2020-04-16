# Chapter 14. MVC 4 : 날짜 값 변환, <br>@PathVariable, 익셉션 처리



> ***이 장에서 다룰 내용***
>
> - *@DateTimeFormat*
>
> - *@PathVariable*
> - *익셉션 처리*





## 날짜를 이용한 회원 검색 기능

회원 가입 일자를 기준으로 검색하는 기능을 구현하면서 스프링 MVC의 특징을 설명한다.
MemberDao 클래스에 selectByRegDate() 메서드를 추가하자.

<script src="https://gist.github.com/1daa6747390111c370fee51bd8cb4189.js"></script>

이 메서드를 이용해서 특정 기간 동안에 가입한 회원 목록을 보여주는 기능을 구현할 것이다.



## 커맨드 객체 Date 타입 프로퍼티 변환 처리 : @DateTimeFormat

검색 기준 시간을 표현하기 위해 커맨드 클래스를 구현하자.

<script src="https://gist.github.com/b3d94b8e7809777edc988f66b76f6eef.js"></script>

스프링은 Long이나 int 같은 기본 데이터 타입으로의 변환은 기본적으로 해주지만 LocalDateTime 타입은 추가 설정이 필요하다.

"2020041602" -> 2020년 4월 16일 2시 값을 갖는 LocalDateTime 객체로 변환해준다.

이것을 사용하는 컨트롤러 코드를 작성하자.

<script src="https://gist.github.com/f58fdff6255662c698f3c614a2d46f6b.js"></script>

새로운 컨트롤러를 작성했으니 ControllerConfig 설정 클래스에 관련 빈 설정을 추가한다.

<script src="https://gist.github.com/9ff170ae6eb5b18a7678f5ce19d200e9.js"></script>

문자열이 LocalDateTime 타입으로 잘 변환 되는지 확인하기 위해 뷰 코드를 작성해야 한다.

MemberListController 클래스의 list() 메서드는 커맨드 객체로 받은 from, to 프로퍼티를 이용해서 해당 기간에 가입한
member 목록을 구하고, 뷰에 "members" 속성으로 전달한다. 뷰 코드는 이에 맞게 ListCommand 객체를 위한 폼을
제공하고 members 속성을 이용해서 회원 목록을 출력하도록 구현하면 된다.

<script src="https://gist.github.com/1ddd6fe55fe3b237f801863581bc3f76.js"></script>



## 변환 처리에 대한 이해

@DateTimeFormat을 이용하면 지정한 형식의 문자열을 자바의 시간 타입으로 변환해준다는 것을 확인했다.

이 변환은 WebDataBinder가 처리해준다. 

스프링 MVC는 요청 매핑 애노테이션 적용 메서드와 DispatcherServlet 사이를 연결하기 위해
RequestMappingHandlerAdapter 객체를 사용하는데, 이 핸들러 어댑터 객체는 요청 파라미터와 커맨드 객체 사이의 
변환 처리를 위해 WebDataBinder를 이용한다.

![image](https://user-images.githubusercontent.com/43431081/77627862-e5375480-6f8a-11ea-8891-523e0ef88dea.png)

WebDataBinder는 직접 타입을 변환하지 않고 ConversionService에 역할을 위임하는데, 스프링 MVC를 위한 설정인@EnableWebMvc를 사용하면 DefaultFormattingConversionService를 기본값으로 사용한다.

DefaultFormattingConversionService는 int, long 같은 기본 데이터 타입뿐만 아니라 @DateTimeFormat을 사용한 시간 관련 타입 변환 기능을 제공한다. 이런 이유로 커맨드로 사용할 클래스에 애노테이션만 붙이면 변환할 수 있는 것이다.





## @PathVariable을 이용한 경로 변수 처리

`http://localhost:8080/members/10`

위 형식처럼 각 회원마다 경로의 마지막 부분이 달라져서 일부가 고정되어 있지 않고 달라질 때 사용할 수 있는 애노테이션이다.

아래처럼 가변 경로를 처리할 수 있게된다.

<script src="https://gist.github.com/0c2ea61c97494dcc0d1bca446ec892ca.js"></script>

매핑 경로에 중괄호로 둘러 쌓인 부분을 경로 변수라고 부른다. {경로 변수}에 해당하는  값은 @PathVariable 파라미터에 전달된다.



## 컨트롤러 익셉션 처리하기

없는 ID를 경로 변수로 사용하거나 타입이 다른 경로 변수를 사용할 때 유용하게 사용할 수 있는게 

@ExceptionHandler다. 같은 컨트롤러에 이 것이 붙은 메서드가 있으면 그 메서드가 익셉션을 처리한다.

```java
@Controller
public class MemberDetailController {

  ...
  
  @ExceptionHandler(TypeMismatchException.class)
  public String handleTypeMismatchException() {
    return "member/invalidId";
  }
  
  @ExceptionHandler(MemberNotFoundException.class)
  public String handleMemberNotFoundException() {
    return "member/noMember";
  }

}
```

애노테이션의 값으로 설정된 익셉션이 발생하면 에러 응답을 보내는 대신 이 메서드를 실행한다.
요청 매핑 애노테이션 적용 메서드와 마찬가지로 뷰 이름을 리턴할 수 있다. 만약 익셉션 객체에 대한 정보를 알고싶다면
메서드 파라미터에 익셉션 객체를 전달받으면 된다.

```java
@ExceptionHandler(TypeMismatchException.class)
public String handleTypeMismatchException(TypeMismatchException ex) {
  // ex를 사용해서 로그 남기는 등 작업
  return "member/invalidId";
}
```



## @ControllerAdvice를 이용한 공통 익셉션 처리

여러 컨트롤러에서 동일하게 처리할 익셉션이 발생하면 **@ControllerAdvice 애노테이션을 이용해서 중복을 없앨 수 있다.**

```java
@ControllerAdvice("spring")
public class CommonExceptionHandler {
  
  @ExceptionHandler(RuntimeException.class)
  public String handleRuntimeException() {
    return "error/commonException";
  }
  
}
```

- **"spring"** : "spring" 패키지와 그 하위 패키지에 속한 컨트롤러에서 RuntimeException이 발생하면 메서드가 호출되도록 범위 지정
- @ControllerAdvice 적용 클래스가 동작하려면 해당 클래스를 스프링에 **빈으로 등록해야 한다.**





## @ExceptionHandler 적용 메서드의 우선 순위

@ControllerAdvice 클래스에 있는 @ExceptionHandler 메서드와 컨트롤러 클래스에 있는 @ExceptionHandler 메서드 중 컨트롤러 클래스에 적용된 메서드가 우선한다.

컨트롤러 메서드 실행 과정 중 익셉션 발생 시 순서

- 같은 컨트롤러에 위치한 @ExceptionHandler 메서드 중 해당 익셉션을 처리할 수 있는 메서드를 탐색
- 없을 경우 @ControllerAdvice 클래스에 위치한 @ExceptionHandler 메서드 탐색

