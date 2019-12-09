# BTL-N1-Network-programming-
- start 4 cmd: server, client 1, 2 ,3 
- enter ip into client 1,2,3 
  + .2.1/client: client create 2 client thread: <br>
    1: master thread connect master server <br>
    2: connect to thread server of client 1.1 <br>
    3: connect to thread server of client 1.2 <br>
  + .1.2/client: client create 1 server thread <br>
    1: master thread connect master server <br>
    2: server thread listen of port 8282 <br>
  + ./client: client create 1 server thread <br>
    1: master thread connect master server <br>
    2: server thread listen of port 8181 <br>
