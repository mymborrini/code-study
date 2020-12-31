package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;

@Entity
@PrimaryKeyJoinColumn(name = "CREDIT_CARD_ID")
public class CreditCard extends BillingDetails {

  @Column(name = "NUMBER", nullable = false)
  private String number;

  @Column(name = "EXP_MONTH", nullable = false)
  private String expMonth;

  @Column(name = "EXP_YEAR", nullable = false)
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
