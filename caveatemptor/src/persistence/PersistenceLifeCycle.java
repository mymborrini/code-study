package persistence;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaValidator;

import model.Item;

/**
 * We refer as the persistence lifecycle as the states an object goes through
 * its life
 * 
 * We also use the term unit of work a set of operations you consider one
 * (usually atomic) group.
 * 
 * A persistence context is a cache that remembers all the modifications and
 * state changes you made to objects in a particular unit of work
 * 
 * Hibernate holds 4 object state: transient, persist, detached, removed
 * 
 * TRANSIENT OBJECT: An object is in transient state after he was creted with a
 * new keyword. Hibernate consider all transient instance to be non
 * transactional any modification of a transient instance isn't known to a
 * persistence context. To become persistent requires either a call to the
 * persistence manager or the creation of a reference from another persistence
 * instance
 * 
 * PERSISTENT OBJECT: A persistent instance is an instance with a database
 * identity. So it has a primary key. Persistence instance are ALWAYS associated
 * with a persisten context. Hibernate caches them and can detect wheter they
 * have been modified by the application.
 * 
 * REMOVED OBJECT: An object is in removed state if it has been scheduled for
 * deletion at the end of a unit of work, but it's still managed by the
 * persistence context until the unit of work completes
 * 
 * DETACHED OBJECT: When the unit of work is completed and the persistence
 * context close tha application still has a handle, a reference to the instance
 * that was saved, this handle is in state detached, indicates that their state
 * is no longer guaranteed to be synchronized with database state; they are no
 * longer attached to a persistence context. The changes into the detached
 * object will not perist in the database at the end of the unit of work. If you
 * want to make your change persistent you have to call the merging operation.
 * The ability to return objects from one persistent context to the presentation
 * layer and later reuse this in a new persistence context allow user to create
 * long units of works named conversation
 * 
 * 
 * THE PERSISTENCE CONTEXT:
 * 
 * The persistence context is a cache of managed entity instance. The
 * persistence context is useful for several reasons:
 * 
 * 1 hibernate can do automaric dirty checking and write behind in transaction
 * 
 * 2 hibernate can use persistence context as a first level cache
 * 
 * 3 hibernate can guarantee a scope of Java object identity
 * 
 * 4 hibernate can extend the peristence context to span a whole conversation
 * 
 * AUTOMATIC DIRTY CHECKING: An object with modifications that have not yet been
 * propagated to the database is considered dirty. Hibernate propagates state
 * changes to the database as late as possible, not every time an in memory
 * obejct change. Doing so Hibernate tries to keep the lock as short as possible
 * Since query are generated at compile time hibernate, by defaults, includes
 * all columns of a dirty object to perist it. You can change this behaviour if
 * you want telling hibernate to generate the query at runtime, in this case
 * only the columns that were changed are updated, even if from a performance
 * point of view this became significant only if you have table with 50 or more
 * columns. By default hibernate compares an old snapshot of an object with the
 * snapshot at synchronization time, and it detects any modification that
 * require an update of the database state.
 * 
 * PERISTENCE CONTEXT CHACHE: Another benefit is repeatable read for entities
 * and the performance advantage of a unit of work-scoped cache. The persistent
 * context cache sometimes helps avoid unnecessary database traffic, but more
 * important ensures that:
 * 
 * 1 the persistence layer isn't vulnerable to stack overflows in the case of
 * circular references
 * 
 * 2 There can never be conflict representation of the same database row at the
 * end of unit work since at most a single obejct represents a database row.
 * 
 * 3 Changes made in a persisten context are immediatly visible to all other
 * code executed inside that persistence context
 * 
 * 
 * CONVERSATIONS:
 * 
 * There are two strategies to implement a conversation in a Hibernate
 * application: with detached objects or by extending a persistence context.
 * 
 * 1 Objects are held in detached state during user think-time, and any
 * modification of these objects is made persistent manually through
 * reattachment or merging. This strategy is also called
 * session-per-request-with-detached-object
 * 
 * | request -----> response | .................... | request ----> response |
 * 
 * | persistence context | ...detached objects -> merging | persistence context|
 * 
 * With the session-per-conversation pattern, you extend a persistence context
 * to span the whole unit of work
 * 
 * | request -----> response | .................... | request ----> response |
 * 
 * |------------------------- persistence context ------------------------- |
 * 
 * Before continue with the conversation explanation is very important to
 * understand the scope of object identity and the relationship between java
 * identity a == b and database identity x.getId().equals(y.getId()). We want
 * the conditions under which java identity is equivalent to db identity
 * 
 * For this scope there are 3 commons choice. 1 A "primitive-persistence" layer
 * with no identity scope makes no guarantees that if a row is accessed twice
 * the same Java object instance will be returned to the application. This
 * becomes problematic if the application modifies to different instances that
 * both represent the same row 2 A persistence layer using "persistence
 * context-scoped identity" guarantees that only one instance represents a
 * particular database row. This avoids the previous probelm 3 A "process-scoped
 * identity" guarantees that only one prokjec instnace represent the row in a
 * whole process (JWM)
 * 
 * Generally persistence context scoped is preferred. But in a large multi
 * threading application the cost of always synch shared access to persistent
 * object it's too high price to pay. It's simpler to have each thread work with
 * distinct set of persistence instace in each persistence context. However when
 * object are not related to persistence context some issue occurs. See
 * IdentityscopeIssue method
 * 
 * A particular conversation reuse the same persistence context for all
 * interactions All request processing during a conversation is managed by the
 * same persistence context. The persistence context isn't closed after a
 * request from the user has been processed. It's disconnected from the database
 * and held in this state during the user think-time. When the user continue the
 * conversation the persistence context is reconnected to the database, and the
 * next request can be processed. At the end of the conversation the
 * persistencce context is sync with tha database and closed. This eliminates
 * objects in detached state, all instances are either transient or persistent.
 * In hibernate terms this strategy uses a single session for the duration of
 * the conversation
 * 
 * 
 * 
 * 
 */
public class PersistenceLifeCycle {

  private static SessionFactory sessionFactory;

  /**
   * If you're working with object in detached state you're dealing
   * with objects that are living outside of a guaranteed scope of object
   * identity. a,b, c are equal from a database identity point of view 
   * but they are different from java identity point of view
   */
  public static void IdentityScopeIssue(){
    Configuration configuration = new Configuration().configure();
    new SchemaValidator(configuration).validate();
    sessionFactory = configuration.buildSessionFactory();

    Session session1 = sessionFactory.openSession();
    Transaction transaction1 = session1.beginTransaction();

    // Load item with identifier
    Object a = session1.get(Item.class, new Long(1234));
    Object b = session1.get(Item.class, new Long(1234));

    ( a == b ) // True, persistent a and b are identical

    transaction1.commit();
    session1.close();

    // references a and b are now to an object in detached state
    Session session2 = sessionFactory.openSession();
    Transaction transaction2 = session2.beginTransaction();

    Object c = session2.get(Item.class, 1234L);

    ( a == c) // False, detached a and persistent c are not identical

    transaction2.commit();
    session2.close();

    // What if now we want to put them in a set... Everytime you have to
    // work with detached object you have to implement your own equals and 
    // hashcode method. An example can be found in User class

  }

}
