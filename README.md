# SimpleFactions

SimpleFactions is a lightweight factions plugin for Spigot/Paper servers (Minecraft 1.21.x target) with chunk claiming, war/ally relations, trust permissions, and a basic GUI menu.

## Features

- Faction lifecycle
  - Create, invite, accept, leave, kick, disband
- Land claims
  - Claim/unclaim current chunk
  - Per-faction claim limits based on member count
  - Spawn-distance claim restriction
  - Territory minimap with relation colors
- Relations
  - Declare war
  - Set ally
  - Set neutral
- Protection
  - Prevents unauthorized break/place in claimed chunks
  - Trusted-player access in claims
  - Admin bypass via permission
- GUI
  - `/f menu` opens a menu for common actions
  - Includes selectors for invite/trust/war targets

## Commands

Primary command aliases: `/faction`, `/f`, `/fac`

- `/f help`
- `/f menu`
- `/f create <name>`
- `/f invite <player>`
- `/f accept`
- `/f leave`
- `/f kick <player>`
- `/f disband`
- `/f claim`
- `/f unclaim`
- `/f trust <player>`
- `/f untrust <player>`
- `/f map`
- `/f list`
- `/f info <faction>`
- `/f war <faction>`
- `/f ally <faction>`
- `/f neutral <faction>`

## Permissions

- `simplefactions.admin`
  - Bypasses claim protection checks

## Configuration

`config.yml`

- `chunksPerMember`
  - Number of claim chunks granted per faction member
- `minClaimDistanceFromSpawnChunks`
  - Minimum chunk distance from spawn to claim
- `war.allowExplosionsInEnemyClaims`
- `war.allowFireSpreadInEnemyClaims`

`factions.yml`

Stores faction, claims, wars, allies, and trusted users data.

## Build

Requirements:

- Java 17
- Maven

From project directory:

```bash
cd /workspace/simplefactions/SimpleFactions
mvn clean package
```

Built jar output:

```text
/workspace/simplefactions/SimpleFactions/target/SimpleFactions-1.0.0.jar
```

## Install

1. Build the plugin jar.
2. Copy jar to your server `plugins/` folder.
3. Start/restart server.
4. Configure `plugins/SimpleFactions/config.yml` as needed.

## Notes

- Current GUI is a foundational flow and can be expanded with pagination and deeper management menus.
- This project is intended to stay lightweight compared to large all-in-one land management plugins.
