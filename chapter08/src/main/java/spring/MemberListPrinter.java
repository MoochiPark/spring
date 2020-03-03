package spring;

public class MemberListPrinter {

  private MemberDao memberDao;
  private MemberPrinter printer;

  public MemberListPrinter(final MemberDao memberDao, final MemberPrinter printer) {
    this.memberDao = memberDao;
    this.printer = printer;
  }

  public void printAll() {
    memberDao.selectAll().forEach(m -> printer.print(m));
  }

}
