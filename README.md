# Nebula

![Velocity](https://flat.badgen.net/badge/Velocity/3.4.0/1197d1?icon=dockbit)

**Nebula** is a server management tool built with Java and integrated with Velocity, designed to handle the dynamic creation, management, and control of Minecraft server instances. It uses Docker to manage Backend-Servers.

## Key Features:
- **Simple Permissions**: Simply define groups in the config.
- **Port Management**: Automatically retrieve free ports to assign to new server instances.
- **Node Management**: Automatically creates and deletes containers when needed.
- **Velocity Integration**: Integrates with Velocity, allowing dynamic server registration and player management within the Velocity proxy.

## Requirements:
- **Java 21**: Required for running the Proxy instance.
- **Velocity Proxy**: No predefined servers are required; servers are dynamically registered.
- **Docker and Ruby on hold server**: Necessary for running the backend servers.
- **User on hold server**: Necessary for interacting with Docker, make sure he has the right permissions.

## Setup:
1. Clone the repository.
2. Build.
3. Put into your plugins folder (Velocity-Proxy).
5. Run Server.
6. Stop and configure and then start again.
   
## Important Info:
On start, Velocity will try to create a server on the first backend server via the Lobby-Template in the config.
Later on, it will create more lobby servers according to the player count limits defined in the config.
## In-Game Commands
Nebula also supports in-game commands for admins to manage server instances directly within Minecraft.
You dont have to use them, Nebula does handle the queue and lobby scaling on its own.
But here they are:

### **Admin Commands:** **Permission: 'velocity.admin'**

- **/admin template [template_name] [new_server_name]**  
  - **Description**: Creates a new server instance using the specified template.
  - **Example**:  
    ```
    /admin template anton691/simple-lobby:latest test
    ```

- **/admin kill [server_name]**  
  - **Description**: Kills a running server instance.
  - **Example**:  
    ```
    /admin kill test
    ```

- **/admin delete [server_name]**  
  - **Description**: Deletes a server instance. (Will Kill it before.)
  - **Example**:  
    ```
    /admin delete test
    ```
    
- **/admin start [server_name]**  
  - **Description**: Starts a server instance.
  - **Example**:  
    ```
    /admin start test
    ```
    
### **Group Commands:** **Permission: 'velocity.admin'**

- **/group assign [player_name] [group_name]**  
  - **Description**: Assigns a specified group to a player. This changes the player's group affiliation to the provided group name.
  - **Example**:  
    ```
    /group assign Alice Moderator
    ```

- **/group create [group_name] [level] [prefix...]**  
  - **Description**: Creates a new group with the given name, level, and prefix. The `level` determines the hierarchy or priority of the group, and the `prefix` is used as a tag for the group.
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
    /group permission add VIP access_premium_features
    ```

- **/group permission remove [group_name] [permission]**  
  - **Description**: Removes a specific permission from a group.
  - **Example**:  
    ```
    /group permission remove VIP access_premium_features
    ```

- **/group permission list [group_name]**  
  - **Description**: Lists all permissions assigned to a specified group.
  - **Example**:  
    ```
    /group permission list VIP
    ```

- **/group info [group_name]**  
  - **Description**: Displays detailed information about a specific group, including its permissions and other relevant details.
  - **Example**:  
    ```
    /group info VIP
    ```

### **Queue Commands:**

- **/queue join [queue name]**  
  - **Description**: Join a queue.
  - **Example**:  
    ```
    /queue join Duels
    ```

- **/queue leave**  
  - **Description**: Leaves current queue.
  - **Example**:  
    ```
    /queue leave
    ```
    
### **Party Commands:**

- **/party invite [player]**  
  - **Description**: Invite a player.
  - **Example**:  
    ```
    /party invite BastiGHG
    ```
    
- **/party accept [invite]**  
  - **Description**: Accept a invite from a player, if no invite is given it will try to use the latest invite.
  - **Example**:  
    ```
    /party accept Aquestry
    ```

- **/party leave**  
  - **Description**: Leave the current party..
  - **Example**:  
    ```
    /party leave
    ```
    
# Multi-Proxy-System

The Multi-Proxy-System is made for server networks with international players, so everybody has a good ping.
You can configure other proxies in the config, there you can also define the HMAC secret and the port.
Also define the level and dont give two proxies the same level else they will shutdown!
After you setup everything correctly groups will now sync.
When a proxy comes only it will get the state and the current groups of the highest level proxy defined in the config.
But after the first sync it will only get the changes.
For example if a admin changes the group of a player that is on another proxie theire nametag will update
if you are using NebulaAPI on the backend. And even if you are not using it, the other proxy will still get the changes.
