# Network Simulator
simulate Ethernet / IP / MPLS traffic

# Setup
The following commands add 2 VMs and connect them with switch and a router

- add vm
- add vm
- connect node10 and node11

# Disconnect command
- disconnect x from y
- disconnect x ( in case of single connection )

# configure connected VMs with IP
if the VMs are not connected to a router, they will not get IP address.
in order to configure the VMs with static IP address, run the command:
- config ip

# MPLS demo
- create demo 3 (Small) or 4 (Large)

# MPLS setup LSP
- config mpls path X
where X is algorithm number
1 for dept first
2 for Dijkstra
