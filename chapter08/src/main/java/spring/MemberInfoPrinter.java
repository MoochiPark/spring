package spring;

import org.springframework.beans.factory.annotation.Autowired;

public class MemberInfoPrinter {

  @Autowired
  private MemberDao memberDao;
  @Autowired
  private MemberPrinter printer;

  public void printMemberInfo(final String email) {
    Member member = memberDao.selectByEmail(email);
    if (member == null) {
      System.out.println("데이터 없음\n");
      return;
    }
    printer.print(member);
    System.out.println();
  }

  public void setMemberDao(final MemberDao memberDao) {
    this.memberDao = memberDao;
  }

  public void setPrinter(final MemberPrinter printer) {
    this.printer = printer;
  }

}
