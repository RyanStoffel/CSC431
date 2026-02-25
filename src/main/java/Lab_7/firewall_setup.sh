#!/usr/bin/env bash
# firewall_setup.sh
#
# This is the ONLY thing the receiver/victim runs manually.
# Sets up a firewall that explicitly blocks port 4444 inbound.
#
# Usage: sudo ./firewall_setup.sh

set -e

echo "=== CSC431 - Victim Firewall Setup ==="
echo ""

sudo ufw --force reset > /dev/null 2>&1

# block the attacker's known C2 port
sudo ufw deny in 4444/tcp comment "Block known malicious port"

# default policies
sudo ufw default deny incoming
sudo ufw default allow outgoing

sudo ufw --force enable > /dev/null 2>&1

echo "Firewall configured:"
echo ""
sudo ufw status verbose
echo ""
echo "Port 4444 inbound: BLOCKED"
echo "All other inbound:  BLOCKED"
echo "All outbound:       ALLOWED"
echo ""
echo "Firewall is active. Machine is 'protected.'"
echo ""
echo "To reset later: sudo ufw --force reset && sudo ufw --force disable"