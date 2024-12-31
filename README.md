# Nebula

![Velocity](https://flat.badgen.net/badge/Velocity/3.4.0/1197d1?icon=dockbit)

**Nebula** is a server management tool built with Java and integrated with Velocity, designed to handle the dynamic creation, management, and control of Minecraft server instances. It uses Docker on Hold-Servers to manage Backend-Servers.

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
You dont have to use them, Nebula does handle the queue and lobby scalign on its own.
But here they are:

### **Admin Commands:** 

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
    
### **Group Commands:** 

- **/group template [template_name] [new_server_name]**  
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
        
## Future Enhancements:
- Multi-Proxy-System
