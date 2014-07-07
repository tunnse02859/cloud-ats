/**
 * 
 */
package org.ats.jenkins;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 18, 2014
 */
public class Main {

  public static void main(String args[]) throws IOException {
    Options options = new Options();
    options.addOption("c", "create-slave", false, "Create Jenkins Slave in this machine.");
    options.addOption("d", "destroy-slave", false, "Destroy Jenkins Slave in this machine.");
    options.addOption("g", "slave-gui", false, "Use Jenkins Slave GUI instance");
    
    CommandLineParser parser = new BasicParser();
    HelpFormatter formatter = new HelpFormatter();
    try {
      CommandLine cmd = parser.parse(options, args);
      if (cmd.hasOption('c')){
        createSlave(cmd.hasOption('g') ? true : false);
      } else if (cmd.hasOption('d')) {
        destroySlave();
      } else {
        formatter.printHelp(Main.class.getName(), options);
      }
    } catch (ParseException e) {
      formatter.printHelp(Main.class.getName(), options);
    }
  }
  
  private static void createSlave(boolean gui) throws IOException {
    JenkinsMaster master = new JenkinsMaster("git.sme.org", "http", 8080);
    NetworkInterface eth0 = NetworkInterface.getByName("eth0");
    Enumeration<InetAddress> inets = eth0.getInetAddresses();
    while (inets.hasMoreElements()) {
      String inetAddress = inets.nextElement().getHostAddress();
      if (inetAddress.startsWith("172.27.4.")) {
        
        JenkinsSlave slave = null;
        Map<String, String> env = null;
        
        if (gui) {
          env = new HashMap<String, String>();
          env.put("DISPLAY", ":0");
        } 
        
        slave = new JenkinsSlave(master, inetAddress, env);
        if (slave.join()) {
          System.out.println("Create slave " + inetAddress + " sucessfully");
        } else {
          System.out.println("Can not create slave" + inetAddress);
        }
        return;
      }
    }
  }
  
  private static void destroySlave() throws IOException {
    JenkinsMaster master = new JenkinsMaster("git.sme.org", "http", 8080);
    NetworkInterface eth0 = NetworkInterface.getByName("eth0");
    Enumeration<InetAddress> inets = eth0.getInetAddresses();
    while (inets.hasMoreElements()) {
      String inetAddress = inets.nextElement().getHostAddress();
      if (inetAddress.startsWith("172.27.4.")) {
        if (new JenkinsSlave(master, inetAddress).release()) {
          System.out.println("Destroyed slave " + inetAddress);
        } else {
          System.out.println("Can not destroy slave " + inetAddress);
        }
      }
    }
  }
}
