Usage : Client 1 < - (use port1) -> Server < - (use port2) - > Client 2


UDPSocket is Blocking to receive(). But UDPChannel is NIO (Non-blocking IO)

It is Relay Server.

Flow :

1. Connect Client 1 use port1
2. Send("start");
3. Connect Client 2 use port2 
4. Send("start");

First, You Need to Send Packet Once. Then Router make routing table. (Hole-Punching)
Second, You can use Relay Server. 

UDP is good for Media Streaming (Video, Audio.. )



