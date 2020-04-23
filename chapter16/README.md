Chapter 16. JSON 응답과 요청 처리



> ***이 장의 내용***
>
> - *JSON 개요*
> - *@RestController를 이용한 JSON 응답 처리*
> - *@RequestBody를 이용한 JSON 요청 처리*
> - *ResponseEntity*



웹 페이지에서 Ajax<sup>Asynchronous JavaScript and XML</sup>를 이용해서 서버 API를 호출하는 경우가 많은데, 이 API는 
웹 요청에 대한 응답으로 HTML 대신 JSON이나 XML을 사용한다. 웹 요청에도 쿼리 문자열 대신에 JSON이나 XML을 데이터로
보내기도 한다. GET이나 POST 뿐만 아니라 PUT, DELETE 같은 다른 방식도 사용한다. 
스프링 MVC를 사용하면 이를 위한 웹 컨트롤러를 쉽게 만들 수 있다.

이 장에서는 스프링 MVC에서 JSON 응답과 요청을 처리하는 방법을 알아보자.



## 1. JSON 개요

JSON<sup>JavaScript Object Notation</sup>은 간단한 형식을 갖는 문자열로 데이터 교환에 주로 사용한다.

```json
{
  "name": "유관순",
  "birthday": "1902-12-16",
  "age": 17,
  "related": ["남동순", "류예도"]
  "edu": [
    {
      "title": "이화학당보통과",
      "year": 1916
    },
    {
      "title": "이화학당고등과",
      "year": 1916
    },
    {
      "title": "이화학당고등과",
      "year": 1919
    }
  ]
}
```

JSON 규칙은 간단하다.

- 중괄호를 사용해서 객체를 표현한다.
- 객체는 (이름, 값) 쌍을 갖는다. 이름과 값은 `:`으로 구분한다.
- 값에는 다음이 올 수 있다.
  - 문자열, 숫자, 불리언, null
  - 배열
  - 다른 객체

문자열은 `"`나 `'`의 사이에 위치한 값이다. `\`를 이용해서 특수 문자를 표시할 수 있다.

숫자는 10진수 표기법이나 지수 표기법<sup>예, 1.07e2</sup>을 따른다.

배열은 대괄호로 표현한다. related 배열은 문자열 값 목록을 가지고 있고 edu 배열은 객체를 값 목록으로 가지고 있다.



## 2. Jackson 의존 설정

Jackson은 자바 객체와 JSON 형식 문자열 간 변환을 처리하는 라이브러리다.
스프링 MVC에서 Jackson 라이브러리를 이용해서 자바 객체를 JSON으로 반환할 수 있다.

이를 위해 Jackson 관련 의존을 추가해야 한다.

> gradle에 의존 추가

```groovy
implementation 'com.fasterxml.jackson.core:jackson-databind:2.11.0.rc1'
```

Jackson은 다음과 같이 자바 객체와 JSON 사이의 변환을 처리한다.

<img src="/Users/daewon/Library/Application Support/typora-user-images/image-20200422123128683.png" alt="image-20200422123128683" style="zoom:50%;" />

Jackson은 프로퍼티<sup>getter 혹은 필드</sup>의 이름과 값을 JSON 객체의 (이름, 값) 쌍으로 사용한다.
Person 객체의 name 프로퍼티 값이 "이름"이라고 할 때 생성되는 JSON 형식 데이터는 ("name": "이름") 이다.
프로퍼티 타입이 배열이나 List인 경우 JSON 배열로 변환된다.



## 3. @RestController로 JSON 형식 응답

스프링 MVC에서 JSON 형식으로 데이터를 응답하는 것은 매우 간단하다. 
@Controller 애노테이션 대신 @RestController 애노테이션을 사용하면 된다. 

<script src="https://gist.github.com/da37e5a6a97d91c4e901e8fbe7d01bdb.js"></script>

기존 컨트롤러 코드와 다른 점은 다음과 같다.

- @Controller 대신 @RestController를 사용
- 요청 매핑 애노테이션 적용 메서드의 리턴 타입으로 일반 객체 사용



@RestController를 붙인 경우 스프링 MVC는 요청 매핑 애노테이션을 붙인 메서드가 리턴한 객체를
알맞은 형식으로 변환해서 응답 데이터로 전송한다.
이때 클래스 패스에 Jackson이 존재하면 JSON 형식의 문자열로 변환해서 응답한다.

Controller를 Config에 추가하자.

<script src="https://gist.github.com/569315994f6eeb9cf9b810bbe9aba402.js"></script>

톰캣을 실행하고 웹 브라우저에서 http://localhost:8080/api/members에 접속해보자.

크롬에서 json-formatter 확장 프로그램을 설치한 뒤에 결과를 보도록 하자.

<img src="/Users/daewon/Library/Application Support/typora-user-images/image-20200423155953602.png" alt="image-20200423155953602" style="zoom:50%;" />

@RestController가 추가되면서 @Controller와 사용되는 @ResponseBody의 사용빈도가 줄었다.



### 3.1 @JsonIgnore를 이용한 제외 처리

응답 결과를 보면 password가 포함되어 있다. 보통 암호와 같이 민감한 데이터는 응답 결과에 포함시키면 안되므로
password 데이터를 응답 결과에서 제외시켜야 한다. Jackson에서 제공하는 @JsonIgnore를 사용하면 이를
간단히 처리할 수 있다.

<script src="https://gist.github.com/423c5e041f992404ebc5cef725a43ffa.js"></script>

서버를 재시작한 뒤 확인해보면 @JsonIgnore를 붙인 대상이 JSON 결과에서 제외된 것을 볼 수 있다.



### 3.2 날짜 형식 변환 처리: @JsonFormat 사용

Jackson에서 날짜나 시간 값을 특정한 형식으로 표현하는 가장 쉬운 방법은 @JsonFormat을 사용하는 것이다.

> 스프링부트 1.5 이전 버전에서는 제대로 변환이 되지 않는다. 따라서 다른 방법을 사용한다.

<script src="https://gist.github.com/f1bb0297605b24b494832d574f6e6bd8.js"></script>

> *Gradle 의존성 추가*

```groovy
implementation 'com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.0.rc1'
```



> *@JsonSerialize 사용*

<script src="https://gist.github.com/91d584f6bd388858b7680c9b475b1f47.js"></script>

<img src="/Users/daewon/Library/Application Support/typora-user-images/image-20200423165058909.png" alt="image-20200423165058909" style="zoom:67%;" />



### 3.2 날짜 형식 변환 처리 : 기본 적용 설정

날짜 형식을 변환할 모든 대상에 @JsonFormat을 붙여야 한다면 상당히 귀찮다.
이런 귀찮음을 피하려면 날짜 타입에 해당하는 모든 대상에 동일한 변환 규칙을 적용할 수 있어야 한다.
@JsonFormat을 사용하지 않고 Jackson의 변환 규칙을 모든 날짜 타입에 적용하려면 스프링 MVC 설정을 변경해야 한다.

스프링 MVC는 자바 객체를 HTTP 응답으로 변환할 때 HttpMessageConverter라는 것을 사용한다.
예를 들어 Jackson을 이용해서 자바 객체를 JSON으로 변환할 때는 MappingJackson2HttpMessageConverter를 쓴다.

<script src="https://gist.github.com/342e9d112b82f6fe62ef82da2ed11a1c.js"></script>

이 코드는 JSON으로 변환할 때 사용할 ObjectMapper를 생성한다. Jackson2ObjectMappingBuilder는
ObjectMapper를 보다 쉽게 생성할 수 있도록 스프링이 제공하는 클래스다.
위 설정은 Jackson이 날짜 형식을 출력할 때 유닉스 타임 스탬프로 출력하는



