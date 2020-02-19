package io.wisoft.daewon.chapter04.config;

import io.wisoft.daewon.chapter04.spring.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppCtx {

  @Bean
  public MemberDao memberDao() {
    return new MemberDao();
  }

  @Bean
  public MemberRegisterService memberRegSvc() {
    return new MemberRegisterService(memberDao());
  }

  @Bean
  public ChangePasswordService changePwdSvc() {
    return new ChangePasswordService();
  }

  @Bean
  @Qualifier("printer")
  public MemberPrinter memberPrinter1() {
    return new MemberPrinter();
  }

  @Bean
  public MemberPrinter memberPrinter2() {
    return new MemberPrinter();
  }

  @Bean
  public MemberListPrinter listPrinter() {
    return new MemberListPrinter();
  }

  @Bean
  public MemberInfoPrinter infoPrinter() {
    return new MemberInfoPrinter();
  }

  @Bean
  public VersionPrinter versionPrinter() {
    return new VersionPrinter();
  }

}
