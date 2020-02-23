package io.wisoft.daewon.chapter05.spring;

import io.wisoft.daewon.chapter05.annotation.ManualBean;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class MemberDao {

  private static long nextId = 0;

  private Map<String, Member> map = new HashMap<>();

  public Member selectByEmail(final String email) {
    return map.get(email);
  }

  public void insert(final Member member) {
    member.setId(++nextId);
    map.put(member.getEmail(), member);
  }

  public void update(final Member member) {
    map.put(member.getEmail(), member);
  }

  public Collection<Member> selectAll() {
    return map.values();
  }

}