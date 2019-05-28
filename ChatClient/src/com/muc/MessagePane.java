package com.muc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Create the GUI JPanel to send messages
 */

public class MessagePane extends JPanel implements MessageListener {
    private final ChatClient client;
    private final String login;



    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> messageList = new JList<>(listModel);
    private JTextField inputField = new JTextField();

    /**
     * new Pane allowing guest to send messages and recieve messages from pane
     * @param client
     * @param login
     *
     */
    public MessagePane(ChatClient client, String login){
        this.client = client;
        this.login = login;

        client.addMessageListener(this);

        setLayout(new BorderLayout());
        add(new JScrollPane(messageList), BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String text = inputField.getText();
                    client.msg(login,text);
                    listModel.addElement("You: "+ text);
                    inputField.setText("");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    /**
     * displays recieved message in pane
     * @param fromLogin
     * @param msgBody
     */
    public void onMessage(String fromLogin, String msgBody){
        if(login.equalsIgnoreCase(fromLogin)) {
            String line = fromLogin + ": " + msgBody;
            listModel.addElement(line);
        }
    }
}
