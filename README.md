# ServerHandlerProxy

**ServerHandlerProxy** is a server management tool built with Java and integrated with Velocity, designed to handle the dynamic creation, management, and control of Minecraft server instances. The project allows server administrators to interact with Minecraft servers via HTTP requests, enabling functionality such as creating, starting, stopping, and deleting server instances.

## Key Features:
- **Dynamic Server Creation**: Easily create Minecraft servers on the fly via HTTP API requests.
- **Server Control**: Template, start, stop, and delete server instances directly from the web interface or API.
- **Simple Permissions**: Simply define admins via there UUID in the config.
- **Port Management**: Automatically retrieve free ports to assign to new server instances.
- **Flask Backend**: Python-based backend using Flask for managing instance operations.
- **Velocity Integration**: Integrates with Velocity, allowing dynamic server registration and player management within the Velocity proxy environment.
- **Password Protection**: Secure operations with password-protected requests for server actions.

## Requirements:
- **Java 21**: Required for running the backend and managing server instances.
- **Velocity Proxy**: No predefined servers are required; servers are dynamically registered.
- **Python 3.8+**: Necessary for running the Flask backend that handles server requests.
- **libtmux**: Used for managing server instances within tmux sessions.
- **Flask**: A Python web framework to handle the requests to the backend.

## Usage:
1. Clone the repository.
2. Configure `config.json` with your desired settings (including passwords and ports).
3. Run the Flask backend to manage server requests.
4. Use the HTTP API to interact with the server instances (create, start, stop, delete).

## In-Game Commands

ServerHandlerProxy also supports in-game commands for admins to manage server instances directly within Minecraft. Here are the available commands:

### **Admin Commands:**

- **/admin template [template_name] [new_server_name]**  
  - **Description**: Creates a new server instance using the specified template.
  - **Example**:  
    ```
    /admin template lobby lobby3
    ```
    This will create a new server called `lobby3` based on the `lobby` template.

- **/admin start [server_name]**  
  - **Description**: Starts a specific server instance.
  - **Example**:  
    ```
    /admin start lobby3
    ```

- **/admin stop [server_name]**  
  - **Description**: Stops a running server instance.
  - **Example**:  
    ```
    /admin stop lobby3
    ```

- **/admin delete [server_name]**  
  - **Description**: Deletes a server instance.
  - **Example**:  
    ```
    /admin delete lobby3
    ```

- **/admin list**  
  - **Description**: Lists all currently running and available server instances.
  - **Example**:  
    ```
    /admin list
    ```

- **/admin info [server_name]**  
  - **Description**: Displays detailed information about a specific server instance, such as the current port, template, and status.
  - **Example**:  
    ```
    /admin info lobby3
    ```

### **Player Commands:**

- **/join [server_name]**  
  - **Description**: Allows players to join a specific server instance.
  - **Example**:  
    ```
    /join lobby3
    ```

## Future Enhancements:
- Player permissions management within the Velocity proxy.
- Improved UI for easier server management.
- Logging and monitoring of server activity.
