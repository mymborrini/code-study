package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;

@Entity
@PrimaryKeyJoinColumn(name = "BANK_ACCOUNT_ID")
public class BankAccount extends BillingDetails {

  @Column(name = "ACCOUNT", nullable = false)
  private String account;

  @Column(name = "BANK_NAME", nullable = false)
  private String bankName;

  @Column(name = "SWIFT", nullable = false)
  private String swift;

  public String getAccount() {
    return account;
  }

  public String getBankName() {
    return bankName;
  }

  public String getSwift() {
    return swift;
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
