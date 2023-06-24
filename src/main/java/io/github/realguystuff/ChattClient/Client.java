/*
 * ChattClient: A privacy-focused chat client.
 * Copyright (C) 2023  realguystuff
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Email: realguybackup@gmail.com
 */

package io.github.realguystuff.ChattClient;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class Client {
    private final Socket socket;
    private BufferedReader buffReader;
    private BufferedWriter buffWriter;
    private final String username;
    private static String ip;
    private static final String version = "1.0.0";


    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.buffWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.buffReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
        } catch (IOException e) {
            System.err.println("Error CL1");
            closeAll(socket, buffReader, buffWriter);
            throw new RuntimeException(e);
        }
    }

    public void sendMessage() {
        try {
            buffWriter.write(username);
            buffWriter.newLine();
            buffWriter.flush();

            Scanner sc = new Scanner(System.in);

            while (socket.isConnected()) {
                String messageToSend = sc.nextLine();
                if (Objects.equals(messageToSend, "/help") || Objects.equals(messageToSend, "/?")) {
                    System.out.println("client: Here are a list of commands and what they do:");
                    System.out.println("client: ---------------------------------------------");
                    System.out.println("client: | /help     | Gives this list. Aliases: /?  |");
                    System.out.println("client: | /ip       | Tells you what IP are you on. |");
                    System.out.println("client: | /whoami   | Tells you your username.      |");
                    System.out.println("client: | /rickroll | Gives rickroll links.         |");
                    System.out.println("client: | /version  | Gives the current version.    |");
                    System.out.println("client: ---------------------------------------------");
                } else if (Objects.equals(messageToSend, "/whoami")) {
                    System.out.println("client: You are \""+username+"\".");
                } else if (Objects.equals(messageToSend, "/rickroll")) {
                    System.out.println("client: Use it wisely (don't use this if they're smart; they can also use /rickroll):");
                    System.out.println("client: Official Music Video: https://www.youtube.com/watch?v=dQw4w9WgXcQ");
                    System.out.println("client: Different link #1: https://www.youtube.com/watch?v=iik25wqIuFo");
                    System.out.println("client: Different link #2: https://www.youtube.com/watch?v=xvFZjo5PgG0");
                    System.out.println("client: Different link #3: ttps://www.youtube.com/watch?v=8ybW48rKBME");
                    System.out.println("client: Different link #4: https://www.youtube.com/watch?v=p7YXXieghto");
                    System.out.println("client: Different link #5: https://www.youtube.com/watch?v=QB7ACr7pUuE");
                } else if (Objects.equals(messageToSend, "/version")) {
                    System.out.println("client: You are running client version "+version);
                } else if (Objects.equals(messageToSend, "/ip")) {
                    System.out.println("client: You are on "+ip+":5000");
                } else {
                    buffWriter.write(username + ": " + messageToSend);
                    buffWriter.newLine();
                    buffWriter.flush();
                }
            }
        } catch (IOException e) {
            System.err.println("Error CL2");
            closeAll(socket, buffReader, buffWriter);
            throw new RuntimeException(e);
        }
    }

    public void readMessage() {
        new Thread(() -> {
            String msgFromGroupChat;
            while (socket.isConnected()) {
                try {
                    msgFromGroupChat = buffReader.readLine();
                    System.out.println(msgFromGroupChat);
                } catch (IOException e) {
                    System.err.println("Error CL3");
                    closeAll(socket, buffReader, buffWriter);
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void closeAll(Socket socket, BufferedReader buffReader, BufferedWriter buffWriter) {
        try {
            if (buffReader != null) {
                buffReader.close();
            }
            if (buffWriter != null) {
                buffWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error CL4");
            closeAll(socket, buffReader, buffWriter);
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, SQLException {
        Console console = System.console();
        if (console == null) {
            System.err.println("No console available. Exiting...");
            System.exit(1);
        }
        System.out.println("Running client version "+version);
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter your username: ");
        String username = sc.nextLine().toLowerCase();
        System.out.print("Enter your password: ");
        char[] passwordChars = console.readPassword();
        // Convert the password to a String
        String password = new String(passwordChars);

        System.out.println("Checking password...");

        // Hash the password using SHA-512 algorithm
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] hashedPassword = md.digest(password.getBytes());

        // Convert the hashed password to a hexadecimal representation
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedPassword) {
            sb.append(String.format("%02x", b));
        }
        String hashedPasswordStr = sb.toString();
        Arrays.fill(passwordChars, ' ');
        System.out.println("Authenticating (this may take a while)...");
        // Perform the database check using the username and hashed password
        Database database = new Database();
        boolean isValidUser = database.call("SELECT * FROM authenticated WHERE username = '" + username + "' AND pw = '" + hashedPasswordStr + "'").next();

        database.close();
        if (isValidUser) {
            System.out.println("Authentication successful!");
            System.out.println("Enter the IP address you want to join (do not add :5000 to the end of the IP): ");
            System.out.print("> ");
            ip = sc.nextLine();

            try {
                System.out.println("Connecting you to " + ip + ":5000 as " + username + "...");
                Socket socket;
                try {
                    socket = new Socket(ip, 5000);
                } catch (ConnectException e) {
                    System.out.println("Error CL7:");
                    System.out.println(e.getMessage());
                    return;
                }
                Client client = new Client(socket, username);
                System.out.println("Connection successful!");
                client.readMessage();
                client.sendMessage();
            } catch (UnknownHostException e) {
                System.err.println("Error CL5: UnknownHostException");
                e.printStackTrace();
                System.out.println("hint: CL5 usually means that you have typed in the IP wrong (maybe you added \":5000\").");
                main(args);
            }
        } else {
            System.out.println("Error CL6: AuthenticationException");
            System.out.println("hint: CL6 usually means that you have typed in your username or password wrong, or the username doesn't exist in the database (try signing UP on the website)");
            main(args);
        }
    }
}
