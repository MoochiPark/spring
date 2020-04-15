package io.wisoft.pdw.spring;

import io.wisoft.pdw.mapper.MemberRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;

public class MemberDao {

  private JdbcTemplate jdbcTemplate;

  public MemberDao(final DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  public Member selectByEmail(final String email) {
    List<Member> results = jdbcTemplate.query(
        "select * from member where EMAIL = ?", new MemberRowMapper(), email);
    return results.isEmpty() ? null : results.get(0);
  }

  public void insert(final Member member) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update((Connection conn) -> {
      PreparedStatement pstmt = conn.prepareStatement(
          "insert into MEMBER (EMAIL, PASSWORD, NAME, REGDATE) values (?, ?, ?, ?)", new String[] {"id"});
      pstmt.setString(1, member.getEmail());
      pstmt.setString(2, member.getPassword());
      pstmt.setString(3, member.getName());
      pstmt.setTimestamp(4, Timestamp.valueOf(member.getRegisterDateTime()));
      return pstmt;
    }, keyHolder);
    Number keyValue = keyHolder.getKey();
    member.setId(keyValue.longValue());
  }

  public void update(final Member member) {
    jdbcTemplate.update(
        "update MEMBER set NAME = ?, PASSWORD = ? where EMAIL = ?",
        member.getName(), member.getPassword(), member.getEmail());
  }

  public List<Member> selectAll() {
    return jdbcTemplate.query("select * from MEMBER", new MemberRowMapper());
  }

  public int count() {
    return jdbcTemplate.queryForObject("select count(*) from MEMBER", Integer.class);
  }

}