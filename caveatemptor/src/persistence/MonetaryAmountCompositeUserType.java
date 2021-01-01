package persistence;

import org.hibernate.usertype.CompositeUserType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Currency;

import org.hibernate.Hibernate;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.Type;

/**
 * This is the same as MonetaryAmountUserType except that here are required two
 * columns not only the amount. So not all the value are USD in this case but
 * there is a column name currency. In this case I can implement a new type
 * separatly and then just change the type into the hibernate type annotations
 * 
 */
public class MonetaryAmountCompositeUserType implements CompositeUserType {

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
   * This function implements merging of detached object state. In this case since
   * is immutable returning the original object is enough
   */
  @Override
  public Object replace(Object original, Object target, SessionImplementor implementor, Object owner) {
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
   * Loading a value now is straight forward i can transform the two column values
   * in a MonetaryAmount directly
   */
  @Override
  public Object nullSafeGet(ResultSet resultSet, String[] names, SessionImplementor session, Object owner)
      throws SQLException {
    BigDecimal value = resultSet.getBigDecimal(names[0]);
    if (resultSet.wasNull())
      return null;

    Currency currency = Currency.getInstance(resultSet.getString(names[1]));
    return new MonetaryAmount(value, currency);
  }

  /**
   * In this case setting a value involves 2 parameters on the prepared statement
   */
  @Override
  public void nullSafeSet(PreparedStatement statement, Object value, int index, SessionImplementor session)
      throws SQLException {
    if (value == null) {
      statement.setNull(index, Hibernate.BIG_DECIMAL.sqlType());
      statement.setNull(index + 1, Hibernate.CURRENCY.sqlType());
    } else {
      MonetaryAmount amount = (MonetaryAmount) value;
      String currencyCode = amount.getCurrency().getCurrencyCode();

      statement.setBigDecimal(index, amount.getAmount());
      statement.setString(index + 1, currencyCode);

    }
  }

  /**
   * A CompositeTypeUser must expose the property names
   */
  @Override
  public String[] getPropertyNames() {
    return new String[] { "amount", "currency" };
  }

  /**
   * A CompositeTypeUser must expose the property types
   */
  @Override
  public Type[] getPropertyTypes() {
    return new Type[] { Hibernate.BIG_DECIMAL, Hibernate.CURRENCY };
  }

  /**
   * Return the value of an Individual property
   */
  @Override
  public Object getPropertyValue(Object component, int property) {
    MonetaryAmount monetaryAmount = (MonetaryAmount) component;
    if (property == 0) {
      return monetaryAmount.getAmount();
    } else {
      return monetaryAmount.getCurrency();
    }
  }

  @Override
  public void setPropertyValue(Object component, int property, Object value) {
    throw new UnsupportedOperationException("Immutable Monetary Amount");
  }

  /**
   * The disassemble method is called when hibernate puts a MonetaryAmount into
   * the second-level cache. This is a cache of data that stores information in a
   * serialized form
   */
  @Override
  public Serializable disassemble(Object value, SessionImplementor session) {
    return (Serializable) value;
  }

  /**
   * The assemble method does the opposite of disasseble. It will transform cached
   * data into an instance of MonetaryAmount
   */
  @Override
  public Object assemble(Serializable cached, SessionImplementor session, Object owner) {
    return cached;
  }

}
