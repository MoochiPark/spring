# Chapter 12. MVC 2 : 메시지, 커맨드 객체 검증

이 장에서는 메시지를 출력하는 방법과 커맨드 객체의 값을 검증하는 방법에 대해 살펴본다.





## \<spring:message> 태그로 메시지 출력하기

<script src="https://gist.github.com/f190bafee75c52c206d8246e740f30e9.js"></script>

<script src="https://gist.github.com/021f70308ff563cad0956daa5dd50291.js"></script>

<script src="https://gist.github.com/a52da42c0d98ae3ff5a7f090615c018c.js"></script>

<script src="https://gist.github.com/a904246ddfae1a49932c987f2fe4e16c.js"></script>

<script src="https://gist.github.com/d563994bdf5bb6b82565af33294ccc6b.js"></script>

수정한 코드의 공통점은 다음과 같다.

- \<spring:message>커스텀 태그를 사용하기 위해 태그 라이브러리 설정 추가.
- \<spring:message> 태그를 사용해 메시지 출력

> 다국어 지원을 위한 메시지 파일
>
> - label_ko.properties
> - label_en.properties





## 커맨드 객체의 값 검증과 에러 메시지 처리

11장에서 작성한 회원 가입 처리 코드가 동작은 하지만 비정상 값을 입력해도 동작하는 문제가 있다.

또다른 문제는 중복된 이메일 주소를 입력해서 다시 폼을 보여줄 때 왜 가입에 실패했는지 이유를 알려주지 않는다.

위에서 언급한 두 가지 문제, 즉 폼 값 검증과 에러 메시지 처리는 어플리케이션 개발에서 놓치면 안되는 부분이다.

스프링은 이 두 가지 문제를 처리하기 위해 다음 방법을 제공한다.

- 커맨드 객체를 검증하고 결과를 에러 코드로 저장
- JSP에서 에러 코드로부터 메시지를 출력



### 커맨드 객체 검증과 에러 코드 지정하기

스프링 MVC에서 커맨드 객체의 값이 올바른지 검사하려면 다음의 두 인터페이스를 사용한다.

- org.springframework.validation.Validator
- org.springframework.validation.Errors



다음은 RegisterRequest 객체를 검증하기 위한 Validator 구현 클래스이다.

<script src="https://gist.github.com/eee1c4c6b164ad3c3b964c8eee5c51d3.js"></script>

- supports() 메서드는 파라미터로 받은 clazz 객체가 RegisterRequest 클래스로 타입 변환이 가능한지 확인한다.
- validate() 메서드의 target 파라미터는 검사 대상 객체이고 errors 파라미터는 검사 결과 에러 코드를 설정하기 위한 객체이다.
  - 검사 대상 객체의 특정 프로퍼티나 상태가 올바른지 검사
  - 올바르지 않다면 Errors의 rejectValue() 메서드를 이용해서 에러 코드 저장



다음은 커맨드 객체를 검증하도록 수정한 코드다.

<script src="https://gist.github.com/0fffaa9b648e88d8f6810a8af787c88c.js"></script>

커맨드 객체의 특정 프로퍼티가 아닌 커맨드 객체 자체가 잘못될 수도 있다. 이런 경우에는 rejectValue() 메서드 대신
reject() 메서드를 사용한다. 예를 들어 로그인 아이디와 비밀번호를 잘못 입력한 경우 특정 프로퍼티에 에러를 추가하기 보다는
커맨드 객체 자체에 에러를 추가해야한다. 이럴 때 reject()를 사용한다.

reject() 메서드는 개별 프로퍼티가 아닌 객체 자체에 에러 코드를 추가하므로 이 에러를 글로벌 에러라고 부른다.

요청 매핑 애노테이션을 붙인 메서드에 Errors 타입의 파라미터를 추가할 떄 주의할 점은 반드시 커맨드 객체 파라미터 다음에 위치해야한다는 것이다. 그렇지 않으면 익셉션이 발생한다.

```java
@PostMapping("/register/step3")
public String handleStep3(Errors errors, RegisterRequest regReq) { // 실행 시점에 익셉션
  ...
}
```



## 글로벌 범위 Validator와 컨트롤러 범위 Validotor

스프링 MVC는 모든 컨트롤러에 적용할 수 있는 글로벌 Validator와 단일  컨트롤러에 적용할 수 있는 Validator를 설정할 수 있다. 이를 사용하면 @Valid를 사용해서 커맨드 객체에 검증 기능을 적용할 수 있다.



### 글로벌 범위 Validator 설정과 @Valid 애노테이션

글로벌 범위 Validator를 적용하려면 다음 두 가지를 설정하면 된다.

- 설정 클래스에서 WebMvcConfigurer의 getValidator() 메서드가 Validator 구현 객체를 리턴하도록 구현
- 글로벌 범위 Validator가 검증할 커맨드 객체에 @Valid 애노테이션 적용



<script src="https://gist.github.com/b8666a2dfe6925906d73bca1cdce927f.js"></script>

스프링 MVC는 WebMvcConfigurer 인터페이스의 getValidator() 메서드가 리턴한 객체를 글로벌 범위 Validotor로 사용한다. 이제 @Valid를 통해 적용할 수 있다. 

그 전에 다음 의존설정을 추가하자.

```groovy
implementation 'javax.validation:validation-api:2.0.1.Final'
```

이제 다음처럼 사용할 수 있다.

<script src="https://gist.github.com/e25124d18ac1f62ea9b5d47ce6eb72a3.js"></
</script>

커맨드 객체에 해당하는 파라미터에 @Valid가 붙으면 글로벌 범위 Validator가 해당 타입을 검증할 수 있는지 확인한다.
검증 가능하면 실제 검증을 수행하고 그 결과를 Errors에 저장한다. 이는 요청 처리 메서드 실행 전에 적용된다.

@Valid 애노테이션을 사용할 때 주의할 점은 Errors 타입 파라미터가 없으면 검증 실패 시 400 에러를 응답한다는 점이다.

> RegisterRequestValidator 클래스는 RegisterRequest 타입의 객체만 검증할 수 있으므로 모든 컨트롤러에 적용할 수 있는 글로벌 범위의 Validator로 적합하지 않다. 스프링 MVC는 자체적으로 글로벌 Validator를 제공하는데,
> 이 것을 사용하면 Bean Validation이 제공하는 애노테이션을 이용해서 값을 검증할 수 있다.



### @InitBinder 애노테이션을 이용한 컨트롤러 범위 Validator

<script src="https://gist.github.com/c9b2ae8ab89ccd0829a1cb9a79057788.js"></script>

@InitBinder가 붙은 메서드는 컨트롤러의 요청 처리 메서드를 실행하기 전에 매번 실행된다. 예를 들어
RegisterController 컨트롤러의 요청 처리 메서드인 handleStep1, 2, 3()을 실행하기 전에 initBinder() 메서드를
매번 호출해서 WebDataBinder를 초기화한다.



> ***글로벌, 컨트롤러 범위 Validator의 우선 순위***
>
> @InitBinder를 붙인 메서드에 WebDataBinder는 내부적으로 Validator의 목록을 갖는데 이 목록에는
> 글로벌 범위 Validator가 기본으로 포함된다. WebDataBinder의 setValidator 메서드를 실행하면
> 기존에 갖고있던 Validator를 목록에서 삭제하고 새로 전달받은 Validator를 목록에 추가한다.
>
> 즉 글로벌 범위 Validator 대신에 컨트롤러 범위 Validator를 사용하게 된다.
> 기존 목록에 추가하고 싶다면 addValidator 메서드를 사용하면 된다.





## Bean Validation을 이용한 값 검증 처리

@Valid 애노테이션은 Bean Validation 스펙에 정의되어 있다. 이 스펙은 @NotNull, Digits, @Size 등의 애노테이션을 
함께 정의하고 있다. 이것들을 사용하면 Validator 작성 없이 애노테이션만으로 커맨드 객체의 값 검증을 처리할 수 있다.