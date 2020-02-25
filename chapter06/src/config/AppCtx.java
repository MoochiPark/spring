package io.wisoft.daewon.config;

import io.wisoft.daewon.spring.Client;
import io.wisoft.daewon.spring.Client2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppCtx {

  @Bean
  public Client client() {
    Client client = new Client();
    client.setHost("host");
    return client;
  }

  @Bean(initMethod = "connect", destroyMethod = "close")
  public Client2 client2() {
    Client2 client = new Client2();
    client.setHost("host");
    return client;
  }

}
