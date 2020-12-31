package model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "BANK_ACCOUNT")
@AttributeOverride(name = "owner", column = @Column(name = "BA_OWNER", nullable = false))
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
