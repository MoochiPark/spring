package io.wisoft.pdw.survey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Question {

  private String title;
  private List<String> options;

  public Question(String title) {
    this.title = title;
  }

  public boolean isChoice() {
    return options != null && !options.isEmpty();
  }

}
