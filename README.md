# Real-time content delivery server for the GigaChat project
Operating on websockets, written in Java.

# API documentation:

Entry point

* WS `ws://<?>/`

Arguments

* `id`: int64 - user ID to log in
* `token`: str - token for user authorization

Commands (prefix `in` means "not implemented") (transmission in a form of JSON)

* Incoming
* * Sending a message
* * * `type`: str - `MESSAGE-POST`
* * * `channel`: int64 - ID of a target channel for sending a message
* * * `author`: int64 - ID of a user who is sending a message
* * * `text`: str - message text
* * `in` Edit a message
* * * `type`: str - `MESSAGE-DELETE`
* * * `channel`: int64 - ID of a target channel for deleting a message
* * * `id`: int64 - ID of a target message for deleting
* * * `text`: str - new message text
* * `in` Deleting a message
* * * `type`: str - `MESSAGE-DELETE`
* * * `channel`: int64 - ID of a target channel for deleting a message
* * * `id`: int64 - ID of a target message for deleting
* * `in` Forwarding a message
* * * `type`: str - `MESSAGE-FORWARD`
* * * `channel`: int64 - ID of a target channel for forward a message
* * * `author`: int64 - ID of a user who is forwarding a message
* * * `source_channel`: int64 - ID of a source channel
* * * `source_message`: int64 - ID of a source message
* * Creating a channel
* * * `type`: str - `CHANNEL-CONTROL-CREATE`
* * * `author`: int64 - a sender of a command
* * * `title`: str - desired channel name
* * `in` Delete a channel
* * * `type`: str - `CHANNEL-CONTROL-DELETE`
* * * `author`: int64 - a sender of a command
* * * `id`: int64 - ID of a target channel
* * Add/remove a user to a channel
* * * `type`: str - `CHANNEL-USERCONTROL-PRESENCE`
* * * `author`: int64 - a sender of a command
* * * `user`: int64 - ID of a target user
* * * `id`: int64 - ID of a target channel
* * * `access`: bool - `true` (add a user if it doesn't exist yet) or `false` (delete a user, if it does exist yet)
* * Enable/disable active channel listening
* * * `type`: str - `SYSTEM-CHANNELS-LISTEN`
* * * `user`: int64 - ID of a user to listen to the channel
* * * `channel`: int64 - ID of a target channel
* * * `status`: bool - `true` (enables wiretapping if it is not already enabled) or `false` (disables wiretapping if it was enabled)
* Outgoing
* * Sending a message
* * * `type`: str - `MESSAGE-POST`
* * * `channel`: int64 - ID of a target channel for sending a message
* * * `author`: int64 - ID of a user who is sending a message
* * * `text`: str - message text
* * `in` Edit a message
* * * `type`: str - `MESSAGE-DELETE`
* * * `channel`: int64 - ID of a target channel for deleting a message
* * * `id`: int64 - ID of a target message for deleting
* * * `text`: str - new message text
* * `in` Deleting a message
* * * `type`: str - `MESSAGE-DELETE`
* * * `channel`: int64 - ID of a target channel for deleting a message
* * * `id`: int64 - ID of a target message for deleting
* * `in` Forwarding a message
* * * `type`: str - `MESSAGE-FORWARD`
* * * `channel`: int64 - ID of a target channel for forward a message
* * * `author`: int64 - ID of a user who is forwarding a message
* * * `source_channel`: int64 - ID of a source channel
* * * `source_message`: int64 - ID of a source message
* * Add/remove a user to a channel
* * * `type`: str - `CHANNEL-USERCONTROL-PRESENCE`
* * * `user`: int64 - ID of a target user
* * * `id`: int64 - ID of a target channel
* * * `access`: bool - `true` (add a user if it doesn't exist yet) or `false` (delete a user, if it does exist yet)
