import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Server extends JFrame {

    private JTextField userText;
    private JTextArea chatWindow;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private ServerSocket server;
    private Socket connection;

    //constructor
    public Server(){
        super("Server (Instant Messenger)");
        userText = new JTextField();
        userText.setEditable(false);
        userText.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        sendMessage(e.getActionCommand());
                        userText.setText("");
                    }
                }
        );
        add(userText, BorderLayout.NORTH);
        chatWindow = new JTextArea();
        add(new JScrollPane(chatWindow));
        setSize(300,150);
        setVisible(true);
    }

    //set up and run the server
    public void startRunning(){
        try{
            server = new ServerSocket(6789,100);
            while (true){
                try{
                    //connect and have conversation
                    waitForConnection();
                    setUpStreams();
                    whileChatting();
                } catch (EOFException eofException){
                    showMessage("\n Server ended the connection!");
                } finally {
                    closeCrap();
                }
            }

        } catch (IOException ioException){
            ioException.printStackTrace();
        }
    }

    //wait for connection, then display connection information
    private void waitForConnection() throws IOException{
        showMessage(" Waiting for someone to connect... \n");
        connection = server.accept();
        showMessage(" Now connected to " + connection.getInetAddress().getHostName());
    }

    //get stream to send and receive data
    private void setUpStreams() throws IOException {
        outputStream = new ObjectOutputStream(connection.getOutputStream());
        outputStream.flush();
        inputStream = new ObjectInputStream(connection.getInputStream());
        showMessage("\n Streams are now set up! \n");
    }

    //during the chat conversation
    private void whileChatting() throws IOException{
        String message = " You are now connected! ";
        sendMessage(message);
        ableToType(true);
        do {
            try {
                message = (String) inputStream.readObject();
                showMessage("\n" + message);
            } catch (ClassNotFoundException classNotFoundException){
                showMessage("\n idk what the user sent! ");
            }
        } while (!message.equals("CLIENT - END"));
    }

    //close streams and sockets after you are done chatting
    private void closeCrap(){
        showMessage("\n Closing connections... \n");
        ableToType(false);
        try{
            outputStream.close();
            inputStream.close();
            connection.close();
        }catch(IOException ioException){
            ioException.printStackTrace();
        }
    }

    //send a message to client
    private void sendMessage(String message){
        try {
            outputStream.writeObject("SERVER - " + message);
            outputStream.flush();
            showMessage("\nSERVER - " + message);
        }catch(IOException ioException){
            chatWindow.append("\n ERROR: CAN'T SEND MESSAGE");
        }
    }

    //updates chatWindow
    private void showMessage(final String text) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        chatWindow.append(text);
                    }
                }
        );
    }

    //let the user type stuff into their box
    private void ableToType(final boolean tof){
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        userText.setEditable(tof);
                    }
                }
        );
    }
}