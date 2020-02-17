package chapter03.config;

import chapter03.spring.MemberDao;
import chapter03.spring.MemberPrinter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConf1 {

  @Bean
  public MemberDao memberDao() {
    return new MemberDao();
  }

  @Bean
  public MemberPrinter memberPrinter() {
    return new MemberPrinter();
  }

}
