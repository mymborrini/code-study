package model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@AttributeOverride(name = "owner", column = @Column(name = "BA_OWNER", nullable = false))
public class BankAccount extends BillingDetails {

  @Id
  @Column(name = "BANK_ACCOUNT_ID")
  private Long id = null;

  @Column(name = "ACCOUNT", nullable = false)
  private String account;

  @Column(name = "BANK_NAME", nullable = false)
  private String bankName;

  @Column(name = "SWIFT", nullable = false)
  private String swift;

  public Long getId() {
    return id;
  }

  public String getAccount() {
    return account;
  }

  public String getBankName() {
    return bankName;
  }

  public String getSwift() {
    return swift;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public void setBankName(String bankName) {
    this.bankName = bankName;
  }

  public void setSwift(String swift) {
    this.swift = swift;
  }
}
