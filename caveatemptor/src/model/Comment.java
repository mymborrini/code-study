package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity
public class Comment {

  @org.hibernate.annotations.Type(type = "rating_enum_type")
  // @Enumerated(EnumType.STRING)
  @Column(name = "RATING", nullable = false, updatable = false)
  private Rating rating;
  private Item auction;

  public Comment() {
  }

}
