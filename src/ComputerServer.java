import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

public class ComputerServer extends JFrame implements ActionListener {
    static ServerSocket server;
    static Socket conn;
    JPanel panel;
    JTextField NewMsg;
    JTextArea ChatHistory;
    JButton Send;
    DataInputStream dis;
    DataOutputStream dos;
    ArrayList<UserAccount> userList;
    Map<String, PrintWriter> onlineStream;
    private static final String LOGIN_ACTION = "LOGIN";
    private static final String SIGNUP_ACTION = "SIGNUP";

    private static final String SIGNUP_SUCCESS = "SIGNUP_SUCCESS";
    private static final String SIGNUP_FAIL_USERNAME = "SIGNUP_FAIL_USERNAME";

    private static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    private static final String LOGIN_FAIL_PASSWORD = "LOGIN_FAIL_PASSWORD";
    private static final String LOGIN_FAIL_USERNAME = "LOGIN_FAIL_USERNAME";

    private static final String NOTIFY_ONLINE = "NOTIFY_ONLINE";
    private static final String REQUEST_ONLINE = "REQUEST_ONLINE";
    private static final String END_NOTIFY_ONLINE = "END_NOTIFY_ONLINE";


    public ComputerServer() throws UnknownHostException, IOException {
        userList = new ArrayList<>();
        onlineStream = new HashMap<>();
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
        server = new ServerSocket(8080);

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
            PrintWriter output;
            ChatHistory.setText(ChatHistory.getText() + '\n' + "Client Found");
            String[] arrIp = socket.getRemoteSocketAddress().toString().split(":");
            String ip = arrIp[0].substring(1);
            String port = arrIp[1];
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream());
                while (true){
                    try {
                        String action = input.readLine();
                        boolean loginOK = false;
                        if (action!=null)
                            switch (action){
                                case LOGIN_ACTION: {
                                    loginOK = doLogIn(input,output,ip);
                                } break;
                                case SIGNUP_ACTION: doSignUp(input,output,ip);break;
                            }
                        if (loginOK) {
                            onlineStream.put(ip,output);
                            break;
                        }
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
                while (true){
                    try {
                        String message = input.readLine();
                        if (message!=null) {
                            if (message.equals(REQUEST_ONLINE)) {
                                notifyOnlineUser();
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                while (true) {
                    try {
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

    private boolean doLogIn(BufferedReader in, PrintWriter out,String ip){
        try {
            boolean canLogin = false;
            String status = LOGIN_FAIL_USERNAME;
            String username = in.readLine();
            String password = in.readLine();
            int lengh = userList.size();
            for (int i=0;i<lengh;i++){
                UserAccount user = userList.get(i);
                if (user.getUsername().equals(username)) {
                    if(user.getPassword().equals(password)){
                        user.setOnline(true);
                        user.setIp(ip);
                        userList.set(i,user);
                        status= LOGIN_SUCCESS;
                        canLogin =  true;
                        break;
                    } else{
                        status = LOGIN_FAIL_PASSWORD;
                        break;
                    }
                }
            }
            out.write(status+"\n");
            ChatHistory.setText(ChatHistory.getText() + '\n' + status);
            out.flush();
            return canLogin;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void doSignUp(BufferedReader in, PrintWriter out,String ip){
        try {
            String username = in.readLine();
            String password = in.readLine();
            String accountname = in.readLine();
            boolean valid = true;
            for (UserAccount user:userList){
                if (user.getUsername().equals(username)) {
                    valid = false;
                    break;
                }
            }
            if (valid){
                UserAccount user = new UserAccount();
                user.setIp(ip);
                user.setUsername(username);
                user.setPassword(password);
                user.setAccountname(accountname);
                userList.add(user);
                out.write(SIGNUP_SUCCESS+"\n");
                out.flush();
                ChatHistory.setText("Success signup");
            } else{
                out.write(SIGNUP_FAIL_USERNAME+"\n");
                ChatHistory.setText(ChatHistory.getText() + '\n' + "fail signup");
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void notifyOnlineUser(){
        for (Map.Entry<String, PrintWriter> olUser : onlineStream.entrySet())
        {
            PrintWriter writer = olUser.getValue();
            String ip = olUser.getKey();
            try {
                writer.write(NOTIFY_ONLINE+"\n");
                writer.flush();
                for (UserAccount account : userList){
                    if (account.isOnline() && !account.getIp().equals(ip)){
                        writer.write(account.getAccountname()+":"+account.getIp()+'\n');
                    }

                }
                writer.flush();
                writer.write(END_NOTIFY_ONLINE+"\n");
                writer.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}