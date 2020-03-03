package main;

import config.AppCtx;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import spring.Member;
import spring.MemberDao;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainForMemberDao {

  private static MemberDao memberDao;
  private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddHHmmss");

  public static void main(String... args) {
    AnnotationConfigApplicationContext ctx =
        new AnnotationConfigApplicationContext(AppCtx.class);

    memberDao = ctx.getBean(MemberDao.class);

    selectAll();
    updateMember();
    insertMember();

    ctx.close();
  }

  private static void selectAll() {
    System.out.println("----- selectAll");
    System.out.println("전체 데이터: " + memberDao.count());
    for (Member m : memberDao.selectAll()) {
      System.out.println(m);
    }
  }

  private static void updateMember() {
    System.out.println("----- updateMember");
    Member member = memberDao.selectByEmail("daewon@wisoft.io");
    String oldPw = member.getPassword();
    String newPw = Double.toHexString(Math.random());
    member.changePassword(oldPw, newPw);

    memberDao.update(member);
    System.out.println("암호 변경: " + oldPw + " > " + newPw);
  }
  private static void insertMember() {
    System.out.println("----- insertMember");

    String prefix = formatter.format(LocalDateTime.now());
    Member member = new Member(prefix + "test.com", prefix, prefix, LocalDateTime.now());
    memberDao.insert(member);
    System.out.println(member.getId() + " 데이터 추가");
  }

}
