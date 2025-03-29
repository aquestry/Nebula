# Nebula

![Velocity](https://flat.badgen.net/badge/Velocity/3.4.0/1197d1?icon=dockbit)

# About Nebula

Nebula is a server management tool built with Java and integrated with Velocity, designed to handle the dynamic creation, management, and control of Minecraft server instances. It uses SSH to connect to the nodes and from there it will use Docker to manage the containers running the Minecraft server instances.

[GitHub](https://github.com/aquestry/Nebula)

## Features

- **Scalable Lobbies**: Nebula has a scalable lobby system where you can define minimum and maximum players per lobby.  
    For example, if you set the minimum to 3 and the maximum to 5, one lobby will be created at the start.  
    When the player count reaches 3 in that lobby, a new one will be created. However, 
    the first lobby will continue filling up to 5 players before the newly created lobby starts accepting players.
- **Party System**: Allows players on a proxy to join each other via a party system.  
    When they queue, they will be placed in the same game.
    The system ensures that the party size matches the game-mode's required player count.
- **Simple Groups**: Easily define groups in the configuration.
- **Node Management**: Automatically creates and deletes containers as needed, always using the least used node.
- **Queue Processor**: Nebula has an intelligent queue processor that manages game mode queues and preloads game mode containers efficiently.
- **Multi-Proxy System**: A ring-based multi-proxy system that currently only synchronizes groups between proxies.

## Requirements

Knowledge about Docker, if not, watch a YouTube tutorial.

### Proxy
- **Velocity**: Your velocity proxy, no predefined servers.
- **Java 21**: Required for running the proxy instance.

### Node
- **Docker and Ruby**: Necessary for running the backend servers.
- **User**: Necessary for interacting with Docker via SSH, make sure he has the right permissions.

## Setup

1. Clone the repository.
2. Build.
3. Put it in your plugin folder.

## Configuring

To configure your nebula proxy you need at least one node that is accessible via SSH.
That can be the same server where the proxy itself is run.
Just put the SSH login details in the config.
If you are only using a password type 'none' in the private key field. 
And if you are using a private key type 'none' in the password field.
After that you can go over the other settings and change them if you want.
You can also configure the messages in the messages.yml, a way to automatically get the messages.yml from GitHub will be added soon.

## My own gamemode?

Every node server has to have the docker image installed locally or the nodes will try to download them from Dockerhub,
and if you have 'pull-start' set to true nebula will update all mentioned images from Dockerhub.
Make sure you have a way to set the Velocity secret, we recommend doing it via env vars that you can set it the config globally or just for specific gamemodes.

#### Dockerhub (Recommended)
If you want to use Dockerhub for your own images look at my example repos on GitHub, and
make a GitHub action to build them to Dockerhub. Enable 'pull-start' if you want Nebula to check for updates on start.

#### Local
If you want to use local images turn of 'pull-start', and put your image on every node.

## Multi-Proxy-System

The Multi-Proxy-System is made for server networks with international players, so everybody has a good ping.
You can configure other proxies in the config, there you can also define the HMAC secret and the port.
Also define the level and don't give two proxies the same level else they will shut down!
After you set up everything correctly groups will now sync.
When a proxy comes only it will get the state and the current groups of the highest level proxy defined in the config.
But after the first sync it will only get the changes.
For example if an admin changes the group of a player that is on another proxy their nametag will update
if you are using NebulaAPI on the backend. And even if you are not using it, the other proxy will still get the changes.

## Group-System

In the perms.conf you can configure groups, each group has a name, a level, a prefix, a list of member uuids and a permission list.
It supports getting permissons from another group with 'group.<group_name>'.
If a player joins he will get the default group you defined in the config.

## Parties

Anybody can create a party by inviting another player, if he is not currently in one.
If the leader of the party trys to join a queue and the queue size matches the party size all party members will be added to the queue.
Also, only the leader can join a queue and invite new members, and if a player quits a party all members will be removed from the current queue.
If the leader quits the party and there is more than one player left and new leader will be chosen.

## Commands

- **Group Command (velocity.admin)**: Allows admins to create and delete groups, assign groups, and get group information.
- **Proxy Command (velocity.admin)**: More like a debug command to get the current nodes and containers from a defined proxy.
- **Container Command (velocity.admin)**: Allows admins to start and stop, create and delete containers.
- **Queue Command ()**: Allows players to join or leave game queues.
- **Party Command ()**: Enables players to create and manage parties.
- **Lobby Command ()**: Gives players the ability to go to a lobby at any time.

All the commands have very advanced tab completion for easy usage.

### **Container Commands:**

- **/container template [template_name] [new_server_name]**
    - **Description**: Creates a new container using the specified template.
    - **Example**:
      ```
      /container template anton691/simple-lobby:latest test
      ```

- **/container kill [container_name]**
    - **Description**: Kills a running container.
    - **Example**:
      ```
      /container kill test
      ```

- **/container delete [container_name]**
    - **Description**: Deletes a container. (Will Kill it before.)
    - **Example**:
      ```
      /container delete test
      ```

- **/container start [container_name]**
    - **Description**: Starts a container.
    - **Example**:
      ```
      /container start test
      ```

### **Proxy Commands:**

- **/proxy nodes [proxy_name]**
    - **Description**: Lists nodes defined on that proxy.
    - **Example**:
      ```
      /proxy nodes proxy-us
      ```

- **/node containers [proxy_name]**
    - **Description**: Lists containers on that proxy.
    - **Example**:
      ```
      /proxy server proxy-de
      ```

### **Group Commands:**

- **/group assign [player_name] [group_name]**
    - **Description**: Assigns a specified group to a player.
    - **Example**:
      ```
      /group assign Papaplatte vip
      ```

- **/group create [group_name] [level] [prefix...]**
    - **Description**: Creates a new group with the given name, level, and prefix. The `level` determines the priority of the group, and the `prefix` is used as a tag for the group.
    - **Example**:
      ```
      /group create VIP 2 "VIP Player"
      ```

- **/group delete [group_name]**
    - **Description**: Deletes an existing group. **Note**: The default group cannot be deleted.
    - **Example**:
      ```
      /group delete VIP
      ```

- **/group list**
    - **Description**: Lists all existing groups.
    - **Example**:
      ```
      /group list
      ```

- **/group permission add [group_name] [permission]**
    - **Description**: Adds a specific permission to a group.
    - **Example**:
      ```
      /group permission add VIP fly.fly
      ```

- **/group permission remove [group_name] [permission]**
    - **Description**: Removes a specific permission from a group.
    - **Example**:
      ```
      /group permission remove vip fly.fly
      ```

- **/group permission list [group_name]**
    - **Description**: Lists all permissions assigned to a specified group.
    - **Example**:
      ```
      /group permission list vip
      ```

- **/group info [group_name]**
    - **Description**: Display detailed information about a specific group.
    - **Example**:
      ```
      /group info vip
      ```

### **Queue Commands:**

- **/queue join [queue name]**
    - **Description**: Join a queue.
    - **Example**:
      ```
      /queue join Duels
      ```

- **/queue leave**
    - **Description**: Leave the current queue.
    - **Example**:
      ```
      /queue leave
      ```

### **Party Commands:**

- **/party invite [player_name]**
    - **Description**: Invite a player.
    - **Example**:
      ```
      /party invite BastiGHG
      ```

- **/party accept [player_name]**
    - **Description**: Accept an invitation from a player, if no invite is given it will try to use the latest invite.
    - **Example**:
      ```
      /party accept Aquestry
      ```

- **/party leave**
    - **Description**: Leave the current party.
    - **Example**:
      ```
      /party leave
      ```

Feel free to join my discord, and if you have more questions or need help just dm me. :)
