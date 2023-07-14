import java.net.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.io.*;
//import java.util.Timer;
//import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class GameServer extends Thread {
    static final int MAX_CONCURRENT_GAMES = 5;
    static final int MIN_NUM_PLAYERS = 2;
    static final int MAX_TEAM_SIZE = 4;
    static final int MAX_PLAYER_QUEUE = 20;

    Random rand = new Random();

    private Socket socket;
    public static int total = 0;
    public static Map<Integer, Socket> waitingQueue = new HashMap<>();
    private static Map<Integer, String> playersTokens = new HashMap<>();
    public static Lock lock = new ReentrantLock();
    public static Condition enoughPlayers = lock.newCondition();

    // static ArrayList<ArrayList<String>> users = new
    // ArrayList<ArrayList<String>>();
    public static final int waitingTime = 5;
    public volatile static ArrayList<Socket> socketsSimple = new ArrayList<Socket>();
    private ArrayList<Socket> threadSockets = new ArrayList<Socket>();
    public static int numberPlayers = 0;
    int port;
    static List<Integer> user_id = new ArrayList<Integer>();
    static List<Integer> user_id1 = new ArrayList<Integer>();
    public static ArrayList<ArrayList<Integer>> user_idR = new ArrayList<ArrayList<Integer>>();

    private static Map<Socket, ArrayList<Integer>> socketsRank = new HashMap<>();

    public static ArrayList<User> users = new ArrayList<User>();
    boolean ranked;
    boolean menu;
    List<Integer> thread_user_id;

    static ExecutorService connectionpool = Executors.newFixedThreadPool(MAX_PLAYER_QUEUE);

    public GameServer(ArrayList<Socket> threadSockets, int port, Socket socket, boolean ranked, List<Integer> ids,
            boolean menu) {
        for (Socket so : threadSockets) {
            this.threadSockets.add(so);
        }
        this.menu = menu;
        this.socket = socket;
        this.port = port;
        this.ranked = ranked;
        this.thread_user_id = ids;
    }

    public static void main(String[] args) {
        if (args.length < 1)
            return;
        ExecutorService gamepool = Executors.newFixedThreadPool(MAX_CONCURRENT_GAMES);
        getUsers();

        int port = Integer.parseInt(args[0]);

        ArrayList<Socket> tsockets = new ArrayList<Socket>();
        GameServer coonectThread = new GameServer(tsockets, port, null, false, null, false);
        coonectThread.start();

        long end = waitingTime * 1000 + System.currentTimeMillis();
        long end1 = waitingTime * 1000 + System.currentTimeMillis();
        int sizeSimple = 0;
        int sizeRank = 0;
        ArrayList<Integer> waitingLevel = new ArrayList<>();

        while (true) {
            for (int i =0;i<socketsSimple.size();i++){
                try {
                    OutputStream outputStream = socketsSimple.get(i).getOutputStream();
                    PrintWriter writer = new PrintWriter(outputStream, true);
                    writer.println("ack");
                    
                    InputStream inputStream = socketsSimple.get(i).getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    try {
                        String s = reader.readLine();
                        if (s == null){
                            System.out.println("User disconected");
                            users.get(i).logOff();
                            socketsSimple.remove(i);
                            user_id.remove(i);
                        }
                    } catch (Exception e) {
                        
                    }
                    
                } catch (IOException e) {
                    
                }
            }

            if (socketsSimple.size() > sizeSimple) {
                end = waitingTime * 1000 + System.currentTimeMillis();
                sizeSimple = socketsSimple.size();
            }
            if (socketsSimple.size() >= MAX_TEAM_SIZE
                    || (System.currentTimeMillis() > end && socketsSimple.size() > 1)) {
                lock.lock();
                int numPlayers;
                ArrayList<Socket> gameSockets = new ArrayList<Socket>();
                List<Integer> gameId = new ArrayList<Integer>();
                if (socketsSimple.size() >= MAX_TEAM_SIZE) {
                    numPlayers = MAX_TEAM_SIZE;
                } else {
                    numPlayers = socketsSimple.size();
                }
                for (int i = 0; i < numPlayers; i++) {
                    gameSockets.add(socketsSimple.get(i));
                    gameId.add(user_id.get(i));
                }
                GameServer thread = new GameServer(gameSockets, 0, null, false, gameId, false);
                gamepool.execute(thread);

                for (int i = 0; i < numPlayers; i++) {
                    socketsSimple.remove(0);
                    user_id.remove(0);
                }
                end = (waitingTime + System.currentTimeMillis()) * 1000;
                sizeSimple = 0;
                lock.unlock();
            }

            if (socketsRank.size() > sizeRank) {
                end1 = waitingTime * 1000 + System.currentTimeMillis();
                sizeRank = socketsRank.size();
                waitingLevel.add(0);
            }

            if (socketsRank.size() == MAX_TEAM_SIZE || socketsRank.size() > 1 && System.currentTimeMillis() > end1) {
                lock.lock();
                ArrayList<ArrayList<Integer>> temp = new ArrayList<>();
                for (ArrayList<Integer> j : socketsRank.values())
                    temp.add(j);
                ArrayList<Integer> teams = new ArrayList<>();

                for (int i = 0; i < temp.size() - 1; i++) {
                    ArrayList<Integer> teams1 = new ArrayList<>();
                    teams1.add(i);
                    for (int k = i + 1; k < temp.size(); k++) {
                        if (temp.get(i).get(1) + waitingLevel.get(i) >= temp.get(k).get(1) &&
                                temp.get(i).get(1) <= temp.get(k).get(1) ||
                                temp.get(i).get(1) - waitingLevel.get(i) <= temp.get(k).get(1) &&
                                        temp.get(i).get(1) >= temp.get(k).get(1)) {
                            teams1.add(k);
                        }
                    }
                    if (teams1.size() > teams.size()) {
                        teams = teams1;
                    }
                }
                if (teams.size() > 3) {
                    ArrayList<Socket> socketsRank1 = new ArrayList<>();
                    ArrayList<Socket> a = new ArrayList<>();
                    List<Integer> gameId = new ArrayList<Integer>();
                    for (Socket i : socketsRank.keySet())
                        a.add(i);
                    for (int j : teams) {
                        socketsRank1.add(a.get(j));
                        gameId.add(socketsRank.get(a.get(j)).get(0));
                        user_id1.remove(socketsRank.get(a.get(j)).get(0));
                        socketsRank.remove(a.get(j));
                    }
                    GameServer thread = new GameServer(socketsRank1, 0, null, true, gameId, false);
                    gamepool.execute(thread);

                    socketsRank1.clear();
                    end1 = (waitingTime + System.currentTimeMillis()) * 1000;
                    sizeRank = 0;
                }
                lock.unlock();

            }

            if (System.currentTimeMillis() > end1) {
                end1 = waitingTime * 1000 + System.currentTimeMillis();
                for (int i = 0; i < waitingLevel.size(); i++) {
                    waitingLevel.set(i, waitingLevel.get(i) + 10);
                }
            }
        }

    }

    public void menu() {
        System.out.println("menu");
        try{
        InputStream input = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        OutputStream output = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(output, true);
        boolean loop = true;
        while (loop) {
            int option = Integer.parseInt(reader.readLine());
            if (option == 1) {
                lock.lock();
                user_id.add(thread_user_id.get(0));
                socketsSimple.add(socket);
                lock.unlock();
                loop = false;
                writer.println("wainting in the simple queue");
            } else if (option == 2) {
                lock.lock();
                user_id1.add(thread_user_id.get(0));
                ArrayList<Integer> tempList = new ArrayList<>();
                tempList.add(thread_user_id.get(0));
                for (User u :users){
                    if (u.id == thread_user_id.get(0)){
                        tempList.add(u.getScore());
                    }
                }
                socketsRank.put(socket, tempList);
                lock.unlock();
                loop = false;
                writer.println("waiting in the rank queue");
            } else {
                loop = false;
                for (User u :users){
                    if (u.id == thread_user_id.get(0)){
                        u.logOff();
                    }
                }
                writer.println("end");
            }
        }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void acceptSockets(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                GameServer t = new GameServer(new ArrayList<Socket>(), 0, socket, false, null, false);
                connectionpool.execute(t);
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private String[] SignIn_Up() throws IOException {
        String[] string = new String[2];
        InputStream input = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String username = reader.readLine();

        string[0] = username;

        if (username.equals("0")) {
            return string;
        }

        String password = reader.readLine();

        string[1] = password;

        return string;
    }

    public void run() {
        if (port != 0) {
            acceptSockets(port);
        } else if (threadSockets.size() > 0) {
            if (this.ranked)
                start_ranked_game();
            else
                start_simple_game();
        } else if (menu) {
            menu();
        } else {
            player_connection();
        }
    }

    public void addPlayerToWaitingQueue(Socket socket, String token, int id) {
        // acquiring a lock
        lock.lock();

        try {
            // if first time getting in queue
            if(!playersTokens.containsKey(token)) {
                // add player to the queue
                waitingQueue.put(id, socket);
            }

            // check if enough waiting users to form a team
            if (waitingQueue.size() >= MAX_TEAM_SIZE) {
                // remove waiting players from the queue
                List<Socket> players = new ArrayList<>();
                for (int i = 0; i < MAX_TEAM_SIZE; i++) {
                    Map.Entry<Integer, Socket> pair = waitingQueue.entrySet().iterator().next();
                    waitingQueue.remove(pair.getKey());
                    players.add(pair.getValue());
                }


                // create and start a new game with the waiting players
                // threadPool.execute(new Game(players));
            } 
        }
        finally {
            lock.unlock();
        }
    }

    public void start_simple_game() {
        System.out.println("Simple game started");
        // ReentrantLock reentrantLock = new ReentrantLock();
        try {
            for (int i = 0; i < threadSockets.size(); i++) {
                OutputStream output = threadSockets.get(i).getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                writer.println("You are connected player " + String.valueOf(i));

            }
            int i = 0;

            for (Socket s : threadSockets) {
                int id = thread_user_id.get(i);
                i++;
                List<Integer> id2 = new ArrayList<Integer>();
                id2.add(id);
                OutputStream output = s.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                int points = rand.nextInt(100) + 1;
                //TimeUnit.SECONDS.sleep(30);
                writer.println("Your points are " + points);
                writer.println("end");
                GameServer gameServer = new GameServer(new ArrayList<Socket>(), 0, s, false, id2, true);
                connectionpool.execute(gameServer);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void start_ranked_game() {
        System.out.println("Ranked game started");
        // ReentrantLock reentrantLock = new ReentrantLock();
        try {
            for (int i = 0; i < threadSockets.size(); i++) {
                OutputStream output = threadSockets.get(i).getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                writer.println("You are connected player " + String.valueOf(i));
            }

            for (int i = 0; i < threadSockets.size(); i++) {
                List<Integer> id2 = new ArrayList<Integer>();
                id2.add(thread_user_id.get(i));
                OutputStream output = threadSockets.get(i).getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                int points = rand.nextInt(100) + 1;
                for (User user : users) {
                    if (user.getId() == thread_user_id.get(i)) {
                        int diference = (points - user.getScore()) / 10;
                        user.setScore(user.getScore() + diference);
                    }
                }
                TimeUnit.SECONDS.sleep(10);
                writer.println("Your points are " + points);
                writer.println("end");
                GameServer gameServer = new GameServer(new ArrayList<Socket>(), 0, threadSockets.get(i), false, id2,
                        true);
                connectionpool.execute(gameServer);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void player_connection() {
        int system = 0;
        boolean loop = true;
        // ReentrantLock reentrantLock = new ReentrantLock();
        try {
            while (loop) {
                switch (system) {
                    case 0:
                        InputStream input = socket.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                        system = Integer.parseInt(reader.readLine());
                        break;
                    case 1:
                        String[] auth = new String[2];
                        auth = SignIn_Up();
                        boolean login = true;
                        if (auth[0].equals("0")) {
                            system = 0;
                            break;
                        }
                        OutputStream output = socket.getOutputStream();
                        PrintWriter writer = new PrintWriter(output, true);
                        for (User i : users) {
                            if (i.getName().equals(auth[0])) {
                                if (i.getPass().equals(auth[1])) {
                                    if (i.isLogIn) {
                                        writer.println("User already loged in");
                                        login = false;
                                        break;
                                    }
                                    i.logIn();
                                    writer.println("true");
                                    System.out.println("User loged in");
                                    login = false;
                                    system = 3;                                                 output = socket.getOutputStream();
                                                 writer = new PrintWriter(output, true);
                                                 System.out.println("id: "+i.getId());
                                                 writer.println(i.getId());
                                                
                                                 // generate a random token for the player
                                                 String token = UUID.randomUUID().toString();
                                                 // store it with player's socket connection
                                                 playersTokens.put(i.getId(), token);
                                                 
                                                // send the token to the player
                                                 //output = socket.getOutputStream();
                                                //writer = new PrintWriter(output, true);
                                                writer.println(token);
                                                 System.out.println("error "+writer.checkError());
                                                 System.out.println("token: "+ token);
                                                 
                                                 // wait for new player connection
                                               
                                    addPlayerToWaitingQueue(socket, token, i.getId());

                                    // player game type
                                    input = socket.getInputStream();
                                    reader = new BufferedReader(new InputStreamReader(input));
                                    int option = Integer.parseInt(reader.readLine());
                                    if (option == 1) {
                                        lock.lock();
                                        user_id.add(i.getId());
                                        socketsSimple.add(socket);
                                        lock.unlock();
                                        loop = false;
                                        writer.println("wainting in the simple queue");
                                    } else if (option == 2) {
                                        lock.lock();
                                        user_id1.add(i.getId());
                                        ArrayList<Integer> tempList = new ArrayList<>();
                                        tempList.add(i.getId());
                                        tempList.add(i.getScore());
                                        socketsRank.put(socket, tempList);
                                        lock.unlock();
                                        loop = false;
                                        writer.println("waiting in the rank queue");
                                    } else {
                                        loop = false;
                                        i.logOff();
                                        writer.println("end");
                                    }
                                    break;
                                }
                            }
                        }
                        if (login) {
                            writer.println("Incorrect user");
                            writer.println("false");
                        }
                        break;
                    case 2:
                        String[] auth1 = new String[2];
                        auth1 = SignIn_Up();
                        boolean user = true;
                        if (auth1[0].equals("0")) {
                            system = 0;
                            break;
                        }
                        output = socket.getOutputStream();
                        writer = new PrintWriter(output, true);
                        for (User i : users) {
                            if (i.getName().equals(auth1[0])) {
                                writer.println("false");
                                System.out.println("User already exists");
                                user = false;
                                break;
                            }
                        }
                        if (!user)
                            break;
                        writer.println("true");
                        System.out.println("New user created");
                        /*
                         * File file = new File("db.txt");
                         * FileWriter fr = new FileWriter(file, true);
                         * fr.write("\nid : " + users.size() + "\nname : " + auth1[0] + "\npass : " +
                         * auth1[1] +
                         * "\nscore : 0");
                         */
                        // ArrayList<String> temp = new ArrayList<String>();
                        User newUser = new User(users.size(), auth1[0], auth1[1], 0);
                        users.add(newUser);
                        // fr.close();
                        system = 0;
                        break;

                    default:
                        break;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void getUsers() {
        File userFile = new File("db.txt");
        Scanner myReader;
        try {
            myReader = new Scanner(userFile);
            while (myReader.hasNextLine()) {

                int id, score;
                String name, pass;

                String data = myReader.nextLine();
                data = data.replace("id : ", "");
                id = Integer.parseInt(data);

                data = myReader.nextLine();
                data = data.replace("name : ", "");
                name = data;

                data = myReader.nextLine();
                data = data.replace("pass : ", "");
                pass = data;

                data = myReader.nextLine();
                data = data.replace("score : ", "");
                score = Integer.parseInt(data);
                users.add(new User(id, name, pass, score));

            }
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}