package com.muc;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient {
    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private OutputStream serverOut;
    private InputStream serverIn;
    private BufferedReader bufferedIn;

    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    private ArrayList<MessageListener> messageListeners = new ArrayList<>();

    public ChatClient(String serverName, int serverPort) {
    this.serverName = serverName;
    this.serverPort = serverPort;
    }

    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient("localhost",8818);
        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String login) {
                System.out.println("Online: "+ login);
            }

            @Override
            public void offline(String login) {
                System.out.println("Offline: "+ login);
            }
        });

        client.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String fromLogin, String msgBody) {
                System.out.println("You got a message from "+ fromLogin + "===>"+ msgBody);
            }
        });

        if(!client.connect()){
            System.err.println("Connect Failed");
        }else{
            System.out.println("Connect success");
            if(client.login("guest","guest")){
                System.out.println("Login Successful");

                client.msg("jim", "Hello world");
            }else{
                System.out.println("Login failed");
            }

            //client.logOff();
        }
    }

    private void msg(String sendTo, String msgBody) throws IOException {
        String cmd = "msg " + sendTo + " " + msgBody + "\n";
        serverOut.write(cmd.getBytes());
    }

    private void logOff() throws IOException {
        String cmd = "logoff\n";
        serverOut.write(cmd.getBytes());
    }

    private boolean login(String login, String password) throws IOException {
        String cmd = "login "+login +" "+password + "\n";
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        System.out.println("Response Line: "+response);

        if("ok login".equalsIgnoreCase(response)){
            startMessageReader();
            return true;
        }else{
            return false;
        }
    }

    private void startMessageReader() {
        Thread t = new Thread(){
            @Override
            public void run(){
                readMessageLoop();
            }
        };
        t.start();
    }

    private void readMessageLoop() {
        try {
            String line;
            while ((line = bufferedIn.readLine()) != null) {
                String[] tokens = StringUtils.split(line);
                //String[] tokens = Line.split(" ");
                if(tokens != null && tokens.length>0) {
                    String cmd = tokens[0];
                    if("online".equalsIgnoreCase(cmd)){
                        handleOnline(tokens);
                    } else if("offline".equalsIgnoreCase(cmd)){
                        handleOffline(tokens);

                    }else if("msg".equalsIgnoreCase(cmd)){
                        String[] tokensMsg = StringUtils.split(line,null,3);
                        handleMessage(tokensMsg);
                    }
                }

            }
        }catch (Exception ex){
            ex.printStackTrace();
            try{
                socket.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    private void handleMessage(String[] tokensMsg) {
        String login = tokensMsg[1];
        String msgBody = tokensMsg[2];

        for(MessageListener listener : messageListeners){
            listener.onMessage(login, msgBody);
        }
    }

    private void handleOffline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListeners){
            listener.offline(login);
        }
    }

    private void handleOnline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListeners){
            listener.online(login);
        }
    }

    private boolean connect() {
        try {
            this.socket = new Socket(serverName, serverPort);
            System.out.println("Client socket port is "+socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public void addUserStatusListener(UserStatusListener listener){
        userStatusListeners.add(listener);
    }
    public void removeUserStatusListener(UserStatusListener listener){
        userStatusListeners.remove(listener);
    }
    public void addMessageListener(MessageListener listener){
        messageListeners.add(listener);
    }
    public void removeMessageListener(MessageListener listener){
        messageListeners.remove(listener);
    }

}
