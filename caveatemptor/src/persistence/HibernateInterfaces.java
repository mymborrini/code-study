package persistence;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaValidator;

import model.Item;

/**
 * The persistent manager usually provides services for the following
 * 
 * 1 BASIC CRUD OPERATION
 * 
 * 2 QUERY EXECUTION
 * 
 * 3 CONTROL OF TRANSACTIONS
 * 
 * 4 MANAGEMENT OF THE PERISTENCE CONTEXT
 * 
 * 
 * PERSISTENCE CONTEXT: Let's talk more in detail about persistence context The
 * persistence context is a cache of persistent objects. Every object in
 * persistent state is known to the persistence context, and a duplicate, a
 * snapshot of each persistent instance is held in the cache. This snapshot is
 * used internally for dirty checkings. The OutOfMemory exception occurs when
 * you load thousend of objects in a session but never intend to modify them. To
 * avoid this you can: check that your queries returns, call session.evict(obj)
 * to detached an object from the persistence context,
 * session.setReadonly(object, true) the persistence context will no longer
 * maintain the snapthot if it's readonly. At the end of the unit of work, all
 * the modifications you made have sync. This process is call flushing of the
 * persistence context
 * 
 * Hibernate flushes occur at the following times: when transaction are commited
 * before query is executed, when the application call session.flush().
 * Hibernate doesn't flush before every query, only if there in-memory data that
 * can influence the query
 * 
 */
public class HibernateInterfaces {

  SessionFactory sessionFactory;

  public SessionFactory getSessionFactory() {
    Configuration configuration = new Configuration().configure();
    new SchemaValidator(configuration).validate();
    return configuration.buildSessionFactory();
  }

  public void storingObject() {

    // Session object does not even obtain a JDBC connection
    // until the connection is required
    Session session = getSessionFactory().openSession();

    // Create a new Transient Object
    Item item = new Item();
    item.setName("Playstation 3");
    item.setEndDate(new Date());

    Transaction transaction = session.beginTransaction();

    // A call to save makes the transient instance item persistent
    // It's now associated with the current session and its persistence
    // context The save operator return the identifier of the persistence istance
    Serializable itemId = session.save(item);

    /**
     * The persistence object may have to be sync to database at some point so it
     * say that a flush occurs. To synch the persistence context hibernate obtains
     * the JDBC connection and issues a single SQL INSERT statement. between save
     * and commit the item object can be modified, and this modification will be
     * persist to datbase when commit occurs
     */
    transaction.commit();

    /**
     * The session is close and the persistence context as well the reference item
     * is a reference to an object in detached state
     */
    session.close();

  }

  public void retrieveObject() {
    Session session = getSessionFactory().openSession();
    Transaction transaction = session.beginTransaction();

    /**
     * The difference between get and load is what happened is the object is not
     * found: get return a null object, load will raise an ObjectNotFoundException.
     * The load method try to return a proxy and only returns an initialized object
     * instance if it's already managed by the current persistence context. The
     * get() method never return a proxy he will always hit the database. Why you
     * should use the load method afterall, it's because it's pretty common to
     * obtain a persistence instance to assign its reference to another instance for
     * example if you need an item just to set an association to a comment like
     * aComment.setForAcution(item) in this case a proxy is enough and a proxy is an
     * identifier value wrapped in a placeholder that looks like the real thing
     */
    Item item = (Item) session.load(Item.class, 1234L);
    // Item item = (Item) session.get(Item.class, 1234L);
    transaction.commit();
    session.close();
  }

  /**
   * Any persistent object returned by get or load is already associated with the
   * current session and persistence context. It can be modified and its state is
   * synch with database. You modifiy the object and these modifications are
   * propagated to the database during flush when transaction.commit() is called
   */
  public void updateObject() {
    Session session = getSessionFactory().openSession();
    Transaction transaction = session.beginTransaction();

    Item item = (Item) session.get(Item.class, 1234L);

    item.setDescription("Playstation is as goog as new");

    transaction.commit();
    session.close();

    // Now item is in a detached statement ... how can i work with it
    item.setDescription("Description updated");
    Session session2 = getSessionFactory().openSession();
    Transaction transaction2 = session2.beginTransaction();

    /**
     * Calling update will call automatically an update to database even if you only
     * want to reattach this object to the persistence context. One way to avoid
     * this update is to map the item class with select-before-update = true
     * attribute, hibernate then determines wheter the object's current state is
     * dirty by making a select and comparing the objects before the update. Another
     * way is to call lock function in this case you re-associated the object to the
     * persistence context without forcing an update. In this case is like you save
     * a proxy instance to the persistence context and so all the modification doing
     * before the lock has no effect. So in this case Description will not be
     * propagated to the database
     */
    session2.update(item);
    session2.lock(item, LockMode.NONE);

    // now item is updated and its in a persistence context so i can
    // use dirty check to make all the operations i want
    item.setEndDate(new Date());

    transaction2.commit();
    session2.close();

  }

  /**
   * You can make a persistent object transient removing its persistence state
   * from the database, with the delete method
   */
  public void deleteObject() {
    Session session = getSessionFactory().openSession();
    Transaction transaction = session.beginTransaction();

    // note that for deleting an object a proxy instance is enough
    Item item = (Item) session.load(Item.class, 1234L);
    session.delete(item);

    /**
     * When you call delete the item is in state removed
     */
    transaction.commit();

    /**
     * After the session is close the item is in state transient
     */
    session.close();
  }

  /**
   * This is the case particular. You have a detached object that change
   * and you want to update it in a persistence context which already has 
   * the instace for that object. In this case update function will give you 
   * an error since this instance is already present in the persistence context
   */
  public void mergeDetachedObject(){
    Item item = new Item();
    item.getId(); // 1234L
    item.setDescription("...");

    Session session = getSessionFactory().openSession();
    Transaction transaction = session.beginTransaction();

    Item item2 = (Item) session.get(Item.class, 1234L);
    session.update(item); // will throw an Exeption since object already exist

    /**
     * With merge commands if the instance is already in the persistence context
     * hibernate will copy all the properties to the instance in persistence context
     * so the new description will be the one in item. If there is no instance
     * it will retrieve it by hitting tha database and then merge the item in the
     * database instace just retireved. If no such an instance exist in db hibernate
     * will then make a simple insert like item was a transient object.
     * 
     * 
     * Some question:
     * 
     * 1 What exactly is copied from item to item2? Merging includes all value-typed
     * properties and all additions and removals of elements to any collection
     * 
     * 2 What state is item now? Any detached object you merge stay detached
     * 
     * 3 Why is item3 returned from the merge() operation? The merge() operation always 
     * returns a handle to the persistent instance it has merge the state into 
     */
    Item item3 = (Item) session.merge(item);
    (item == item2) // FALSE
    (item == item3) // False
    (item2 == item3) // true

    transaction.commit();
    session.close();
  }

}
