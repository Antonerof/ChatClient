import message.Message;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Main {

    private static String urlServer = "http://127.0.0.1:8080";
    final static Scanner scanner = new Scanner(System.in);
    static String login;

    public static void main(String[] args) {
        try {
            int respCode;
            do {
                respCode = login();
                if (respCode != 200) {
                    System.out.println("Incorrect login or password");
                }
            } while (respCode != 200);

            Thread th = new Thread() {

                private int n;

                @Override
                public void run() {
                    try {
                        while (!isInterrupted()) {
                            URL url = new URL(urlServer + "/get?from=" + n + "&username=" + login);
                            HttpURLConnection http = (HttpURLConnection) url.openConnection();
                            try {
                                InputStream is = http.getInputStream();
                                Message m = null;
                                if (is.available() > 0) {
                                    do {
                                        m = Message.readFromStreamXML(is);
                                        if (m != null) {
                                            System.out.println(m.toString());
                                            n++;
                                        }
                                    } while (m != null);
                                }
                            } finally {
                                http.disconnect();

                            }
                            sleep(100);
                        }
                    } catch (Exception e) {
                        return;
                    }
                }
            };
            th.setDaemon(true);
            th.start();

            try {
                while (true) {
                    String s = scanner.nextLine();

                    if (s.isEmpty())
                        break;

                    if ("!list".equals(s)) {
                        URL url = new URL(urlServer + "/list");
                        getList(url);
                        continue;
                    }

                    if ("!roomlist".equals(s)) {
                        URL url = new URL(urlServer + "/listroom");
                        getList(url);
                        continue;
                    }
                    if ((s.length() > 5) && "!room".equals(s.substring(0, 5)) && !("!roomlist".equals(s))) {
                        URL url = new URL(urlServer + "/room");
                        String id = s.substring(6);
                        goToRoomById(url, id);
                        continue;
                    }

                    int del = s.indexOf(':');
                    String to = "";
                    String text = s;

                    if (del >= 0) {
                        to = s.substring(0, del);
                        text = s.substring(del + 1);
                    }

                    Message m = new Message(login, to, text);

                    int res = m.send(urlServer + "/add");
                    if (res != 200) {
                        System.out.println("HTTP error: " + res);
                        break;
                    }
                }
            } finally {
                th.interrupt();
                scanner.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int login() throws IOException {

        System.out.println("Enter login: ");
        login = scanner.nextLine();
        System.out.println("Enter password: ");
        String password = scanner.nextLine();


        URL url = new URL(urlServer + "/login");
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        OutputStream os = http.getOutputStream();

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
        writer.write(login);
        writer.newLine();
        writer.write(password);
        writer.flush();
        writer.close();

        return http.getResponseCode();
    }

    private static void getList(URL url) throws IOException {
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        try {
            InputStream is = http.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } finally {
            http.disconnect();
        }
    }

    private static void goToRoomById(URL url, String idRoom) throws IOException {
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);

        OutputStream os = http.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
        writer.write(idRoom);
        writer.newLine();
        writer.write(login);
        writer.flush();
        writer.close();
        if (http.getResponseCode() != 200) {
            System.out.println("Connection error, try again.");
        }


    }
}
