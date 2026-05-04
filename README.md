very simple factions plugins, easy to use. originally made for me & my friends, i've decided to release it. 
## SimpleFactions Commands

### Basic
- `/f create <name>` — Create a faction.
- `/f invite <player>` — Invite a player to your faction. Owner only.
- `/f accept` — Accept a faction invite.
- `/f leave` — Leave your current faction. Owners must disband instead.
- `/f kick <player>` — Kick a member from your faction. Owner only.
- `/f disband` — Delete your faction. Owner only.

### Claims
- `/f claim` — Claim the chunk you are standing in.
- `/f unclaim` — Unclaim the chunk you are standing in. Owner only.
- `/f map` — Show a small chunk map around you.

### War
- `/f war <faction>` — Declare war on another faction. Owner only.

## Claim Rules

- Each faction gets **10 claimed chunks per member**.
- Claims cannot overlap.
- Land within **30 chunks of spawn** cannot be claimed.
- Players cannot break or place blocks in enemy claims unless war rules allow it.
- Admins with `simplefactions.admin` can bypass claim protection.

## Map Legend

- `✦` — Your current chunk
- `■` gray — Wilderness
- `■` green — Your faction
- `■` red — Another faction
