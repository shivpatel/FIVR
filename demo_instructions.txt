FIVR FTA Demo Instructions


1. cd into src directory on 2 command lines

2. run make on one of the command lines

3. For Client use the command: java FIVRClient/Client

4. For Server use the command: java FIVRServer/Server

5.Follow on-screen instructions

Note all paths are relative. For example, to use the client's "post [filename]" command, 
you should use something like "post ClientFiles/image.png" to send a file called image.png from the ClientFiles folder. 
Note that end file will show up in ServerFiles. You should also use a similar structure with the "get [filename]" command.


=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
Alternate Method of running the code:
Using FIVRClient.jar and FIRVServer.jar

cd to the src directory that contains FIVRClient.jar and FIVRServer.jar

(assuming java has been installed in the machine you are on) type:
java -jar <jarname>

This will open the fta application. From there, you will see instructions on the available commands

Example:
Starting the server:
java -jar FIVRServer.jar

Starting the client:
java -jar FIVRClient.jar                
