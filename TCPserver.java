package CHATROOM;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//服务端
public class TCPserver {
    private ServerSocket serverSocket;
    /*
    创建一个线程池来管理客户端的连接线程
    避免系统资源过度浪费
     */
    private ExecutorService exec;
    //获取客户端之间私聊的信息
    private Map<String, PrintWriter> storeInfo;
    //构造函数
    public TCPserver(){
        try{
             serverSocket=new ServerSocket(10011);
             storeInfo=new HashMap<String, PrintWriter>();
             exec= Executors.newCachedThreadPool();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
    //以map形式存入客户端信息
    private  void putIn(String key,PrintWriter pw){
        synchronized (this){
            storeInfo.put(key, pw);
        }
    }
    //将给定的输出流从共享集合中删除
    private  synchronized  void remove(String key){
        storeInfo.remove(key);
        System.out.println("当前在线人数为: "+storeInfo.size());
    }
    //群发消息给所有客户端  storeInfo.values返回值的数组
    private synchronized  void  SendToAll(String message){
        for(PrintWriter out:storeInfo.values()){
            out.println(message);
        }
    }
    //私聊信息转发给指定的客户端
    private synchronized  void SendToSomeone(String name,String message){
        PrintWriter pw=storeInfo.get(name);//将对应客户端的聊天信息取出作为私聊内容发送出去
        if(pw!=null) pw.println(message);
    }
    public  void start(){
        try {
            while (true) {
                System.out.println("等待客户端连接中····");
                Socket s=serverSocket.accept();

                //习惯获取客户端IP地址
                String ip=s.getInetAddress().getHostAddress();
                System.out.println(ip+"·····connected");

                //启动一个线程，由线程对客户端的请求进行处理，这样方便再次进行监听下一个客户端

                exec.execute(new ListenrClient(s));//通过线程池分配线程

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    //处理客户端发来的消息，循环接收客户端发来的信息并打印到控制台
    class ListenrClient implements Runnable {
        private  Socket s;
        private  String name;
        public ListenrClient(Socket s){
            this.s=s;
        }
        //内部类获取昵称
        private @NotNull String getName() throws Exception {
            try {
                //服务端的输入流接收客户端发来的昵称输出流
                BufferedReader bufIn = new BufferedReader(new InputStreamReader(s.getInputStream()));
                //服务端将昵称验证结果通过自身的输出流发送给客户端
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream(),"UTF-8"),true);
                //读取客户端发来的昵称
                while (true) {
                    String name = bufIn.readLine();
                    if ((name.trim().length() == 0) || storeInfo.containsKey(name))
                        pw.println("FAIL");
                    else {
                        pw.println("ok");
                        return name;
                    }
                }
            } catch (Exception e) {
               throw e;//必须使用这种处理方式，因为函数必须要有返回值
            }
        }
        public void run() {
            try{
                PrintWriter pw=new PrintWriter(new OutputStreamWriter(s.getOutputStream(),"UTF-8"),true);
                //将客户昵称和其所说的内容存入共享集合Hashmap当中
                name=getName();
                putIn(name,pw);
                Thread.sleep(5);
                SendToAll("[系统通知] "+ name + "已上线");
                //通过客户端的socket获取输入流
                //读取客户端发送过来的信息
                BufferedReader bufIn=new BufferedReader(new InputStreamReader(s.getInputStream(),"UTF-8"));
                String message=null;
                while ((message=bufIn.readLine())!=null){
                    //检验是否为私聊(私聊的标准格式:@名字:内容）
                    if(message.startsWith("@")){
                        int index=message.indexOf(":");
                        if(index >=0){
                            //获取昵称
                            String theName=message.substring(1,index);
                            String info=message.substring(index+1,message.length());
                            info=name+" "+info;
                            //将私聊信息发送过去
                            SendToSomeone(theName,info);
                            continue;
                        }

                    }
                    //遍历所有输出流，将该客户端发送的信息转发给所有客户端
                    System.out.println(name+":"+message);
                    SendToAll(name+":"+message);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }finally {
                remove(name);
                //通知所有客户端，xx用户已经下线
                SendToAll("[系统通知]"+name+"已经下线");
                if(s!=null){
                    try {
                        s.close();
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        TCPserver ts=new TCPserver();
        ts.start();
    }
}
