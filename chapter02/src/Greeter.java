package chapter02;

public class Greeter {

  private String format;

  public String greet(final String guest) {
    return String.format(this.format, guest);
  }

  public void setFormat(final String format) {
    this.format = format;
  }

}
