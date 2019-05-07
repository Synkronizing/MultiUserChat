package com.muc;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ServerWorker extends Thread{

    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;

    public ServerWorker(Server server, Socket clientSocket)
    {
        this.server = server;
        this.clientSocket = clientSocket;
    }

        @Override
        public void run(){
            try{
                handleClientSocket();
            }catch(IOException e){
                e.printStackTrace();
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }


    private void handleClientSocket() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while((line = reader.readLine()) != null){
            String[] tokens = StringUtils.split(line);
            //String[] tokens = Line.split(" ");
            if(tokens != null && tokens.length>0){
                String cmd = tokens[0];

                if("logoff".equalsIgnoreCase(cmd) || "quit".equalsIgnoreCase(cmd)){
                    handleLogOff();
                    break;
                } else if ("login".equalsIgnoreCase(cmd)){
                    handleLogin(outputStream,tokens);
                }
                else{
                    String msg = "unknown "+cmd+"\n\r";
                    outputStream.write(msg.getBytes());
                }
            }
        }
        clientSocket.close();
    }

    private void handleLogOff() throws IOException {
        List<ServerWorker> workerList = server.getWorkerList();
        // send other online users current status
        String onlineMsg = "offline "+ login+"\n";
        for(ServerWorker worker : workerList){
            if(!login.equals(worker.getLogin())) {
                worker.send(onlineMsg);
            }
        }
        clientSocket.close();
    }

    public String getLogin() {

        return login;
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if(tokens.length == 3){
            String login = tokens[1];
            String password = tokens[2];
            if(login.equals("guest")&& password.equals("guest") || (login.equals("jim") && password.equals("bob"))){
                String msg = "ok login\n\r";
                outputStream.write(msg.getBytes());

                this.login = login;
                System.out.println("User logged in successfully "+ login);

                List<ServerWorker> workerList = server.getWorkerList();

                //send current user all other online logins
                for(ServerWorker worker: workerList){
                    if(worker.getLogin()!= null) {  // to not show null login for other users
                        if(!login.equals(worker.getLogin())) { // doesn't show that I am online to myself
                            String msg2 = "online " + worker.getLogin() + "\n";
                            send(msg2);
                        }
                    }
                }

                // send other online users current status
                String onlineMsg = "online "+ login+"\n";
                for(ServerWorker worker : workerList){
                    if(!login.equals(worker.getLogin())) {

                        worker.send(onlineMsg);
                    }
                }
            }else{
                String msg = "error login\n\r";
                outputStream.write(msg.getBytes());

            }
        }

    }

    private void send(String onlineMsg) throws IOException {
        if(login != null) {
            outputStream.write(onlineMsg.getBytes());
        }
    }

}
