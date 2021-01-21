package persistence;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.transaction.UserTransaction;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.context.ManagedSessionContext;

import model.Bid;
import model.Item;

/**
 * This is what we wanna do:
 * 
 * 1 Check the winning bid of the auction
 * 
 * 2 Charge the cost of the auction
 * 
 * 3 Notify the seller and the winner
 */
public class Conversations {

  public static SessionFactory getSessionFactory() {
    return null;
  }

  /**
   * 
   */
  public static class ManageAuctionHibernate {

    ItemDAO itemDAO = new ItemDAO();
    PaymentDAO paymentDAO = new PaymentDAO();

    /**
     * if no transaction is boundered this code is really dangerous it has an anti
     * pattern named session-per-operation and its dangerouse because each operation
     * has its own persistence context. A single persistence context shouldn't be
     * used to process a particular operation, but the whole event (which naturally
     * may require several operations). The scope of the persistence context is
     * often the same scope as the database transaction. This is also known as a
     * session-per-request
     * 
     * All the data-access code that calls getCurrentSession() on the global shared
     * sessionFactory gets access to the same currentSession if it's called on the
     * same thread. In fact hibernate binds the current session to the currently
     * java running thread
     */
    public void endAuction(Item item) {
      try {
        getSessionFactory().getCurrentSession().beginTransaction();
        // Reattch item
        itemDao.makePersistent(item);

        // Set winning bid
        Bid winningBid = itemDAO.getMaxBid(item.getId());
        item.setSuccessfulBid(winningBid);
        item.setBuyer(winningBid.getBidder());

        // Charge seller
        Payment payment = new Payment();
        paymentDAO.makePersistent(payment);

        // Notify seller and winner
        getSessionFactory().getCurrentSession().getTransaction().commit();

      } catch (Exception e) {
        try {
          getSessionFactory().getCurrentSession().getTransaction().rollback();
        } catch (Exception rbEx) {
          // TODO: handle exception
        }
        throw e;
      }

    }

  }

  /**
   * Let's do the same thing with JTA. As you see itemDAO is not changed, so even
   * with JTA you can use getCurrentSession() in order to achieve the current
   * session since jta is upon hibernate
   */
  public static class ManageAuctionJTA {

    UserTransaction utx = null;
    ItemDAO itemDAO = new ItemDAO();
    PaymentDAO paymentDAO = new PaymentDAO();

    public ManageAuctionJTA() {
      utx = (UserTransaction) new InitialContext().lookup("UserTransaction");
    }

    public void endAuction(Item item) {
      try {
        utx.begin();

        itemDAO.makePersistent(item);

        Bid winningBid = itemDAO.getMaxBid(item.getId());
        item.setSuccessfulBid(winningBid);
        item.setBuyer(winningBid.getBidder());

        // Charge seller
        Payment payment = new Payment();
        paymentDAO.makePersistent(payment);

        utx.commit();

      } catch (Exception e) {
        try {
          utx.rollback();
        } catch (Exception rbEx) {
          // TODO: handle exception
        }
        throw e;
      }
    }

  }

  public static class ManageAuctionDetachedObject {

    /**
     * First transaction to fetch the objects, simplify the code excluding exception
     * handling. The item object is now in a detached state
     */
    public Item getAuction(Long itemId) {
      Session session = getSessionFactory().getCurrentSession();
      session.beginTransaction();
      Item item = (Item) session.get(Item.class, itemId);
      session.getTransaction().commit();

      return item;
    }

    /**
     * The saveOrUpdate method in this case is really useful because in general you
     * don't know if item is in a transient state or in a detached state
     */
    public void endAuction(Item item) {
      Session session = getSessionFactory().getCurrentSession();
      session.beginTransaction();

      // Reattach item
      session.saveOrUpdate(item);

      // ALl other stuff...

      session.getTransaction().commit();

    }

  }

  /**
   * This is known as session per conversation strategy. A new session and
   * persistence context are opened at the beginning of the conversation. The
   * first step, loading of the item object is implemented in a first database
   * transaction. You commit the transaction but hold on the session and its
   * internal persistence context. Any object is never detached, so all the
   * modifications will be flushed at the end of this Session. Of course you have
   * to set the flushmode to manual since it must not be flushing at the end of
   * the first transaction.
   * 
   * Let's talk about a topic, the identifier generator during save(). In most of
   * cases like sequence ecc.. hibernate does not execute an instance insert in
   * database, since hibernate can work in-memory and does not need to execute an
   * insert to return the identifier. The exceptions are identifier generator that
   * are triggered after the insert occurs... one of them is identity which
   * require that a row is inserted first. In this case an immediate INSERT is
   * executed when you call save() because you're commiting database transactions
   * during conversations this insertion may have permanent effects. Since it's
   * already commited to database this could not be rolled back, or not rolled
   * back entirily. In mysql for example since you are inside a transaction what
   * happens is that you roll back your persistence context entirly, so you didn'
   * update any table in any case BUT the ids that hibernate had asked to mysql
   * won't be rolled back. So when you will make another insert in the future
   * mysql will have an empty sequence in the place of the ids asked from the
   * transaction rolled back.
   * 
   * The question now is how to manage an extending persistence context strategy
   * in case of jta since Jta or any other design pattern close the session after
   * commiting the transaction, Hibernate has a class named ManagedSessionContext
   * to manage this
   * 
   * In particular this class has 3 methods: bind, unbind, getCurrentSession
   * 
   * bind -- when a conversation starts a new Session must be opened and bound to
   * serve the first request in the conversation, you also have to set
   * FlushMode.MANUAL on that new Session, because you don't want any persistence
   * context synchronization to occur behind your back.
   * 
   * getCurrentSession -- All data access code that now calls
   * sessionFactory.getCurrentSession() receives the Session you bound
   * 
   * unbind -- when a request in the conversation completes, you need to call
   * unbind() and store the now disconnected Session somewhere, until the next
   * request in the conversation is made. Or, if it was the last request in the
   * conversation you need to flush and close the Session
   * 
   * All these steps can be implemented in an interceptor see
   * HibernateInterceptor.java
   * 
   *
   * 
   */
  public static class ManageAuctionExtendingPersistenceContext {

  }

  public static class ItemDAO {

    /**
     * You can use getCurrentSession in order to return the session inside the
     * transaction
     */
    public Bid getMaxBid(Long itemId) {
      Session session = getSessionFactory().getCurrentSession();
      return (Bid) session.createQuery("...").uniqueResult();
    }

  }

  /**
   * This is a pretty common code... just consider that the client when calling
   * endAuction needs to know that he have to discard the old item and substitute
   * it with the new one.
   */
  public static class ManageAuctionDetachedObjectJpa {

    public Item getAuction(Long itemId) {
      EntityManager em = emf.createEntityManager();
      EntityTransaction tx = em.getTransaction();

      tx.begin();

      Item item = em.find(Item.class, itemId);

      tx.commit();
      em.close();

      return item;
    }

    public Item endAuction(Item item) {

      EntityManager em = emf.createEntityManager();
      EntityTransaction tx = em.getTransaction();

      tx.begin();

      Item mergedItem = em.merge(item);

      // Do stuff

      tx.commit();
      em.close();

      return mergedItem;

    }

  }

  /**
   * 
   */
  public static class ManageAuctionExtendingPersistenceContextJpa {

  }

  /**
   * Let's explore conversations with JPA. like in hibernate you have to consider
   * three aspects when writing a conversations with JPA.
   * 
   * 1 JPA if it's deployed stand alone does not have the getCurrentSession()
   * feature
   * 
   * 2 If you decide to use a detached objects approach as your conversation
   * implementation strategy, you need to make changes to detached object
   * persistent. JPA only supports merging
   * 
   * 3 Even without getCurrentSession() there is a way in JPA to implemnts
   * session-per-conversation
   * 
   * The only way to get an EntityManager is through instantiation with the
   * createEntityManager() method on the factory. In other words all your data
   * access methods use their own EntityManger instance. This is the session per
   * operation anti-pattern we identified earlier.
   * 
   * There are three possible solutions for this issue:
   * 
   * 1 You can instantiate an EntityManger for the whole DAO when the DAO is
   * created. However transaction demarcation is still an issue with this
   * strategy; all DAO operations on all DAOs still can't be grouped as one atomic
   * and isolated unit of work
   * 
   * 2 You can instantiate a single EntityManager in your controller and pass it
   * into all DAOs when you create the DAOs (constructor injection). The code that
   * handles an EntityManager an be paired with transaction demarcation code in a
   * single location, the controller
   * 
   * 3 You can instantiate a single EntityManger in an interceptor and bind it to
   * a ThreadLocal variable in a helper class. The DAOs retrieve the current
   * EntityManager from the thread local This strategy simulates the
   * getCurrentSession() funcionality from hibernate
   * 
   */

  public static class ItemDAOJPA {

    /**
     * No persistence context propagation exist in JPA, if the application handles
     * the entity manager on its own. All your data access method use their own
     * EntityManger instance this is the session-per-operation antipattern. Worse
     * there is no sensible location for transaction demarcation that spans several
     * data access operations this is the session-per-operation antipattern
     * 
     * 
     */
    public Bid getMaxBid(Long itemId) {
      Bid maxBid;
      EntityManager em = null;
      EntityTransaction tx = null;

      try {
        em = getEntityManagerFactory().createEntityManager();
        tx = em.getTransaction();

        tx.begin();

        maxBid = (Bid) em.createQuery("...").getSingleResult();

        tx.commit();
      } finally {
        // TODO: handle exception
        em.close();
      }
      return maxBid;
    }

  }

}
