# MoneySMP

A gradually expanding Money SMP plugin for **Paper 1.21.11**.

## Implemented features

### 001 — Basic Economy
- Configurable starting balance (default $100.00); `/money`, `/bal`, `/balance`.
- UUID-based balances stored as integer cents.

### 002 — Player Payments
- `/pay <player> <amount>` transfers money to an online player.

### 003 — Admin Economy
- `/eco give|take|set <player> <amount>`; permission: `moneysmp.admin`.

### 004 — Server Item Shop
- `/shop` opens the configurable purchase GUI.

### 005–006 — Player Chest Shops
- `/pshop create <price>`, `price`, `remove`, `list`; chest stock is player-managed and registered shops are protected.

### 007 & 009 — Auction House
- `/ah`, `/ah sell <price> [amount]`, `/ah mine`, `/ah cancel <id>`; configurable listing cap and expiry.

### 008 — Player Trading
- `/trade <player|accept|offer|confirm|cancel>`; item-stack escrow and disconnect cancellation.

### 010 — Daily Rewards
- `/daily claim`, `/daily status`; configurable 24-hour reward.

### 011 — Jobs
- `/job join <miner|farmer|hunter|fisher>`, `/job leave`, `/job info`; activity rewards are configurable.

### 012 — Player Bounties
- `/bounty place <player> <amount>`, `/bounty list`, `/bounty cancel <id>`; bounty escrow and PvP payout.

### 013 — Player Bank
- `/bank balance`, `/bank deposit <amount>`, `/bank withdraw <amount>`.

### 014 — Balance Leaderboard
- `/baltop` lists the ten richest recorded accounts.

### 015 — Item Selling
- `/sell hand`, `/sell all`; prices configured under `sell.prices`.

### 016 — Six-Team System and Team Treasury
- Six configurable teams (Red, Blue, Green, Yellow, Purple, Orange by default).
- `/team join <team>`, `/team leave`, `/team list`, `/team info [team]`.
- `/team deposit <amount>` adds wallet funds to the team treasury.
- The team leader can use `/team withdraw <amount>`; memberships, leaders and treasury balances persist in `teams.yml`.

### 017 — Player Tier Rankings
- Tiers: S, A, B, C, D and F; players without an assigned tier are Unranked.
- `/tier` shows your tier; `/tier list` shows assigned players.
- Admins use `/tier set <player> <S|A|B|C|D|F|unranked>`.

### 018 — Control Points
- Admins set capture locations with `/controlpoint set <name>`.
- Teams capture by holding the area uncontested for the configured number of seconds.
- Owned points periodically pay configurable money into the team's treasury.
- `/controlpoint list` and `/controlpoint status <name>` show ownership and capture progress.

## Configuration
- Paper 1.21.11 and Java 21 are required.
- Team names: `teams.names`.
- Control point values: `control-points.capture-seconds`, `radius`, `reward-cents`, `reward-interval-seconds`.
- All money amounts are in cents internally.

## Build
Run `mvn clean package`. Output: `target/money-smp-1.5.0.jar`.
