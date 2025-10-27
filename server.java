import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.Random;
import java.util.concurrent.*;
import org.apache.xmlrpc.webserver.WebServer; 
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.XmlRpcException;

//Use semaphore, (semaphore(1)) and have 
public class server {

   public static Semaphore theTalkingStick = new Semaphore(1);

   public static void main(String[] args) {

      Scanner scanner = new Scanner(System.in);
      try {
         PropertyHandlerMapping phm = new PropertyHandlerMapping();
         XmlRpcServer xmlRpcServer;
         WebServer server = new WebServer(8888);
         xmlRpcServer = server.getXmlRpcServer();
         phm.addHandler("sample", server.class);
         xmlRpcServer.setHandlerMapping(phm);
         System.out.println("Server starting on port 8888");
         server.start();

         while(true){

            String input = scanner.nextLine();

            if(input.equals("log")){
                log();
            }

            if(input.equals("restock")){
                restock();
            }

            if(input.contains("updatePrice")){
                int spaceIndex = input.indexOf(" ");
                int lastSpaceIndex = input.lastIndexOf(" ");
                if(spaceIndex == -1 || spaceIndex == lastSpaceIndex){
                   System.out.println("Usage: updatePrice <bookId> <newPrice>");
                }
                else{
                int bookId = Integer.valueOf(input.substring(spaceIndex+1, lastSpaceIndex));
                float newPrice = Float.valueOf(input.substring(lastSpaceIndex+1)); 
                updatePrice(bookId, newPrice);
                }
            }

            if(input.equals("exit")){
                System.exit(1);
            }
         }
      } catch (Exception e) {
         System.err.println( e.getClass().getName() + ": " + e.getMessage() );
         System.exit(0);
      }

   }

   public Object[] search(String topic) {
      Statement stmt = null;
      Connection c = null;
      ResultSet rs = null;
      String[] booklist = new String[2];

      try {
         theTalkingStick.acquire();
         Class.forName("org.sqlite.JDBC");
         c = DriverManager.getConnection("jdbc:sqlite:test.db");
         c.setAutoCommit(false);
         System.out.println("Opened database successfully");
         stmt = c.createStatement();
         rs = stmt.executeQuery("SELECT * FROM BOOKS WHERE TOPIC LIKE '" + topic + "';");
         int i = 0;
         booklist[0] = "ID: null | TITLE: null";
         booklist[1] = "ID: null | TITLE: null";
         
         while(rs.next()) {
            int id = rs.getInt("id");
            String title = rs.getString("title");
            booklist[i] = "ID: " + id + " | TITLE: " + title;
            System.out.println(booklist[i]);
            i++;
         }

         System.out.println();
         rs.close();
         stmt.close();
         c.close();
      } catch (Exception e) {
         System.err.println( e.getClass().getName() + ": " + e.getMessage() );
         System.exit(0);
      } finally {
          theTalkingStick.release();
      }

      return booklist;
   }

   public String lookup(int itemNumber) {
      Statement stmt = null;
      Connection c = null;
      ResultSet rs = null;
      String book = null;

      try {
         theTalkingStick.acquire();
         Class.forName("org.sqlite.JDBC");
         c = DriverManager.getConnection("jdbc:sqlite:test.db");
         c.setAutoCommit(false);
         System.out.println("Opened database successfully");
         stmt = c.createStatement();
         rs = stmt.executeQuery("SELECT * FROM BOOKS WHERE ID = " + itemNumber + ";");

         int i = 0;

         String title = rs.getString("title");
         String topic = rs.getString("topic");
         int stock = rs.getInt("stock");
         float price = rs.getFloat("price");
         book = "TITLE: " + title + " | TOPIC: " + topic + " | STOCK: " + stock + " | PRICE: " + price;
         System.out.println(book);
         System.out.println();
         rs.close();
         stmt.close();
         c.close();
      } catch (Exception e) {
         System.err.println( e.getClass().getName() + ": " + e.getMessage() );
         System.exit(0);
      } finally {
          theTalkingStick.release();
      }


      return book;
   }

   public Object[] buy(int itemNumber) {
      Statement stmt = null;
      Connection c = null;
      ResultSet rs = null;
      ResultSet rs2 = null;
      int currentStock = 0;
      String[] buyResults = new String[3];

      try {
         theTalkingStick.acquire();
         Class.forName("org.sqlite.JDBC");
         c = DriverManager.getConnection("jdbc:sqlite:test.db");
         c.setAutoCommit(false);
         System.out.println("Opened database successfully");
         stmt = c.createStatement();
         rs = stmt.executeQuery("SELECT STOCK FROM BOOKS WHERE ID = " + itemNumber + ";");
         currentStock = rs.getInt("stock");
         buyResults[0] = "CURRENT STOCK = " + currentStock;

         if (currentStock > 0) {
            --currentStock;
            stmt.executeUpdate("UPDATE BOOKS set STOCK = " + currentStock + " WHERE ID = " + itemNumber + ";");
            rs2 = stmt.executeQuery("SELECT TOTAL_PURCHASES FROM TOTAL_PURCHASES WHERE ID = " + itemNumber + ";");
            int purchased = rs2.getInt("total_purchases");
            ++purchased;
            stmt.executeUpdate("UPDATE TOTAL_PURCHASES SET TOTAL_PURCHASES = " + purchased + " WHERE ID = " + itemNumber + ";");
            c.commit();
            buyResults[1] = "BOUGHT BOOK ID = " + itemNumber;
         } else {
            String[] noStock = new String[1]; 
            noStock[0] = "CANNOT BUY IF STOCK==0";
            System.out.print(Arrays.toString(buyResults));
            System.out.println(Arrays.toString(noStock));
            return noStock;
         }

         buyResults[2] = ("UPDATED STOCK: " + currentStock);
         System.out.println(buyResults[0]);
         System.out.println();
         rs.close();
         stmt.close();
         c.close();
      } catch (Exception e) {
         System.err.println( e.getClass().getName() + ": " + e.getMessage() );
         System.exit(0);
      } finally {
          theTalkingStick.release();
      }


      return buyResults;
   }

   private static void log() {
      Statement stmt = null;
      Connection c = null;
      ResultSet rs = null;

      try {
         theTalkingStick.acquire();
         Class.forName("org.sqlite.JDBC");
         c = DriverManager.getConnection("jdbc:sqlite:test.db");
         c.setAutoCommit(false);
         System.out.println("Opened database successfully");
         stmt = c.createStatement();
         rs = stmt.executeQuery("SELECT * FROM TOTAL_PURCHASES;");

         while(rs.next()) {
            int id = rs.getInt("id");
            int purchases = rs.getInt("total_purchases");
            System.out.println("ID: " + id + " --- TOTAL_PURCHASES: " + purchases);
         }

         System.out.println();
         rs.close();
         stmt.close();
         c.close();
         theTalkingStick.release();
      } catch (Exception e) {
         System.err.println( e.getClass().getName() + ": " + e.getMessage() );
         System.exit(0);
      }
   }

   private static void restock() {
      Statement stmt = null;
      Connection c = null;
      ResultSet rs = null;
      Random rand = new Random();
      int currID = 0;


      try {
         theTalkingStick.acquire();
         Class.forName("org.sqlite.JDBC");
         c = DriverManager.getConnection("jdbc:sqlite:test.db");
         c.setAutoCommit(false);
         System.out.println("Opened database successfully");
         stmt = c.createStatement();
         rs = stmt.executeQuery("SELECT * FROM BOOKS;");
         PreparedStatement updateStmt = c.prepareStatement("UPDATE BOOKS SET STOCK = ? WHERE ID = ?");

         while(rs.next()) {
            int randInt = rand.nextInt(330)+20;
            int stock = rs.getInt("stock");
            currID = rs.getInt("id");
            System.out.println("ID: " + currID + " --- OLD STOCK: " + stock);
            if(randInt > stock){stock = randInt;} else{stock += randInt;}
            updateStmt.setInt(1, stock);
            updateStmt.setInt(2, currID);
            updateStmt.executeUpdate();
            System.out.println("ID: " + currID + " --- NEW STOCK: " + stock);
         }

         System.out.println();
         c.commit();
         rs.close();
         stmt.close();
         updateStmt.close();
         c.close();
         theTalkingStick.release();
      } catch (Exception e) {
         System.err.println( e.getClass().getName() + ": " + e.getMessage() );
         System.exit(0);
      }

   }

   private static void updatePrice(int itemNumber, float itemPrice) {
      Statement stmt = null;
      Connection c = null;
      ResultSet rs = null;

      try {
         theTalkingStick.acquire();
         Class.forName("org.sqlite.JDBC");
         c = DriverManager.getConnection("jdbc:sqlite:test.db");
         c.setAutoCommit(false);
         System.out.println("Opened database successfully");
         stmt = c.createStatement();
         rs = stmt.executeQuery("SELECT * FROM BOOKS WHERE ID = " + itemNumber + ";");
         float oldPrice = rs.getFloat("price");
         stmt.executeUpdate("UPDATE BOOKS set PRICE = " + (float)itemPrice + " WHERE ID = " + itemNumber + ";");
         c.commit();
         System.out.println("OLD PRICE OF BOOK ID " + itemNumber + ": " + oldPrice);
         System.out.println("NEW PRICE OF BOOK ID " + itemNumber + ": " + itemPrice);
         System.out.println();
         rs.close();
         stmt.close();
         c.close();
         theTalkingStick.release();
      } catch (Exception e) {
         System.err.println( e.getClass().getName() + ": " + e.getMessage() );
         System.exit(0);
      }

   }
}
