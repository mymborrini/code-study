package persistence;

import javax.naming.InitialContext;
import javax.transaction.UserTransaction;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

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
   * already commited to database this could not be rolled back
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

}
