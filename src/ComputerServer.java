import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Random;

public class ComputerServer extends JFrame implements ActionListener {
    static ServerSocket server;
    static Socket conn;
    JPanel panel;
    JTextField NewMsg;
    JTextArea ChatHistory;
    JButton Send;
    DataInputStream dis;
    DataOutputStream dos;

    public ComputerServer() throws UnknownHostException, IOException {

        panel = new JPanel();
        NewMsg = new JTextField();
        ChatHistory = new JTextArea();
        Send = new JButton("Send");
        this.setSize(500, 500);
        this.setVisible(true);
        this.setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        panel.setLayout(null);
        this.add(panel);
        ChatHistory.setBounds(20, 20, 450, 360);
        panel.add(ChatHistory);
        NewMsg.setBounds(20, 400, 340, 30);
        panel.add(NewMsg);
        Send.setBounds(375, 400, 95, 30);
        panel.add(Send);
        this.setTitle("Server");
        Send.addActionListener(this);
        InetAddress locIP = InetAddress.getByName("192.168.1.101");
//        InetAddress locIP = InetAddress.getLocalHost();
        //        server = new ServerSocket(8090, 1, InetAddress.getLocalHost());
        server = new ServerSocket(8090, 0, locIP);

        System.out.print(InetAddress.getLocalHost());
        ChatHistory.setText("Waiting for Client");
//        ChatHistory.setText(ChatHistory.getText() + '\n' + "Client Found");
        while (true) {
            try {
                conn = server.accept();
                new EchoThread(conn).start();
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
//            try {
//                DataInputStream dis = new DataInputStream(conn.getInputStream());
//                String string = dis.readUTF();
//                ChatHistory.setText(ChatHistory.getText() + '\n' + "Client:"
//                        + string);
//            } catch (Exception e1) {
//                ChatHistory.setText(ChatHistory.getText() + '\n'
//                        + "Message sending fail:Network Error");
//                try {
//                    Thread.sleep(3000);
//                    System.exit(0);
//                } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
        }
    }

    public class EchoThread extends Thread {
        protected Socket socket;

        public EchoThread(Socket clientSocket) {
            this.socket = clientSocket;
        }

        public void run() {
            Random random = new Random();
            int ran = random.nextInt()%100;
            BufferedReader input;
            ChatHistory.setText(ChatHistory.getText() + '\n' + "Client Found");
//            try {
////                inp = socket.getInputStream();
////                brinp = new BufferedReader(new InputStreamReader(inp));
////                out = new DataOutputStream(socket.getOutputStream());
//                DataInputStream dis = new DataInputStream(conn.getInputStream());
//                String string = dis.readUTF();
//            } catch (IOException e) {
//                return;
//            }
            try {
//                PrintWriter output = new PrintWriter(socket.getOutputStream());
                while (true) {
                    try {
                        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String message = input.readLine();
                        if (message!=null) ChatHistory.setText(ChatHistory.getText() + '\n' + "Thread "+ran+":"+message);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        if ((e.getSource() == Send) && (NewMsg.getText() != "")) {
            ChatHistory.setText(ChatHistory.getText() + '\n' + "ME:"
                    + NewMsg.getText());
            try {
                DataOutputStream dos = new DataOutputStream(
                        conn.getOutputStream());
                dos.writeUTF(NewMsg.getText()+"\n");
//                PrintWriter output = new PrintWriter(conn.getOutputStream());
//                output.write(NewMsg.getText()+"\n");
//                output.flush();
            } catch (Exception e1) {
                try {
                    Thread.sleep(3000);
                    System.exit(0);
                } catch (InterruptedException e2) {
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                }
            }
            NewMsg.setText("");
        }
    }

    public static void main(String[] args) throws UnknownHostException,
            IOException {
        new ComputerServer();
    }
}