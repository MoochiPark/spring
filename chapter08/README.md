# Chapter 08. DB 연동

> ***이 장의 내용***
>
> - *DataSource 설정*
> - *JdbcTemplate을 이용한 쿼리 실행*
> - *DB 관련 익셉션 변환 처리*
> - *트랜잭션 처리*



## JDBC 프로그래밍의 단점을 보완하는 스프링

JDBC API를 이용하면 DB 연동에 필요한 Connection을 구한 다음 쿼리를 실행하기 위한 PreparedStatement를 생성한다.
그리고 쿼리를 실행한 뒤에는 finally 블록에서 ResultSet, PreparedStatement, Connection을 닫는다.

여기서 문제점은 JDBC 프로그래밍을 할 때 구조적으로 Connection을 구하고 ResultSet, PreparedStatement, Connection을 닫는 사실상 데이터 처리와는 상관없는 코드를 반복하게 된다는 것이다.

구조적인 반복을 줄이기 위한 방법은 템플릿 메서드 패턴과 전략 패턴을 함께 사용하는 것이다. 스프링은 바로 이 두 패턴을 엮은
JdbcTemplate 클래스를 제공한다. 이 클래스를 사용하면 JDBC 코드를 다음처럼 바꿀 수 있다.

```java
List<Member> results = jdbcTemplate.query(
  "select * from MEMBER where EMAIL = ?",
  new RowMempper<Member>() {
    @Override
    public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
      Member member = new Member(rs.getString("EMAIL"),
                                 rs.getString("PASSWORD"),
                                 rs.getString("NAME"),
                                 rs.getTimestamp("REGDATE"));
      member.setId(rs.getLong("ID"));
      return member;
    }
  },
  email);
return results.isEmpty() ? null : results.get(0);
```

아직 이 코드가 어떤 의미를 갖는지 정확히 모르지만 JDBC를 사용한 코드와 비교하면 구조적으로 중복되는 코드가 꽤 줄었다.
자바 8의 람다를 사용하면 다음과 같이 코드를 더 줄일 수 있다.

```java
List<Member> results = jdbcTemplate.query(
  "select * from MEMBER where EMAIL = ?",
  (ResultSet rs, int rowNum) -> {
    Member member - new Member(rs.getString("EMAIL"),
                               rs.getString("PASSWORD"),
                               rs.getString("NAME"),
                               rs.getTimestamp("REGDATE"));
    member.setId(rs.getLong("ID"));
    return member;
  },
  email);
return results.isEmpty() ? null : results.get(0);
```

스프링이 제공하는 또 다른 장점은 트랜잭션 관리가 쉽다는 것이다. JDBC API로 트랜잭션을 처리하려면 다음과 같이 Connection의 setAutoCommit(false)을 이용해서 자동 커밋을 비활성화 하고 commit()과 rollback() 메서드를 이용해서
트랜잭션을 커밋하거나 롤백해야 한다. 
하지만 스프링을 사용하면 트랜잭션을 적용하고 싶은 메서드에 @Transactional 애노테이션을 붙이기만 하면 된다.

```java
@Transactional
public void insert(final Member meber) {
  ...
}
```

커밋, 롤백 처리는 스프링이 알아서 처리하므로 개발자는 트랜잭션 처리를 제외한 핵심 코드만 집중해서 작성하면 된다.



## 프로젝트 준비

### 프로젝트 생성

> *새로 추가한 의존 모듈*

```groovy
     testCompile group: 'junit', name: 'junit', version: '4.12'
    implementation 'org.springframework:spring-context:5.2.3.RELEASE'
    implementation 'org.springframework:spring-jdbc:5.2.3.RELEASE'
    implementation 'org.apache.tomcat:tomcat-jdbc:9.0.31'
    implementation 'org.postgresql:postgresql:42.2.10.jre7'
```

- **spring-jdbc** : JdbcTemplate 등 JDBC 연동에 필요한 기능을 제공한다.
- **tomcat-jdbc **: DB 커넥션풀 기능을 제공한다.
- **postgresql** : postgresql 연결에 필요한 JDBC 드라이버를 제공한다.

스프링이 제공하는 트랜잭션 기능을 사용하려면 spring-tx 모듈이 필요한데, spring-jdbc 모듈을 포함하면 자동으로 포함된다.

> ***커넥션 풀이란?***
>
> 실제 서비스 운영 환경에서는 서로 다른 장비를 이용해서 자바 프로그램과 DBMS를 실행한다. 자바 프로그램에서 DBMS로 커넥션을 생성하는 시간은 매우 길기 때문에 DB 커넥션을 생성하는 시간은 전체 성능에 영향을 미칠 수 있다.
> 또한 동시에 접속하는 사용자수가 많으면 사용자마다 DB 커넥션을 생성해서 DBMS에 무리를 준다.
>
> 최초 연결에 따른 응답 속도 저하와 동시 접속자가 많을 때 발생하는 부하를 줄이기 위해 사용하는 것이 커넥션 풀이다.
> 커넥션 풀은 일정 개수의 DB 커넥션을 미리 만들어두는 기법이다. DB 커넥션이 필요한 프로그램은 커넥션 풀에서 커넥션을
> 가져와 사용한 뒤 커넥션을 다시 풀에 반납한다. 커넥션을 미리 생성해두기 때문에 커넥션을 사용하는 시점에서 커넥션을 
> 생성하는 시간을 아낄 수 있다. 또한 동시 접속자가 많더라도 커넥션을 생성하는 부하가 적기 때문에 더 많은 동시 접속자를 처리할 수 있다. 커넥션도 일정 개수로 유지해서 DBMS에 대한 부하를 일정 수준으로 유지할 수 있게 해준다.
>
> DB 커넥션 풀 기능을 제공하는 모듈 중 현시점에서 지속적인 개발, 성능 등을 고려하면 Tomcat JDBC나 HikariCP를
> 권한다. 

3장의 예제 코드에서 다음 코드를 복사한다. 이들 코드는 모두 spring 패키지에 속하므로 spring 패키지를 생성한 뒤 복사하자.

- ChangePasswordService.java
- DuplicateMemberException.java
- Member.java, MemberDao.java
- MemberInfoPrinter.java, MemberListPrinter.java
- MemberNotFoundException.java
- MemberPrinter.java
- MemberRegisterService.java, RegisterRequest.java
- WrongIdPasswordException.java

이 장에서는 DB를 사용해서 MemberDao 클래스를 구현할 것이므로 다음과 같이 MemberDao를 작성하자.

> *MemberDao.java*

```java
package spring;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MemberDao {
  
  public Member selectByEmail(final String email) {
    return null;
  }
  
  public void insert(final Member member) {
    
  }
  
  public void update(final Member member) {
    
  }
  
  public Collection<Member> selectAll() {
    return null;
  }

}
```



### DB 테이블 생성

교재에서는 MySQL로 되어있지만 자주 사용할 postgresql로 작성하였다.

```sql
CREATE TABLE member (
    id serial primary key,
    email varchar(255) unique,
    password varchar(100),
    name varchar(100),
    regdate timestamp
);
```



다음 쿼리를 실행해서 예제에서 사용할 데이터를 미리 생성하자.

```sql
INSERT INTO member (email, password, name, regdate)
values ('daewon@wisoft.io', '1234', 'pdw', now());
```

DB 테이블과 사용할 데이터까지 생성했으니 이제 스프링이 지원하는 DB 기능을 사용해서 MemberDao를 구현, 사용하자.



### DataSource 설정

JDBC API는 DriverManager 외에 DataSource를 이용해서 DB 연결을 구하는 방법을 정의하고 있다.
DataSource를 사용하면 다음 방식으로 Connection을 구할 수 있다.

```java
Connection conn = null;
try {
  // dataSource는 생성자나 설정 메서드를 이용해서 주입받음
  conn = dataSource.getConnection();
}
```

스프링이 제공하는 DB 연동 기능은 DataSource를 사용해서 DB Connection을 구한다. DB 연동에 사용할 DataSource를
스프링 빈으로 등록하고 DB 연동 기능을 구현한 빈 객체는 DataSource를 주입받아 사용한다.

Tomcat JDBC 모듈은 javax.sql.DataSource를 구현한 DataSource 클래스를 제공한다.
이 클래스를 스프링 빈으로 등록해서 DataSource로 사용할 수 있다.

> *AppCtx.java*

```java
@Configuration
public class AppCtx {

  @Bean(destroyMethod = "close")
  public DataSource dataSource() {
    DataSource ds = new DataSource();
    ds.setDriverClassName("org.postgresql.jdbc.Driver");
    ds.setUrl("jdbc:postgresql://satao.db.elephantsql.com:5432/voipmttw");
    ds.setUsername("voipmttw");
    ds.setPassword("9U_pMlo8JwC-4kkYy83YFip30jEhS1Xi");
    ds.setInitialSize(2);
    ds.setMaxActive(10);
    return ds;
  }

}
```

destroyMethod 속성값을 close로 설정했다. close 메서드는 커넥션 풀에 보관된 Connection을 받는다.



### Tomcat JDBC의 주요 프로퍼티

Tomcat JDBC 모듈의 DataSource 클래스는 커넥션 풀 기능을 제공하는 DataSource 구현 클래스이다.
DataSource 클래스는 커넥션을 몇 개 만들지 지정할 수 있는 메서드를 제공한다. 주요 설정 메서드는 교재<sup>187p</sup>를 참고하자.

주요 설정 메서드를 이해하려면 커넥션의 상태를 알아야 한다. 커넥션 풀은 커넥션을 생성하고 유지한다.
커넥션 풀에 커넥션을 요청하면 커넥션은 활성<sup>active</sup> 상태가 되고, 커넥션을 다시 커넥션 풀에 반납하면 유휴<sup>idle</sup> 상태가 된다.
DataSource.getConnection()을 실행하면 커넥션 풀에서 커넥션을 가져와 커넥션이 활성 상태가 된다. 반대로 커넥션을
종료<sup>close</sup>하면 커넥션은 풀로 돌아가 유휴 상태가 된다. 

Connection을 구하고 종료하는 코드를 작성해보자. 명시적으로 보여주기 위해 Connection 관련 코드와 Statement, ResultSet 코드를 별도의 try 블록으로 나누었다.

> *DbQuery.java*

```java
public class DbQuery {
  
  private DataSource dataSource;
  
  public DbQuery(final DataSource dataSource) {
    this.dataSource = dataSource;
  }
  
  public int count() {
    Connection conn = null;
    try {
      conn = dataSource.getConnection(); // 풀에서 구함
      String query = "select count(*) from MEMBER";
      try (PreparedStatement ps = conn.prepareStatement(query);
           ResultSet rs = ps.executeQuery()) {
        rs.next();
        return rs.getInt(1);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      if (conn != null)
        try {
          conn.close(); // 풀에 반환
        } catch (SQLException e) {
          e.printStackTrace();
        }
    }
  }
  
}
```

dataSource.getConnection()을 실행하면 DataSource에서 커넥션을 구하는데 이때 풀에서 커넥션을 가져온다.
이 시점에서 커넥션<sup>conn</sup>은 활성 상태이다. 

conn.close()를 실행하면 실제 커넥션을 끊지 않고 풀에 반환한다. 풀에 반환된 커넥션은 유휴 상태가 된다.

- **maxActive**

  활성 상태가 가능한 최대 커넥션 개수를 지정한다. 이를 40으로 지정하면 동시에 커넥션 풀에서 가져올 수 있는 커넥션 개수가 40개라는 뜻이다.

- **maxWait**

  활성 상태 커넥션이 최대인 상태에서 커넥션 풀에 다시 커넥션을 요청하면 다른 커넥션이 반환될 때까지 대기한다.
  대기 시간 내에 풀에 반환된 커넥션이 있으면 해당 커넥션을 구하게 되고, 없으면 익셉션이 발생한다.

- **InitialSize**

  커넥션 풀을 사용하는 이유는 성능 때문이다. 매번 새로운 커넥션을 생성하면 그때마다 연결 시간이 소모된다.
  커넥션 풀을 사용하면 미리 커넥션을 생성하고 필요할 때에 꺼내 쓰므로 커넥션을 구하는 시간이 줄어 전체 응답 시간도 짧아진다. 그래서 커넥션 풀을 초기화할 때 최소 수준의 커넥션을 미리 생성하는 것이 좋다. 이를 정할때 InitialSize를 사용한다.



커넥션 풀에 생성된 커넥션은 지속적으로 재사용된다. 근데 한 커넥션이 영원히 유지되는 것은 아니다. DBMS 설정에 따라 일정 시간 내에 쿼리를 실행하지 않으면 연결을 끊기도 한다. 예를 들어 DBMS에 5분 동안 쿼리를 실행하지 않으면 DB 연결을 끊도록 했는데, 커넥션 풀에 특정 커넥션이 5분 넘게 유휴 상태로 존재했을 경우 DBMS는 해당 커넥션의 연결을 끊지만 커넥션은 여전히
풀 속에 남아 있다. 이 상태에서 해당 커넥션을 사용하려하면 익셉션이 발생한다.

특정 시간대에 사용자가 없으면 이런 상황이 발생할 수도 있는데, 이런 문제를 방지하려면 커넥션 풀의 커넥션이 유효한지 주기적으로 검사해야 한다. 이와 관련된 속성이 **minEvitableIdleTimeMills, timeBetweenEvictionRunsMills, testWhileIdle**이다. 예를 들어 10초 주기로 커넥션이 유휴한지를 검사하고 최소 유휴 시간을 3분으로 지정해보자.

```java
@Configuration
public class AppCtx {

  @Bean(destroyMethod = "close")
  public DataSource dataSource() {
    ...
    ds.setTestWhileIdle(true); // 유휴 커넥션 검사
    ds.setMinEvictableIdleTimeMills(1000 * 60 * 3); // 최소 유휴 시간 3분
    ds.setTimeBetweenEvictionRunsMills(1000 * 10);  // 10초 주기로 검사
    return ds;
  }
```





## JbbcTemplate을 이용한 쿼리 실행

사실 스프링을 사용하면  DataSource나 Connection, Statement, ResultSet을 직접 사용하지 않고 JdbcTemplate을 이용해서 편리하게 쿼리를 실행할 수 있다. 앞서 비워 둔 MemberDao 클래스에 코드를 채워나가면서 사용법을 익혀보자.



### JdbcTemplate 생성하기

가장 먼저 해야 할 작업은 JdbcTemplate 객체를 생성하는 것이다.

> *MemberDao.java*

```java
public class MemberDao {

  private JdbcTemplate jdbcTemplate;
  
  public MemberDao(final DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }
...
```

JdbcTemplate 객체를 생성하려면 DataSource를 생성자에 전달하면 된다. 이를 위해 DataSource를 주입받도록 생성자를 
구현했다. 물론 설정 메서드 방식을 이용해서 DataSource를 주입받고 생성해도 된다.

JdbcTemplate을 생성하는 코드를 MemberDao 클래스에 추가했으니 스프링 설정에 MemberDao 빈 설정을 추가하자.

> *AppCtx.java*

```java
@Configuration
public class AppCtx {

  @Bean(destroyMethod = "close")
  public DataSource dataSource() {
    ...
  }

  @Bean
  public MemberDao memberDao() {
    return new MemberDao(dataSource());
  }
```



### JdbcTemplate을 이용한 조회 쿼리 실행

jdbcTemplate 클래스는 SELECT 쿼리 실행을 위한 query() 메서드를 제공한다. 자주 사용되는 쿼리 메서드는 다음과 같다.

- List<T> query(String sql, RowMapper<T> rowMapper)
- List<T> query(String sql, Object[] args, RowMapper<T> rowMapper)
- List<T> query(String sql, RowMapper<T> rowMapper, Object... args)

query() 메서드는 sql로 전달받은 쿼리를 실행하고 RowMapper를 이용해 ResultSet의 결과를 자바 객체로 변환한다.
sql 파라미터가 아래와 같이 인덱스 기반 파라미터를 가진 쿼리면 args 파라미터를 이용해 각 인덱스 파라미터의 값을 정한다.

```sql
select * from member where email = ?
```

쿼리 실행 결과를 자바 객체로 변환할 때 사용하는 RowMapper 인터페이스는 다음과 같다.

```java
package org.springframework.jdbc.core;

public interface RowMapper<T> {
  T mapRow(ResultSet rs, int rowNum) throws SQLException;
}
```



RowMapper의 mapRow() 메서드는 SQL 실행 결과로 구한 ResultSet에서 한 행의 데이터를 읽어와 자바 객체로 변환하는
매퍼 기능을 구현한다. RowMapper 인터페이스를 작성할 수도 있지만 임의 클래스나 람다식으로 RowMapper의 객체를 생성해서 query() 메서드로 전달할 때도 많다. 예를 들어 다음 처럼 임의 클래스를 이용해서 MemberDao의 selectByEmail() 메서드를 구현할 수 있다.

> *MemberDoo.java*

```java
public class MemberDao {
  ...
  public Member selectByEmail(final String email) {
    List<Member> results = jdbcTemplate.query(
        "select * from MEMBER where EMAIL = ?",
        new RowMapper<Member>() {
          @Override
          public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
            Member member = new Member(
                rs.getString("EMAIL"),
                rs.getString("PASSWORD"),
                rs.getString("NAME"),
                rs.getTimestamp("REGDATE").toLocalDateTime());
                member.setId(rs.getLong("ID"));
                return member;
          }
        }, email);
    return results.isEmpty() ? null : results.get(0);
  }
```

이 코드에서 쿼리는 인덱스 파라미터<sup>?</sup>를 포함하고 있다. 인덱스 파라미터에 들어갈 값은 `}, email);` 부분에서 지정한다.
이 query() 메서드의 세 번째 파라미터는 가변 인자로 인덱스 파라미터가 두 개 이상이면 다음과 같이 각 값을 콤마로 구분한다.

```java
List<Member> results = jdbcTemplate.query(
        "select * from MEMBER where EMAIL = ?",
        new RowMapper<Member>() {...}, email, name); // 물음표 개수만큼 값 전달
```

RowMapper는 ResultSet에서 데이터를 읽어와 Member 객체로 변환해주는 기능을 제공하므로 RowMapper의 타입으로
Member를 사용했다<sup>RowMapper<Member></sup>. mapRow() 메서드는 파라미터로 전달받은 ResultSet에서 데이터를 읽어와 Member 객체를  생성해서 리턴하도록 구현했다.

람다를 사용하면 훨씬 간단하게 구현이 가능하다.

```java
      List<Member> results = jdbcTemplate.query(
          "select * from MEMBER where EMAIL = ?",
          (rs, rowNum) -> {
            Member member = new Member(
                rs.getString("EMAIL"),
                rs.getString("PASSWORD"),
                rs.getString("NAME"),
                rs.getTimestamp("REGDATE").toLocalDateTime());
            member.setId(rs.getLong("ID"));
            return member;
          }, email);
```

동일한 RowMapper 구현을 여러 곳에서 사용한다면 아래 처럼 RowMapper 인터페이스를 구현한 클래스를 만들어 코드 중복을 막을 수 있다.

```java
public class MemberRowMapper implements RowMapper<Member> {

  @Override
  public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
    Member member = new Member(
        rs.getString("EMAIL"),
        rs.getString("PASSWORD"),
        rs.getString("NAME"),
        rs.getTimestamp("REGDATE").toLocalDateTime());
    member.setId(rs.getLong("ID"));
    return member;
  }

}

  public Member selectByEmail(final String email) {
    List<Member> results = jdbcTemplate.query(
        "select * from MEMBER where EMAIL = ?", new MemberRowMapper(), email);
    return results.isEmpty() ? null : results.get(0);
  }
```

query() 메서드는 쿼리를 실행한 결과가 존재하지 않으면 길이가 0인 List를 리턴하므로 isEmpty()로 결과가 존재하는지의 여부를 확인할 수 있다.

이제 MemberDao에서 JdbcTemplate의 query()를 사용하는 또 다른 메서드인 selectAll()을 구현해보자.

> *MemberDao.java*

```java
  ...
  public List<Member> selectAll() {
    return jdbcTemplate.query("select * from MEMBER", new MemberRowMapper());
  }
  ...
```

RowMapper 구현 클래스를 이미 만들어놨으니 그대로 사용하면 된다.



### 결과가 1행인 경우 사용할 수 있는 queryForObject() 메서드

count(*) 쿼리는 결과가 한 행 뿐이니 쿼리 결과를 List로 받기보다는 정수 타입으로 받는 것이 편리할 것이다.
이를 위한 메서드가 바로 queryForObject() 이다. 이를 사용하면 count(\*) 쿼리 실행 코드를 다음 처럼 구현할 수 있다.

> *MemberDao.java*

```java
  ...
  public int count() {
    return jdbcTemplate.queryForObject("select count(*) from MEMBER", Integer.class);
  }
  ...
```

queryForObject() 메서드의 두 번째 파라미터는 칼럼을 읽어올 때 사용할 타입을 지정한다. 
예를 들어 평균을 구한다면 Double 타입을 사용할 수 있다.

```java
double avg = queryForObject(
  "select avg(height) from FURNITURE where TYPE=? and STATUS=?",
  Double.class, 100, "S");
```

위 코드에서 볼 수 있듯이 queryForObject() 메서드도 쿼리에 인덱스 파라미터를 사용할 수 있다.
인덱스 파라미터가 존재하면 파라미터의 값을 가변 인자로 전달한다.

실행 결과 칼럼이 두 개 이상이면 RowMapper를 파라미터로 전달해서 결과를 생성할 수 있다.
예를 들어 특정 ID를 갖는 회원 데이터를 queryForObject()로 읽어오고 싶다면 다음처럼 할 수 있다.

```java
Member member = jdbcTemplate.queryForObject(
  "select * from MEMBER where ID=?",
  new MemberRowMapper(), 100);
```

기존의 query() 메서드와의 차이점은 리턴 타입이 List가 아니라 RowMapper로 변환해주는 타입이라는 점이다.



주요 queryForObject() 메서드는 다음과 같다.

- T queryForObject(String sql, Class<T> requiredType)
- T queryForObject(String sql, Class<T> requiredType, Object... args)
- T queryForObject(String sql, RowMapper<T> rowMapper)
- T queryForObject(String sql, RowMapper<T> rowMapper, Object... args)



queryForObject() 메서드를 사용하려면 쿼리 실행 결과는 반드시 한 행이어야 한다. 만약 쿼리 실행 결과 행이 없거나 두 개 이상이면 IncorrectResultSizeDataAccessException이 발생한다. 행의 개수가 0이면 하위 클래스인 EmptyResultDataAccessException이 발생한다. 따라서 결과 행이 정확히 한 개가 아니면 query() 메서드를 사용해야 한다.



### JdbcTemplate을 이용한 변경 쿼리 실행

INSERT, UPDATE, DELETE 쿼리는 update() 메서드를 사용한다.

- int update(String sql)
- int update(String sql, Object... args)



update() 메서드는 쿼리 실행 결과로 변경된 행의 개수를 리턴한다. update() 메서드의 사용 예를 보자.

> *MemberDao.java*

```java
  ...
  public void update(final Member member) {
    jdbcTemplate.update(
        "update MEMBER set NAME = ?, PASSWORD = ? where EMAIL = ?",
        member.getName(), member.getPassword(), member.getEmail());

  }
  ...
```



### PreparedStatementCreator를 이용한 쿼리 실행

지금까지 작성한 코드는 쿼리에서 사용할 값을 인자로 전달했다.

```java
jdbcTemplate.update(
        "update MEMBER set NAME = ?, PASSWORD = ? where EMAIL = ?",
        member.getName(), member.getPassword(), member.getEmail());
```

대부분 이와 같은 방법으로 쿼리의 인덱스 파라미터의 값을 전달할 수 있다.

PreparedStatement의 set 메서드를 사용해서 직접 인덱스 파라미터의 값을 설정해야 할 때도 있는데, 이 때는
PreparedStatementCreator를 인자로 받는 메서드를 이용해서 직접 PreparedStatement를 생성하고 설정해야 한다.

PreparedStatementCreator 인터페이스는 다음과 같다.

```java
@FunctionalInterface
public interface PreparedStatementCreator {

	PreparedStatement createPreparedStatement(Connection con) throws SQLException;

}
```

createPreparedStatement() 메서드는 Connection 타입의 파라미터를 갖는다. PreparedStatementCreator를 구현한
클래스는 createPreparedStatement() 메서드의 파라미터로 전달받는 Connection을 이용해서 PreparedStatement 객체를 생성하고 인덱스 파라미터를 알맞게 설정한 뒤에 리턴하면 된다.

> *MemberDao.java*

```java
  ...
  public void insert(final Member member) {
    jdbcTemplate.update((Connection conn) -> {
      // 파라미터로 전달받은 Connection을 이용해서 PreparedStatement 생성
      PreparedStatement pstmt = conn.prepareStatement(
          "insert into MEMBER (EMAIL, PASSWORD, NAME, REGDATE) values (?, ?, ?, ?)");
      // 인덱스 파라미터의 값 설정
      pstmt.setString(1, member.getEmail());
      pstmt.setString(2, member.getPassword());
      pstmt.setString(3, member.getName());
      pstmt.setTimestamp(4, Timestamp.valueOf(member.getRegisterDateTime()));
      // 생성한 PreparedStatement 객체 리턴
      return pstmt;
    });
  }
  ...
```



JdbcTemplate가 제공하는 메서드 중 PreparedStatementCreator 인터페이스를 파라미터로 갖는 메서드는 다음과 같다.

- List<T> query(PreparedStatementCreator psc, RowMapper<T> rowMapper)
- int update(PreparedStatementCreator psc)
- int update(PreparedStatementCreator psc, KeyHolder generatedKeyHolder)

세 번째 메서드는 자동 생성되는 키값을 구할 때 사용한다. 이에 대한 내용을 살펴보자.



### INSERT 쿼리 실행 시 KeyHolder를 이용해서 자동 생성 키값 구하기

postgresql의 SERIAL 칼럼은 행이 추가되면 자동으로 값이 할당되는 칼럼으로서 주요키 칼럼에 사용된다.
SERIAL과 같은 자동 증가 칼럼을 가진 테이블에 값을 삽입하면 해당 칼럼의 값이 자동으로 생성된다.
œ따라서 INSERT 쿼리에 자동 증가 칼럼에 해당하는 값은 지정하지 않는다.

그런데 쿼리 실행 후에 생성된 키값을 알고 싶다면 어떻게 해야 할까?
update() 메서드는 변경된 행의 개수를 리턴할 뿐 생성된 키값을 리턴하지는 않는다.

JdbcTemplate은 KeyHolder라는 자동으로 생성된 키값을 구할 수 있는 방법을 제공하고 있다.
다음과 같이 MemberDao의 insert() 메서드에 삽입하는 Member 객체의 ID 값을 구할 수 있다.

> *MemberDao.java*

```java
  ...
  public void insert(final Member member) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update((Connection conn) -> {
      PreparedStatement pstmt = conn.prepareStatement(
          "insert into MEMBER (EMAIL, PASSWORD, NAME, REGDATE) values (?, ?, ?, ?)",
        new String[] {"ID"});
      pstmt.setString(1, member.getEmail());
      pstmt.setString(2, member.getPassword());
      pstmt.setString(3, member.getName());
      pstmt.setTimestamp(4, Timestamp.valueOf(member.getRegisterDateTime()));
      return pstmt;
    }, keyHolder);
    Number keyValue = keyHolder.getKey();
    member.setId(keyValue.longValue());
  }
  ...
```

1. GeneratedKeyHolder 객체를 생성한다. 이 클래스는 자동 생성된 키값을 구해주는  KeyHolder 구현 클래스이다.
2. update() 메서드는 PreparedStatementCreator 객체와 KeyHolder 객체를 파라미터로 갖는다.
3. Prepared 객체를 생성하는데 두 번째 파라미터는 자동 생성되는 키 칼럼 목록을 지정할 때 사용한다.
   MEMBER 테이블은 ID 칼럼이 자동 증가 키 칼럼이므로 두 번째 파라미터 값으로 String 배열인 {"ID"}를 주었다.
4. JdbcTemplate.update() 메서드의 두 번째 파라미터로 KeyHolder 객체를 전달한다.



JdbcTemplate의 update() 메서드는 PreparedStatement를 실행한 후 자동 생성된 키값을 KeyHolder에 보관한다.
KeyHolder에 보관된 키값은 getKey() 메서드로 구한다. 이 메서드는 java.lang.Number 타입으로 리턴하므로 Number의
intValue(), longValue() 등의 메서드로 원하는 타입의 값으로 변환 가능하다.



## MemberDao 테스트하기

지금까지 JdbcTemplate을 이용해서 MemberDao 클래스를 완성했다. 
간단한 메인 클래스를 작성해서 MemberDao가 정상적으로 동작하는지 확인해보자. 

> *MainForMemberDao.java*

```java
public class MainForMemberDao {

  private static MemberDao memberDao;
  private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddHHmmss");

  public static void main(String... args) {
    AnnotationConfigApplicationContext ctx =
        new AnnotationConfigApplicationContext(AppCtx.class);

    memberDao = ctx.getBean(MemberDao.class);

    selectAll();
    updateMember();
    insertMember();

    ctx.close();
  }

  private static void selectAll() {
    System.out.println("----- selectAll");
    System.out.println("전체 데이터: " + memberDao.count());
    for (Member m : memberDao.selectAll()) {
      System.out.println(m);
    }
  }

  private static void updateMember() {
    System.out.println("----- updateMember");
    Member member = memberDao.selectByEmail("daewon@wisoft.io");
    String oldPw = member.getPassword();
    String newPw = Double.toHexString(Math.random());
    member.changePassword(oldPw, newPw);

    memberDao.update(member);
    System.out.println("암호 변경: " + oldPw + " > " + newPw);
  }
  private static void insertMember() {
    System.out.println("----- insertMember");

    String prefix = formatter.format(LocalDateTime.now());
    Member member = new Member(prefix + "test.com", prefix, prefix, LocalDateTime.now());
    memberDao.insert(member);
    System.out.println(member.getId() + " 데이터 추가");
  }

}
```



**실행 결과**

```java
----- selectAll
전체 데이터: 1
1:daewon@wisoft.io:pdw
----- updateMember
암호 변경: 0x1.01cc8270127dbp-1 > 0x1.291b7b0618623p-1
----- insertMember
2 데이터 추가
```



## 스프링의 익셉션 변환 처리

postgresql 용 JDBC 드라이버는 SQL 문법이 잘못된 경우 SQLException을 상속받은 PSQLException을 발생시키는데
JdbcTemplate은 이 익셉션을 DataAccessException을 상속받은 BadSqlGrammerException으로 변환한다.

DataAccessException은 데이터 연결에 문제가 있을 때 스프링 모듈이 발생시킨다.
이렇게 변환하여 발생시키는 주된 이유는 연동 기술에 상관없이 동일하게 익셉션을 처리할 수 있도록 하기 위함이다.
스프링은 JDBC, JPA, Mybatis 등에 대한 연동을 지원하는데 각각의 구현 기술마다 익셉션을 다르게 처리해야 한다면
개발자는 기술마다 익셉션 처리 코드를 작성해야 할 것이다. **각 연동 기술에 따라 발생하는 익셉션을 스프링이 제공하는**
**익셉션으로 변환함으로써 구현 기술에 상관없이 동일한 코드로 익셉션을 처리할 수 있게 된다**.

또한 DataAccessException은 RuntimeException이다. JDBC를 직접 이용하면 try~catch나 메서드의 throws 처리로 
반드시 SQLException을 처리해야 하는데 DataAccessException은 RuntimeException<sup>언체크 예외</sup>이므로 필요한 경우에만
익셉션을 처리하면 된다.

```java
// JDBC
try {
  pstmt = conn.prepareStatement(someQuery);
  ...
} catch (SQLException ex) {
  ... 
}

// 스프링
jdbcTemplate.update(someQuery, param1);
```



## 트랜잭션 처리

예를 들어 이메일이 유효한지를 판단하기 위해 다음 두 쿼리를 실행한다고 하자.

```java
jdbcTemplate.update("update MEMBER set EMAIL = ?", email);
jdbcTemplate.update("insert EMAIL_AUTH values (? 'T')", email);
```

그런데 만약 첫 번째 쿼리를 실행한 후 두 번째 쿼리를 실행하는 시점에 문제가 발생한다면 첫 번째 쿼리 실행 결과가 DB에 반영
되고 이후 해당 사용자의 이메일 주소는 인증되지 않은 상태로 남아 있게 될 것이다.

따라서 두 번째 쿼리 실행에 실패하면 첫 번째 쿼리도 취소해야 올바른 상태를 유지할 것이다.

이렇게 두 개 이상의 쿼리를 한 작업으로 실행해야 할 때 사용하는 것이 트랜잭션<sup>transaction</sup>이다.

- 트랜잭션은 여러 쿼리를 논리적으로 하나의 작업으로 묶어준다. 
- 한 트랜잭션으로 묶인 쿼리 중 하나라도 실패하면 전체 쿼리를 실패로 간주하고 이전 실행한 쿼리를 취소한다.
- 실행 결과를 취소하고 DB를 기존 상태로 되돌리는 것을 롤백, 실행에 성공하여 DB에 실제 반영하는 것을 커밋이라 한다.

JDBC는 setAutoCommit()과 commit(), rollback()을 사용하여 직접 트랜잭션 범위를 관리하기 때문에 개발자가 트랜잭션을
커밋하는 코드나 롤백하는 코드를 누락하기 쉽다. 게다가 구조적인 중복이 반복되는 문제도 있다.

스프링이 제공하는 트랜잭션 기능을 사용하면 중복이 없는 매우 간단한 코드로 트랜잭션 범위를 지정할 수 있다.



### @Transactional을 이용한 트랜잭션 처리

스프링이 제공하는 이 @Transactional 애노테이션을 사용하면 트랜잭션 범위를 매우 쉽게 지정할 수 있다.
다음과 같이 트랜잭션 범위에서 실행하고 싶은 메서드에 @Transactional 애노테이션만 붙이면 된다.

```java
  @Transactional
  public void changePassword(final String email, String oldPwd, final String newPwd) {
    Member member = memberDao.selectByEmail(email);
    if (member == null) throw new MemberNotFoundException();
    member.changePassword(oldPwd, newPwd);
    memberDao.update(member);
  }
```

스프링은 위 메서드를 동일한 트랜잭션 범위에서 실행한다. 따라서 memberDao.selectByEmail()에서 실행하는 쿼리와
member.changePassword()에서 실행하는 쿼리는 한 트랜잭션에 묶인다.

@Transactional 애노테이션이 제대로 동작하려면 다음의 두 가지 내용을 스프링 설정에 추가해야 한다.

- 플랫폼 트랜잭션 매니저<sup>PlatformTransactionManager</sup> 빈 설정
- @Transaction 애노테이션 활성화 설정



> *AppCtx.java*

```java
@Configuration
@EnableTransactionManagement
public class AppCtx {

  ...

  @Bean
  public PlatformTransactionManager transactionManager() {
    DataSourceTransactionManager tm = new DataSourceTransactionManager();
    tm.setDataSource(dataSource());
    return tm;
  }
  ...
```

- PlatformTransactionManager는 스프링이 제공하는 트랜잭션 매니저 인터페이스이다. 스프링은 구현기술에 상관없이
  동일한 방식으로 트랜잭션을 처리하기 위해 이 인터페이스를 사용한다. 
  JDBC는 DataSourceTransactionManager 클래스를 PlatformTransactionManager로 사용한다.
  dataSource 프로퍼티를 이용해서 트랜잭션 연동에 사용할 DataSource를 지정한다.
- EnableTransactionManagement 애노테이션은 @Transactional 애노테이션이 붙은 메서드를 트랜잭션 범위에서
  실행하는 기능을 활성화한다. 등록된 PlatformTransactionManager 빈을 사용해서 트랜잭션을 적용한다.
- 트랜잭션 처리를 위한 설정을 완료하면 트랜잭션 범위에서 실행하고 싶은 스프링 빈 객체의 메서드에 @Transactional을
  붙이면 된다. 



> *예: ChangePasswordService.java*

```java
public class ChangePasswordService {

  private MemberDao memberDao;

  @Transactional
  public void changePassword(final String email, String oldPwd, final String newPwd) {
    Member member = memberDao.selectByEmail(email);
    if (member == null) throw new MemberNotFoundException();
    member.changePassword(oldPwd, newPwd);
    memberDao.update(member);
  }
  ...
```

실제로 트랜잭션 범위에서 실행되는지 보기위해 AppCtx 설정 클래스에 트랜잭션 관련 설정과 ChangePasswordService 클래스를 빈으로 추가하자.

> *AppCtx.java*

```java
...
  @Bean
  public ChangePasswordService changePasswordService() {
    ChangePasswordService pwdSvc = new ChangePasswordService();
    pwdSvc.setMemberDao(memberDao());
    return pwdSvc;
  }
...
```



> *MainForCPS.java*

```java
public class MainForCPS {

  public static void main(String... args) {
    AnnotationConfigApplicationContext ctx =
        new AnnotationConfigApplicationContext(AppCtx.class);

    ChangePasswordService cps =
        ctx.getBean(ChangePasswordService.class);

    try {
      cps.changePassword("daewon@wisoft.io", "1234", "1111");
      System.out.println("암호를 변경했습니다.");
    } catch (MemberNotFoundException e) {
      System.out.println("회원 데이터가 존재하지 않습니다.");
    } catch (WrongIdPasswordException e) {
      System.out.println("암호가 올바르지 않습니다.");
    }
    ctx.close();
  }

}
```

위 코드를 실행하면 실제로 트랜잭션이 시작되고 커밋되는지 확인할 수 없다.
이를 확인하는 방법은 스프링이 출력하는 로그 메시지를 보는 것이다. 트랜잭션과 관련된 로그 메시지를 추가로 출력하기 위해
Logback을 사용해보자.

> 스프링 5 버전은 자체 로깅 모듈인 spring-jcl을 사용한다. 이 로깅 모듈은 직접 로그를 남기지 않고 다른 로깅 모듈을
> 사용해서 로그를 남긴다. 예를 들어 클래스패스에 Logback이 존재하면 Logback을 이용해서 로그를 남기고 Log4j2가
> 존재하면 Log4j2를 이용해서 로그를 남긴다. 따라서 사용할 로깅 모듈만 클래스패스에 추가해주면 된다.



> *build.gradle*

```groovy
dependencies {
    testCompile group: 'junit', name: 'junit', version: '4.12'
    implementation 'org.springframework:spring-context:5.2.3.RELEASE'
    implementation 'org.springframework:spring-jdbc:5.2.3.RELEASE'
    implementation 'org.apache.tomcat:tomcat-jdbc:9.0.31'
    implementation 'org.postgresql:postgresql:42.2.10.jre7'
    implementation 'org.slf4j:slf4j-api:1.7.30'
    implementation 'ch.qos.logback:logback-classic:1.3.0-alpha5'
}
```



클래스 패스에 Logback 설정 파일을 위치시켜야 하므로 src/main/resources 패키지에 다음과 같이 작성하자.

> *logback.xml*

```xml
<?xml version="1.0" encoding="UTF-8"?>

<configuration>
    <appender name="stdout" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d %5p %c{2} - %m%n</pattern>
        </encoder>
    </appender>
    <root level="INFO">
        <appender-ref ref="stdout"/>
    </root>
    
    <logger name="org.springframework.jdbc" level="DEBUG"/> // 로그 메시지 상세하게 (DEBUG 레벨로)
</configuration>
```



이제 MainForCPS를 실행해보자. 

**실행 결과**

2020-03-03 05:44:13,267 DEBUG o.s.j.d.DataSourceTransactionManager - Creating new transaction with name [spring.ChangePasswordService.changePassword]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
2020-03-03 05:44:14,737 DEBUG o.s.j.d.DataSourceTransactionManager - Acquired Connection [ProxyConnection[PooledConnection[org.postgresql.jdbc.PgConnection@6f80fafe]]] for JDBC transaction
2020-03-03 05:44:14,739 DEBUG o.s.j.d.<u>**DataSourceTransactionManager - Switching JDBC Connection**</u> [ProxyConnection[PooledConnection[org.postgresql.jdbc.PgConnection@6f80fafe]]] to manual commit
2020-03-03 05:44:14,748 DEBUG o.s.j.c.JdbcTemplate - Executing prepared SQL query
2020-03-03 05:44:14,749 DEBUG o.s.j.c.JdbcTemplate - Executing prepared SQL statement [select * from member where EMAIL = ?]
2020-03-03 05:44:14,844 DEBUG o.s.j.c.JdbcTemplate - Executing prepared SQL update
2020-03-03 05:44:14,844 DEBUG o.s.j.c.JdbcTemplate - Executing prepared SQL statement [update MEMBER set NAME = ?, PASSWORD = ? where EMAIL = ?]
2020-03-03 05:44:14,906 DEBUG o.s.j.d.<u>**DataSourceTransactionManager - Initiating transaction commit**</u>
2020-03-03 05:44:14,906 DEBUG o.s.j.d.<u>**DataSourceTransactionManager - Committing JDBC transaction on Connection**</u> [ProxyConnection[PooledConnection[org.postgresql.jdbc.PgConnection@6f80fafe]]]
2020-03-03 05:44:14,970 DEBUG o.s.j.d.DataSourceTransactionManager - Releasing JDBC Connection [ProxyConnection[PooledConnection[org.postgresql.jdbc.PgConnection@6f80fafe]]] after transaction
암호를 변경했습니다.



강조 표시한 메시지를 보면 트랜잭션을 시작하고 커밋한다는 로그를 확인할 수 있다.

DB에 있는 암호와 코드에서 입력한 암호가 맞지 않는다면 WrongIdPasswordException이 발생한다. 이때 콘솔에 출력되는
로그 메시지를 확인해보자.

**실행 결과**

...

2020-03-03 05:49:32,488 DEBUG o.s.j.d.DataSourceTransactionManager - Switching JDBC Connection [ProxyConnection[PooledConnection[org.postgresql.jdbc.PgConnection@6f80fafe]]] to manual commit
2020-03-03 05:49:32,496 DEBUG o.s.j.c.JdbcTemplate - Executing prepared SQL query
2020-03-03 05:49:32,497 DEBUG o.s.j.c.JdbcTemplate - Executing prepared SQL statement [select * from member where EMAIL = ?]
2020-03-03 05:49:32,590 DEBUG o.s.j.d.DataSourceTransactionManager - **<u>Initiating transaction rollback</u>**
2020-03-03 05:49:32,590 DEBUG o.s.j.d.DataSourceTransactionManager - **<u>Rolling back JDBC transaction on Connection</u>** [ProxyConnection[PooledConnection[org.postgresql.jdbc.PgConnection@6f80fafe]]]

...

트랜잭션을 롤백헀다는 로그 메시지가 찍힌다. 그렇다면 도대체 트랜잭션을 시작하고, 커밋하고, 롤백하는 것은 누가 어떻게 처리하는 건지 의문이 생긴다. 이 내용을 이해하려면 프록시를 알아야 한다.



### @Transactional과 프록시

앞서 여러 빈 객체에 공통으로 적용되는 기능을 구현하는 방법으로 AOP를 설명했는데 트랜잭션도 공통 기능 중 하나이다.
스프링은 @Transactional 애노테이션을 이용해서 트랜잭션을 처리하기 위해 내부적으로 AOP를 사용한다.
스프링에서 AOP는 프록시를 통해서 구현된다는 것을 생각하면 트랜잭션 처리도 프록시를 통해서 이루어진다고 유추할 수 있다.

실제로 @Transactional 애노테이션을 적용하기 위해 @EnableTransactionManagement 태그를 사용하면 스프링은 
@Transactional 애노테이션이 적용된 빈 객체를 찾아서 알맞은 프록시 객체를 생성한다. MainForCPS를 예를 들어보자.

![image](https://user-images.githubusercontent.com/43429667/75717380-5e3ce680-5d14-11ea-81b8-b0c77f9e399b.png)

ChangePasswordService 클래스의 메서드에 @Transactional 애노테이션이 적용되어 있으므로 스프링은 트랜잭션 기능을 적용한 프록시 객체를 생성한다. MainForCPS 클래스에서 getBean(ChangePasswordService.class) 코드를 실행하면
ChangePasswordService 객체 대신에 트랜잭션 처리를 위해 생성한 프록시 객체를 리턴한다.

이 프록시 객체는 @Transactional이 붙은 메서드를 호출하면 1.1 처럼 PlatformTransactionManager를 사용해서 트랜잭션을 시작한다. 트랜잭션을 시작한 후 실제 객체의 메서드를 호출하고<sup>1.2~1.3</sup>, 성공적으로 실행되면 트랜잭션을 커밋한다.



### @Transactional 적용 메서드의 롤백 처리

커밋을 수행하는 주체가 프록시 객체였듯이 롤백을 처리하는 주체 또한 프록시 객체이다.

```java
    try {
      cps.changePassword("daewon@wisoft.io", "1234", "1111");
      System.out.println("암호를 변경했습니다.");
    } catch (MemberNotFoundException e) {
      System.out.println("회원 데이터가 존재하지 않습니다.");
    } catch (WrongIdPasswordException e) {
      System.out.println("암호가 올바르지 않습니다.");
    }
```

이 부분을 보면 WrongIdPasswordException이 발생했을 때 트랜잭션이 롤백된 것을 알 수 있다. 실제로 @Transactional을
처리하기 위한 프록시 객체는 원본 객체의 메서드를 실행하는 과정에서 RuntimeException이 발생하면 트랜잭션을 롤백한다.

![image](https://user-images.githubusercontent.com/43429667/75718187-d9eb6300-5d15-11ea-9064-822cc0053de5.png)

별도 설정을 추가하지 않으면 발생한 익셉션이 RuntimeException일 때 트랜잭션을 롤백한다.
WrongIdPasswordException 클래스를 구현할 때 RuntimeException을 상속한 이유는 바로 트랜잭션 롤백을
염두해 두었기 때문이다.

JdbcTemplate은 DB 연동 과정에서 문제가 생기면 DataAccessException을 발생한다 했는데 역시 RuntimeException
이므로 JdbcTemplate의 기능을 실행하는 도중 익셉션이 발생해도 프록시는 트랙잭션을 롤백한다.

만약 RuntimeException이 아닌 익셉션이 발생한 경우에도 트랜잭션을 롤백하고 싶다면 @Transactional의 rollbackFor 
속성을 사용해야 한다.

```java
@Transactional(rollbackFor = {SQLException.class, IOException.class})
public void someMethod() {
  ...
}
```

이와 반대 설정을 제공하는 것은 noRollbackFor 속성이다. 이 속성은 지정한 익셉션이 발생해도 커밋한다.



### @Transactional의 주요 속성

보통 이 속성들을 사용할 일이 없지만 간혹 필요할 때가 있으니 있다는 정도만 알고 넘어가자.

| 속성        | 타입        | 설명                                                         |
| ----------- | ----------- | ------------------------------------------------------------ |
| value       | String      | 트랜잭션을 관리할 때 사용할 PlatformTransactionManager 빈의 이름을 지정한다. 기본값은 " ". |
| propagation | Propagation | 트랜잭션 전파 타입을 지정한다. 기본값은 Propagation.REQUIRED. |
| isolation   | Isolation   | 트랜잭션 격리 레벨을 지정한다. 기본값은 Isolation.DEFAULT.   |
| timeout     | int         | 트랜잭션 제한 시간을 지정한다. 기본값은 -1로 이 경우 데이터베이스의 타임아웃 시간을 사용한다. 초 단위로 지정한다. |



@Transactional 애노테이션의  value 속성값이 없으면 등록된 빈 중에서 타입이 PlatformTransactionManager인 빈을 
사용한다. AppCtx 설정 클래스는 DataSourceTransactionManager를 트랜잭션 관리자로 사용했다.

Propagation 열거 타입에 정의되어있는 값은 트랜잭션 전파와 관련되어 있는데 이 내용은 뒤에서 설명한다.

Isolation 열거 타입에 정의된 값은 다음과 같다.

| 값               | 설명                                                         |
| ---------------- | ------------------------------------------------------------ |
| DEFAULT          | 기본 설정을 사용한다.                                        |
| READ_UNCOMMITTED | 다른 트랜잭션이 커밋하지 않은 데이터를 읽을 수 있다.         |
| READ_COMMITTED   | 다른 트랜잭션이 커밋한 데이터를 읽을 수 있다.                |
| REPEATABLE_READ  | 처음에 읽어 온 데이터와 두 번째 읽어 온 데이터가 동일한 값을 찾는다. |
| SERIALIZABLE     | 동일한 데이터에 대해서 동시에 두 개 이상의 트랜잭션을 수행할 수 없다. |

> 트랜잭션 격리 레벨은 동시에 DB에 접근할 때 그 접근을 어떻게 제어할지에 대한 설정을 다룬다.



### @EnableTransactionManagement 애노테이션의 주요 속성

| 속성             | 설명                                                         |
| ---------------- | ------------------------------------------------------------ |
| proxyTargetClass | 클래스를 이용해서 프록시를 생성할지 여부를 지정한다. 기본값은 false로서 인터페이스를 이용해서 프록시를 생성한다. |
| order            | AOP의 적용 순서를 지정한다. 기본값은 가장 낮은 우선순위에 해당하는  Integer.MAX_VALUE다. |



### 트랜잭션 전파

Propagation 열거 타입 값 목록에서  REQUIRED 값의 설명은 다음과 같다.

- 메서드를 수행하는 데 트랜잭션이 필요하다는 것을 의미한다. 현재 진행 중인 트랜잭션이 존재하면 그 트랜잭션을 사용한다.
  존재하지 않으면 새로운 트랜잭션을 생성한다.



이 설명을 이해하려면 트랜잭션 전파가 무엇인지 알아야 한다. 

```java
public class SomeService {
  
  private AnyService anyService;
  
  @Transactional
  public void some() {
    anyService.any();
  }
  
  public void setAnyService(final AnyService as) {
    this.anyService = as;
  }
  
  
}

public class AnyService {
  @Transactional
  public void any() {...}
}
```

```java
@Configuration
@EnableTransactionManagement
public class Config {
  
  @Bean
  public SomeService some() {
    ...
  }
  
  @Bean
  public AnyService any() {
    ...
  }
  
}
```

위의 설정에 따르면 두 클래스에 대해 프록시가 생성된다. 즉 SomeService의 some() 메서드와 AnyService()의 any() 메서드는 호출하면 트랜잭션이 시작된다. 그런데 some() 메서드는 내부에서 다시 any() 메서드를 호출하고 있다.

@Transactional의 propagation 속성의 기본값은 Propagation.REQUIRED이다. REQUIRED는 현재 진행 중인 트랜잭션이 존재하면 해당 트랜잭션을 사용하고 존재하지 않으면 새로운 트랜잭션을 생성한다고 했다. 처음 some() 메서드를 호출하면 
트랜잭션을 새로 시작한다. 하지만  some() 메서드 내부에서  any() 메서드를 호출하면 이미  some() 메서드에 의해 시작된
트랜잭션이 존재하므로 any() 메서드를 호출하는 시점에는 새로 생성하지 않는다. 대신 존재하는 트랜잭션을 그대로 사용한다.
즉  some() 메서드와 any() 메서드를 한 트랜잭션으로 묶어서 실행하는 것이다.

```java
public class ChangePasswordService {

  private MemberDao memberDao;

  @Transactional
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
public class MemberDao {
  ...
  public void update(final Member member) {
    jdbcTemplate.update(
        "update MEMBER set NAME = ?, PASSWORD = ? where EMAIL = ?",
        member.getName(), member.getPassword(), member.getEmail());
  }
  ...
```

changePassword() 메서드는 MemberDao의 update() 메서드를 호출하고 있다. 그런데 MemberDao.update()는
@Transactional 애노테이션이 적용되어있지 않다. 이 경우 JdbcTemplate 클래스 덕분에 트랜잭션 범위에서 쿼리를 실행할 수
있게 된다. JdbcTemplate은 진행 중인 트랜잭션이 존재하면 해당 트랜잭션 범위에서 쿼리를 실행한다.

![image](https://user-images.githubusercontent.com/43429667/75722913-83ceed80-5d1e-11ea-9a69-e32e38d002ec.png)

- 과정 1에서 트랜잭션을 시작한다. ChangePasswordService의 @Transactional이 붙은 메서드를 실행하므로 프록시가 트랜잭션을 시작한다. 
- 과정 2.1.1과 2.2.1은 JdbcTemplate을 실행한다. 이 시점에서 트랜잭션이 진행 중이다. 이 경우  JdbcTemplate은 이미 진행 중인 트랜잭션 범위에서 쿼리를 실행한다. 따라서 changePassword() 메서드에서 실행하는 모든 쿼리는 하나의 범위에서 실행된다. 과정 2와 2.3 사이에서 익셉션이 발생해서 롤백하면 2.2.1의 수정 쿼리도 롤백된다.



## 전체 기능 연동한 코드 실행

> *AppCtx.java*

```java
package config;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import spring.*;

@Configuration
@EnableTransactionManagement
public class AppCtx {

  @Bean(destroyMethod = "close")
  public DataSource dataSource() {
    DataSource ds = new DataSource();
    ds.setDriverClassName("org.postgresql.Driver");
    ds.setUrl("jdbc:postgresql://satao.db.elephantsql.com:5432/voipmttw");
    ds.setUsername("voipmttw");
    ds.setPassword("9U_pMlo8JwC-4kkYy83YFip30jEhS1Xi");
    ds.setInitialSize(2);
    ds.setMaxActive(10);
    ds.setTestWhileIdle(true); // 유휴 커넥션 검사
    ds.setMinEvictableIdleTimeMillis(1000 * 60 * 3); // 최소 유휴 시간 3분
    ds.setTimeBetweenEvictionRunsMillis(1000 * 10);  // 10초 주기로 검사
    return ds;
  }

  @Bean
  public PlatformTransactionManager transactionManager() {
    DataSourceTransactionManager tm = new DataSourceTransactionManager();
    tm.setDataSource(dataSource());
    return tm;
  }

  @Bean
  public MemberDao memberDao() {
    return new MemberDao(dataSource());
  }

  @Bean
  public MemberRegisterService memberRegisterService() {
    return new MemberRegisterService(memberDao());
  }

  @Bean
  public ChangePasswordService changePasswordService() {
    ChangePasswordService pwdSvc = new ChangePasswordService();
    pwdSvc.setMemberDao(memberDao());
    return pwdSvc;
  }

  @Bean
  public MemberPrinter memberPrinter() {
    return new MemberPrinter();
  }

  @Bean
  public MemberListPrinter listPrinter() {
    return new MemberListPrinter(memberDao(), memberPrinter());
  }

  @Bean
  public MemberInfoPrinter infoPrinter() {
    MemberInfoPrinter infoPrinter = new MemberInfoPrinter();
    infoPrinter.setMemberDao(memberDao());
    infoPrinter.setPrinter(memberPrinter());
    return infoPrinter;
  }

}
```



> *Main.java*

```java
package main;

import config.AppCtx;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import spring.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

  private static AnnotationConfigApplicationContext ctx = null;

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
      } else if (command.startsWith("change ")) {
        processChangeCommand(command.split(" "));
      } else if (command.equals("list")) {
        processListCommand();
      } else if (command.startsWith("info ")) {
        processInfoCommand(command.split(" "));
      } else {
        printHelp();
      }
    }
    ctx.close();
  }

  private static void processNewCommand(final String... args) {
    if (args.length != 5) {
      printHelp();
      return;
    }
    MemberRegisterService regSvc =
        ctx.getBean(MemberRegisterService.class);
    RegisterRequest req = new RegisterRequest();
    req.setEmail(args[1]);
    req.setName(args[2]);
    req.setPassword(args[3]);
    req.setConfirmPassword(args[4]);

    if (req.isPasswordEqualToConfirmPassword()) {
      System.out.println("암호와 확인이 일치하지 않습니다.\n");
      return;
    }
    try {
      regSvc.regist(req);
      System.out.println("등록했습니다.\n");
    } catch (DuplicateMemberException e) {
      System.out.println("이미 존재하는 이메일입니다\n");
    }
  }

  private static void processChangeCommand(final String... arg) {
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

  private static void processListCommand() {
    MemberListPrinter listPrinter =
        ctx.getBean("listPrinter", MemberListPrinter.class);
    listPrinter.printAll();
  }

  private static void processInfoCommand(String... arg) {
    if (arg.length != 2) {
      printHelp();
      return;
    }
    MemberInfoPrinter infoPrinter =
        ctx.getBean("infoPrinter", MemberInfoPrinter.class);
    infoPrinter.printMemberInfo(arg[1]);
  }

  private static void printHelp() {
    System.out.println();
    System.out.println("잘못된 명령입니다. 아래 명령어 사용법을 확인하세요.");
    System.out.println("명령어 사용법:");
    System.out.println("new 이메일 이름 암호 암호확인");
    System.out.println("change 이메일 현재비번 변경비번");
    System.out.println("info 이메일");

    System.out.println();
  }

}
```

