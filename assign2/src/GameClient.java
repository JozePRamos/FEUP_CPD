import java.net.*;
import java.util.Scanner;

import javax.swing.text.StyledEditorKit.BoldAction;

import java.io.*;
import java.util.ArrayList;

public class GameClient {
    public static void main(String[] args) {
        if (args.length < 2) return;
        
        int player;
        String token;
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        Scanner scanner = new Scanner(System.in);
        int system = 0;
        ArrayList<String> inputs = new ArrayList<String>();
        try (Socket socket = new Socket(hostname, port)) {
            boolean loop = true;
            boolean game = true;
            while(game){
                loop = true;
            while(loop){
                inputs.clear();
                switch (system) {
                    case 0 -> {
                        System.out.println("#################################\n\n\n" + "Hello welcome to Lucky's War!\n\n" +
                                "1) Login\n\n" + "2) Signup\n" + "\n\n#################################");
                        String temp = scanner.nextLine();
                        while (!(temp.equals("1")) && !(temp.equals("2"))) {
                            System.out.println("Wrong input please try again\n");
                            temp = scanner.nextLine();
                        }

                        system = Integer.parseInt(temp);

                        OutputStream output = socket.getOutputStream();
                        PrintWriter writer = new PrintWriter(output, true);

                        writer.println(temp);
                    }
                    case 1, 2 -> {
                        System.out.println("#################################\n" + "Press 0 to go back");
                        System.out.println("Please type your username:");
                        inputs.add(scanner.nextLine());
                        if (inputs.get(0).equals("0")) {
                            system = 0;
                            OutputStream output = socket.getOutputStream();
                            PrintWriter writer = new PrintWriter(output, true);

                            writer.println("0");
                            continue;
                        }
                        System.out.println("Please type your password:");
                        inputs.add(scanner.nextLine());
                        if (system == 1)
                            system = 3;
                        else
                            system = 4;
                    }
                    case 3 -> {
                        InputStream input = socket.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                        System.out.println("ok: "+reader.ready());
                        String login = reader.readLine();
                        System.out.println(login);
                    if (Boolean.parseBoolean(login)) {
                        System.out.println("Welcome user");
                        /*

                        /*input = socket.getInputStream();
                        reader = new BufferedReader(new InputStreamReader(input));
                        player = Integer.parseInt(reader.readLine());
                        System.out.println("1");
                        
                        input = socket.getInputStream();
                        reader = new BufferedReader(new InputStreamReader(input));
                        token = reader.readLine();
                        System.out.println("2");
                 */
                        system = 5; 
                                            
                    }
                    else{
                        system = 1;
                    }
                    }
                    case 4 -> {
                        InputStream input = socket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    if (Boolean.parseBoolean(reader.readLine())) {
                        System.out.println("User Created");
                        
                        system = 0;
                    }
                    else{
                        System.out.println("Username already exists");
                        system = 2;
                    }
                    }
                    case 5 -> {
                        System.out.println("Ready to play");
                        
                        System.out.println("#################################\n\n\n" + "Choose the mode you want to play:\n\n" +
                                "1) Simple\n\n" + "2) Rank\n\n" + "3) Exit\n" + "\n\n#################################");
                        String temp = scanner.nextLine();
                        while (!(temp.equals("1")) && !(temp.equals("2")) && !(temp.equals("3"))) {
                            System.out.println("Wrong input please try again\n");
                            temp = scanner.nextLine();
                        }
                        if (temp.equals("3")){
                            system = 6;
                        }

                        OutputStream output = socket.getOutputStream();
                        PrintWriter writer = new PrintWriter(output, true);

                        writer.println(temp);
                        loop = false;
                    }
                }

                for (String i : inputs) {
                    OutputStream output = socket.getOutputStream();
                    PrintWriter writer = new PrintWriter(output, true);

                    writer.println(i);
                }

            }
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            loop = true;
            while (loop && system != 6){
                String line = reader.readLine();
                if (line.equals("end")) loop = false;
                else if (line.equals("ack")){
                    OutputStream outputStream = socket.getOutputStream();
                    PrintWriter writer = new PrintWriter(outputStream, true);
                    writer.println("ack");
                }
                else System.out.println(line);
            }
            if (system == 6){
                break;
            }else system = 5;

            System.out.println("sy: "+system);
        }

 
        } catch (UnknownHostException ex) {
 
            System.out.println("Server not found: " + ex.getMessage());
 
        } catch (IOException ex) {
 
            System.out.println("I/O error: " + ex.getMessage());
        }
        scanner.close();
    }
    
}
