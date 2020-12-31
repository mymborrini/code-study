package model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@AttributeOverride(name = "owner", column = @Column(name = "CC_OWNER", nullable = false))
public class CreditCard extends BillingDetails {

  @Id
  @Column(name = "CREDIT_CARD_ID")
  private Long id = null;

  @Column(name = "NUMBER", nullable = false)
  private String number;

  @Column(name = "EXP_MONTH", nullable = false)
  private String expMonth;

  @Column(name = "EXP_YEAR", nullable = false)
  private String expYear;

  public Long getId() {
    return id;
  }

  public String getNumber() {
    return number;
  }

  public String getExpMonth() {
    return expMonth;
  }

  public String getExpYear() {
    return expYear;
  }

  public void setId(Long id) {
    this.id = id;
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
