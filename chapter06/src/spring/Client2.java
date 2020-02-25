package io.wisoft.daewon.spring;

import org.springframework.context.annotation.Bean;

public class Client2 {

  private String host;

  public void setHost(final String host) {
    this.host = host;
  }

  public void connect() {
    System.out.println("Client2.connect() 실행");
  }

  public void send() {
    System.out.println("Client2.send to " + host);
  }

  public void close() {
    System.out.println("Client2.close() 실행");
  }

}
