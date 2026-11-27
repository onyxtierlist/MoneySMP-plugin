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
- Place a chest and look directly at it while holding the item you want to sell.
- Run `/pshop create <price>` to register it as your shop.
- Put stock into the chest. Other players right-click the chest to browse its stock.
- Buyers click a listing to purchase one item. The item is removed from chest stock and payment goes to the owner.
- Shop ownership and item prices persist in `plugins/MoneySMP/player-shops.yml`.

## Feature 006 — Shop Management
- `/pshop price <price>` while looking at your shop chest and holding an item changes that item's price.
- `/pshop remove` removes your shop registration but leaves the chest and its contents intact.
- `/pshop list` lists your registered shops.
- Only the owner can open the chest normally; other players receive the shop GUI.
- Items without a configured price are visible but cannot be purchased.

## Requirements
- Paper 1.21.11
- Java 21
- Maven

## Build
Run `mvn clean package`. The plugin JAR is `target/money-smp-0.6.0.jar`. Copy it into the server's `plugins` folder and restart.

## Commands
| Command | Description |
|---|---|
| `/money` | View balance |
| `/pay <player> <amount>` | Pay a player |
| `/eco give|take|set <player> <amount>` | Admin economy |
| `/shop` | Open server shop |
| `/pshop create <price>` | Register the chest you're looking at |
| `/pshop price <price>` | Set price for the item you're holding |
| `/pshop remove` | Remove your shop |
| `/pshop list` | List your shops |

For player shops, the price is per item and uses dollars with up to two decimal places. Owners stock the chest manually.
