package persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Version;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;

import jdk.jfr.Enabled;
import model.Item;

// this is just an explanation class with more or less no code
public class ControlTransactionConcurrency {
  /**
   * Transactional System like database attempt to ensure transaction isolation
   * meaning that, from the point of view of each concurrent transaction, it
   * appears that no other transaction are in progress. A transaction may place a
   * lock on a particular item of data in the database temporarily preventing
   * access to that item by other transactions. Many modern database such as
   * Oracle and PostgresSql implement transaction isolation with multiversion
   * concurrency control (MVCC)
   * 
   * Hibernate NEVER locks anything in memory, and all the lock is guaranteed from
   * the DBMS, on the other hand some features in Hibernate and JPA can improve
   * the isolation guaranteed beyond what is provided ba the DBMS
   * 
   * 
   * DATABASE LEVEL CONCURRENCY
   * 
   * Let's start saying that different isolation levels are available, which,
   * naturally, weaken full isolation but increase performance and scalability of
   * the system. Let's see at some phenomena that can occur in base at the system
   * isolation mechanism
   * 
   * LOST UPDATE -- occurs if two transactions both update a row and then the
   * second transactions aborts... with rolling back causing all the two changes
   * to be lost. This occurs in system which does not implement locking
   * 
   * DIRTY READ -- occurs if one transaction reads changes made by another
   * transaction that has not yet been committed. This is dangerous because the
   * changes made by the other transaction can be rolled back and invalid data may
   * be written by the first transaction
   * 
   * UNREPEATABLE READ -- occurs if a transaction read a row twice and reads
   * different state each time. For example another transaction may have written
   * to the row and commited between the two read. A special case is for example
   * when two transaction read a value from a row one write and commit and the
   * other write and commit the value make by the first one is lost
   * 
   * PHANTOM READ -- is similar to the previous one but in this case a transaction
   * read a set of value twice, another transaction make an insert or a delete
   * between the two
   * 
   * 
   * TRANSACTION ISOLATION LEVEL
   * 
   * This levels define the level of isolation, with increased levels of isolation
   * comes higher cost and serious degradation of performance and scalability.
   * 
   * READ UNCOMMITED -- A system that allows DIRTY READ but not LOST UPDATE This
   * isolation level may be implemented in the database management system with
   * exclusive write locks, but you can read anytime
   * 
   * READ COMMITED -- A system that allows UNREPEATABLE READ but not DIRTY READ.
   * This may be achieved by using shared read locks and exclusive write locks.
   * Reading transaction don't block other transaction from accessing a row.
   * However an uncommited writing transaction blocks all other transactions from
   * accessing the row
   * 
   * REPEATABLE READ -- A system that does not allow UNREPEATABLE READ or DIRTY
   * READ. Reading transaction blocks writing transaction (but not other reading
   * transaction), and writing transactions block all other transaction
   * 
   * SERIALIZABLE -- This isolation level emulates serial transaction ececution,
   * as if transactions were executed one ofter the other rather than concurrently
   * 
   * The method depends on the DBMS and how it is configured, for example mysql
   * has a default REPEATABLE READ method. For the best maybe with Hibernate and
   * its persistence context since you can open more trnasaction into a single
   * session you always read from persistence context so UNREPEATABLE READ problem
   * is already fixed.
   * 
   * The transaction level is a global option which influence all connections.
   * From time to time is useful to specify a more restrictice lock for a
   * particular transaction
   * 
   * OPTIMISTIC CONCURRENCY CONTROL
   * 
   * An optimistic approach always assumes that everything will be OK and that
   * conflict data modifications are rare. Optimistic concurrency control raises
   * an error only at the end of a unit of work, when data is written. This
   * approach guarantees the best performance and scalability
   * 
   * If two transaction read a value and then commit the result you could have a
   * lost update... so you have three ways to deal with it.
   * 
   * LAST COMMIT WINS -- The second commit overrides the first one
   * 
   * FIRST COMMIT WINS -- The first is commited and the second one at the
   * commitment raise an error. The second transaction must restart again with
   * fresh data
   * 
   * MERGE CONFLICT UPDATES -- Same as before but this time the user may applay
   * changes selectively instead of going through all the work in the conversation
   * again
   * 
   * The default is the last commit wins strategy. In some cases this is not the
   * best solutions so we have to enable an optimistic concurrency control
   * pattern, that in hibernate is done using versioning
   * 
   * You have to add a version property which can be a number or a timestamp which
   * needs to enable optimistic locking. Generally a timestamp is in general less
   * sage since JVM usually does not have milliseconds accuracy.
   * 
   * How does this work, for example if you select an item with version 1 the next
   * update will be something like:
   * 
   * update item set initial_price='12.99', obj_version=2 where item_id=1234 and
   * obj_version=1
   * 
   * If another transaction before occurs the obj_version is no more 1 and this
   * action never happens. In this case HIbernate throws a
   * StaleObjectStateException. The state that was present when you loaded the
   * Item is no longer be present in the database at flush time; hence you are
   * woring with stale data and have to notify the user.
   * 
   * Hibernate increments the version number whenever an entity instace is dirty.
   * 
   * If you are working with a legacy database so you cannot add a new column
   * hibernate can still implements the optimistic concurrency control with
   * different strategy. This will work only for objects that are retrieved and
   * modified in the same session. This is enough by confront the snapshot in the
   * persistence context with the instance in the database when all is flushed
   * Hibernate can works in two ways. It can raise a StaleObjectStateException if
   * you set optimistic lock attribute on the class mapping or it can update only
   * the values which are not been modified by the last transaction if you set
   * optimistic-lock=dirty. The last one is like two transaction can modified the
   * same item and a conflict will raise only if they modifiy the same value. Of
   * course you have to enable the dynamic update true at start up since Hibernate
   * cannot generate those query at the startup
   * 
   * We have now covered the basic isolation levels of a database connection, with
   * the conclusion that you should almost always rely on read-committed
   * guarantees from your database. Automatic versioning in Hibernate and JPA
   * prevents lost updates, but to deal with nonrepeatable reads, you need
   * additional isolation guarantees
   * 
   * There are several ways to prevent nonrepeatable reads. One way is of course
   * change the datbase isolation level, but this is not really good for
   * scalability and performance it will be better to change the isolation level
   * only for a single unit of work, using a PESSIMISTIC CONCURRENCY STRATEGY See
   * repeatable reads method
   */
  @Entity
  @org.hibernate.annotations.Entity(optimisticLock = org.hibernate.annotations.OptimisticLockType.ALL)
  public static class Obj {

    @Version
    @Column(name = "OBJ_VERSION")
    private int version;

  }

  /**
   * In this case there are two reads of the same entity in two different moment
   * In this case since another transaction update description in between the two
   * queries To avoid this you have to set lockmode.upgrade in ordet to stop any
   * other transaction to write between the two transaction. A little variation is
   * UPDAGRADE_NOWAIT with this clause the second transaction will recevie an
   * error, instead with only upgrade the result will only be that the second
   * transaction will wait until the lock is realeased. This i an example of
   * pessimistic concurrency strategy, and as explained before.
   * 
   * HIBERNATE supports the following lockmode:
   * 
   * LockMode.NONE --- don't go to the datbase unless the object isn't in any
   * cache.
   * 
   * LockMode.READ --- Bypass all caches and perform a version check to verify
   * that the object in memory is the same version that currently exist in the
   * database
   * 
   * LockMode.UPGRADE --- make a SELECT ... FOR UPDATE, if that is supported from
   * the database otherwise it will return to LockMode.READ. This will obtain a
   * database pessimistic level lock
   * 
   * LockMode.UPGRADE_NOWAIT --- As explained before raise an exception
   * 
   * LockMode.FORCE -- Force an increment of the objects version in the database
   * to indicate that it has been modified by the current transaction. This is
   * useful because by default hibernate will update versioning of an entity which
   * is modified, with this lock you can update an entity which is not modified in
   * order to show that was request by the transaction
   * 
   * LockMode.WRITE -- Obtained automatically when Hibernate has written a row in
   * the current transaction
   */
  public void repeatableReads() {
    Session session = sessionFacotry.opernSession();
    Transaction tx = session.beginTransaction();

    Item item = (Item) session.get(Item.class, 123L);
    session.lock(item, LockMode.UPGRADE);

    String description = (String) session.createQuery("select i.description from Item i where i.id = :itemid")
        .setParameter("itemid", item.getId()).uniqueResult();

    tx.commit();
    session.close();
  }

}
