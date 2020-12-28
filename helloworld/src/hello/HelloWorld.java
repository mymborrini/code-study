package hello;

import java.util.*;

import org.hibernate.*;
import persistence.*;

public class HelloWorld {

  public static void main(String[] args) {
    
    Long msgIdCreated = firstUnitOfWork();
    thirdUnitOfWork(msgIdCreated);
    secondUnitOfWork();
    HibernateUtil.shutdown();

  }

  private static Long firstUnitOfWork(){

    System.out.println("First unit of work");
    Session session = HibernateUtil.getSessionFactory().openSession();
    Transaction transaction = session.beginTransaction();
    
    Message message = new Message("Hello World");
    Long msgId = (Long) session.save(message);
    transaction.commit();
    session.close();
    return msgId;
  }

  private static void secondUnitOfWork(){


    System.out.println("Second unit of work");
    Session session = HibernateUtil.getSessionFactory().openSession();
    Transaction transaction = session.beginTransaction();

    List messages = session.createQuery("from Message m order by m.text asc").list();
    System.out.println( messages.size() + " message(s) found: ");

    for (Iterator iter = messages.iterator(); iter.hasNext(); ){
      Message loadMessage = (Message) iter.next();
      System.out.println(loadMessage.getText());
    }

    transaction.commit();
    session.close();

  }

  private static void thirdUnitOfWork(Long msgId){
    
    Session session = HibernateUtil.getSessionFactory().openSession();
    Transaction transaction = session.beginTransaction();

    Message message = (Message) session.get(Message.class, msgId);
    message.setText("Greetings Earthling");
    message.setNextMessage(
      new Message( "Take me to your leader (please)" )
    );
    transaction.commit();
    session.close();



  }

}