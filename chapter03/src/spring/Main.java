package chapter03.spring;

public class Main {

  public static void main(String... args) {
    MemberDao memberDao = new MemberDao();
    MemberRegisterService regSvc = new MemberRegisterService(memberDao);
    ChangePasswordService pwdSvc = new ChangePasswordService();
    pwdSvc.setMemberDao(memberDao);
    ... // regSvc와 pwdSvc를 사용하는 코드
  }

}