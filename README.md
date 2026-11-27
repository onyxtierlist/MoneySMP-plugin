# MoneySMP

A Paper 1.21.11 plugin inspired by Money SMP. Requires Java 21.

## Features

### Economy and trading
1. **Basic economy** — configurable starting balance; `/money`, `/bal`, `/balance`.
2. **Player payments** — `/pay <player> <amount>`.
3. **Admin economy** — `/eco give|take|set <player> <amount>`.
4. **Server shop** — `/shop` opens the configured item shop.
5. **Player chest shops** — `/pshop create <price>` while looking at a chest.
6. **Shop management** — `/pshop price <price>`, `/pshop remove`, `/pshop list`.
7. **Auction house** — `/ah`, `/ah sell <price> [amount]`, `/ah mine`, `/ah cancel <id>`.
8. **Player trading** — `/trade <player>`, `/trade accept`, `/trade offer`, `/trade confirm`, `/trade cancel`.
9. **Auction management** — configurable listing caps and expiry.
10. **Daily rewards** — `/daily claim`, `/daily status`.
11. **Jobs** — `/job join <miner|farmer|hunter|fisher>`, `/job leave`, `/job info`.
12. **Player bounties** — `/bounty place <player> <amount>`, `/bounty list`, `/bounty cancel <id>`.
13. **Player bank** — `/bank balance`, `/bank deposit <amount>`, `/bank withdraw <amount>`.
14. **Balance leaderboard** — `/baltop`.
15. **Item selling** — `/sell hand`, `/sell all`.

### Season and team systems
16. **Six teams and team treasury** — `/team join <team>`, `/team leave`, `/team list`, `/team info [team]`, `/team deposit <amount>`, `/team withdraw <amount>`. The first member becomes leader; only the leader can withdraw.
17. **Tier rankings** — S, A, B, C, D, F; `/tier`, `/tier list`, admin `/tier set <player> <tier>`.
18. **Control points** — `/controlpoint set <name>`, `/controlpoint list`, `/controlpoint status <name>`. Teams capture uncontested zones and earn treasury income.
19. **Gladiator arena** — configure `/gladiator set lobby|spawn1|spawn2`; players use `/gladiator challenge <player>`, `/gladiator accept`, `/gladiator leave`.
20. **Natural disasters** — `/disaster start <storm|earthquake|heatwave|blizzard> [seconds]`, `/disaster stop`, `/disaster status`. These effects do not intentionally destroy terrain.
21. **Random events** — `/randomevent list`, `/randomevent start <event> [seconds]`, `/randomevent stop`, `/randomevent status`.
22. **Capture the Flag** — configure locations with `/ctf set <team> <flag|base>`; use `/ctf start`, `/ctf status`, `/ctf stop`.
23. **Automatic event rotation** — optionally start a random event on a configurable schedule.

24. **Season team points** — `/season points`, `/season top`, admin `/season award <team> <points>`, `/season set <team> <points>`, `/season reset`. Team standings persist in `season-points.yml`.

## Random events implemented

- **Slippery:** temporary speed and jump effects.
- **Healthy:** maximum health increases by 3 hearts.
- **Anonymous:** temporary invisibility.
- **Double Dough:** bounty payouts are doubled while active.
- **Miner's Haven:** ore drops are doubled.
- **Exhausted:** temporary Slowness.
- **Forbidden Fruit:** apple drops from leaves become three golden apples.
- **Vulnerable:** maximum health is set to 7.5 hearts.
- **Ultra Hardcore:** natural saturation regeneration is disabled.
- **Shieldless:** shield interactions are disabled.
- **Shopping Spree:** Hero of the Village is applied for 20 minutes.
- **Stubby Legs:** fall damage is doubled.

The Season 7 reference describes 17 random events, but the currently accessible list names only 16. Audible and Your Eyes Only need client-side voice/camera support, which a server-only Paper plugin cannot enforce. Flimsy Armor, Opposite Day, and the unidentified seventeenth entry are not yet complete. This project does not claim full event parity yet.

## Configuration

- `season-points.yml`: persistent team season standings.

- `starting-balance`: first-join wallet amount.
- `shop.items`: server shop prices.
- `auction.max-listings`, `auction.duration-hours`: auction settings.
- `daily-reward`: daily reward amount.
- `jobs.rewards`: job payout amounts.
- `bounty.minimum-cents`: minimum bounty.
- `sell.prices`: item sale prices.
- `teams.names`: the six team names.
- `control-points.*`: capture time, radius, and team treasury reward.
- `ctf.score-to-win`: CTF score limit.
- `random-events.automatic`: enable scheduled random events.
- `random-events.interval-minutes`, `random-events.duration-seconds`: schedule settings.

## Build

Run `mvn clean package`. Output: `target/money-smp-1.8.0.jar`.
