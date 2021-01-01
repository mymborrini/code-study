package persistence;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Currency;

import org.hibernate.Hibernate;
import org.hibernate.usertype.UserType;

import model.User;

/**
 * In this case We suppose that the database save all the currency in USD when
 * we load the information we need to convert them into the currency specified
 * by the user
 */

public class MonetaryAmountUserType implements UserType {

  /**
   * This method tells Hibernate what sql columns types to use for DDL schema
   * generation. In this case returning BIG_DECIMAL sqlType we let Hibernate
   * decide which type column is better based on Dialect
   */
  @Override
  public int[] sqlTypes() {
    return new int[] { Hibernate.BIG_DECIMAL.sqlType() };
  }

  /**
   * This method tells hibernate what java value type class is mapped
   */
  @Override
  public Class<?> returnedClass() {
    return MonetaryAmount.class;
  }

  /**
   * For immutable type like this one hibernate some minor performance
   * optimization during update
   */
  @Override
  public boolean isMutable() {
    return false;
  }

  /**
   * The usertype is also partially responsable for creating a snapshot of value
   * in the first place. In this case since is an immutable class the deep copy
   * returns its arguments, otherwise it would return a copy of the argument to be
   * used as a snapshot value
   */
  @Override
  public Object deepCopy(Object value) {
    return value;
  }

  /**
   * The disassemble method is called when hibernate puts a MonetaryAmount into
   * the second-level cache. This is a cache of data that stores information in a
   * serialized form
   */
  @Override
  public Serializable disassemble(Object value) {
    return (Serializable) value;
  }

  /**
   * The assemble method does the opposite of disasseble. It will transform cached
   * data into an instance of MonetaryAmount
   */
  @Override
  public Object assemble(Serializable cached, Object owner) {
    return cached;
  }

  /**
   * This function implements merging of detached object state. In this case since
   * is immutable returning the original object is enough
   */
  @Override
  public Object replace(Object original, Object target, Object owner) {
    return original;
  }

  /**
   * The two methods below is used in dirty checking to check if an object has
   * been changed
   */
  @Override
  public boolean equals(Object x, Object y) {
    if (x == y)
      return true;
    if (x == null || y == null)
      return false;
    return x.equals(y);
  }

  @Override
  public int hashCode(Object x) {
    return x.hashCode();
  }

  /**
   * This is the core, this method intercept the resultSet returned from JDBC
   * query and, in this case convert the MonetaryAmount into the one the user
   * wants
   */
  @Override
  public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner) throws SQLException {
    BigDecimal valueInUSD = resultSet.getBigDecimal(names[0]);

    // Deferred check after first read;
    if (resultSet.wasNull())
      return null;

    Currency userCurrency = User.getPreferences().getCurrency();
    MonetaryAmount amount = new MonetaryAmount(valueInUSD, Currency.getInstance("USD"));
    return amount.convertTo(userCurrency);

  }

  /**
   * This method is called when the prepared statement for jdbc is called, in this
   * case it takes anyCurrency from MonetaryAmount and convert it to USD since in
   * this example all the currency in db are in USD
   */
  @Override
  public void nullSafeSet(PreparedStatement statement, Object value, int index) throws SQLException {
    if (value == null) {
      statement.setNull(index, Hibernate.BIG_DECIMAL.sqlType());
    } else {
      MonetaryAmount anyCurrency = (MonetaryAmount) value;
      MonetaryAmount amountInUSD = MonetaryAmount.convert(anyCurrency, Currency.getInstance("USD"));
      statement.setBigDecimal(index, amountInUSD.getAmount());
    }
  }

}
