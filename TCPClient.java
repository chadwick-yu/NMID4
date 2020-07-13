package CHATROOM;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//客户端
public class TCPClient {
    static private Socket s;
    public TCPClient(){}

    public static void main(String[] args) throws Exception {
        Scanner scanner=new Scanner(System.in);
        String serverIP;
        System.out.println("请设置服务器端口IP: ");
        serverIP=scanner.next();
        s=new Socket(serverIP,10011);
            TCPClient Client=new TCPClient();
            Client.start();
    }
    public void start(){
        try{
            Scanner scanner=new Scanner(System.in);
            setName(scanner);

            //接收服务端发送过来的信息的线程启动
            ExecutorService exec= Executors.newCachedThreadPool();
            exec.execute(new ListenServer());
            //建立输出流，给服务端发信息
            PrintWriter pw=new PrintWriter(new OutputStreamWriter(s.getOutputStream(),"UTF-8"),true);
            while (true){
                pw.println(scanner.nextLine());
            }
        }
        catch (Exception e){
            e.printStackTrace();
            //客户端
        }finally {
            if(s!=null){
                try{
                    s.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
    private void setName(Scanner scan) throws Exception{
        String name;
        //创建输出流
        PrintWriter pw=new PrintWriter(new OutputStreamWriter(s.getOutputStream(),"UTF-8"),true);
        //创建输入流
        BufferedReader bufIn=new BufferedReader(new InputStreamReader(s.getInputStream(),"UTF-8"));
        while (true) {
            System.out.println("请输入你的昵称:");
            name=scan.nextLine();
            if(name.trim().equals("")){
                System.out.println("昵称不得为空");
            }
            else{
                pw.println(name);
                String pass=bufIn.readLine();
                if(pass!=null && (!pass.equals("ok"))){
                    System.out.println("昵称已经被占用，请重新输入：");
                }else{
                    System.out.println("昵称"+name+"已经设置成功，可以开始聊天了");
                    break;
                }
            }
        }

    }
    //循环读取服务端发送过来的信息并输出到客户端的控制台
    class ListenServer implements Runnable{
        @Override
        public void run(){
        try{
            BufferedReader bufIn=new BufferedReader(new InputStreamReader(s.getInputStream(),"UTF-8"));
            String message;
            while ((message = bufIn.readLine()) != null) {
                System.out.println(message);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
    }


}
