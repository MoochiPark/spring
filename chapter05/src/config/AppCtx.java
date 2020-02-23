package io.wisoft.daewon.chapter05.config;

import io.wisoft.daewon.chapter05.spring.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(basePackages = {"io.wisoft.daewon.chapter05.spring"})
public class AppCtx {

  @Bean
  @Qualifier("printer")
  public MemberPrinter memberPrinter1() {
    return new MemberPrinter();
  }

  @Bean
  @Qualifier("summaryPrinter")
  public MemberSummaryPrinter memberPrinter2() {
    return new MemberSummaryPrinter();
  }

  @Bean
  public VersionPrinter versionPrinter() {
    return new VersionPrinter();
  }

}
