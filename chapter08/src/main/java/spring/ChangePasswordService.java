package spring;

import org.springframework.transaction.annotation.Transactional;

public class ChangePasswordService {

  private MemberDao memberDao;

  @Transactional
  public void changePassword(final String email, String oldPwd, final String newPwd) {
    Member member = memberDao.selectByEmail(email);
    if (member == null) throw new MemberNotFoundException();
    member.changePassword(oldPwd, newPwd);
    memberDao.update(member);
  }

  public void setMemberDao(final MemberDao memberDao) {
    this.memberDao = memberDao;
  }

}