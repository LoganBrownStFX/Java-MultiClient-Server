

import java.io.*;
import java.net.*;
import java.text.DecimalFormat;

import javafx.application.Application;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Client2 extends Application {
    // IO streams
    DataOutputStream toServer = null;
    DataInputStream fromServer = null;

    @Override // Override the start method in the Application class
    public void start(Stage primaryStage) {

        GridPane mainPane = new GridPane();
        // Text area to display contents
        TextField emailTF = new TextField();
        TextField yrs = new TextField("Number of Years");
        TextField pmt = new TextField("PMT");
        Button submit = new Button("SUBMIT");
        Label fv = new Label("Future Value");
        Label status = new Label("Status");


        mainPane.add(emailTF,0,0);
        //mainPane.add(yrs,0,1);
        //mainPane.add(pmt,0,2);
        mainPane.add(fv,0,3);
        mainPane.add(status,0,5);
        mainPane.add(submit,4,8);

        mainPane.setAlignment(Pos.CENTER);
        emailTF.setAlignment(Pos.BOTTOM_RIGHT);
        yrs.setAlignment(Pos.BOTTOM_RIGHT);
        pmt.setAlignment(Pos.BOTTOM_RIGHT);
        fv.setAlignment(Pos.BOTTOM_RIGHT);

        // Create a scene and place it in the stage
        Scene scene = new Scene(mainPane, 400, 400);
        primaryStage.setTitle("Client"); // Set the stage title
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.show(); // Display the stage

        submit.setOnAction(e -> {
            try {
                // Get the radius from the text field
                //Double email = Double.parseDouble(emailTF.getText());
                String email = emailTF.getText();
                // Send the radius to the server
                toServer.writeUTF(email);
                //toServer.writeDouble(years);
                //toServer.writeDouble(monthPay);
                toServer.flush();

                // Get area from the server
                String mess = fromServer.readUTF();



                // Display to the text area
                fv.setText("Future Value is " + mess + "\n");

            }
            catch (IOException ex) {
                System.err.println(ex);
            }
        });

        try {
            // Create a socket to connect to the server
            Socket socket = new Socket("localhost", 8001);
            // Socket socket = new Socket("130.254.204.36", 8000);
            // Socket socket = new Socket("drake.Armstrong.edu", 8000);

            // Create an input stream to receive data from the server
            fromServer = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            // Create an output stream to send data to the server
            toServer = new DataOutputStream(socket.getOutputStream());
        }
        catch (IOException ex) {
            status.setText(ex.toString() + '\n');
        }
    }

}