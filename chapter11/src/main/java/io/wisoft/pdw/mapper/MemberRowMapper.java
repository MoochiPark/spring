package io.wisoft.pdw.mapper;


import io.wisoft.pdw.spring.Member;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MemberRowMapper implements RowMapper<Member> {

  @Override
  public Member mapRow(final ResultSet rs, final int rowNum) throws SQLException {
    Member member = new Member(
        rs.getString("EMAIL"),
        rs.getString("PASSWORD"),
        rs.getString("NAME"),
        rs.getTimestamp("REGDATE").toLocalDateTime());
    member.setId(rs.getLong("ID"));
    return member;
  }

}
