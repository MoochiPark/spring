package io.wisoft.pdw.config;

import io.wisoft.pdw.controller.RegisterController;
import io.wisoft.pdw.controller.SurveyController;
import io.wisoft.pdw.spring.MemberRegisterService;
import io.wisoft.pdw.spring.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ControllerConfig {

  @Autowired
  private MemberRegisterService memberRegisterService;

  @Bean
  public RegisterController registerController() {
    RegisterController controller = new RegisterController();
    controller.setMemberRegisterService(memberRegisterService);
    return controller;
  }

  @Bean
  public SurveyController surveyController() {
    return new SurveyController();
  }

}
