# Chapter 10. 스프링 MVC 프레임워크 동작 방식

> ***이 장의 내용***
>
> - *스프링 MVC 구성 요소*
> - *DIspatcherServlet*
> - *WebMvcConfigurer와 스프링 MVC 설정*





> *앞 장의 예제 코드를 실행하기 위한 스프링 MVC 설정*

<script src="https://gist.github.com/0c53e9109d2d683897204c9d9cc59f69.js"></script>

위 설정을 하면 남은 작업은 컨트롤러와 뷰 생성을 위한 한  JSP 코드를 작성하는 것이다.

단순해 보이는 이 설정은 실제로는 백여 줄에 가까운 설정을 대신 만들어주는데 이 모두를 알 필요는 없고
단지 스프링 MVC를 구성하는 주요 요소가 무엇이고 각 구성 요소들이 서로 어떻게 연결되는지 이해하면
다양한 환경에서 스프링 MVC를 빠르게 적용하는데 많은 도움이 될 것이다.



## 1. 스프링 MVC 핵심 구성 요소

> *스프링 MVC의 핵심 구성 요소*

# 그림 

- **<\<spring bean>>** : 스프링 빈으로 등록해야 하는 것
- **DispatcherServlet**: 모든 연결을 담당. 브라우저로 부터 요청이 들어오면 요청을 처리하기 위한
  컨트롤러 객체를 검색. 이때 HandlerMapping이라는 빈 객체에게 컨트롤러 검색을 요청.
- **HandlerMapping**: 클라이언트의 요청 경로를 이용해서 이를 처리할 컨트롤러 빈 객체를 DispatcherServlet에 전달한다. 
- **HandlerAdapter**: @Controller, Controller 인터페이스, HttpRequestHandler 인터페이스를 동일한 방식으로 중간에 사용되는 빈.
-  DispatcherServlet은 HandlerMapping이 찾아준 컨트롤러 객체를 처리할 수 있는 HandlerAdapter 빈에게 요청 처리를 위임한다<sup>3</sup>. 
- HandlerAdapter는 컨트롤러의 알맞은 메서드를 호출해서 요청을 처리하고, 그 결과를 ModelAndView라는 객체로 변환해서 DispatcherServlet에 리턴한다.
- ModelAndView를 받은 DispatcherServlet은 결과를 보여줄 뷰를 찾기 위해 ViewResolver 빈 객체를 사용한다<sup>7</sup>. 
  ModelAndView는 컨트롤러가 리턴한 뷰 이름을 담고 있는데, ViewResolver는 이 뷰 이름에 해당하는 View 객체를 찾거나 생성해서 리턴한다. 
  응답을 생성하기 위해 JSP를 사용하는 ViewResolver는 매번 새로운 View 객체를 생성해서 DispatcherServlet에 리턴한다.
- DispatcherServlet은 ViewResolver가 리턴한 View 객체에게 응답 결과 생성을 요청한다<sup>8</sup>.
  JSP를 사용하는 경우 View 객체는 JSP를 실행함으로써 웹 브라우저에게 전송할 응답 결과를 생성하고 모든 과정이 끝난다.



처리 과정을 보면 DispatcherServlet을 중심으로 HandlerMapping, HandlerAdapter, 컨트롤러,
ViewResolver, View, JSP가 각자 역할을 수행해서 클라이언트의 요청을 처리하는 것을 알 수 있다.



### 1.1 컨트롤러와 핸들러

**클라이언트의 요청을 실제로 처리하는 것은 컨트롤러이고 DispatcherServlet은 클라이언트의 요청을 전달받는 창구 역할을 한다**. DispatcherServlet의 요청을 처리할 컨트롤러를 찾아주는 객체는 ControllerMapping 타입이 아닌 HandlerMapping타입 일까?

스프링 MVC는 웹 요청을 처리할 수 있는 범용 프레임워크이므로 DispatcherServlet 입장에서는 
클라이언트의 요청을 처리하는 객체의 타입이 꼭 @Controller를 적용한 클래스일 필요는 없다.
실제로 제공하는 타입중에는 HttpRequestHandler도 있다.

이런 이유로 스프링 MVC는 웹 요청을 실제로 처리하는 객체를 핸들러라고 표현하고 있고 위에서 부르던
컨트롤러들은 전부 스프링 MVC 입장에서는 핸들러가 된다. 따라서 **특정 요청 경로를 처리해주는 핸들러를 찾아주는 객체를 HandlerMapping이라고 부른다.**

**DispatcherServlet은 핸들러 객체의 실제 타입에 상관없이 실행 결과를 ModelAndView라는 타입으로만 받을 수 있으면 된다**. 그런데 핸들러의 실제 구현 타입에 따라 ModelAndView를 반환하지 않는 객체도 있다. 따라서 **핸들러의 처리 결과를 ModelAndView로 변환해주는 HandlerAdapter 객체가 필요하다.**

핸들러 객체의 실제 타입마다 알맞은 HandlerMapping과 HandlerAdapter가 존재하므로 사용할
핸들러의 종류에 따라 해당 HandlerMapping과 HandlerAdapter를 스프링 빈으로 등록해야 한다.
물론 스프링이 제공하는 설정 기능을 이용하면 직접 등록하지 않아도 된다.



## 2. DispatcherServlet과 스프링 컨테이너

> *Chapter 09 web.xml*

```xml
<servlet>
        <servlet-name>dispatcher</servlet-name>
        <servlet-class>
            org.springframework.web.servlet.DispatcherServlet
        </servlet-class>
        <init-param>
            <param-name>contextClass</param-name>
            <param-value>
                org.springframework.web.context.support.AnnotationConfigWebApplicationContext
            </param-value>
        </init-param>
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>
                io.wisoft.daewon.config.MvcConfig
                io.wisoft.daewon.config.ControllerConfig
            </param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>
```

9장의 web.xml을 보면 DispatcherServlet의 contextConfiguration 초기화 파라미터를 통해
스프링 설정 클래스 목록을 전달했고, DispatcherServlet은 전달받은 설정 파일을 이용해서 스프링 컨테이너를 생성한다. 앞에서의 HandlerMapping, HandlerAdapter, 컨트롤러, ViewResolver 등의 빈은 생성한 스프링 컨테이너에서 구한다. 따라서 설정 파일에 이 빈들에 대한 정의가 포함되어야 한다.



## 3. @Controller를 위한 <br>HandlerMapping과 HandlerAdapter

위에서 설정 파일에 빈들에 대한 정의가 포함되어야 한다고 했는데, 9장에서는 단지 @EnableWebMvc 애노테이션만 추가했다.

@EnableWebMvc는 매우 다양한 스프링 빈 설정을 추가해준다. 이 태그가 빈으로 추가해주는 클래스 중
@Controller 타입의 핸들러 객체를 처리하기 위한 두 클래스도 포함되어 있다.

- **RequestMappingHandlerMapping**: @Controller 애노테이션이 적용된 객체의 요청 매핑 애노테이션<sup>@GetMapping</sup> 값을 이용해서 웹 브라우저의 요청을 처리할 컨트롤러 빈을 찾는다.
- **RequestMappingHandlerAdapter**: 컨트롤러의 메서드를 알맞게 실행하고 그 결과를 ModelAndView로 변환해서 DispatcherServlet에 리턴한다. 

9장의 코드를 다시 봐보자.

<script src="https://gist.github.com/f1b0834fbb4959bedcf7a63bd81fc1d5.js"></script>

RequestMappingHandlerAdapter 클래스는 "/hello" 요청 경로에 대해 hello() 메서드를 호출한다.이때 Model 객체를 생성해서 첫 번째 파라미터로 전달한다.
비슷하게 이름이 "name"인 HTTP 요청 파라미터의 값을 두 번째 파라미터로 전달한다.

RequestMappingHandlerAdapter는 컨트롤러 메서드 결과 값이 String 타입이면 해당 값을 뷰 이름으로 갖는 ModelAndView 객체를 생성해서 DispatcherServlet에 리턴한다.
이때 첫 번째 파라미터로 전달한 Model 객체에 보관된 값도 ModelAndView에 함께 전달한다.
예제에선 "hello"를 리턴하므로 뷰 이름으로 "hello"를 사용한다.



## 4. WebMvcConfigurer 인터페이스와 설정

@EnableWebMvc 애노테이션을 사용하면 @Controller 애노테이션을 붙인 컨트롤러를 위한 설정을 생성한다. 또한 WebMvcConfigurer 타입의 빈을 이용해서 MVC 설정을 추가로 생성한다.

<script src="https://gist.github.com/c93ebb9532ff84fea726a149dc8d45f9.js"></script>

위에서 설정 클래스는 WebConfigurer 인터페이스를 구현하고 있다. @Configuration을 붙인 클래스 역시 컨테이너에 빈으로 등록되므로 MvcConfig는 WebMvcConfigurer 타입의 빈이 된다.

@EnableWebMvc를 사용하면 WebMvcConfigurer 타입인 빈 객체의 메서드를 호출해서 MVC 설정을 추가한다. 예를 들어 ViewResolver 설정을 추가하기 위해 WebMvcConfigurer 타입의 빈 객체의
configureViewResolvers() 메서드를 호출한다. 따라서 WebMvcConfigurer 인터페이스를 구현한
설정 클래스는 configureViewResolvers() 메서드를 재정의해서 알맞은 뷰 관련 설정을 추가하면 된다.



## 6. 디폴트 핸들러와 HandlerMapping의 우선순위

9장에서 web.xml 설정을 보면 DispatcherServlet에 대한 매핑 경로를 '/'로 주었다.
이 경우 .jsp로 끝나는 요청을 제외한 모든 요청을 DispatcherServlet이 처리한다.
그렇지만 @EnableWebMvc가 등록하는 HandlerMapping은 @Controller를 적용한 빈 객체가 처리할 수 있는 요청 경로만 대응할 수 있다. 따라서 컨트롤러 객체를 찾지 못하면 DispatcherServlet은 404 응답을 전송한다.





## 정리

- DispatcherServlet은 웹 브라우저의 요청을 받기 위한 창구 역할을 하고, 다른 주요 구성 요소들을 이용해서 요청 흐름을 제어하는 역할을 한다. 
- HandlerMapping은 클라이언트의 요청을 처리할 핸글러 객체를 찾아준다. 핸들러<sup>커맨드</sup> 객체는 클라이언트의 요청을 실제로 처리한 뒤 뷰 정보와 모델을 설정한다.
- HandlerAdpater는 DispatcherServlet과 핸들러 객체 사이의 변환을 알맞게 처리 해준다.
- ViewResolver는 요청 처리 결과를 생성할 View를 찾아주고 View는 최종적으로 클라이언트에 응답을 생성해서 전달한다.





