ServerHandlerProxy
ServerHandlerProxy is a server management tool built with Java and integrated with Velocity, designed to handle the dynamic creation, management, and control of Minecraft server instances. The project allows server administrators to interact with Minecraft servers via HTTP requests, enabling functionality such as creating, starting, stopping, and deleting server instances.

Key Features:
Dynamic Server Creation: Easily create Minecraft servers on the fly via HTTP API requests.
Server Control: Start, stop, and delete server instances directly from the web interface or API.
Port Management: Automatically retrieve free ports to assign to new server instances.
Flask Backend: Python-based backend using Flask for managing instance operations.
Velocity Integration: Integrates with Velocity, allowing dynamic server registration and player management within the Velocity proxy environment.
Password Protection: Secure operations with password-protected requests for server actions.
Custom Plugin Support: Includes a custom Velocity plugin for additional server handling capabilities.
Requirements:
Java 11+
Velocity Proxy
Python 3.8+ (for the Flask backend)
libtmux (for managing server instances in tmux sessions)
Usage:
Clone the repository.
Configure config.json with your desired settings (including passwords and ports).
Run the Flask backend to manage server requests.
Use the HTTP API to interact with the server instances (create, start, stop, delete).
Future Enhancements:
Player permissions management within the Velocity proxy.
Improved UI for easier server management.
Logging and monitoring of server activity.
