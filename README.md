## Practical Course Cloud Databases - MS5

The Server contains 3 components:

1) ms5-client.jar -  Client for connecting to KV Server instances.
2) ms5-server.jar -  KVServer.
3) ms5-ecs.jar - Responsible for managing KV server instances.

## ECS Server Configuration

In order to setup the ECS server the user need to configure the public-private key SSH authentication on their machine/machines
and the private key should be put into the following directory:

"/home/" + userName + "/.ssh/id_rsa" where username is the username configured in the ecs.config


The KV Servers can be configured using ecs.config 
ecs.config contains all the servers available for ecs to launch and it needs following parameters: 


node1 username 127.0.0.1 50000 3000 /home/username/Desktop/Praktikum/gr6/ms5-server.jar /home/username/data/node1/
<node unique name> <ssh username> <ip> <ipport> <adminPort> <jar location> <server storage location>

To start ECS Server on a custom port with a custom config file, use the format
java -jar ms5-ecs.jar ecs.config 40000

#ECS Commands

initService <number of nodes> <Cache Size> <Cache Strategy> - Initialize n number of servers
start - Start Receiving client calls
stop - Stop receiving client calls
shutdown - Shutdown all servers
addNode cacheSize cacheType - Add a new server at arbitrary position
removeNode - Remove a Server
metaData -  meta Data of Servers
quit - shutdown ECS Server

#KV Client(ms5-client.jar)

connect <address> <port> - Tries to establish a TCP- connection to the server at <address> and <port>.
disconnect - Tries to disconnect from server
put <key> <data> - Tries to store key value in the  KV server.
put <key> - Tries to delete key from the responsible KV server.
get <key> - Tries to get key from the responsible KV server.
quit - quits the echo client
help - displays list of available commands

The Client should know atleast one ip and port of a running KV Server to begin communication:
In case of SERVER_NOT_RESPONSIBLE the client automatically connects the correct node and performs the operation.

'
#Replication:

The Server automatically replicates the data batch by batch basis every minute to the next 2 nodes in the ring.

#Failure Detection

The server follows a ring based failure detection. The server pings the next node in the ring for availability and 
informs the ECS in case of failure. The ECS replaces the server in cases of failure.

#Log Files
The log files are located in the home directory(ubuntu) and jar file location. 