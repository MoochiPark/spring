# Chapter 09. 스프링 MVC 시작하기

> ***실행 환경***
>
> - tomcat 9.0.31
> - gradle
> - intellJ IDEA
> - Mac OSX



## 스프링 MVC를 위한 설정

이 장에서 만들 예제는 매우 간단하지만 스프링 MVC를 실행하는데 필요한 최소 설정을 해야한다.

- 스프링 MVC의 주요 설정<sup>HandlerMapping, ViewPesolver 등</sup>
- 스프링 <sub>ㅗㅗ</sub><sup>ㅎㅎ</sup>
- 스프링의 DispatcherServlet 설정



### 스프링 MVC 설정

<script src="https://gist.github.com/26186057661b0095fcc6f3a41f0bf9ea.js"></script>

- **2행**: @EnableWebMvc 애노테이션은 스프링  MVC 설정을 활성화한다. 스프링 MVC를 사용하는데 필요한 다양한 설정을 생성한다.
- **5~9행**: DispatcherServlet의 매핑 경로를 '/'로 주었을 때, JSP/HTML/CSS 등을 올바르게 처리하기 위한 설정을 추가한다.
- **11~14행**: JSP를 이용해서 컨트롤러의 실행 결과를 보여주기 위한 설정을 추가한다.

실제로 스프링 2.5나 3 버전에서 스프링 MVC를 사용하려면 상황에 맞는 설정을 일일이 구성해야 했는데, 이런 복잡한 설정을
대신 해 주는 것이 바로 @EnableWebMvc 애노테이션이다.

@EnableWebMvc 애노테이션을 사용하면 내부적으로 다양한 빈 설정을 추가해준다. 이 것을 직접하려면 수십 줄에 가까운 코드를 작성해야 할 것이다.

@EnableWebMvc 애노테이션이 스프링 MVC를 사용하는데 필요한 기본적인 구성을 설정해준다면, WebMvcConfigurer 인터페이스는 스프링 MVC의 개별 설정을 조정할 때 사용한다. 

아직 각 설정이 어떻게 작용하는지 모르지만, 이 설정 코드에서 중요한 점은 위 설정이면 스프링 MVC를 이용해서 개발하는데 필요한 최소 설정이 끝난다는 것이다.



### web.xml 파일에 DispatcherServlet 설정

스프링 MVC가 웹 요청을 처리하려면 DispatcherServlet을 통해서 웹 요청을 받아야 한다. 이를 위해 web.xml 파일에 DispatcherServlet을 등록한다. src/main/webapp/WEB-INF 폴더에 작성하면 된다.

<script src="https://gist.github.com/MoochiPark/2e53bd0bac233bf64d13435db5ea91b2.js"></script>

- **10~13행**: DispatcherServlet을 dispatcher라는 이름으로 등록한다.

- **14~19행**: contextClass 초기화 파라미터를 설정한다. 자바 설정을 사용하는 경우 AnnotationConfigWebAppicationContext 클래스를 사용한다. 이 클래스는 자바 설정을 이용하는 웹 어플리케이션 용 스프링 컨테이너 클래스이다.
- **20~26행**: contextConfiguration 초기화 파라미터의 값을 지정한다. 이 파라미터에는 스프링 설정 클래스 목록을 지정한다. 각 설정 파일의 경로는 줄바꿈이나 콤마로 구분한다.
- **27행**: 톰캣과 같은 컨테이너가 웹 어플리케이션을 구동할 때 이 서블릿을 함께 실행하도록 설정한다.
- **30~33행**: 모든 요청을 DispatcherServlet이 처리하도록 서블릿 매핑을 설정했다.
- **35~48행**: HTTP 요청 파라미터의 인코딩 처리를 위한 서블릿 필터를 등록한다. 스프링은 인코딩 처리를 위한 필터인 CharacterEncodingFilter 클래스를 제공한다. 40~43행처럼 encoding 초기화 파라미터를 설정해서 HTTP 요청 파라미터를 읽어올 때 사용할 인코딩을 지정한다. 



DispatcherServlet은 초기화 과정에서 contextConfiguration 초기화 파라미터에 지정한 설정 파일을 이용해서 스프링 컨테이너를 초기화한다. 즉 MvcConfig 클래스와 ControllerConfig 클래스를 이용해서 스프링 컨테이너를 생성한다.
ControllerConfig 클래스는 컨트롤러 구현 부분에서 작성할 것이다.



## 코드 구현

필요한 설정은 끝났으니 실제로 코드를 구현하고 실행해보자. 작성할 코드는 다음과 같다.

- 클라이언트의 요청을 알맞게 처리할 컨트롤러
- 처리 결과를 보여줄 JSP



매우 단순한 코드지만 스프링 MVC를 이용해서 웹 어플리케이션을 개발할 때 필요한 코드가 무엇인지 이해하는데 충분할 것이다.



### 컨트롤러 구현

<script src="https://gist.github.com/a3a27ef867f19a95d168686ac60b0497.js"></script>

- **8행**: @Controller 애노테이션을 적용한 클래스는 스프링 MVC에서 컨트롤러로 사용한다.
- **11행**: @GetMapping 애노테이션은 메서드가 처리할 요청 경로를 지정한다. 위 코드의 경우 "/hello" 경로로 들어온 요청을 hello() 메서드를 이용해서 처리한다고 설정했다. 이름에서 알 수 있듯이 HTTP 요청 메서드 중 GET 메서드에 대한 매핑을 설정한다.
- **12행**: Model 파라미터는 컨트롤러의 처리 결과를 뷰에 전달할 때 사용한다.
- **13행**: @RequestParam 애노테이션은 HTTP 요청 파라미터의 값을 메서드의 파라미터로 전달할 때 사용된다.
  위 코드의 경우 name 요청 파라미터의 값을 name 파라미터에 전달한다.
- **14행**: "greeting"이라는 모델 속성에 값을 설정한다. 값으로는 "안녕하세요"와 name 파라미터의 값을 연결한 문자열이고,
  뒤에서 작성할 JSP 코드는 이 속성을 이용해서 값을 출력한다.
- **15행**: 컨트롤러의 처리 결과를 보여줄 뷰 이름으로 "hello"를 사용한다.



스프링 MVC 프레임워크에서 컨트롤러<sup>Controller</sup>란 간단하게 웹 요청을 처리하고 그 결과를 뷰에 전달하는 스프링 빈 객체다.
스프링 컨트롤러로 사용될 클래스는 @Controller 애노테이션을 붙여야 하고, @GetMapping 애노테이션이나 @PostMapping 애노테이션과 같은 요청 매핑 애노테이션을 이용해서 처리할 경로를 지정해 주어야 한다.



@GetMapping 애노테이션과 요청 URL 간의 관계 그리고 @RequestParam 애노테이션과 요청 파라미터의 관계를 보자.

![image](https://user-images.githubusercontent.com/43429667/76303390-fd5d7180-6304-11ea-94aa-cc1031ab8826.png)

@GetMapping 애노테이션의 값은 서블릿 컨텍스트 경로<sup>또는 웹 어플리케이션 경로</sup>를 기준으로 한다. 예를 들어 톰캣의 경우
webapp/sp5-chap09 폴더는 웹 브라우저에서 http://host/sp5-chap09 경로에 해당하는데 이때 sp5-chap09가
컨텍스트 경로가 된다. http://host/sp5-chap09/main/list 경로를 처리하기 위한 컨트롤러는 @GetMapping("main/list")를 사용해야 한다. 

@RequestParam 애노테이션은 HTTP 요청 파라미터를 메서드의 파라미터로 전달받을 수 있게 해준다. value 속성은 HTTP 요청 파라미터의 이름을 지정하고 required 속성은 필수 여부를 지정한다.

model.addAttribute() 메서드는 뷰에 전달할 데이터를 지정하기 위해 사용된다. 

`model.addAttribute("greeting", "안녕하세요" + name);`

첫 번째 파라미터는 데이터를 식별하는데 사용되는 속성 이름, 두 번째 파라미터는 속성 이름에 해당하는 값이다.
뷰 코드는 이 속성 이름을 사용해서 컨트롤러가 전달한 데이터에 접근하게 된다.

마지막으로 @GetMapping이 붙은 메서드는 컨트롤러의 실행 결과를 보여줄 뷰 이름을 리턴한다.
이 뷰 이름은 논리적인 이름이며 실제로 뷰 이름에 해당하는 뷰 구현을 찾아주는 것은 ViewResolver가 처리한다.

컨트롤러를 구현했다면 컨트롤러를 스프링 빈으로 등록하자. 

<script src="https://gist.github.com/eebf54b208b67d04bb48d61c8551eb91.js"></script>



### JSP 구현

컨트롤러가 생성한 결과를 보여줄 뷰 코드를 만들어보자. 

<script src="https://gist.github.com/d97e280e526f0c8df91a0abf79e5b5a3.js"></script>

뷰 이름과 JSP 파일과의 연결은 MvcConfig 클래스의 다음 설정을 통해서 이루어진다.

<script src="https://gist.github.com/2df1f0f9a8f5c4da3384cb10f84eddc1.js"></script>

registry.jsp() 코드는 JSP를 뷰 구현으로 사용할 수 있도록 해주는 설정이다. jsp() 메서드의 첫 번째 인자는 JSP 파일 경로를 찾을 때 사용할 접두어고, 두 번째는 접미사이다. 뷰 이름의 앞과 뒤에 접두어와 접미사를 붙여 최종적으로 사용할 JSP 파일의 경로를 결정한다. 뷰의 이름이 "hello"인 경우 <u>/WEB-INF/view/</u>hello<u>.jsp</u>가 된다.

hello.jsp 코드를 보면 다음처럼 JSP EL<sup>Expression Language</sup>을 사용했다.

`인사말: ${greeting}`

이 표현식의 "greeting"은 컨트롤러 구현에서 Model에 추가한 속성의 이름인 "greeting"과 동일하다. 이렇게 컨트롤러에서 
설정한 속성을 뷰 JSP 코드에서 접근할 수 있는 이유는 스프링 MVC 프레임워크가 모델에 추가한 속성을 JSP에서 접근할 수  
있게 HttpServletRequest에 옮겨주기 때문이다.

![image](https://user-images.githubusercontent.com/43429667/76305931-60510780-6309-11ea-9408-ae6c28eab107.png)

따라서 JSP로 뷰 코드를 구현할 경우 컨트롤러에서 추가한 속성의 이름을 이용해서 속성값을 응답 결과에 출력하게 된다.



## 실행하기

http://localhost:8080/hello?name=dw

위 주소로 접속하면 웹 브라우저에 응답 결과가 출력되는 것을 확인할 수 있을 것이다.

![image](https://user-images.githubusercontent.com/43429667/76309456-26cfca80-6310-11ea-9924-8b37c7745720.png)

실행 결과를 보면 hello.jsp에서 생성한 결과가 웹 브라우저에 출력된 것을 알 수 있고, name 파라미터로 지정한 값이 HelloController를 거쳐 JSP까지 전달된 것을 알 수 있다.





## 정리

이 장에서 우린 다음 작업을 했다.

- 스프링 MVC 설정
- 웹 브라우저의 요청을 처리할 컨트롤러 구현
- 컨트롤러의 처리 결과를 보여줄 뷰 코드 구현



우리가 앞으로 살펴볼 코드는 여기서 작성했던 코드의 구조를 크게 벗어나지 않는다. 단지 다음과 같은 확장만 있을 뿐이다.

- 컨트롤러에서 서비스나 DAO를 사용해서 클라이언트의 요청을 처리
- 컨트롤러에서 요청 파라미터의 값을 하나의 객체로 받고 값 검증
- 스프링이 제공하는 JSP 커스텀 태그를 이용해서 폼 처리
- 컨트롤러에서 세션이나 쿠키를 사용
- 인터셉터로 컨트롤러에 대한 접근 처리
- JSON 응답 처리

