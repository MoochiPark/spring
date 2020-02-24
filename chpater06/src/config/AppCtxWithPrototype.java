package io.wisoft.daewon.config;

import io.wisoft.daewon.spring.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class AppCtxWithPrototype {

  @Bean
  @Scope("prototype")
  public Client client() {
    Client client = new Client();
    client.setHost("host");
    return client;
  }

}
