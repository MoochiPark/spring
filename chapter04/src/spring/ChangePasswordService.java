package io.wisoft.daewon.chapter04.spring;

import org.springframework.beans.factory.annotation.Autowired;

public class ChangePasswordService {

  @Autowired
  private MemberDao memberDao;

  public void changePassword(final String email, String oldPwd, final String newPwd) {
    Member member = memberDao.selectByEmail(email);
    if (member == null) throw new MemberNotFoundException();
    member.changePassword(oldPwd, newPwd);
    memberDao.update(member);
  }

  @Autowired
  public void setMemberDao(final MemberDao memberDao) {
    this.memberDao = memberDao;
  }

}