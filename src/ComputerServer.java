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
    ArrayList<UserAccount> userList;
    Map<String, PrintWriter> onlineStream;
    Map<String, PrintWriter> groupStream;
    ArrayList<GroupUser> groupUsersList;
    GroupUser groupOne;


    public ComputerServer() throws IOException {
        userList = new ArrayList<>();
        onlineStream = new HashMap<>();
        groupStream = new HashMap<>();
        groupUsersList=new ArrayList<>();
        ArrayList<UserAccount> listUserInGroup=new ArrayList<>();
        ArrayList<GroupMessage> listMessageInGroup=new ArrayList<>();
        groupOne=new GroupUser("MMT", listUserInGroup, listMessageInGroup);
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
        server = new ServerSocket(8080);

        System.out.print(InetAddress.getLocalHost());
        ChatHistory.setText("Waiting for Client");
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
                boolean checkGroup=false;
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream());
                while (true){
                    Thread.sleep(50);
                    try {
                        String action = input.readLine();
                        boolean loginOK = false;
                        if (action!=null)
                            switch (action){
                                case SocketProtocol.LOGIN_ACTION: {
                                    loginOK = doLogIn(input,output,ip);
                                    if (loginOK) onlineStream.put(ip,output);
                                    checkGroup=false;
                                } break;
                                case SocketProtocol.SIGNUP_ACTION: doSignUp(input,output,ip);break;
                                case SocketProtocol.GROUP_ACTION:{
                                    groupStream.put(ip, output);
                                    loginOK=true;
                                    checkGroup=true;
                                    
                                }break;
                                case SocketProtocol.UPDATE_IMAGE: {
                                    String filename = input.readLine();
                                    try{
                                        File file = new File(filename);
                                        DataInputStream dis = new DataInputStream(socket.getInputStream());
                                        FileOutputStream fos = new FileOutputStream(file);
                                        byte[] buffer = new byte[4069];
                                        int read;
                                        while ((read = dis.read(buffer)) > 0) {
                                            fos.write(buffer, 0, read);
                                        }
//                                    while (dis.read(buffer) > 0) {
//                                        fos.write(buffer);
//                                    }
                                        dis.close();
                                        fos.close();

                                    } catch (Exception e){
                                        e.printStackTrace();
                                        System.out.println(e.toString());
                                    }

//                                    if (socket!=null) socket.close();
                                }
                            }
                        if (loginOK) {
                            break;
                        }
                    } catch (IOException e){
                        e.printStackTrace();
                        return;
                    }
                }
                if(checkGroup==false){
                    while (true){
                        Thread.sleep(50);
                        try {
                            String message = input.readLine();
                            if (message!=null) {
                                switch (message){
                                    case SocketProtocol.GET_PERSONAL_INFO:{
                                        boolean exist = false;
                                        for (UserAccount user:userList){
                                            if (user.getIp().equals(ip)){
                                                output.write(SocketProtocol.VALID_USER+"\n");
                                                output.write(user.getAccountname()+"\n");
                                                output.write(user.getUsername()+"\n");
                                                output.write(user.getIp()+"\n");
                                                output.flush();
                                                exist = true;
                                                break;
                                            }
                                        }
                                        if (!exist){
                                            output.write(SocketProtocol.INVALID_USER+"\n");
                                            output.flush();
                                        }
                                        break;
                                    }
                                    case SocketProtocol.REQUEST_ONLINE:{
                                        notifyOnlineUser();
                                        break;
                                    }
                                    case SocketProtocol.LOGOUT_ACTION:{
                                        onlineStream.remove(ip);
                                        removeUserByIp(ip);
                                        notifyOnlineUser();
                                        break;
                                    }   
                                    
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                }
                else{
                    while (true){
                        Thread.sleep(50);
                        try {
                            String message = input.readLine();
                            if (message!=null) {
                                switch (message){
                                    case SocketProtocol.JOIN_TO_GROUP:{
                                        // String subIp=input.readLine();
                                        for(UserAccount user:userList){
                                            if(user.getIp().equals(ip)){
                                                groupOne.listUser.add(user);
                                            }
                                        }
                                        notifyGroupUser();
                                        break;
                                    }  
                                    case SocketProtocol.REQ_TO_GET_MESSAGE:{
                                        output.write(SocketProtocol.MESSAGE_RESPONE_IN_GROUP+"\n");
                                        output.flush();
                                        for (GroupMessage msg:groupOne.listMessage){
                                            output.write(msg.getUsername()+"\n");
                                            output.write(msg.getAccountname()+"\n");
                                            output.write(msg.getMessage()+"\n");
                                        }
                                        output.flush();
                                        output.write(SocketProtocol.END_MESSAGE_RESPONE_IN_GROUP+"\n");
                                        output.flush();
                                        break;
                                    }
                                    case SocketProtocol.MESSAGE_IN_GROUP:{
                                        String newMessage=input.readLine();
                                        // String myIP=input.readLine();
                                        String[] name = getNameByIp(ip);
                                        GroupMessage gMsg = new GroupMessage(name[0],name[1],newMessage);
                                        groupOne.listMessage.add(gMsg);
                                        sendMessageToUserInGroup(gMsg,ip);
                                        break;
                                    }
                                    case SocketProtocol.OUT_GROUP:{
                                        groupStream.remove(ip);
                                        for(int i=0;i<groupOne.listUser.size();i++){
                                            UserAccount user=groupOne.listUser.get(i);
                                            if(user.getIp().equals(ip)){
                                                groupOne.listUser.remove(user);
                                            }
                                        }
                                        notifyGroupUser();
                                        
                                    }
                    
                                }
                                
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }
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
            String status = SocketProtocol.LOGIN_FAIL_USERNAME;
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
                        status= SocketProtocol.LOGIN_SUCCESS;
                        canLogin =  true;
                        break;
                    } else{
                        status = SocketProtocol.LOGIN_FAIL_PASSWORD;
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
                out.write(SocketProtocol.SIGNUP_SUCCESS+"\n");
                out.flush();
                ChatHistory.setText("Success signup");
            } else{
                out.write(SocketProtocol.SIGNUP_FAIL_USERNAME+"\n");
                ChatHistory.setText(ChatHistory.getText() + '\n' + "fail signup");
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void notifyGroupUser(){
        for (Map.Entry<String, PrintWriter> pair : groupStream.entrySet())
        {
            PrintWriter writer = pair.getValue();
            String ip = pair.getKey();
            try {
                writer.write(SocketProtocol.NOTIFY_JOIN_TO_GROUP+"\n");
                writer.flush();
                for(UserAccount account:groupOne.listUser){
                    writer.write(account.getAccountname()+":"+account.getIp()+"\n");
                }
                writer.flush();
                writer.write(SocketProtocol.END_NOTIFY_JOIN_TO_GROUP+"\n");
                writer.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    void sendMessageToUserInGroup(GroupMessage gMsg,String myIp){
        for (Map.Entry<String, PrintWriter> pair : groupStream.entrySet())
        {
            PrintWriter writer = pair.getValue();
            String ip = pair.getKey();
            if (myIp.equals(ip)) continue;
            try {
                writer.write(SocketProtocol.MESSAGE_SINGLE_RESPONE_IN_GROUP+"\n");
                writer.write(gMsg.getUsername()+"\n");
                writer.write(gMsg.getAccountname()+"\n");
                writer.write(gMsg.getMessage()+"\n");
                writer.flush();
                writer.write(SocketProtocol.END_MESSAGE_SINGLE_RESPONE_IN_GROUP+"\n");
                writer.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void notifyOnlineUser(){
        for (Map.Entry<String, PrintWriter> olUser : onlineStream.entrySet())
        {
            PrintWriter writer = olUser.getValue();
            String ip = olUser.getKey();
            try {
                writer.write(SocketProtocol.NOTIFY_ONLINE+"\n");
                writer.flush();
                for (UserAccount account : userList){
                    if (account.isOnline() && !account.getIp().equals(ip)){
                        writer.write(account.getAccountname()+":"+account.getIp()+'\n');
                    }

                }
                writer.flush();
                writer.write(SocketProtocol.END_NOTIFY_ONLINE+"\n");
                writer.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void removeUserByIp(String ip){
        int lengh = userList.size();
        for (int i=0;i<lengh;i++){
            UserAccount user = userList.get(i);
            if (user.getIp().equals(ip)){
                user.setOnline(false);
            }
            userList.set(i,user);
        }
    }

    String[] getNameByIp(String ip){
        for (UserAccount account:userList){
            if (account.getIp().equals(ip)) return new String[] {account.getUsername(),account.getAccountname()};
        }
        return new String[] {"",""};
    }
}