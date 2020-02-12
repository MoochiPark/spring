package chapter03.assembler;

import chapter03.spring.ChangePasswordService;
import chapter03.spring.MemberDao;
import chapter03.spring.MemberRegisterService;

public class Assembler {

  private MemberDao memberDao;
  private MemberRegisterService regSvc;
  private ChangePasswordService pwdSvc;

  public Assembler() {
    memberDao = new MemberDao();
    regSvc = new MemberRegisterService(memberDao);
    pwdSvc = new ChangePasswordService();
    pwdSvc.setMemberDao(memberDao);
  }

  public MemberDao getMemberDao() {
    return memberDao;
  }

  public MemberRegisterService getMemberRegisterService() {
    return regSvc;
  }

  public ChangePasswordService getChangePasswordService() {
    return pwdSvc;
  }

}