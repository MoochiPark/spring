# Chapter 11.<br>MVC 1 : 요청 매핑, 커맨드 객체, <br>리다이렉트, 폼 태그, 모델

> ***이 장의 내용***
>
> - *@RequestMapping 설정*
> - *요청 파라미터 접근*
> - *리다이렉트*
> - *개발 환경 구축*
> - *스프링 폼 태그*
> - *모델 처리*



스프링 MVC를 사용해서 웹 앱을 개발한다는 것은 결국 컨트롤러와 뷰 코드를 구현한다는 것이다.
어떤 컨트롤러를 이용해서 어떤 요청 경로를 처리할 지 결정하고, 웹 브라우저가 전송한 요청에서 필요한 값을 구하고, 처리 결과를 뷰를 통해 보여주면 된다.



## 1. 프로젝트 준비

> *gradle 의존성 추가*

```groovy
dependencies {
    testCompile group: 'junit', name: 'junit', version: '4.11'
    testCompile group: 'junit', name: 'junit', version: '4.12'
    implementation 'javax.servlet:javax.servlet-api:4.0.1'
    implementation 'javax.servlet.jsp:javax.servlet.jsp-api:2.3.3'
    implementation 'jstl:jstl:1.2'
    implementation 'org.springframework:spring-webmvc:5.2.4.RELEASE'
    implementation 'org.springframework:spring-jdbc:5.2.3.RELEASE'
    implementation 'org.apache.tomcat:tomcat-jdbc:9.0.31'
    implementation 'org.postgresql:postgresql:42.2.10.jre7'
}
```



> *MemberConfig.java*

<script src="https://gist.github.com/dcf18d4d5768f5a3832bb907cb8a86e4.js"></script>

> MvcConfig.java

<script src="https://gist.github.com/db419ea48d26ac2458b3fc39dbcebd44.js"></script>

> *web.xml*

<script src="https://gist.github.com/b0044bd74de1af4d342385080cc92340.js"></script>



## 2. 요청 매핑 애노테이션을 이용한 경로 매핑

웹 어플리케이션을 개발하는 것은 다음 코드를 작성하는 것이다.

- 특정 요청 URL을 처리할 코드
- 처리 결과를 HTML과 같은 형식으로 응답하는 코드

이 중 첫 번째는 @Controller를 사용한 컨트롤러 클래스를 이용해서 구현한다. 컨트롤러 클래스는 요청 매핑 애노테이션을 사용해서 메서드가 처리할 요청 경로를 지정한다. 

여러 단계를 거쳐 하나의 기능이 완성되는 경우 관련 요청 경로를 한 개의 컨트롤러 클래스에서 처리하면 코드 관리에 도움이 된다.

> *예제*

```java
@Controller
public class RegistController {
  
  @RequestMapping("/register/step1")
  public String handleStap1() {
    return "register/step1";
  }

  @RequestMapping("/register/step2")
  public String handleStap2() {
    return "register/step2";
  }

  @RequestMapping("/register/step3")
  public String handleStap3() {
    return "register/step3";
  }

}
```

다음처럼 공통되는 경로를 담은 RequestMapping 애노테이션을 클래스에 적용할 수 있다.

```java
@Controller
@RequestMapping("/register") // 각 메서드 공통 경로
public class RegistController {
  
  @RequestMapping("/step1")  // 공통 경로 제외 나머지 경로
  public String handleStep1() {
    return "register/step1";
  }
  ...
}
```



예제 코드로 직접 해보자. 회원 가입의 약관을 보여주는 첫 번째 과정이라 해보자.

<script src="https://gist.github.com/0b04a4f99b77c47ea8d3f1df2d1dbc84.js"></script>

> *step1.jsp*

<script src="https://gist.github.com/791d8e0d48254338e92fcfe761caa930.js"></script>

<script src="https://gist.github.com/447ad2f8109d7306306ca4b34f99bc04.js"></script>

> *실행 결과*

<img src="https://user-images.githubusercontent.com/43429667/79099376-ef38c000-7d9e-11ea-99a5-87eabc155fa7.png" alt="image" style="zoom:50%;" />



## 3. GET과 POST 구분: @GetMapping, @PostMapping

주로 폼을 전송할 때 POST 방식을 사용하는데 스프링 MVC는 별도 설정이 없으면 RequestMapping에 지정한 경로와 일치하는 요청을 처리한다. 만약 POST 방식 요청만 처리하고 싶다면 @PostMapping 애노테이션으로 제한할 수 있다.

```java
  @PostMapping("/register/step2")
  public String handleStep2() {
    return "registe/step2";
  }
```

위 같이 설정하면 GET 방식의 요청은 처리하지 않는다. 반대로 @GetMapping 애노테이션이 있다.
이 두 애노테이션을 사용하면 같은 요청 경로를 다른 메서드로 처리할 수 있다.



## 4. 요청 파라미터 접근

컨트롤러 메서드에서 요청 파라미터를 사용하는 첫 번째 방법은 HttpServletRequest를 직접 이용하는 것이다.
예를 들면 컨트롤러 처리 메서드의 파라미터로 사용하고 getParameter() 메서드를 이용해서 파라미터 값을 구하면 된다.

<script src="https://gist.github.com/a8b18bd65c0072b8bec4014fb9601dad.js"></script>

요청 파라미터에 접근하는 다른 방법은 @RequestParam을 사용하는 것이다. 요청 파라미터 개수가 몇 개 안 되면 간단하게 요청 파라미터의 값을 구할 수 있는 방법이다. 

<script src="https://gist.github.com/92af2a171bfaf0f5ee843be579d4299c.js"></script>

스프링 MVC는 파라미터 타입에 맞게 String 값을 변환해준다. Boolean 타입 외에 기본, 래퍼 타입에 대한 변환을 지원한다.

> *step2.jsp*

<script src="https://gist.github.com/a3c074662c1cdfa19b800127416320ce.js"></script>



## 5. 리다이렉트 처리

RegisterController.handleStep2() 메서드는 POST 방식만을 처리하기 때문에 GET 요청은 처리하지 않으므로 405 상태 
코드를 응답한다.

![image-20200414134127749](/Users/daewon/Library/Application Support/typora-user-images/image-20200414134127749.png)

잘못된 요청이 왔을 때 에러 화면보단 알맞은 경로로 리다이렉트하는 것이 더 좋을 때가 있다.

컨트롤러에서 특정 페이지로 리다이렉트하는 방법은 간단한데, `redirect:경로`를 뷰 이름으로 리턴하면 된다.

<script src="https://gist.github.com/5b904513fe252e361f3dd046c1ddc9eb.js"></script>



## 6. 커맨드 객체를 이용해서 요청 파라미터 사용하기

스프링은 폼의 파라미터의 값을 읽어와 설정하는 코드를 작성해야하는 불편함을 줄이기 위해 요청 파라미터의 값을 커맨드 객체에
담아주는 기능을 제공한다. 

커맨드 객체는 다음과 같이 요청 매핑 애노테이션이 적용된 메서드의 파라미터에 위치한다.

```java
  @PostMapping("/register/step3")
  public String handleStep3(final RegisterRequest regReq) {
    ...
  } 
```

스프링은 커맨드 객체의 메서드를 사용해서 요청 파라미터의 값을 커맨드 객체에 복사한 뒤 파라미터로 전달한다.
즉 스프링 MVC가 handleStep3() 메서드에 전달할 객체를 생성하고 그 객체의 세터 메서드를 이용해서 일치하는 요청 파라미터의 값을 전달한다.

폼에 입력한 값을 커맨드 객체로 전달받아 회원가입을 처리하는 코드를 추가해보자.

<script src="https://gist.github.com/f2ddf9c6a99aa1e8dac7bfe8acc6cffb.js"></script>

<script src="https://gist.github.com/114a54aa4bf156809e2c84e92bfb3d03.js"></script>

> *회원 가입에 성공했을 때 결과를 보여줄 step3.jsp*

<script src="https://gist.github.com/66a93b0d08e19743646c9b317d6d2e54.js"></script>





## 7. 뷰 JSP 코드에서 커맨드 객체 사용하기

```xml
<p><strong>${registerRequest.name}님 </strong>회원 가입을 완료했습니다.</p>
```

여기서 registerRequest가 커맨드 객체에 접근할 때 사용한 속성이다. 스프링 MVC는 커맨드 객체의 첫 글자를 소문자로 바꾼 클래스 이름과 동일한 속성 이름을 사용해서 커맨드 객체를 뷰에 전달한다.



## 8. @ModelAttribute로 커맨드 객체 속성 이름 변경

커맨드 객체에 접근할 때 사용할 속성 이름을 변경하고 싶다면 이 것을 사용하면 된다.

```java
  public String handleStep3(@ModelAttribute("formData") final RegisterRequest regReq) {
    ...
```

위 설정을 사용하면 뷰 코드에서 "formData"라는 이름으로 커맨드 객체에 접근할 수 있다.



## 9. 커맨드 객체와 스프링 폼 연동

회원 정보 입력 폼에서 중복된 이메일이 입력되면 텅 빈 폼을 보여주므로 값을 다시 입력해야하는 불편점이 있다.
다시 폼을 보여줄 때 커맨드 객체의 값을 폼에 채워주면 이런 불편함을 해소할 수 있다.

<script src="https://gist.github.com/036ff73885ade7f9cbe06f02f45596d9.js"></script>

스프링 MVC가 제공하는 커스텀 태그를 사용하면 좀 더 간단하게 출력할 수 있다.

<script src="https://gist.github.com/34f7145b098d0a1eecd5e34c31d516aa.js"></script>

\<form:form> 태그를 사용하려면 커맨드 객체가 존재해야 한다. 다음처럼 모델에 추가해주자.

<script src="https://gist.github.com/b3352b7863d0a67700770b052c42cb4a.js"></script>



## 10. 컨트롤러 구현 없는 경로 매핑

회원 가입 후 첫 화면으로 링크를 이동하는 코드는 요청 경로와 뷰 이름을 연결해주는 것에 불과하다. 이를 위해 클래스를 만드는 것은 성가신 일이므로 다음처럼 간단하게 구현할 수 있다.

```java
@Override
public void addViewControllers(ViewControllerRegistry registry) {
  registry.addViewController("/main").setViewName("main");
}
```

<script src="https://gist.github.com/10d06f8c79d63a7c9cbaec4b9fbf71ab.js"></script>

