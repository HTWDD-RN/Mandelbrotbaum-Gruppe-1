# Project plan

## Architecture
- One Client
- Many Server
- The client makes a request to a server and assigns it a range to calculate.
- They return the iteration value back to the client as a chunk.
- It should work like scanlines so that a server gets assigned a y range.

- Client has MVP architecture
- Client and server communicate with RMI protocol

- Server finishes its range and then returns the whole section back to the client.
- Client creates an image from all received chunks.