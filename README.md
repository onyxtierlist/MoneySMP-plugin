# MoneySMP

A Paper 1.21.11 plugin inspired by Money SMP. Requires Java 21.

## Features

### Economy and trading
1. **Basic economy** — configurable starting balance; `/money`, `/bal`, `/balance`.
2. **Player payments** — `/pay <player> <amount>`.
3. **Admin economy** — `/eco give|take|set <player> <amount>`.
4. **Server shop** — `/shop` opens the configured item shop.
5. **Player chest shops** — `/pshop create <price>` while looking at a chest.
6. **Shop management** — `/pshop price <price>`, `remove`, `list`.
7. **Auction house** — `/ah`, `/ah sell <price> [amount]`, `/ah mine`, `/ah cancel <id>`.
8. **Player trading** — `/trade <player>`, `accept`, `offer`, `confirm`, `cancel`.
9. **Auction management** — configurable listing caps and expiry.
10. **Daily rewards** — `/daily claim`, `/daily status`.
11. **Jobs** — `/job join <miner|farmer|hunter|fisher>`, `/job leave`, `/job info`.
12. **Player bounties** — `/bounty place`, `/bounty list`, `/bounty cancel`.
13. **Player bank** — `/bank balance`, `deposit`, `withdraw`.
14. **Balance leaderboard** — `/baltop`.
15. **Item selling** — `/sell hand`, `/sell all`.

### Season and team systems
16. **Six teams and team treasury** — `/team join`, `leave`, `list`, `info`, `deposit`, `withdraw`.
17. **Tier rankings** — S, A, B, C, D, F; `/tier`, `/tier list`, admin `/tier set`.
18. **Control points** — `/controlpoint set`, `list`, `status`; teams capture zones and earn treasury income.
19. **Gladiator arena** — configure arena locations and run player challenges.
20. **Natural disasters** — `/disaster start|stop|status`; effects do not intentionally destroy terrain.
21. **Random events** — `/randomevent list|start|stop|status`.
22. **Capture the Flag** — configure with `/ctf set`; use `/ctf start|status|stop`.
23. **Automatic event rotation** — optional scheduled random events.
24. **Season team points** — `/season points`, `/season top`, admin `award|set|reset`.
25. **Timed season finale** — `/finale start [seconds]`, `/finale status`, `/finale results`, `/finale stop`. The finale records starting standings, runs for a configurable duration, then announces the team(s) with the highest season points. Default duration is 30 minutes; allowed range is 30 seconds to 24 hours. Finale state and timestamps are stored in `finale.yml`.

## Random events currently implemented

- Slippery (speed/jump effects), Healthy (maximum health increase), Anonymous (invisibility), Double Dough (bounty payout multiplier), Miner's Haven (ore drops), Exhausted (Slowness), Forbidden Fruit (golden apple leaf drops), Vulnerable (reduced maximum health), Ultra Hardcore (no natural saturation regeneration), Shieldless (shield interaction cancellation), Shopping Spree (Hero of the Village), and Stubby Legs (increased fall damage).

The reference list mentions additional client-dependent or not-yet-verified events. Audible and Your Eyes Only need compatible client support; a server-only Paper plugin cannot enforce client voice/camera behavior. Some event descriptions are not sufficiently verified to implement faithfully. The plugin does not claim full parity with every Season 7 feature.

## Configuration

- `config.yml`: starting balance, shops, rewards, jobs, teams, control points, CTF, random events.
- `season-points.yml`: persistent team season standings.
- `finale.yml`: finale state and run history metadata.
- Other managers maintain their own data files.

## Build

Run `mvn clean package`. Output: `target/money-smp-1.9.0.jar`.
