/**
 * 
 */
package org.ats.common.ssh;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Queue;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 24, 2014
 */
public class SSHClient {
  
  public static void sendFile(String host, int port, String username, String password, String folderDest, String fileDest, File file) {

    FileInputStream fis = null;
    try {

      Session session = getSession(host, port, username, password);

      boolean ptimestamp = true;

      Channel channel = session.openChannel("exec");
      ((ChannelExec) channel).setCommand("mkdir -p " + folderDest);
      channel.connect();
      channel.disconnect();

      // exec 'scp -t rfile' remotely
      String command = "scp -t " + folderDest + "/" + fileDest;
      channel = session.openChannel("exec");
      ((ChannelExec) channel).setCommand(command);

      // get I/O streams for remote scp
      OutputStream out = channel.getOutputStream();
      InputStream in = channel.getInputStream();

      channel.connect();

      if (checkAck(in) != 0) {
        throw new RuntimeException("Error when check ACK");
      }

      if (ptimestamp) {
        command = "T " + (file.lastModified() / 1000) + " 0";
        // The access time should be sent here,
        // but it is not accessible with JavaAPI ;-<
        command += (" " + (file.lastModified() / 1000) + " 0\n");
        out.write(command.getBytes());
        out.flush();
        if (checkAck(in) != 0) {
          throw new RuntimeException("Error when check ACK");
        }
      }

      // send "C0644 filesize filename", where filename should not include '/'
      long filesize = file.length();
      command = "C0644 " + filesize + " ";
      if (file.getName().lastIndexOf('/') > 0) {
        command += file.getName()
            .substring(file.getName().lastIndexOf('/') + 1);
      } else {
        command += file.getName();
      }
      command += "\n";
      out.write(command.getBytes());
      out.flush();

      if (checkAck(in) != 0) {
        throw new RuntimeException("Error when check ACK");
      }

      // send a content of lfile
      fis = new FileInputStream(file);
      byte[] buf = new byte[1024];
      while (true) {
        int len = fis.read(buf, 0, buf.length);
        if (len <= 0)
          break;
        out.write(buf, 0, len); // out.flush();
      }
      fis.close();
      fis = null;
      // send '\0'
      buf[0] = 0;
      out.write(buf, 0, 1);
      out.flush();
      if (checkAck(in) != 0) {
        throw new RuntimeException("Error when check ACK");
      }
      out.close();

      channel.disconnect();
      session.disconnect();

    } catch (Exception e) {
      System.out.println(e);
      try {
        if (fis != null)
          fis.close();
      } catch (Exception ee) {
      }
    }
  }
  
  private static int checkAck(InputStream in) throws IOException{
    int b=in.read();
    // b may be 0 for success,
    //          1 for error,
    //          2 for fatal error,
    //          -1
    if(b==0) return b;
    if(b==-1) return b;

    if(b==1 || b==2){
      StringBuffer sb=new StringBuffer();
      int c;
      do {
        c=in.read();
        sb.append((char)c);
      }
      while(c!='\n');
      if(b==1){ // error
        System.out.print(sb.toString());
      }
      if(b==2){ // fatal error
        System.out.print(sb.toString());
      }
    }
    return b;
  }
  
  public static void sendFile(String host, int port, String username, String password, String folderDest, String fileDest, InputStream source)
      throws Exception {
    Session session = getSession(host, port, username, password);

    Channel channel = session.openChannel("exec");
    ((ChannelExec) channel).setCommand("mkdir -p " + folderDest);
    channel.connect();
    channel.disconnect();

    // exec 'scp -t rfile' remotely
    String command = "scp -t " + folderDest + "/" + fileDest;
    channel = session.openChannel("exec");
    ((ChannelExec) channel).setCommand(command);

    // get I/O streams for remote scp
    OutputStream out = channel.getOutputStream();
    channel.connect();

    // send "C0644 filesize filename", where filename should not include '/'
    BufferedInputStream bis = new BufferedInputStream(source);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buff = new byte[1024];
    for (int l = bis.read(buff); l != -1; l = bis.read(buff)) {
      baos.write(buff, 0, l);
    }
    bis.close();
    
    long filesize = baos.size();
    System.out.println(folderDest + "/" + fileDest + " has size: " + filesize);
    
    command = "C0644 " + filesize + " ";
    command += fileDest;
    command += "\n";
    out.write(command.getBytes());
    out.flush();

    System.out.println("command = " + command);
    
    // send a content of lfile
    out.write(baos.toByteArray());
      
    // send '\0'
    out.flush();
    out.close();

    channel.disconnect();
    session.disconnect();
  }
  
  /**
   * 
   * @param host the host ip
   * @param port the port
   * @param timeout the timeout seconds
   * @return connect established
   * @throws IOException
   */
  public static boolean checkEstablished(final String host, final int port, int timeout) throws IOException {
    long start = System.currentTimeMillis();
    while(true) {
      try {
        Socket socket = new Socket(host, port);
        socket.close();
        return true;
      } catch(Exception e) {
        if((System.currentTimeMillis() - start) > timeout * 1000)
          return false;
      } 
    }
  }

  public static Session getSession(String host, int port, String username, String password) throws JSchException {
    JSch jsch = new JSch();

    Session session = jsch.getSession(username, host, port);
    session.setPassword(password);
    session.setConfig("StrictHostKeyChecking", "no");
    session.connect();
    return session;
  }
  
  public static Channel execCommand(String host, int port, String uname, String pwd, String cmd, InputStream is, OutputStream err) throws JSchException, IOException {
    Session session = getSession(host, port, uname, pwd);
    return execCommand(session, cmd, is, err);
  }
  
  public static Channel execCommand(Session session, String command, InputStream is, OutputStream error) throws JSchException, IOException {
    ChannelExec channel = (ChannelExec) session.openChannel("exec");
    channel.setCommand(command);
    channel.setInputStream(is);
    
    ((ChannelExec)channel).setErrStream(error);

    channel.connect();
    
    return channel;
  }
  
  public static int printOut(PrintStream out, Channel channel) throws IOException {
    InputStream in=channel.getInputStream();
    byte[] tmp=new byte[1024];
    while(true){
      while(in.available()>0){
        int i=in.read(tmp, 0, 1024);
        if(i<0)break;
        out.print(new String(tmp, 0, i));
      }
      if(channel.isClosed()){
        if(in.available()>0) continue;
        return channel.getExitStatus();
//        out.println("exit-status: "+channel.getExitStatus());
      }
      try{Thread.sleep(1000);}catch(Exception ee){}
    }
  }
  
  public static int printOut(Queue<String> queue, Channel channel) throws IOException {
    InputStream in=channel.getInputStream();
    byte[] tmp=new byte[1024];
    while(true){
      while(in.available()>0){
        int i=in.read(tmp, 0, 1024);
        if(i<0)break;
        queue.add(new String(tmp, 0, i));
      }
      if(channel.isClosed()){
        if(in.available()>0) continue;
        return channel.getExitStatus();
//        out.println("exit-status: "+channel.getExitStatus());
      }
      try{Thread.sleep(1000);}catch(Exception ee){}
    }
  }
}
