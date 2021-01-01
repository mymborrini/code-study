package persistence;

import java.util.Currency;
import java.util.Properties;
import java.io.Serializable;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Parameter;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.math.BigDecimal;

/**
 * Suppose now to have currency column and have some table where the amount is
 * saved as USD and some others where is saved in EUR. When you save You want to
 * convert with the Currency this Converter was parameterized with So the
 * solution is to implements ParameterizedType. In this case you also have two
 * properly define the types you want to use in order to set the parameterized
 * types
 */

@org.hibernate.annotations.TypeDefs({
    @org.hibernate.annotations.TypeDef(name = "monetary_amount_usd", typeClass = persistence.MonetaryAmountConversionType.class, parameters = {
        @Parameter(name = "convertTo", value = "USD") }),
    @org.hibernate.annotations.TypeDef(name = "monetary_amount_eur", typeClass = persistence.MonetaryAmountConversionType.class, parameters = {
        @Parameter(name = "convertTo", value = "EUR") }) })
public class MonetaryAmountConversionType implements ParameterizedType, UserType {

  // Configuration Parameter
  private Currency convertTo;

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
   * Get in this case has no surpise I just take the value and the currency from
   * db
   */
  @Override
  public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner) throws SQLException {
    BigDecimal value = resultSet.getBigDecimal(names[0]);

    // Deferred check after first read;
    if (resultSet.wasNull())
      return null;

    // take the currency from the database
    Currency currency = Currency.getInstance(resultSet.getString(names[1]));

    return new MonetaryAmount(value, currency);

  }

  /**
   * This method will convert all to the parameterized currency
   */
  @Override
  public void nullSafeSet(PreparedStatement statement, Object value, int index) throws SQLException {
    if (value == null) {
      statement.setNull(index, Types.NUMERIC);
      statement.setNull(index + 1, Types.VARCHAR);
    } else {
      MonetaryAmount amount = (MonetaryAmount) value;
      // When storing convert the amount to the currency which was parameterized
      MonetaryAmount dbAmount = MonetaryAmount.convert(amount, convertTo);

      statement.setBigDecimal(index, dbAmount.getAmount());
      statement.setString(index + 1, dbAmount.getCurrency().getCurrencyCode());
    }
  }

  /**
   * Hiberante will call this method to initialize this class (on startup) with a
   * convertTo parameter
   */
  @Override
  public void setParameterValues(Properties parameters) {
    this.convertTo = Currency.getInstance(parameters.getProperty("convertTo"));
  }

}
