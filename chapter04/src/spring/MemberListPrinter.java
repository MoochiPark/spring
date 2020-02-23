package io.wisoft.daewon.chapter04.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class MemberListPrinter {

  private MemberDao memberDao;
  private MemberPrinter printer;

  public MemberListPrinter() {
  }

  public MemberListPrinter(final MemberDao memberDao, final MemberPrinter printer) {
    this.memberDao = memberDao;
    this.printer = printer;
  }

  public void printAll() {
    memberDao.selectAll().forEach(m -> printer.print(m));
  }

  @Autowired
  public void setMemberDao(final MemberDao memberDao) {
    this.memberDao = memberDao;
  }

  @Autowired
  public void setPrinter(final MemberSummaryPrinter printer) {
    this.printer = printer;
  }

}
