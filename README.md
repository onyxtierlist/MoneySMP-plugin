# MoneySMP

A gradually expanding Money SMP plugin for **Paper 1.21.11**.

## Feature 001 — Basic Economy
- New players receive a configurable starting balance (default: $100.00).
- `/money` displays the player's balance; `/bal` and `/balance` are aliases.
- Balances are stored by UUID in `plugins/MoneySMP/balances.yml`.
- Currency is stored as integer cents to avoid floating-point arithmetic for balances.

## Feature 002 — Player Payments
- `/pay <player> <amount>` sends money to another online player.
- Player-name tab completion is available.
- Payments must be greater than $0.00 and support up to two decimal places.
- Players cannot pay themselves; the sender must have enough funds.
- Both players receive a confirmation message. Transfers are atomic and saved immediately.

## Feature 003 — Admin Economy
- `/eco give <player> <amount>` adds money.
- `/eco take <player> <amount>` removes money, without allowing a negative balance.
- `/eco set <player> <amount>` sets the exact balance; setting to $0.00 is allowed.
- Admin commands currently target online players and include tab completion.
- Requires `moneysmp.admin`, granted to operators by default.
- Changes are saved immediately and the affected player is notified.

## Requirements
- Paper 1.21.11
- Java 21
- Maven

## Build
Run `mvn clean package` from the project root. The plugin JAR is created at `target/money-smp-0.3.0.jar`.
Copy the JAR into your server's `plugins` folder and restart the server.

## Configuration
Edit `plugins/MoneySMP/config.yml`:
```yaml
starting-balance: 100.00
```
This value is only assigned to players who do not already have a saved balance.

## Commands
| Command | Description |
|---|---|
| `/money` | View your balance |
| `/bal`, `/balance` | Aliases for `/money` |
| `/pay <player> <amount>` | Pay an online player |
| `/eco give <player> <amount>` | Add money (operator permission) |
| `/eco take <player> <amount>` | Remove money (operator permission) |
| `/eco set <player> <amount>` | Set balance (operator permission) |

Permissions: `moneysmp.balance` and `moneysmp.pay` default to everyone; `moneysmp.admin` defaults to operators.

## Development approach
Features are added in small milestones. Each milestone should be built and tested on a Paper 1.21.11 server before the next feature is started.
