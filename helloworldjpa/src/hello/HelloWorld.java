package hello;

import java.util.*;

import javax.persistence.*;

public class HelloWorld {

  public static void main(String[] args) {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("helloworld");

    firstUnitOfWork(emf);
    secondUnitOfWork(emf);
    emf.close();

  }

  private static void firstUnitOfWork(EntityManagerFactory emf) {

    System.out.println("First unit of work");
    EntityManager entityManger = emf.createEntityManager();

    EntityTransaction transaction = entityManger.getTransaction();
    transaction.begin();

    Message message = new Message("Hello World");
    entityManger.persist(message);
    transaction.commit();
    entityManger.close();

  }

  private static void secondUnitOfWork(EntityManagerFactory emf) {

    System.out.println("Second unit of work");
    EntityManager entityManager = emf.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();

    transaction.begin();

    List messages = entityManager.createQuery("from Message m order by m.text asc").getResultList();
    System.out.println(messages.size() + " message(s) found: ");

    for (Object m : messages) {
      Message loadMessage = (Message) m;
      System.out.println(loadMessage.getText());
    }

    transaction.commit();
    entityManager.close();
  }

}