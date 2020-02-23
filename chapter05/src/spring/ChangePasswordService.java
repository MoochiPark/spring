package io.wisoft.daewon.chapter05.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
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