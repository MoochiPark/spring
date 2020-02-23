package io.wisoft.daewon.chapter05.spring;

import java.time.LocalDateTime;

public class Member {

  private Long id;
  private String email;
  private String password;
  private String name;
  private LocalDateTime registerDateTime;

  public Member(final String email, final String password, final String name,
                final LocalDateTime registerDateTime) {
    this.email = email;
    this.password = password;
    this.name = name;
    this.registerDateTime = registerDateTime;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public String getName() {
    return name;
  }

  public LocalDateTime getRegisterDateTime() {
    return registerDateTime;
  }

  public void changePassword(final String oldPassword, final String newPassword) {
    if (!password.equals(oldPassword)) throw new WrongIdPasswordException();
    this.password = newPassword;
  }

}