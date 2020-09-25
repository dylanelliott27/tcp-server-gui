package com.server;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


import java.net.*;
import java.io.IOException;

import static java.lang.Integer.parseInt;


public class TcpServer {
    private int port, maxPendingConnections;
    private String ipAddress;
    private ServerSocket simpleServ;
    private Socket fakeClient;
    @FXML
    private TextArea textArea;
    @FXML
    private TextField portTextField;
    @FXML
    private TextField ipTextField;
    @FXML
    private TextField maxConnectionsField;
    @FXML
    private TextField toStrField;

    @FXML
    public void initialize() {
        toStrField.setText(toString());
    }


    public TcpServer() {
        port = 3000;
        ipAddress = "0.0.0.0";
        maxPendingConnections = 50;
    }

    public void verifySettings() {
        textArea.clear();
        port = 0;
        ipAddress = null;
        maxPendingConnections = 0;

        if (portTextField.getText().equals("")) {
            port = 3000;
        } else {
            try {
                setPort(parseInt(portTextField.getText()));
            } catch (Exception e) {
                if (e instanceof NumberFormatException) {
                    textArea.appendText("Port number must be an integer. Re-enter and verify config again. \n");
                } else {
                    textArea.appendText("Port must be between 0 - 65535. Re-enter and verify config again. \n");
                }
            }
        }

        if (ipTextField.getText().equals("")) {
            ipAddress = "0.0.0.0";
        } else {
            try {
                setIP(ipTextField.getText()); // As per docs if not defined then defaults to all local addresses
            } catch (Exception e) {
                textArea.appendText("IP Address either not found on system, or invalid format. (ex: 127.0.0.1 or localhost. Re-enter and verify config again. \n");
            }
        }

        if (maxConnectionsField.getText().equals("")) {
            maxPendingConnections = 50;
        } else {
            try {
                setMaxConnections(parseInt(maxConnectionsField.getText())); // Defaults to 50 if not defined
            } catch (Exception e) {
                if (e instanceof NumberFormatException) {
                    textArea.appendText("Max Pending Connections must be an integer. Re-enter and verify config again. \n");
                } else {
                    textArea.appendText("Max Pending Connections must be between 0 - 100. Re-enter and verify config again. \n");
                }
            }
        }

        if (port != 0 && maxPendingConnections != 0 && ipAddress != null) {
            textArea.appendText("CONFIG IS GOOD! Ready to init on port: " + port + " IP: " + ipAddress + " MaxCon: " + maxPendingConnections + "\n Press Initialize Server to start!");
            toStrField.appendText(toString());
        }
    }

    public void initializeServer() {
        try {
            simpleServ = new ServerSocket(getPort());
            System.out.println("SERVER STARTED ON: " + getIP() + ":" + getPort());
            textArea.setText("SERVER STARTED ON: " + getIP() + ":" + getPort() + "\n");
            this.listenForConnections();
        } catch (IOException exception) {
            textArea.setText("Server already listening on this port, or error binding to port \n");
        }
    }


    public String getIP() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public int getMaxConnections() {
        return maxPendingConnections;
    }

    public String toString() {
        return ("Simple TCP server ready to go on IP address: " + getIP() + ":" + getPort());
    }

    public void setIP(String ipAddress) {
        // Best way to validate this is through the getByName method...
        // As per https://docs.oracle.com/javase/7/docs/api/java/net/InetAddress.html#getByName(java.lang.String)
        // Will throw exception if value passed to it is not valid/not found.

        try {
            InetAddress.getByName(ipAddress);
        } catch (Exception e) {
            // Throw back new exception
            throw new IllegalArgumentException("IP address invalid/not found. Re-enter and verify config again.");
        }
        this.ipAddress = ipAddress;
    }


    public void setMaxConnections(int connections) {
        if (connections > 100 || connections < 0)
            throw new IllegalArgumentException("Max pending connections must be less than 100 & greater than 0. Re-enter and verify config again.");
        else
            this.maxPendingConnections = connections;
    }


    public void setPort(int port) {
        if (port < 0 || port > 65535)
            throw new IllegalArgumentException("Port cannot be less than 0 or greater than 65535. Re-enter and verify config again.");
        else
            this.port = port;
    }


    public void listenForConnections() {
        textArea.appendText("Waiting for incoming connections... \n");
        // Pass to another thread as ServerSocket.accept is blocking & need to call the connect method still
        new Thread(() -> {
            try {
                Socket connectedClient = simpleServ.accept();
                System.out.println("CONNECTION MADE FROM: " + connectedClient.getRemoteSocketAddress());
                textArea.appendText("CONNECTION MADE FROM: " + connectedClient.getRemoteSocketAddress() + "\n");
            } catch (IOException exception) {
                System.out.println("Server socket closed early");
            }
        }).start();
    }

    public TcpServer connectFakeClient() {
        if (simpleServ == null) {
            textArea.setText("Please initialize server first.");
            return null;
        }
        if (fakeClient != null) {
            textArea.setText("You can only spawn one fake client");
            return null;
        }
        try {
            InetAddress fakeClientIP = InetAddress.getByName("localhost");
            fakeClient = new Socket(fakeClientIP, getPort());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return this;
    }

    public void cleanup() {
        if (simpleServ == null && fakeClient == null) {
            textArea.setText("There is no open sockets to close");
            return;
        }

        try {
            System.out.println("SHUTTING DOWN");
            textArea.appendText("SHUTTING DOWN \n");
            simpleServ.close();
            if (fakeClient != null) {
                fakeClient.close();
                fakeClient = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
