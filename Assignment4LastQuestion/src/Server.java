import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;



public class Server extends Application{

    private Statement statement;
    private Connection connection;
    Socket socket = null;
    private TextArea ta = new TextArea();
    private int clientNo = 0;


    public void start(Stage primaryStage)
            throws SQLException, ClassNotFoundException{

        initializeDB();


        Scene scene = new Scene(new ScrollPane(ta), 420, 200);
        primaryStage.setTitle("Server"); // Set the stage title
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.show(); // Display the stage

        new Thread( () -> {
            try {
                // Create a server socket
                ServerSocket serverSocket = new ServerSocket(8001);
                ta.appendText("MultiThreadServer started at "
                        + new java.util.Date() + '\n');

                while (true) {
                    // Listen for a new connection request
                    socket = serverSocket.accept();

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
                    ta.appendText("New Thread" +"\n");
                }
            }
            catch(IOException ex) {
                System.err.println(ex);
            }
        }).start();
    }

    class HandleAClient implements Runnable{
        private Socket socket;
        public HandleAClient(Socket socket) {
            this.socket = socket;
        }

        public void run(){

            try{


                DataOutputStream outputToClient = new DataOutputStream(
                        socket.getOutputStream());
                DataInputStream inputFromClient = new DataInputStream(
                        new BufferedInputStream(socket.getInputStream()));


                while(true){
                    int token = inputFromClient.read();

                    switch (token){
                        case 0: handleSignIn(inputFromClient,outputToClient);
                        case 1: handleSignUp(inputFromClient,outputToClient);
                        case 2: handleUpdateInfo(inputFromClient,outputToClient);
                        case 3: handleFirstSearch(inputFromClient,outputToClient);
                        case 4: handleLastSearch(inputFromClient, outputToClient);
                        case 5: handleWCSearch(inputFromClient, outputToClient);
                        case 6: handleSendMessage(inputFromClient, outputToClient);
                        case 7: handleGetMessages(inputFromClient, outputToClient);

                    }

                }

            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        public void handleSignIn(DataInputStream fromClient, DataOutputStream objTo){

            try{
                ta.appendText("Authenticating USER....." + "\n");
                String Uemail = fromClient.readUTF();
                String Upassword = fromClient.readUTF();

                if(authUser(Uemail, Upassword).equals("1")){

                    ta.appendText("USER AUTHENTICATED" + "\n");
                    objTo.writeUTF("1");
                    objTo.flush();
                    run();
                } else if (authUser(Uemail, Upassword).equals("0")){
                    objTo.writeUTF("0");
                    objTo.flush();
                    run();
                } else{
                    objTo.writeUTF("-1");
                    objTo.flush();
                }

            }catch (Exception ex){
                ex.printStackTrace();

            }



        }
        public void handleSignUp(DataInputStream inputFromClient, DataOutputStream outputToClient){
            try{
                ta.appendText("Attempting Sign UP......" +"\n");
                String name = inputFromClient.readUTF();
                String email = inputFromClient.readUTF();
                String password = inputFromClient.readUTF();
                String book = inputFromClient.readUTF();

                if(signUpUser(name, email, password, book)){
                    outputToClient.writeBoolean(true);
                    ta.appendText("USER SIGN UP SUCCESS" + "\n");
                } else{
                    outputToClient.writeBoolean(false);
                    ta.appendText("USER SIGNUP FAILED" + "\n");
                }

            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        public String authUser(String Uemail, String Upassword) throws SQLException{
            String query = "select * from users where email = '" +Uemail + "' AND password = '" + Upassword +"';";
            ResultSet resultSet = statement.executeQuery(query);

            if (resultSet.next()){
                return resultSet.getString("accounttype");
            }

            return" ";

        }
        public boolean signUpUser(String name, String email, String password, String book) throws  SQLException{
            String checkEmailQuery = "select * from users where email = '" +email + "';";
            ResultSet resultSet = statement.executeQuery(checkEmailQuery);

            if(!resultSet.next()){
                String signUpQuery = " insert into users (username, email, password, book, accounttype )"
                        + " values (?, ?, ?, ?, ?)";
                PreparedStatement stmt = connection.prepareStatement(signUpQuery);
                stmt.setString(1, name);
                stmt.setString(2, email);
                stmt.setString(3, password);
                stmt.setString(4, book);
                stmt.setInt(5, 1);

                stmt.execute();

                return true;
            } else{
                return false;
            }
        }
        public void handleUpdateInfo(DataInputStream input, DataOutputStream outputStream) throws SQLException{
            String updateQuery = "update users set username = ?, email = ?, password = ?, book = ? where email = ?";

            PreparedStatement stmt = connection.prepareStatement(updateQuery);
            try {
                String email = input.readUTF();
                stmt.setString(1, input.readUTF());
                stmt.setString(2, email);
                stmt.setString(3, input.readUTF());
                stmt.setString(4, input.readUTF());
                stmt.setString(5, email);


            }catch(IOException ex){
                ex.printStackTrace();
            }

            stmt.execute();

        }
        public void handleFirstSearch(DataInputStream from, DataOutputStream to)throws IOException, SQLException{
            String query = "select * from users where UID = 1";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery(query);

            if(rs.next()){
                to.writeUTF(rs.getString("username"));
            }
        }
        public void handleLastSearch(DataInputStream from, DataOutputStream to)throws IOException, SQLException{
            String query = "select * from users,(select max(UID) as maxuid from users) maxresults where UID = maxresults.maxuid";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery(query);

            if(rs.next()){
                to.writeUTF(rs.getString("username"));
            }
        }
        public void handleWCSearch (DataInputStream in, DataOutputStream out) throws IOException, SQLException{
            String query = "select * from users where username LIKE ? ";
            PreparedStatement stmt = connection.prepareStatement(query);

            stmt.setString(1, "%" + in.readUTF() + "%");
            ResultSet rs = stmt.executeQuery(query);

            if(rs.next()){
                out.writeUTF(rs.getString("username"));
            }
        }

        public void handleSendMessage(DataInputStream in, DataOutputStream out) throws IOException, SQLException{

            String message = in.readUTF();
            String book = in.readUTF();
            ArrayList<String> userList = new ArrayList<>();


            String query = "select * from users where book =  '" + book +"';";
            ResultSet rs = statement.executeQuery(query);


            int counter = 0;
            while(rs.next()){

                userList.add(counter, rs.getString("email"));
                counter++;
            }

            for(int i = 0; i<userList.size(); i++){

                String deleteQuery = "update users set messsges = NULL where email = ?";
                PreparedStatement delStatement = connection.prepareStatement(deleteQuery);
                delStatement.setString(1, userList.get(i));

                String messagequery = "update users set messages = ? where email = ?";
                PreparedStatement statement = connection.prepareStatement(messagequery);
                statement.setString(1, message);
                statement.setString(2, userList.get(i));
                statement.execute();
            }
        }
        public void handleGetMessages(DataInputStream in, DataOutputStream out) throws IOException, SQLException{
            ta.appendText("here");
            String email = in.readUTF();

            String messagesQuery = "select * from users where email = '" + email + "';";
            ResultSet rs = statement.executeQuery(messagesQuery);

            if(rs.next()){
                out.writeUTF(rs.getString("messages"));
            }
        }
    }

    public void initializeDB() throws ClassNotFoundException, SQLException{
        Class.forName("com.mysql.cj.jdbc.Driver");
        ta.appendText("Driver Loaded" + "\n");

        connection = DriverManager.getConnection(("jdbc:mysql://127.0.0.1:3306/assignment?user=root&password=toms427"));
        ta.appendText("DB Connected" + "\n");


        statement = connection.createStatement();


    }

}

