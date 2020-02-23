package io.wisoft.daewon.chapter05.config;

import io.wisoft.daewon.chapter05.annotation.ManualBean;
import io.wisoft.daewon.chapter05.annotation.NoProduct;
import io.wisoft.daewon.chapter05.spring.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(basePackages = {"io.wisoft.daewon.chapter05.spring"},
//  excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "spring\\..*Dao"))
//  excludeFilters = @ComponentScan.Filter(type = FilterType.ASPECTJ, pattern = "spring.*Dao"))
//    excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION,
//    classes = {NoProduct.class, ManualBean.class} ))
//    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
//      classes = MemberDao.class ))
    excludeFilters = {
      @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = ManualBean.class),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "spring2\\..*")
    })
public class AppCtxWithExclude {

  @Bean
  public MemberDao memberDao() {
    return new MemberDao();
  }

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
