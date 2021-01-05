package persistence;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;

import model.Item;
import model.User;

/**
 * The Jpa persistence application can be written with managed or unmanaged unit
 * of work, this refers the possibility to create a persistence layer which runs
 * and work without any special runtime environment.
 */
public class JPAInterfaces {

  /**
   * The entity manager has a fresh persistence context assigned when it's created
   */
  public EntityManager getEntityManager() {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("caveatEmptorDB");
    return emf.createEntityManager();
  }

  /**
   * Since the entity manager in this case is closed by the application I have to
   * close it manually
   */
  public void storeObject() {
    EntityManager em = getEntityManager();
    Item item = new Item();
    item.setName("Playstation 3");
    item.setEndDate(new Date());

    EntityTransaction tx = em.getTransaction();
    tx.begin();

    /**
     * Like in Hibernate interfaces the perist method persit this item in the
     * persistence context. Note that in this case persist does not return the
     * database identifier. Even Hibernate interface has a persist method but with a
     * huge differece... Persit in hibernate (during flushing) persist only the main
     * entity without any associations in collections;
     */
    em.persist(item);

    tx.commit();
    em.close();

  }

  public void retrieveObject() {
    EntityManager em = getEntityManager();
    EntityTransaction tx = em.getTransaction();

    tx.begin();

    /**
     * In this case i have to work with a generic method so there is no need to cast
     * it. Hibertante API has to work with jdbc API which is not generic so a cast
     * is necessary. Find is like get in Hibernate it always hit the database or the
     * persistence context if the instance exist there. You can always tell
     * entityManager to attempt the retrieval of a placeholder, it works exactly
     * like load in hibernate. If you check any other property differnt from the id
     * it will make a select to the database in case the instance is not found it
     * will raise an exception
     */
    Item item = em.find(Item.class, 1234L);
    Item itemReferece = em.getReference(Item.class, 1234L);

    tx.commit();
    em.close();
  }

  /**
   * This is the same example with dirty checkings that works with hibernate
   */
  public void updateObject() {
    EntityManager em = getEntityManager();
    EntityTransaction tx = em.getTransaction();

    tx.begin();

    Item item = em.find(Item.class, 1234L);
    item.setDescription("....");

    tx.commit();
    em.close();

  }

  public void removeObject() {
    EntityManager em = getEntityManager();
    EntityTransaction tx = em.getTransaction();

    tx.begin();

    Item item = em.find(Item.class, 1234L);
    em.remove(item);

    tx.commit();
    em.close();
  }

  // As in hibernate API you can control the flushing in this way
  // AUTO means like in hibernate so before a query, transacion commit, em.flush
  // COMMIT when is called transacion commit and flush and not before query
  // FLUSH (only hibernate api) when session.flush() is called and nothing else
  // will
  // sync the database
  public void flushingControl() {
    EntityManager em = getEntityManager();
    em.setFlushMode(FlushModeType.COMMIT);
    EntityTransaction tx = em.getTransaction();

    tx.begin();

    Item item = em.find(Item.class, 1234L);
    item.setDescription("....");

    tx.commit();
    em.close();
  }

  /**
   * Working with detached object... is like in hibernate api In this method is to
   * teach that detached object occurs when the entitymanager is close so in this
   * case.. the setDescription for the item will take place since the item still
   * exist in the persistence context
   */
  public void persisteceContextScope() {
    EntityManager em = getEntityManager();
    em.setFlushMode(FlushModeType.COMMIT);
    EntityTransaction tx = em.getTransaction();

    tx.begin();

    Item item = em.find(Item.class, 1234L);

    tx.commit();

    item.setDescription("....");

    tx.begin();
    User user = em.find(User.class, 3456L);
    user.setPassword("secret...");
    tx.commit();

    em.close();
  }

  /**
   * Just like in hibernate exist the em.clear() method which remove all instances
   * from persistene context. Java persistence does not have the evict() method
   * like hibernate did. In this case since the persistence context was clear the
   * new description will not be sync
   */
  public void manualDetachement() {
    EntityManager em = getEntityManager();
    EntityTransaction tx = em.getTransaction();

    tx.begin();

    Item item = em.find(Item.class, 1234L);

    em.clear();

    item.setDescription("....");

    tx.commit();
    em.close();

  }

  /**
   * 
   */
  public void mergingDetachment() {
    EntityManager em = getEntityManager();
    EntityTransaction tx = em.getTransaction();

    tx.begin();

    Item item = em.find(Item.class, 1234L);

    tx.commit();
    em.close();

    item.setDescription("....");
    EntityManager em2 = getEntityManager();
    EntityTransaction tx2 = em2.getTransaction();
    tx2.begin();

    /**
     * The merging method works exaclty like hibernate
     */
    Item mergedItem = (Item) em2.merge(item);

    tx2.commit();
    em2.close();

  }

}
