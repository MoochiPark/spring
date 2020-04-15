package io.wisoft.pdw.controller;

import io.wisoft.pdw.survey.AnsweredData;
import io.wisoft.pdw.survey.Question;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/survey")
public class SurveyController {

  @GetMapping
  public ModelAndView form() {
    List<Question> questions = createQuestion();
    ModelAndView mav = new ModelAndView();
    mav.setViewName("survey/surveyForm");
    return mav.addObject("questions", questions);
  }

  private List<Question> createQuestion() {
    return List.of(new Question("당신의 역할은 무엇입니까?", List.of("서버", "프론트", "풀스택")),
        new Question("많이 사용하는 개발 도구는?", List.of("이클립스", "intelliJ", "서브라임")),
        new Question("하고싶은 말은?"));
  }


  @PostMapping
  public String submit(@ModelAttribute("ansData") final AnsweredData data) {
    return "survey/submmited";
  }

}
