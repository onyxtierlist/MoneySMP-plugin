# MoneySMP

A gradually expanding Money SMP plugin for **Paper 1.21.11**.

## Feature 001 — Basic Economy
- New players receive a configurable starting balance (default: $100.00).
- `/money` displays the player's balance; `/bal` and `/balance` are aliases.
- Balances are stored by UUID in `plugins/MoneySMP/balances.yml`.
- Currency is stored as integer cents.

## Feature 002 — Player Payments
- `/pay <player> <amount>` pays an online player.
- Supports up to two decimal places, blocks self-payments and insufficient funds.

## Feature 003 — Admin Economy
- `/eco give <player> <amount>`, `/eco take <player> <amount>`, and `/eco set <player> <amount>`.
- Operator-only by default via `moneysmp.admin`.

## Feature 004 — Server Item Shop
- `/shop` opens the configurable server shop.
- Click to buy one item; affordability and inventory space are checked.

## Feature 005 — Player-Owned Chest Shops
- Look at a chest while holding the item you want to sell and run `/pshop create <price>`.
- Stock the chest manually. Other players right-click it to browse stock and buy one item.
- Shop ownership and item prices persist in `plugins/MoneySMP/player-shops.yml`.

## Feature 006 — Shop Management
- `/pshop price <price>` changes the price for the held item at your shop chest.
- `/pshop remove` unregisters your shop without deleting its contents.
- `/pshop list` lists your registered shops.
- Registered shop chests are protected from breaking until removed.

## Feature 007 — Auction House
- `/ah` opens a GUI of active player listings.
- `/ah sell <price> [amount]` lists the held item stack (price is the total listing price).
- Clicking a listing purchases the entire listed stack.
- Auction data and item stacks persist in `plugins/MoneySMP/auctions.yml`.

## Feature 008 — Player Trading
- `/trade <player>` sends a trade request; the other player uses `/trade accept`.
- Each player holds the item stack they want to exchange and uses `/trade offer`.
- Both players use `/trade confirm` to complete the exchange.
- `/trade cancel` returns escrowed items. Disconnecting cancels the active trade.

## Feature 009 — Auction Management
- `/ah mine` lists your active auctions.
- `/ah cancel <id>` cancels your own listing and returns its item.
- Listings expire after the configured duration (default: 48 hours).
- The per-player listing limit is configurable (default: 5).
- Settings are in `plugins/MoneySMP/config.yml` under `auction`.

## Commands
| Command | Description |
|---|---|
| `/money` | View balance |
| `/pay <player> <amount>` | Pay an online player |
| `/eco give|take|set <player> <amount>` | Admin economy |
| `/shop` | Open server shop |
| `/pshop create <price>` | Register the chest you're looking at |
| `/pshop price <price>` | Set price for the held item |
| `/pshop remove` | Remove your shop |
| `/pshop list` | List your shops |
| `/ah` | Open auction house |
| `/ah sell <price> [amount]` | List held items |
| `/ah mine` | View your listings |
| `/ah cancel <id>` | Cancel your listing |
| `/trade <player>` | Request a trade |
| `/trade accept` | Accept a request |
| `/trade offer` | Escrow your held stack |
| `/trade confirm` | Confirm the item exchange |
| `/trade cancel` | Cancel and return escrow |

## Requirements
- Paper 1.21.11
- Java 21
- Maven

## Build
Run `mvn clean package`. The plugin JAR is `target/money-smp-0.9.0.jar`. Copy it into the server's `plugins` folder and restart.
