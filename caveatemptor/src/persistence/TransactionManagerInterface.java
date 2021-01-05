package persistence;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.transaction.UserTransaction;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;
import org.hibernate.tool.hbm2ddl.SchemaValidator;

import jdk.jshell.spi.ExecutionControl.RunException;

/**
 * In a system that manipulates data in several database you need a transaction
 * manager that can handle several resources in one system transaction. Such
 * transaction processing system expose the Java Transaction API (JTA) for
 * interaction with the developer. This is different from Hibernate transaction
 * interface since it binds your code to direct JDBC.
 * 
 * Let's summeraize some interface and when they are used:
 * 
 * 1 java.sql.Connection -- Plain JDBC transaction demarcation with
 * setAutoCommit(false) , commit(), and rollback(). It can but shouldn't be used
 * in a Hibernate application, because it binds your application to a plain JDBC
 * environment.
 * 
 * 2 org.hibernate.Transaction -- Unifed transaction demarcation in Hibernate
 * application. It works in a nonmanaged plain JDBC environment and also in the
 * application server with JTA as the underlying system transaction service. Use
 * this API in Java SE if you can't have a JTA compatible transaction service
 * 
 * 3 javax.transaction.UserTransaction -- Standardized interface for
 * programmatic transaction control in Java. This should be your primary choice
 * whenever you have a JTA-compatible transaction service and want to control
 * transaction progreammatically
 * 
 * 4 javax.persistence.EntityTransaction -- Standardized interface for
 * programmatic transaction control in Java SE applications that use Java
 * Persistence
 * 
 * 
 * 
 */
public class TransactionManagerInterface {

  public SessionFactory getHibernateSessionFactory() {
    Configuration configuration = new Configuration().configure();
    new SchemaValidator(configuration).validate();
    return configuration.buildSessionFactory();
  }

  /**
   * Hibernate obtains a JDBC connection for each Session you're going to work
   * with A hibernate session is lazy, it means it does not consume any resources
   * unless they are absolutely needed, so when you call openSession() you don't
   * obtain a connection. Only when you Started a transaction you obtain a
   * connection and set autocommit to false. Beginning a new transaction with the
   * same session obtains a new connection from the pool. All hibernate exceptions
   * are runtime exception and there are many subtypes that help you to identify
   * the error:
   * 
   * 1 HibernateException -- is a generic error.
   * 
   * 2 JDBCException -- is any exception thrown by hibernate's internal JDBC layer
   * This kind of exception is always caused by a particular SQL statement and you
   * can get the offending statement with getSql()
   * 
   * All of this means that you have to catch Runtime exception, no matter how you
   * want to handle exception at compile time.
   * 
   * Is a BEST PRACTICE to use this exception not to validate your statement since
   * remeber all exception in hibernate are fatal. But to dispaly a better looking
   * fatal exception
   */
  public void hibernateTransaction() {

    Session session = null;
    Transaction transaction = null;

    try {
      session = getHibernateSessionFactory().openSession();
      transaction = session.beginTransaction();

      // Some methods invented
      concludeAuction(session);

      transaction.commit();

    } catch (RuntimeException e) {
      transaction.rollback();
    } finally {
      session.close();
    }

  }

  /**
   * A managed runtime environment can manage resource for you, in most cases the
   * resources that are managed are datbase connection For example an application
   * service. Jta will manage a pool of database connection for you. There are
   * some benefits of usign a transaction manager as jta:
   * 
   * 1 it will unify all resources. no matter of what type and expose transaction
   * control to you with a single standarized API. This means you can replace
   * Hibernate Transaction API and use JTA directly everywhere
   * 
   * 2 you can enlist multiple reources in a single transaction. If you work with
   * multiple database you probably want a two-phase commit protocol to guarantee
   * atomicity of a transaction across resource boundaries. In this case hiberate
   * is auomatically configured with several SessionFactorys and their session
   * obtain managed database connections that all participate in the same system
   * transaction
   * 
   * This is a very simplify case that show how it could be the code with multiple
   * resources. In this case I use jta unmanaged.
   * 
   */
  public void jtaHibernateTransaction() {
    javax.transaction.UserTransaction utx = (javax.transaction.UserTransaction) new InitialContext()
        .lookup("java:comp/UserTransaction");

    Session session1 = null;
    Session session2 = null;

    try {
      utx.begin();
      session1 = auctionDatabase.openSession();
      session2 = billingDatabase.openSession();

      cloncludeAuction(session1);
      billAuction(session2);

      utx.commit();

    } catch (RuntimeException ex) {
      try {
        utx.rollback();
      } catch (RuntimeException rbEx) {
        log.error("Couln't roll back transaction", rbEx);
      }
      throw ex;
    } finally {
      session1.close();
      session2.close();
    }
  }

  /**
   * In this case I use jta managed application. The same code i wrote prevously
   * with programmatic jta demarcation is now moed into a stateless session bean.
   * This in written with ejb3.0 but it should be valid for every other containre
   * rmanager like spring boot.
   * 
   * Once the method returns if the transaction started when this method is call
   * the transaction commits, If an exception occurs during this method everythins
   * is roll back
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void jtaTransactionInsideContainer() {
    Session sesssion1 = auctionDatabase.openSession();
    Session session2 = billingDatabase.openSession();

    concludeAuction(session1);
    billAuction(session2);
  }

  /**
   * With jpa you also have the design choice to make between programmatic
   * transaction demarcation in application code or declarative transaction. The
   * description resource-local transaction applies to all transactions that are
   * controlled by the application and that aren't partecipating in a global
   * system transaction. The entity transaction is available ONLY for resource-local
   * transaction.
   */
  public void jpaTransaction() {

    EntityManager em = null;
    EntityTransaction tx = null;

    try {
      em = emf.createEntityManager();
      tx = em.getTransaction();
      tx.begin();

      concludeAuction(em);

      tx.commit();

    } catch (Exception ex) {
      try {
        tx.rollback()
      } catch (Exception rbEx) {
       log.error("Could not roll back transaction", rbEx)
      }
      throw ex;
    } finally {
      em.close();
    }

  }

}
