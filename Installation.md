# System Requirements #

  * JDK 1.4 or higher

# Runtime #

  * add expect4j.jar to your classpath
  * add oro-2.0.8.jar to your classpath

# Dependencies #

The only real dependency is Oro for its Perl5Matcher. Other dependencies come from using specific helper functions:

  * ExpectUtils.ssh( hostname, username, password ) - Uses jsch-0.1.32.jar or newer
  * ExpectUtils.telnet( hostname, port) - Uses commons-net-1.4.1.jar

The same depdenencies apply to tcl. If you use 'spawn ssh hostname' or 'telnet hostname port', include the respective jar file.

# Getting Started #

## Java ##

expect4j supports interacting with a Socket or a InputStream/OutputStream pair.  For example:

```
Socket s = new Socket(remotehost, 80);
Expect4j expect = new Expect4j(s);
```

Then you're able to run the basic set of expect command.

### send ###

```
expect.send("GET " + url + " HTTP 1.1\r\n");
expect.send("Host: " + remotehost + "\r\n");
expect.send("Connection: close\r\n");
expect.send("User-Agent: Expect4j\r\n");
expect.send("\r\n");
```
### expect ###

```
expect.expect("HTTP/1.1 200 OK");
```

Now, if you know HTTP, you'll see that line is quite inadequate. See [ExpectUtils.java](http://code.google.com/p/expect4j/source/browse/trunk/src/main/java/expect4j/ExpectUtils.java#44), it has pre-built versions of and Http, SSH, telnet and exec.

## Tcl ##

expect4j will register itself as the 'expect 2.0' extension. So by just setting up the classpath (see above), this command will work in your tcljava script:

```
package require expect
```

### spawn ###

expect4j only supports ssh and telnet. And because both of those are being emulated you have to use a specific formatting for the spawn command so that it can be intercepted by the expect4j code.  For example if you specify:

```
/usr/local/bin/secureshell my.host.name (don't do this)
```

The library won't be able to detected that you want to use ssh emulation. What the libary looks for is the work "ssh" or "telnet" in the first argument to spawn. So, these will work:

```
spawn $path(ssh) -l username host
spawn /usr/local/bin/ssh hostname
spawn telnet hostname port
```

Since ssh traditionally asks for the password on the console, and the jsch library does not, a special parameter is supported by expect4j to add a password.

```
spawn ssh -l username -P password host
```

### Supported Commands ###
  * exp\_continue
  * exp\_internal 0;
  * expect
  * log\_user 0;
  * send " ";
  * send -- "$command\r";
  * sleep
  * spawn
  * stty -echo;
  * stty echo;
  * timestamp

### Supported Variables ###
  * expect\_out(1,string)
  * through
  * expect\_out(5,string)
  * expect\_out(buffer)
  * spawn\_id

### Example ###

run.bat
```
set CLASSPATH=%CLASSPATH%;lib\tcljava.jar
set CLASSPATH=%CLASSPATH%;lib\jacl.jar
set CLASSPATH=%CLASSPATH%;lib\itcl.jar
set CLASSPATH=%CLASSPATH%;lib\tjc.jar

set CLASSPATH=%CLASSPATH%;lib\expect4j-1.1.jar
set CLASSPATH=%CLASSPATH%;lib\jsch-0.1.32.jar
set CLASSPATH=%CLASSPATH%;lib\oro-2.0.8.jar
set CLASSPATH=%CLASSPATH%;lib\commons-net-1.4.1.jar

set JACL_MAIN=tcl.lang.Shell

echo %CLASSPATH%

java tcl.lang.Shell telnet.tcl
```

telnet.tcl
```
package require java
java::load expect4j.ExpectEmulation
package require expect

#Spawn a telnet session to the telnet server running on the target device
spawn telnet 192.168.0.1
 
expect "login: "
send "root\r"
expect "Password:"
send "supersecret\r"

expect "root@router:~# "
```