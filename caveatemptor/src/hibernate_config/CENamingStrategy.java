package hibernate_config;

import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.util.StringHelper;

// With this abstract class i can create a custom naming strategy

public class CENamingStrategy extends ImprovedNamingStrategy {

  private static final long serialVersionUID = 999887766L;

  // This method is called only if annotations like Table, column or joinColumn
  // does not specify a specific name
  public String classToTableName(String className) {
    return StringHelper.unqualify(className);
  }

  public String propertyToColumnName(String propertyName) {
    return propertyName;
  }

  // This is called when a name in Table column or joinColumn annotations are
  // specified
  public String tableName(String tableName) {
    return "CE_" + tableName;
  }

  public String columnName(String columnName) {
    return columnName;
  }

  public String propertyToTableName(String className, String propertyName) {
    return "CE_" + classToTableName(className) + "_" + propertyToColumnName(propertyName);
  }

}
