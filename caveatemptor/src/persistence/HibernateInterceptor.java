package persistence;

import java.lang.reflect.Method;

import org.hibernate.FlushMode;
import org.hibernate.classic.Session;
import org.hibernate.context.ManagedSessionContext;

/**
 * With hibernate interceptor I have to
 * 
 * 1 when the request arrives the interceptor runs and opens a new Session,
 * automatic flushing disable, and bind this session
 * 
 * 2 A transaction is started, before the controller (lets call the methods
 * wrapped by the interceptor that way) handle the event
 * 
 * 3 All data acess inside the controller can now call getCurrentSession. When
 * the controller finish its work the interceptor runs again and unbinds the
 * current session (which in general means that close JDBC connection but does
 * not close the session which is disconnected and store it somewhere in order
 * to use the same Session again later)
 * 
 * 4 Commitment of the transaction
 * 
 * 5 As soon as the second request hit the server the interceptor runs, detected
 * that there is a disconnected stored Session and binds it
 * 
 * 6 The controller handles events after a new transaction starts
 * 
 * 7 The controller finish his job and the interceptor detects that the
 * conversation is over so it unbinds the Session as always but then flush the
 * session and commit the transaction. Finally the conversation is complete and
 * the interceptor closes the Session
 * 
 * So in the end the disconnected Session can be saved in different way, for
 * example if there is an endpoint in the HttpSession
 * 
 * The special token can be saved in different places
 */
public class HibernateInterceptor {

  public Object invoke(Method method, Object... args) {

    // Which session to use?
    Session currentSession = null;

    if (disconnectedSession == null) {

      // Start a new Conversation
      currentSession = sessionFactory.openSession();
      currentSession.setFlushMode(FlushMode.MANUAL);
    } else {
      // In the middle of a conversation
      currentSession = disconnectedSession;
    }

    // Bind before processing event
    ManagedSessionContext.bind(currentSession);

    // Begin a database transaction, reconnects Session
    currentSession.beginTransaction();

    // Process the event by invocking the wrapper execute()
    Object returnedValue = method.invoke(args);

    // Unbind after processing the event
    currentSession = ManagedSessionContext.unbind(sessionFactory);

    // Decide if this was the last event in the conversation

    if (returnedValue.containsEndOfConversationToken()) {

      // The event was the last event flush commit close
      currentSession.flush();
      currentSession.getTransaction().commit();
      currentSession.close();

      disconnectedSession = null;
    } else {

      // Event was not the last event continue the conversation
      currentSession.getTransaction().commit(); // Disconnets
      disconnectedSession = currentSession;
    }

    return returnedValue;
  }

}
