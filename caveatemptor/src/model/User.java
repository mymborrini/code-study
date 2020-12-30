package model;

import java.util.StringTokenizer;

public class User {

  private String firstName;
  private String lastName;

  // Generally an hibernate custom type is better to handle this
  public String getName() {
    return firstName + " " + lastName;
  }

  public void setName(String name) {
    StringTokenizer t = new StringTokenizer(name);
    firstName = t.nextToken();
    lastName = t.nextToken();
  }

}
