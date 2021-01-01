package persistence;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Currency;

import javax.persistence.Column;

/**
 * I create this custom value type in order to handle both amount and currency
 * As you can see there is no setter method which means MonetaryAmount is an
 * immutable class and this is good because simplify coding
 */

public class MonetaryAmount implements Serializable {

  private static final long serialVersionUID = 13456L;

  /**
   * This field is settled by default to 1 during insertion in this case you have
   * to trigger a select after an insertion or update in order to get this value
   * but you have to keep the column updatable and insertable
   * 
   */

  @Column(name = "AMOUNT", columnDefinition = "number(10,2) default '1'")
  @org.hibernate.annotations.Generated(org.hibernate.annotations.GenerationTime.INSERT)
  private final BigDecimal amount;

  private final Currency currency;

  public MonetaryAmount(BigDecimal amount, Currency currency) {
    this.amount = amount;
    this.currency = currency;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public Currency getCurrency() {
    return currency;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || o.getClass() != getClass())
      return false;

    MonetaryAmount that = (MonetaryAmount) o;

    if (amount != null && !amount.equals(that.amount))
      return false;
    if (currency != null && !currency.equals(that.currency))
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((amount == null) ? 0 : amount.hashCode());
    result = prime * result + ((currency == null) ? 0 : currency.hashCode());

    return result;
  }

  public Object convertTo(Currency userCurrency) {
    return null;
  }

  public static MonetaryAmount convert(MonetaryAmount anyCurrency, Currency instance) {
    return null;
  }

}
