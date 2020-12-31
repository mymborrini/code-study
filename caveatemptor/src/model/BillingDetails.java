package model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

// Mapped superclass is necessary if you want all the properies to be mapped

@MappedSuperclass
public abstract class BillingDetails {

  @Column(name = "OWNER", nullable = false)
  private String owner;

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

}
