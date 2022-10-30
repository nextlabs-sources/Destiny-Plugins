# Oauth2 JWT Secret Plugin

## Overview

This plugin is to sync JWT secrets (passphrases) associated with clients (Control Center users) to Java Policy Controller so that Java Policy Controller can authenticate clients calling REST APIs with valid API tokens.

Currently the JWT secret for a Control Center user (Only for internal users except for Administrator) is the value of the attribute of the user: "**jwt_passphrase**". Only users with this attribute can request a JWT from Control Center.

The plugin sends the secrets during every heartbeat to Java Policy Controller. Java Policy Controller will update it's list of user-secret store in-memory during each heartbeat and serialize the store to disk (file jwt_secret.bin under dpc folder). During Java Policy Controller starting process, before first effective heartbeat, it still can authenticate clients using local saved secret store file.

## Installation (Server – Control Center)

1. Stop Control Center
2. Unzip plugin zip file
3. The zip file should contain a folder named “Control Center”
4. Open the “Control Center” folder
5. Copy Oauth2JWTSecret-Plugin-server.jar to [Policy Server]/server/plugins/jar
6. Copy JwtSecretServer.properties to [Policy Server]/server/plugins/config
7. Edit JwtSecretServer.properties:
    * Make sure jar-path points to correct path of Oauth2JWTSecret-Plugin-server.jar copied (No need to replace [policy-server] as Control Center can interpret it to actual installation location)
8. Start Control Center

## Installation (Client – Java Policy Controller)

1. Stop Java Policy Controller
2. Unzip plugin zip file
3. The zip file should contain a folder named “Policy Controller”
4. Open the “Policy Controller” folder
5. Copy Oauth2JWTSecret-Plugin-client.jar to [DPC]/jservice/jar
6. Copy JwtSecretClient.properties.properties to [DPC]/jservice/config
7. Edit JwtSecretClient.properties.properties:
    * Modify jar-path to the actual absolute path of Oauth2JWTSecret-Plugin-client.jar copied (Java Policy Controller Can't interpret [NextLabs] within the value properly)
8. Modify Java Policy Controller's application server config
    * For tomcat:
      - Open tomcat's server.xml under [CATALINA_HOME]/conf
      - Under **Context** element whose **path** attribute is "/dpc", modify parameter **EnableJWTAuthenticationFilter** value to **true**.
    * For Jboss:
      - Open dpc.properties under [DPC]
      - Modify value of **EnableJWTAuthenticationFilter** to **true**.
9. Start Java Policy Controller
