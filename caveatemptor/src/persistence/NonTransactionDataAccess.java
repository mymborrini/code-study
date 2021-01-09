package persistence;

import org.hibernate.Session;

import model.Item;

/**
 * Let's talk about autcommit mode. The term nontransactional data access means
 * there are no EXPLICIT transaction boundaires no system transaction and that
 * the behaviour of data access is that of the autocommit mode It doesn't mean
 * no physical database transactions are involved
 */
public class NonTransactionDataAccess {

  /**
   * A new session is opened, It does not obtain a database connection at this
   * point Call get method triggers an sql select. The session now obtains a JDBC
   * connection from the connection pool. Hibernate by default immediatly turns
   * off the autocommit mode. This effectively starts a jdbc transaction The
   * Select is executed inside this JDBC transaction. The session is closed and
   * the connection is returned to the pool and released by Hibernate. Hibernate
   * calls close on JDBC Connection.
   * 
   * What happened to this uncommit transaction... it depends on the JDBC vendor
   * For example Oracle roll back all the transaction so in this case since ypu
   * have a select nothing chage but what about an update... in that case no
   * update will be take place since everything is rolledback
   */
  public void nonTransactionalExample() {
    Session session = sessionFactory.openSession();
    session.get(Item.class, 123L);
    session.close();
  }

}
