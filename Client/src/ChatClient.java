import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.google.gson.*;

public class ChatClient {
    JTextArea chatArea; // окно для сообщений
    JTextField outputLine; //отправка сообщений
    BufferedReader reader;
    PrintWriter writer;
    Socket sock;
    JFrame frame;
    String name;


    public static void main(String []args) {
        new ChatClient().go();
    }

    public void initUser() {
        JDialog initFrame = new JDialog(frame,"Зарегестрируйтесь", true);
        initFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel initPanel = new JPanel();
        JButton initButton = new JButton("Войти");
        JTextField nameText = new JTextField(20);

        initButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (!nameText.getText().equals("")) {
                    name = nameText.getText();
                    initFrame.setVisible(false);
                }
            }
        });

        initPanel.add(nameText);
        initPanel.add(initButton);

        initFrame.getContentPane().add(BorderLayout.CENTER, initPanel);
        initFrame.setSize(500,150);
        initFrame.setVisible(true);
    }

    public void go() {
        /*
        Инициализируем GUI и принимающий поток
         */
        frame = new JFrame("Клиент чата");
        JPanel mainPanel = new JPanel();

        name = "";
        while (name.equals("")) initUser();

        chatArea = new JTextArea(15,50);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setEditable(false);

        JScrollPane scroller = new JScrollPane(chatArea);
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        outputLine = new JTextField(20);
        JButton sendButton = new JButton("Отправить");
        sendButton.addActionListener(new SendButtonListener());

        mainPanel.add(scroller);
        mainPanel.add(outputLine);
        mainPanel.add(sendButton);
        setUpNetworking();
        Date now = new Date();
        SimpleDateFormat formatNow = new SimpleDateFormat(" dd.MM.yy hh:mm:ss ");
        writer.println(formatNow.format(now) + "Server: " + name + " присоединился.");

        Thread readerThread = new Thread(new IncomingReader());
        readerThread.start();

        frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
        frame.setSize(700, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }


    private void setUpNetworking() {
        try {
            sock = new Socket("192.168.1.151",5000);
            InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
            reader = new BufferedReader(streamReader);
            writer = new PrintWriter(sock.getOutputStream());
            System.out.println("Подключено.");
        } catch(IOException ex) {ex.printStackTrace();}
    }


    public class SendButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            try {
                Date now = new Date();
                SimpleDateFormat formatNow = new SimpleDateFormat(" dd.MM.yy hh:mm:ss ");
                String message = formatNow.format(now) + name + ": " +  outputLine.getText();
                writer.println(message);
                writer.flush();
            }
            catch(Exception ex) {ex.printStackTrace();}
            outputLine.setText("");
            outputLine.requestFocus();
        }
    }


    public class IncomingReader implements Runnable {
        public void run() {
            String message;

            try {
                while ((message = reader.readLine()) != null) {
                    chatArea.append(message + "\n");
                }
            }
            catch(Exception ex) {ex.printStackTrace();}
        }
    }
}
