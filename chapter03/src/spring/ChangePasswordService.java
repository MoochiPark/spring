package chapter03.spring;

public class ChangePasswordService {

  private MemberDao memberDao;

  public void chagnePassword(final String email, String oldPwd, final String newPwd) {
    Member member = memberDao.selectByEmail(email);
    if (member == null) throw new MemberNotFoundException();
    member.changePassword(oldPwd, newPwd);
    memberDao.update(member);
  }

  public void setMemberDao(MemberDao memberDao) {
    this.memberDao = memberDao;
  }

}