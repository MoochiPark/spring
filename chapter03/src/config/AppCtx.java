package chapter03.config;

import chapter03.spring.*;
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
  ChangePasswordService changePwdSvc() {
    ChangePasswordService changePwdSvc = new ChangePasswordService();
    changePwdSvc.setMemberDao(memberDao());
    return changePwdSvc;
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
    //    infoPrinter.setMemberDao(memberDao());
//    infoPrinter.setPrinter(memberPrinter());
    // 세터 메서드를 사용해서 의존 주입을 하지 않아도
    // 스프링 컨테이너가 @Autowired를 붙인 필드에
    // 자동으로 해당 타입의 빈 객체를 주입
    return new MemberInfoPrinter();
  }

  @Bean
  public VersionPrinter versionPrinter() {
    VersionPrinter versionPrinter = new VersionPrinter();
    versionPrinter.setMajorVersion(5);
    versionPrinter.setMinorVersion(0);
    return versionPrinter;
  }

}
