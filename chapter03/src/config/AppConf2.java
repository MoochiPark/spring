package chapter03.config;

import chapter03.spring.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConf2 {

  @Autowired
  private MemberDao memberDao;          // 스프링 오류로 인해 붉은 라인이 나온다.

  @Autowired
  private MemberPrinter memberPrinter;

  @Bean
  public MemberRegisterService memberRegSvc() {
    return new MemberRegisterService(memberDao);
  }

  @Bean
  public ChangePasswordService changePwdSvc() {
    ChangePasswordService pwdSvc = new ChangePasswordService();
    pwdSvc.setMemberDao(memberDao);
    return pwdSvc;
  }

  @Bean
  public MemberListPrinter listPrinter() {
    return new MemberListPrinter(memberDao, memberPrinter);
  }

  @Bean
  public MemberInfoPrinter infoPrinter() {
    MemberInfoPrinter infoPrinter = new MemberInfoPrinter();
    infoPrinter.setMemberDao(memberDao);
    infoPrinter.setPrinter(memberPrinter);
    return infoPrinter;
  }

  @Bean
  public VersionPrinter versionPrinter() {
    VersionPrinter versionPrinter = new VersionPrinter();
    versionPrinter.setMajorVersion(5);
    versionPrinter.setMinorVersion(0);
    return versionPrinter;
  }

}
