# Real-time content delivery server for the GigaChat project
Operating on WebSockets, written in Java.

# API documentation

### Connection

Entry point

* WS `ws://<?>/`

Arguments

* `id`: int64 - user ID to log in
* `token`: str - token for user authorization

Result

Was the authorization completed?
* TRUE
* * The connection will be established, you will be notified.
* FALSE
* * The connection will be closed with code `406`

### Communication

Common commands template

**\<COMMAND\>%\<CONTROLHASH\>%\<DATA\>**

* COMMAND - string of upper latin letters and the `-` signs, which are the separator of the identification of the team categories. Examples:
* * `CHANNELS-USE-MESSAGES-POST-NEW`
* * `CHANNELS-ADMIN-USERS-ADD`
* * `CHANNELS-USE-MESSAGES-ATTACHMENTS-REORGANIZE`
* CONTROLHASH - an arbitrary string to be indexed by the client that sent it. It is recommended to use a hash-code from DATA
* DATA - string in JSON format containing the command arguments