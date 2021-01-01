package persistence;

import java.util.Properties;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.Serializable;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.common.util.ReflectHelper;
import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.usertype.ParameterizedType;

/**
 * Why should you implement a my own custom mapping type for enumeration since
 * Hibernate and JPA can persist and load enumeration. In general is a good
 * exercise and can see EnhancedUserType with xml behavior. Since if you want to
 * implement this through an xml configuration there are a lot of parameters you
 * need to know In this case somwthing like this will be enough
 * 
 * <typedef class="persistence.StringEnumUserType" name="rating_enum_type">
 * <param name="enumClassname">model.Rating</param> </typedef>
 * 
 * <basic name="rating">
 * <column name="RATING" type="rating" not-null="true" update= "false" access=
 * "field" />
 * 
 */
@org.hibernate.annotations.TypeDefs({
    @org.hibernate.annotations.TypeDef(name = "rating_enum_type", typeClass = persistence.MonetaryAmountConversionType.class, parameters = {
        @Parameter(name = "enumClass", value = "model.Rating") }) })
public class StringEnumUserType implements EnhancedUserType, ParameterizedType {

  private Class<Enum> enumClass;

  /**
   * The configuration parameter for this custom mapping type is the name of the
   * enumeration class it's used for
   */
  public void setParameterValues(Properties parameters) {
    String enumClassName = parameters.getProperty("enumClassname");

    try {
      enumClass = ReflectHelper.classForName(enumClassName);
    } catch (ClassNotFoundException e) {
      throw new HibernateException("Enum class not found", e);
    }

  }

  public Class<?> returnedClass() {
    return enumClass;
  }

  /**
   * A single varchar column is enough to store the enumeration
   */
  public int[] sqlTypes() {
    return new int[] { Hibernate.STRING.sqlType() };
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
   * The methods below are part of the EnhancedUserType and are used for XML
   * marshalling
   */
  @Override
  public Object fromXMLString(String xmlValue) {
    return Enum.valueOf(enumClass, xmlValue);
  }

  @Override
  public String objectToSQLString(Object value) {
    return '\'' + ((Enum) value).name() + '\'';
  }

  @Override
  public String toXMLString(Object value) {
    return ((Enum) value).name();
  }

  /**
   * When you are loading an enumeration, you get its name from the database and
   * create an instance
   */
  @Override
  public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner) throws SQLException {
    String name = resultSet.getString(names[0]);
    return resultSet.wasNull() ? null : Enum.valueOf(enumClass, name);
  }

  /**
   * When you're saving an enumeration, you store its name as string
   */
  @Override
  public void nullSafeSet(PreparedStatement st, Object value, int index) throws SQLException {
    if (value == null) {
      st.setNull(index, Hibernate.STRING.sqlType());
    } else {
      st.setString(index, ((Enum) value).name());
    }
  }

}
