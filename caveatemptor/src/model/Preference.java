package model;

import java.util.Currency;

public class Preference {

  private Currency currency;

  public Preference(Currency currency) {
    this.currency = currency;
  }

  public Currency getCurrency() {
    return currency;
  }

  public void setCurrency(Currency currency) {
    this.currency = currency;
  }

}
