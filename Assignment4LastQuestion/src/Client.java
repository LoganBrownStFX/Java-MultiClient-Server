import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.*;
import java.net.Socket;

public class Client extends Application {

    DataOutputStream toServer = null;
    DataInputStream fromServer = null;




    Stage window;
    Scene SignIn, SignUp, Home, Admin;

    private int token = -1;

    //Sign In Page
    private TextField email = new TextField("Email");
    private TextField password = new TextField("Password");
    private Button signIn = new Button("SIGN IN");
    private Label status = new Label("USER NOT SIGNED IN");
    private Button signUp = new Button("SIGN UP");
    //Sign Up Page
    private TextField name = new TextField("Name");
    private TextField book = new TextField("Favourtie Book");
    private TextField emailSU = new TextField("Email");
    private TextField passwordSU = new TextField("Password");
    private Button signUpSU = new Button("SIGN UP");
    private Label statusSU = new Label();
    //Regular User Home Page
    private TextField nameHome = new TextField("Name");
    private TextField bookHome = new TextField("Favourtie Book");
    private TextField emailHome = new TextField("Email");
    private TextField passwordHome = new TextField("Password");
    private Button updateInfo = new Button("Update");
    private TextArea messageTa = new TextArea();
    //Admin Home Page
    private TextField nameAdmin = new TextField("Name");
    private TextField bookAdmin = new TextField("Favourtie Book");
    private TextField emailAdmin = new TextField("Email");
    private TextField passwordAdmin = new TextField("Password");
    private Button updateAdmin = new Button("Update");
    private Button searchAdmin = new Button("Search");
    private TextField searchBox = new TextField();
    private TextField messageBox = new TextField();
    private TextField bookBox = new TextField();
    private Button sendButton = new Button("Send");
    private TextArea messageAdmin = new TextArea();







    public void start(Stage primaryStage){

        window = primaryStage;
        //Sign In page
        VBox main = new VBox(10);
        main.getChildren().addAll(email,password,signIn,signUp,status);
        SignIn = new Scene(main, 600, 600);

        //Sign Up Page
        VBox signUpScene = new VBox(10);
        signUpScene.getChildren().addAll(name, emailSU, passwordSU, book,statusSU, signUpSU);
        SignUp = new Scene(signUpScene, 600, 600);

        //Reg User Page
        VBox homePage = new VBox(10);
        homePage.getChildren().addAll(nameHome, emailHome, passwordHome, bookHome, updateInfo, messageTa);
        Home = new Scene(homePage, 600, 600);

        //Admin Page
        VBox adminPage = new VBox(10);
        HBox inside = new HBox(10);
        HBox inside2 = new HBox (10);
        inside.getChildren().addAll(searchBox, searchAdmin);
        inside2.getChildren().addAll(messageBox, bookBox, sendButton);
        adminPage.getChildren().addAll(nameAdmin, emailAdmin, passwordAdmin, bookAdmin, updateAdmin, inside2, inside, messageAdmin);
        Admin = new Scene(adminPage, 600, 600);


        window.setTitle("Client"); // Set the stage title
        window.setScene(SignIn); // Place the scene in the stage
        window.show(); // Display the stage

        //Handle the Two Buttons on the Main Landing Page
        signIn.setOnAction(e ->handleSignIn());
        signUp.setOnAction(e-> {
            primaryStage.setScene(SignUp);
            handleSignUp();
        });

        ConnectToServer();


    }

    public void ConnectToServer(){
        try {
            // Create a socket to connect to the server
            Socket socket = new Socket("localhost", 8001);
            toServer= new DataOutputStream(socket.getOutputStream());
            fromServer = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

        }
        catch (IOException ex) {
            status.setText(ex.toString() + '\n');
        }

    }
    //Handler for when the Sign In Button is Clicked
    public void handleSignIn() {

        token = 0;
        try {

            //Send Token
            toServer.write(token);
            toServer.flush();
            //Send Username and Email
            toServer.writeUTF(email.getText());
            /*if(validateField(email.getText())){

            } else{
                status.setText("Invalid email");
            }*/
            toServer.writeUTF(password.getText().trim());
            toServer.flush();

            //Handle the Response from the Backend
            String message = fromServer.readUTF();
            if(message.equals("1")){
                status.setText("User Authenticated");
                goToHomePage(email.getText());

            }else if (message.equals("0")){
                status.setText("Admin Logged In");
                goToAdminHome(email.getText());
            } else{
                status.setText("Wrong Email or Password");
            }

        }catch (IOException ex){
                ex.printStackTrace();
        }


    }

    //Handle the Sign Up Button
    public void handleSignUp(){
        token = 1;

        signUpSU.setOnAction(e ->{
            try {
                //Send Token
                toServer.write(token);
                toServer.flush();
                //Send Entered Fields
                toServer.writeUTF(name.getText());
                if(validateField(email.getText())){
                    toServer.writeUTF(emailSU.getText());
                }
                toServer.writeUTF(passwordSU.getText());
                toServer.writeUTF(book.getText());
                toServer.flush();

                //Handle Backends Reply
                if(fromServer.readBoolean()){
                    statusSU.setText("User Successfully Signed Up");
                    goToHomePage(emailSU.getText());
                }else{
                    statusSU.setText("Error: Can't register User");

                }
            }catch (IOException ex){
                ex.printStackTrace();
            }

        });


    }

    //Load the Home Page
    public void goToHomePage(String userEmail) throws IOException{

        window.setScene(Home);
        emailHome.setText(userEmail);

        /*

        token = 7;
        toServer.write(token);
        toServer.flush();
        toServer.writeUTF(userEmail);
        */
        //Receive the Messages for the Signed in User
        //messageTa.appendText("Message from Admin: " + fromServer.readUTF() + "\n");

        updateInfo.setOnAction(e -> {
            token = 2;

            //Make sure the User is only Editing their own information
            if(userEmail.equals(emailHome.getText()) && validateField(emailHome.getText())){
                try{
                    toServer.write(token);
                    toServer.flush();

                    toServer.writeUTF(emailHome.getText());
                    toServer.writeUTF(nameHome.getText());
                    toServer.writeUTF(passwordHome.getText());
                    toServer.writeUTF(book.getText());
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
    }
    //Load the Admin's Home Page
    public void goToAdminHome(String userEmail) throws IOException{
        window.setScene(Admin);
        emailAdmin.setText(userEmail);
        /*
        token = 7;
        toServer.write(token);
        toServer.flush();
        toServer.writeUTF(userEmail);
        //Receive the Messages for the Signed in User
        messageTa.appendText("Message from Admin: " + fromServer.readUTF() + "\n");
        */
        updateAdmin.setOnAction(e -> {
            token = 2;
            if(userEmail.equals(emailAdmin.getText())){
                try{
                    toServer.write(token);
                    toServer.flush();

                    toServer.writeUTF(nameHome.getText());
                    toServer.writeUTF(passwordHome.getText());
                    toServer.writeUTF(book.getText());
                    toServer.writeUTF(nameHome.getText());



                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }

        });

        searchAdmin.setOnAction(event -> {

            if(searchBox.getText().equals("First")){
                token = 3;
                try{
                    toServer.write(token);
                    toServer.flush();

                    String firstUser = fromServer.readUTF();

                    messageAdmin.appendText("First User = " + firstUser + "\n");
                } catch(IOException ex){
                    ex.printStackTrace();
                }
            }else if (searchBox.getText().equals("Last")){
                token = 4;
                try{
                    toServer.write(token);
                    toServer.flush();

                    String firstUser = fromServer.readUTF();

                    messageAdmin.appendText("Last User = " + firstUser + "\n");
                } catch(IOException ex){
                    ex.printStackTrace();
                }
            } else{
                token = 5;

                try{
                    toServer.write(token);
                    toServer.flush();

                    toServer.writeUTF(searchBox.getText().trim());
                    String user = fromServer.readUTF();

                    messageAdmin.appendText("Search Results: " + user + "\n");


                }catch (IOException ex){
                    ex.printStackTrace();
                }
            }
        });

        sendButton.setOnAction(e ->{
            token = 6;

            try{
                toServer.write(token);
                toServer.flush();

                toServer.writeUTF(messageBox.getText().trim());
                toServer.writeUTF(bookBox.getText());
                toServer.flush();

            }catch (IOException ex){
                ex.printStackTrace();
            }
        });
    }
    public boolean validateField(String entry){
        if (!entry.contains("@") && (!entry.contains("."))){
            return false;
        }
        return true;
    }
}

