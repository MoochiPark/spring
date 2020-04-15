package io.wisoft.pdw.controller;

import io.wisoft.pdw.spring.DuplicateMemberException;
import io.wisoft.pdw.spring.MemberRegisterService;
import io.wisoft.pdw.spring.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegisterController {

  private MemberRegisterService memberRegisterService;

  public void setMemberRegisterService(MemberRegisterService memberRegisterService) {
    this.memberRegisterService = memberRegisterService;
  }

  @RequestMapping("/register/step1")
  public String handleStep1() {
    return "register/step1";
  }

  @PostMapping("/register/step2")
  public String handleStep2(@RequestParam(value = "agree", defaultValue = "false") Boolean agree, Model model) {
    if (!agree) {
      return "register/step1";
    }
    model.addAttribute("formData", new RegisterRequest());
    return "register/step2";
  }

  @GetMapping("/register/step2")
  public String handleStep2Get() {
    return "redirect:/register/step1";
  }

  @PostMapping("/register/step3")
  public String handleStep3(@ModelAttribute("formData") final RegisterRequest regReq) {
    try {
      memberRegisterService.regist(regReq);
      return "register/step3";
    } catch (DuplicateMemberException e) {
      return "register/step2";
    }
  }


}
