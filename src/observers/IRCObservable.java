package observers;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;

public class IRCObservable extends Thread {

    private boolean changed = false;
    private Vector<IRCObserver> obs;

    private String server;
    private int port;
    private String nick;
    private String username;
    private String realName;
    private String password;

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    private boolean isConnected = false;
    private ArrayList<String> channels = new ArrayList<>();

    public IRCObservable() {
        obs = new Vector<>();
    }

    public IRCObservable(String server, int port, String nick) {
        this();
        this.server = server;
        this.port = port;
        this.nick = nick;
    }

    public synchronized void addObserver(IRCObserver o) {
        if (o == null)
            throw new NullPointerException();
        if (!obs.contains(o)) {
            obs.addElement(o);
        }
    }

    public synchronized void deleteObserver(IRCObserver o) {
        obs.removeElement(o);
    }

    public void notifyObservers() {
        notifyObservers(null, null, null);
    }

    public void notifyObservers(String channel, String name, String message) {

        Object[] arrLocal;

        synchronized (this) {
            if (!changed)
                return;
            arrLocal = obs.toArray();
            clearChanged();
        }

        for (int i = arrLocal.length - 1; i >= 0; i--)
            ((IRCObserver) arrLocal[i]).ircMessageReceived(this, channel, name, message);
    }

    public synchronized void deleteObservers() {
        obs.removeAllElements();
    }

    protected synchronized void setChanged() {
        changed = true;
    }

    protected synchronized void clearChanged() {
        changed = false;
    }

    public synchronized boolean hasChanged() {
        return changed;
    }

    public synchronized int countObservers() {
        return obs.size();
    }


    @Override
    public void run() {
        super.setName(nick + " - " + server + " IRC Thread");

        try {
            // Connect directly to the IRC server.
            socket = new Socket(server, port);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            if (password != null && password.length() > 0)
                writer.write("PASS " + password + "\r\n");

            // Log on to the server.
            writer.write("NICK " + nick + "\r\n");
            writer.write("USER " + username + " 8 * : " + realName + "\r\n");

            writer.flush();

            // Read lines from the server until it tells us we have connected.
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("004")) {
                    // We are now logged in.
                    isConnected = true;
                    System.out.println("Connected to " + server + ":" + port + " as " + nick);
                    break;
                } else if (line.contains("433")) {
                    System.out.println("ERROR: Nickname is already in use. Disconnecting.");
                    disconnect();
                    return;
                }
            }

            writer.flush();

            // Keep reading lines from the server.
            while (socket.isConnected() && (line = reader.readLine()) != null) {
                if (line.toLowerCase().startsWith("ping")) {
                    // We must respond to PINGs to avoid being disconnected.
                    writer.write("PONG " + line.substring(5) + "\r\n");
                    writer.flush();
                } else if (line.contains("PRIVMSG ")) {
                    String name, channel, message;
                    if (line.contains("!")) {
                        setChanged();
                        name = line.substring(1, line.indexOf("!"));
                        String s = line.split("PRIVMSG ")[1];
                        channel = s.split(" :")[0];
                        message = s.split(" :")[1];
                        notifyObservers(channel, name, message);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ERROR: Something went wrong with the IRC thread.");
        }
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setNickname(String nick) {
        this.nick = nick;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public void setServerPassword(String password) {
        this.password = password;
    }

    public void joinChannel(String channel) {
        if (!isConnected || !channel.startsWith("#") || channels.contains(channel)) return;
        try {
            writer.write("JOIN " + channel + "\r\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR: Something went wrong when trying to join the channel.");
        }
    }

    public void partChannel(String channel) {
        if (!isConnected || !channel.startsWith("#") || !channels.contains(channel)) return;
        try {
            writer.write("PART " + channel + "\r\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR: Something went wrong when trying to part the channel.");
        }
        channels.remove(channel);
    }

    public void sendMessage(String channel, String message) {
        if (!isConnected) return;
        try {
            if (channel.startsWith("#")) joinChannel(channel);
            writer.write("PRIVMSG " + channel + " :" + message + "\r\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR: Something went wrong when trying to send the message.");
        }
    }

    public void sendNotice(String channel, String message) {
        if (!isConnected) return;
        try {
            writer.write("NOTICE " + channel + " :" + message + "\r\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR: Something went wrong when trying to send the notice.");
        }
    }

    public void sendMessageNickServ(String message) {
        if (!isConnected) return;
        try {
            writer.write("PRIVMSG NickServ :" + message + "\r\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR: Something went wrong when trying to send the message to NickServ.");
        }
    }

    public void addUserMode(String mode) {
        if (!isConnected) return;
        try {
            writer.write("MODE " + nick + " +" + mode + "\r\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR: Something went wrong when trying to add the user mode.");
        }
    }

    public void removeUserMode(String mode) {
        if (!isConnected) return;
        try {
            writer.write("MODE " + nick + " -" + mode + "\r\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR: Something went wrong when trying to add the user mode.");
        }
    }

    public void disconnect() {
        if (!isConnected) return;
        isConnected = false;
        try {
            writer.close();
            reader.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR: Failed to close socket.");
        }
        System.out.println("Disconnected.");
    }

    public boolean isConnected() {
        return isConnected;
    }

}