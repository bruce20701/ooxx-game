import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class MyFrame extends JFrame {
    private JPanel menuPanel,endPanel,hostPanel,clientPanel;
    private GamePanel gamePanel;
    private JButton local,host,client,rule,backToMenu,startGame,join;
    private JTextField ipInput;
    private JLabel winner,ip,roomInfo;
    private Socket s;
    private Server server;
    private Client clientServer;
    private String position = null;
    private boolean isEnd = false;
    private int winStartX,winStartY,winEndX,winEndY;
    public MyFrame(){
        setLayout(new FlowLayout());
        setSize(490,520);

        menuPanel = new JPanel();
        gamePanel = new GamePanel();
        hostPanel = new JPanel();
        clientPanel = new JPanel();
        endPanel = new JPanel();
        local = new JButton("本機對戰");
        host = new JButton("創建房間");
        client = new JButton("加入房間");
        rule = new JButton("規則");
        startGame = new JButton("開始遊戲");
        ipInput = new JTextField(10);
        join = new JButton("進入");
        backToMenu = new JButton("返回菜單");
        ip = new JLabel();
        roomInfo = new JLabel();

        winner = new JLabel();

        menuPanel.add(local);
        menuPanel.add(host);
        menuPanel.add(client);
        menuPanel.add(rule);
        add(menuPanel);

        hostPanel.add(ip);
        hostPanel.add(startGame);
        hostPanel.add(roomInfo);

        clientPanel.add(ipInput);
        clientPanel.add(join);

        Font font = new Font("新細明體",Font.PLAIN,30);
        winner.setFont(font);
        endPanel.add(winner);
        endPanel.add(backToMenu);

        local.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gamePanel.isLocal = true;
                setContentPane(gamePanel);
                revalidate();
            }
        });

        host.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setContentPane(hostPanel);
                revalidate();
                server = new Server();
                server.start();
            }
        });

        startGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gamePanel.isLocal = false;
                gamePanel.isServer = true;
                gamePanel.resetGame();
                setContentPane(gamePanel);
                revalidate();
            }
        });

        client.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setContentPane(clientPanel);
                revalidate();
            }
        });

        join.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gamePanel.isLocal = false;
                gamePanel.isServer = false;
                gamePanel.resetGame();
                setContentPane(gamePanel);
                revalidate();
                clientServer = new Client();
                clientServer.start();
            }
        });

        rule.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(MyFrame.this,"在9x9的棋盤中，先以五個連成一線的即為勝者","規則說明",JOptionPane.INFORMATION_MESSAGE);
            }
        });

        backToMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gamePanel.resetGame();
                setContentPane(menuPanel);
                revalidate();
            }
        });

    }
    public class Server extends Thread{
        private ServerSocket ss;
        public boolean isFirst;
        public Server(){
            try {
                ss = new ServerSocket(8888);
                isFirst = true;
            } catch (IOException e) {
                System.out.println("error");
            }
        }
        public void run(){
            while (true){
                try {
                    if(isFirst){
                        InetAddress addr = InetAddress.getLocalHost();
                        ip.setText("本機IP:"+addr.getHostAddress());
                        roomInfo.setText("等待玩家進入");
                        s = ss.accept();
                        roomInfo.setText(s.getInetAddress().getLocalHost()+"已進入房間");
                        isFirst = false;
                    }
                    else {
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                        BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                        while (position == null){
                            try {
                                sleep(10);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        bw.write(position+"\n");
                        bw.flush();
                        if(isEnd)
                            this.stop();
                        else
                            position = null;
                        String mess = br.readLine();
                        int i = Integer.parseInt(mess) / 10;
                        int j = Integer.parseInt(mess) % 10;
                        gamePanel.lattices[i][j] = "X";
                        gamePanel.repaint();
                        gamePanel.winCheck(i,j);
                        gamePanel.isO = true;
                        if(isEnd)
                            this.stop();
                    }
                }catch (IOException e){
                    throw new RuntimeException(e);
                }
            }
        }
    }
    public class Client extends Thread{
        private boolean isFirst;
        public Client(){
            isFirst = true;
        }
        public void run() {
            while (true){
                try {
                    if(isFirst){
                        s = new Socket(ipInput.getText(),8888);
                        isFirst = false;
                    }
                    else{
                        InputStream is = s.getInputStream();
                        OutputStream os = s.getOutputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
                        System.out.println("等待訊息");
                        String mess = br.readLine();
                        System.out.println("收到訊息");
                        int i = Integer.parseInt(mess) / 10;
                        int j = Integer.parseInt(mess) % 10;
                        gamePanel.lattices[i][j] = "O";
                        gamePanel.repaint();
                        gamePanel.winCheck(i,j);
                        gamePanel.isO = false;
                        if(isEnd)
                            this.stop();
                        else
                            position = null;
                        while (position == null){
                            try {
                                sleep(10);
                                System.out.println("等待position");
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        bw.write(position+"\n");
                        bw.flush();
                        System.out.println("已送出訊息");
                        if(isEnd)
                            this.stop();
                        else
                            position = null;
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    public void win(boolean isO){
        if(isO) {
            winner.setForeground(Color.red);
            winner.setText("P1 win!!");
        }
        else{
            winner.setForeground(Color.blue);
            winner.setText("P2 win!!");
        }

        setContentPane(endPanel);
        revalidate();
    }
    public class GamePanel extends JPanel {
        private String[][] lattices = new String[9][9];
        private boolean isO = true;
        private boolean isLocal = true;
        private boolean isServer = true;
        private PlayBoard playBoard;
        private JLabel message;

        public GamePanel(){
            setLayout(new BorderLayout());

            playBoard = new PlayBoard();
            message = new JLabel();

            add(playBoard,BorderLayout.CENTER);
            add(message,BorderLayout.SOUTH);
        }

        private class PlayBoard extends JPanel implements ActionListener{
            public int dx = 0;
            public int dy = 0;
            public int count = 0;

            public PlayBoard(){
                MyListener handler = new MyListener();
                addMouseListener(handler);
                Timer timer = new Timer(100,this);
                timer.start();
            }

            @Override
            public void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics;
                g.setStroke(new BasicStroke(3));
                super.paintComponent(g);
                //畫棋盤
                for(int i = 0; i < 10; i++){
                    g.drawLine(10+i*50,10,10+i*50,460);
                }
                for(int i = 0; i < 10; i++){
                    g.drawLine(10,10+i*50,460,10+i*50);
                }
                //畫棋子
                for(int i = 0; i < 9; i++){
                    for(int j = 0; j < 9; j++) {
                        if(lattices[i][j] != null) {
                            if (lattices[i][j].equals("O")){
                                g.setColor(Color.red);
                                g.drawOval(15 + i * 50, 15 + j * 50, 40, 40);
                            }
                            else{
                                g.setColor(Color.blue);
                                g.drawLine(15 + i * 50, 15 + j * 50, 55 + i * 50, 55 + j * 50);
                                g.drawLine(55 + i * 50, 15 + j * 50, 15 + i * 50, 55 + j * 50);
                            }
                        }
                    }
                }
                if(isEnd){
                    g.setColor(Color.green);
                    g.drawLine(winStartX,winStartY,winStartX + dx,winStartY + dy);
                }
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if(isEnd){
                    if(count<=10){
                        dx += (winEndX - winStartX)/10;
                        dy += (winEndY - winStartY)/10;
                        count++;
                        repaint();
                    }
                    else
                        win(!isO);
                }

            }
        }
        private class MyListener extends MouseInputAdapter {
            @Override
            public void mousePressed(MouseEvent e){
                int i = (e.getX()-10)/50;
                int j = (e.getY()-10)/50;
                if(lattices[i][j] == null && !isEnd){
                    message.setText("");
                    if(isLocal){
                        if(isO)
                            lattices[i][j] = "O";
                        else
                            lattices[i][j] = "X";
                        isO = !isO;
                        repaint();
                        winCheck(i,j);
                    }
                    else{
                        if(isServer && isO){
                            lattices[i][j] = "O";
                            isO = false;
                            repaint();
                            position = i+""+j;
                            winCheck(i,j);
                        }

                        else if (!isServer && !isO){
                            lattices[i][j] = "X";
                            isO = true;
                            repaint();
                            position = i+""+j;
                            winCheck(i,j);
                        }
                        else
                            message.setText("現在是對手的回合");
                    }
                }
                else
                    message.setText("請選擇空的格子");
            }
        }

        public void winCheck(int i, int j){
            int score = 0;
            int shift = 1;

            //左上右下斜
            while(i + shift < 9 && j + shift < 9 && lattices[i+shift][j+shift] != null && lattices[i+shift][j+shift].equals(lattices[i][j])){
                score++;
                shift++;
            }
            winEndX = (i + shift - 1) * 50 + 10;
            winEndY = (j + shift - 1) * 50 + 10;
            shift = 1;
            while(i - shift >= 0 && j - shift >= 0 && lattices[i-shift][j-shift] != null && lattices[i-shift][j-shift].equals(lattices[i][j])){
                score++;
                shift++;
            }
            winStartX = (i - shift + 1) * 50 + 10;
            winStartY = (j - shift + 1) * 50 + 10;
            if(score>=4){
                isEnd = true;
                return;
            }


            //右上左下斜
            score = 0;
            shift = 1;
            while(i + shift < 9 && j - shift >= 0 && lattices[i+shift][j-shift] != null && lattices[i+shift][j-shift].equals(lattices[i][j])){
                score++;
                shift++;
            }
            winEndX = (i + shift - 1) * 50 + 10;
            winEndY = (j - shift + 1) * 50 + 10;
            shift = 1;
            while(i - shift >= 0 && j + shift < 9 && lattices[i-shift][j+shift] != null && lattices[i-shift][j+shift].equals(lattices[i][j])){
                score++;
                shift++;
            }
            winStartX = (i - shift + 1) * 50 + 10;
            winStartY = (j + shift - 1) * 50 + 10;
            if(score>=4){
                isEnd = true;
                return;
            }

            //直向
            score = 0;
            shift = 1;
            while(i + shift < 9 && lattices[i+shift][j] != null && lattices[i+shift][j].equals(lattices[i][j])){
                score++;
                shift++;
            }
            winEndX = (i + shift - 1) * 50 + 10;
            winEndY = j * 50 + 35;
            shift = 1;
            while(i - shift >= 0 && lattices[i-shift][j] != null && lattices[i-shift][j].equals(lattices[i][j])){
                score++;
                shift++;
            }
            winStartX = (i - shift + 1) * 50 + 10;
            winStartY = winEndY;
            if(score>=4){
                isEnd = true;
                return;
            }

            //橫向
            score = 0;
            shift = 1;
            while(j + shift < 9 && lattices[i][j+shift] != null && lattices[i][j+shift].equals(lattices[i][j])){
                score++;
                shift++;
            }
            winEndX = i * 50 + 35;
            winEndY = (j + shift - 1) * 50 + 10;
            shift = 1;
            while(j - shift >= 0 && lattices[i][j-shift] != null && lattices[i][j-shift].equals(lattices[i][j])){
                score++;
                shift++;
            }
            winStartX = winEndX;
            winStartY = (j - shift + 1) * 50 + 10;
            if(score>=4)
                isEnd = true;
        }
        public void resetGame(){
            lattices = new String[9][9];
            isO = true;
            isEnd = false;
            position = null;
            gamePanel.playBoard.dx = 0;
            gamePanel.playBoard.dy = 0;
            gamePanel.playBoard.count = 0;
        }
    }
}
