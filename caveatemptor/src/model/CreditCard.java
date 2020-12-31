package model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "CREDIT_CARD")
@DiscriminatorValue("CC")
@AttributeOverride(name = "owner", column = @Column(name = "CC_OWNER", nullable = false))
public class CreditCard extends BillingDetails {

  @Column(name = "NUMBER")
  private String number;

  @Column(name = "EXP_MONTH")
  private String expMonth;

  @Column(name = "EXP_YEAR")
  private String expYear;

  public String getNumber() {
    return number;
  }

  public String getExpMonth() {
    return expMonth;
  }

  public String getExpYear() {
    return expYear;
  }

  public void setNumber(String number) {
    this.number = number;
  }

  public void setExpMonth(String expMonth) {
    this.expMonth = expMonth;
  }

  public void setExpYear(String expYear) {
    this.expYear = expYear;
  }

}
