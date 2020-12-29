package persistence;

import org.hibernate.*;
import org.hibernate.cfg.*;
import org.hibernate.tool.hbm2ddl.SchemaValidator;

public class HibernateUtil {

  private static SessionFactory sessionFactory;

  static {

    try {
      Configuration configuration = new Configuration().configure();
      new SchemaValidator(configuration).validate();
      sessionFactory = configuration.buildSessionFactory();
    } catch (Throwable ex) {
      throw new ExceptionInInitializerError(ex);
    }

  }

  public static SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  public static void shutdown() {
    getSessionFactory().close();
  }

}