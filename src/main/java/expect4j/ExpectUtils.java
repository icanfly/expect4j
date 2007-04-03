/*
 * ExpectUtils.java
 *
 * Created on November 20, 2006, 8:40 PM
 */

package expect4j;

import com.jcraft.jsch.*;
import java.io.*;
import java.net.Socket;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import expect4j.matches.*;
import java.util.logging.*;
import org.apache.commons.net.telnet.*;
import org.apache.commons.net.io.*;

/**
 * Utilities functions to help access the expect4j library in the most common
 * ways. This functions can be used directly or used as a model to copy for your
 * own uses.
 *
 * @author justin
 */
public abstract class ExpectUtils {
    static final public java.util.logging.Logger log = java.util.logging.Logger.getLogger(ExpectUtils.class.getName());
    
    /**
     * Simulates: curl http://remotehost/url
     */
    public static String Http(String remotehost, String url) throws Exception {
        Socket s = null;
        s = new Socket(remotehost, 80);
        log.fine("Connected to " + s.getInetAddress().toString() );
        /*
        Reader reader = null;
        Writer writer = null;
         
        reader = new InputStreamReader(s.getInputStream());
        writer = new OutputStreamWriter(s.getOutputStream());
         
        log.fine("Writing url");
        writer.write("GET http://" + remotehost + url + "\r\n\r\n");
        writer.flush();
         
        log.fine("Looking for feedback");
        char response[] = new char[256];
        int length = 0;
        while ( (length = reader.read(response,0,255)) != -1 ) {
            String line = new String(response,0,length);
            log.fine("Server: >" + line + "<");
        }
         
        reader.close();
        writer.close();
        s.close();
         
        if( true ) return "";
         */
        Expect4j expect = new Expect4j(s);
        
        expect.send("GET " + url + " HTTP 1.1\r\n");
        expect.send("Host: " + remotehost + "\r\n");
        expect.send("Connection: close\r\n");
        expect.send("User-Agent: Expect4j\r\n");
        expect.send("\r\n");
        log.fine("Sent header info");
        
        String remaining = null;
        expect.expect( new Match[] {
            new RegExpMatch("HTTP/1.[01] \\d{3} (.*)\n?\r", new Closure() {
                public void run(ExpectState state) {
                    log.fine("HTTP Header");
                    
                    // save http code
                    String match = state.getMatch();
                    String parts[] = match.split(" ");
                    
                    state.addVar("httpCode", parts[1]);
                    state.exp_continue();
                }
            }),
            new RegExpMatch("Content-Type: (.*\\/.*)\r\n", new Closure() {
                public void run(ExpectState state) {
                    state.addVar("contentType", state.getMatch() );
                    state.exp_continue();
                }
            }),
            new EofMatch( new Closure() { // should cause entire page to be collected
                public void run(ExpectState state) {
                    log.fine("Capturing until EOF");
                }
            }), // Will cause buffer to be filled up till end
            new TimeoutMatch(10000, new Closure() {
                public void run(ExpectState state) {
                    log.fine("Timeout");
                }
            })
        });
        
        remaining = expect.getLastState().getBuffer(); // from EOF matching
        
        String httpCode = (String) expect.getLastState().getVar("httpCode");
        System.out.println("HTTP Code: " + httpCode);

        String contentType = (String) expect.getLastState().getVar("contentType");
        log.fine("Content Type: " + contentType );
        
        s.close();
        
        return remaining;
    }
    
    /**
     * Simulates: spawn ssh $remote_server
     */
    public static Expect4j SSH(String hostname, String username, String password) throws Exception {
        return SSH(hostname, username, password, 22);
    }
    
    public static Expect4j SSH(String hostname, String username, String password, int port) throws Exception {
        JSch jsch=new JSch();
        
        //jsch.setKnownHosts("/home/foo/.ssh/known_hosts");
        
        Session session=jsch.getSession(username, hostname, port);
        if( password != null) {
            log.finer("Using password");
            session.setPassword(password);
        }
        
        java.util.Hashtable config=new java.util.Hashtable();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setDaemonThread(true);
        session.connect(3 * 1000);   // making a connection with timeout.
        
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        
        //channel.setInputStream(System.in);
        //channel.setOutputStream(System.out);
        
        channel.setPtyType("vt102");
        
        Hashtable env=new Hashtable();
        //env.put("LANG", "ja_JP.eucJP");
        channel.setEnv(env);
        
        Expect4j expect = new Expect4j(channel.getInputStream(), channel.getOutputStream());
        
        channel.connect(5*1000);
        
        return expect;
        
    }
    
    /**
     * TODO Simulate "Could not open connection to the host, on port...."
     * TODO Simulate "Connection refused"
     */
    public static Expect4j telnet(String hostname, int port) throws Exception {
        // This library has trouble with EOF
        final TelnetClient client = new TelnetClient();
        
        TerminalTypeOptionHandler ttopt = new TerminalTypeOptionHandler("VT100", false, false, true, true);
        EchoOptionHandler echoopt = new EchoOptionHandler(true, false, true, false);
        SuppressGAOptionHandler gaopt = new SuppressGAOptionHandler(false, false, false, false);
        client.addOptionHandler( ttopt );
        client.addOptionHandler( echoopt );
        client.addOptionHandler( gaopt );
        
        client.connect(hostname, port);
        InputStream is =  new FromNetASCIIInputStream( client.getInputStream() ); // null until client connected
        OutputStream os = new ToNetASCIIOutputStream( client.getOutputStream() );
        
        StreamPair pair = new StreamPair(is, os) {
            public void close() {
                //super.close();
                try {
                    if( client != null ) client.disconnect();
                }catch(IOException ioe) {
                    
                }
            }
        };
                
        
        /*
        URL url=new URL("telnet", hostname, port, "",  new thor.net.URLStreamHandler());
        final URLConnection urlConnection=url.openConnection();
        urlConnection.connect();
        if (urlConnection instanceof TelnetURLConnection) {
            ((TelnetURLConnection)urlConnection).setTelnetTerminalHandler(new SimpleTelnetTerminalHandler());
        }
        OutputStream os=urlConnection.getOutputStream();
        InputStream is=urlConnection.getInputStream();
         
        StreamPair pair = new StreamPair(is, os) {
            public void close() {
                try { ((TelnetURLConnection)urlConnection).disconnect(); }catch(Exception e) { }
            }
        };
         */
        Expect4j expect4j = new Expect4j(pair);
        
        return expect4j;
    }
    
    /*
    class SimpleTelnetTerminalHandler extends DefaultTelnetTerminalHandler implements TelnetConstants {
        public void LineFeed() {
            System.out.print('\n');
            System.out.flush();
        }
        public void CarriageReturn() {
            System.out.print('\r');
            System.out.flush();
        }
        public void BackSpace() {
            System.out.print((char)BS);
            System.out.flush();
        }
        public void HorizontalTab() {
            System.out.print((char)HT);
            System.out.flush();
        }
    }
     */
    public static Expect4j spawn(String cmdLine) throws Exception {
        String[] cmdArgs = cmdLine.split(" ");
        Process process = Runtime.getRuntime().exec( cmdArgs );
        
        InputStream is = process.getInputStream();
        OutputStream os = process.getOutputStream();
        StreamPair iopair = new StreamPair(is, os);
        Expect4j expect = new Expect4j( iopair );
        
        return expect;
    }
}
