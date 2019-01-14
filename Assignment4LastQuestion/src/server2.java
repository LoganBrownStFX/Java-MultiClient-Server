
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class server2 extends Application {
    // Text area for displaying contents
    private TextArea ta = new TextArea();

    // Number a client
    private int clientNo = 0;
    private Statement statement;
    private Connection connection;

    @Override // Override the start method in the Application class
    public void start(Stage primaryStage) throws SQLException, ClassNotFoundException{

        initializeDB();
        // Create a scene and place it in the stage
        Scene scene = new Scene(new ScrollPane(ta), 450, 200);
        primaryStage.setTitle("MultiThreadServer"); // Set the stage title
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.show(); // Display the stage

        new Thread( () -> {
            try {
                // Create a server socket
                ServerSocket serverSocket = new ServerSocket(8001);
                ta.appendText("MultiThreadServer started at "
                        + new Date() + '\n');

                while (true) {
                    // Listen for a new connection request
                    Socket socket = serverSocket.accept();

                    // Increment clientNo
                    clientNo++;

                    Platform.runLater( () -> {
                        // Display the client number
                        ta.appendText("Starting thread for client " + clientNo +
                                " at " + new Date() + '\n');

                        // Find the client's host name, and IP address
                        InetAddress inetAddress = socket.getInetAddress();
                        ta.appendText("Client " + clientNo + "'s host name is "
                                + inetAddress.getHostName() + "\n");
                        ta.appendText("Client " + clientNo + "'s IP Address is "
                                + inetAddress.getHostAddress() + "\n");
                    });

                    // Create and start a new thread for the connection
                    new Thread(new HandleAClient(socket)).start();
                }
            }
            catch(IOException ex) {
                System.err.println(ex);
            }
        }).start();
    }

    // Define the thread class for handling new connection
    class HandleAClient implements Runnable {
        private Socket socket; // A connected socket

        /** Construct a thread */
        public HandleAClient(Socket socket) {
            this.socket = socket;
        }

        /** Run a thread */
        public void run() {
            try {
                // Create data input and output streams
                DataInputStream inputFromClient = new DataInputStream( new BufferedInputStream(socket.getInputStream())
                        );
                DataOutputStream outputToClient = new DataOutputStream(
                        socket.getOutputStream());

                // Continuously serve the client
                while (true) {
                    // Receive radius from the client
                    //Double mess = inputFromClient.readDouble();
                    String mess = inputFromClient.readUTF();
                   // double years = inputFromClient.readDouble();
                    //double PMT = inputFromClient.readDouble();

                    //double numerator = (Math.pow(1 + intRate/12,years*12) - 1);
                    //double denominator = intRate/12;
                    //double futVal = PMT * (numerator/denominator);


                    outputToClient.writeUTF(mess);

                    Platform.runLater(() ->{

                        ta.appendText("Future Value is: " + mess + '\n');
                    });
                }
            }
            catch(IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void initializeDB() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        ta.appendText("Driver Loaded" + "\n");

        connection = DriverManager.getConnection(("jdbc:mysql://127.0.0.1:3306/assignment?user=root&password=toms427"));
        ta.appendText("DB Connected" + "\n");


        statement = connection.createStatement();


    }

}