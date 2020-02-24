package io.wisoft.daewon.main;

import io.wisoft.daewon.config.AppCtx;
import io.wisoft.daewon.spring.Client;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public class Main {

  public static void main(String... args) {
    AbstractApplicationContext ctx = new AnnotationConfigApplicationContext(AppCtx.class);

    Client client = ctx.getBean(Client.class);
    client.send();

    ctx.close();
  }

}
