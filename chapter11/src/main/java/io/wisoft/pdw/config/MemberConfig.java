package io.wisoft.pdw.config;

import io.wisoft.pdw.spring.ChangePasswordService;
import io.wisoft.pdw.spring.MemberDao;
import io.wisoft.pdw.spring.MemberRegisterService;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class MemberConfig {

  @Bean(destroyMethod = "close")
  public DataSource dataSource() {
    DataSource ds = new DataSource();
    ds.setDriverClassName("org.postgresql.Driver");
    ds.setUrl("jdbc:postgresql://satao.db.elephantsql.com:5432/voipmttw?characterEncoding=utf8");
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

}


