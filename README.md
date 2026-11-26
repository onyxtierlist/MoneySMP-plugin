# MoneySMP

A gradually expanding Money SMP plugin for **Paper 1.21.11**.

## Feature 001 — Basic Economy

- New players receive a configurable starting balance (default: $100.00).
- `/money` displays the player's balance.
- `/bal` and `/balance` are aliases.
- Balances are stored by UUID in `plugins/MoneySMP/balances.yml`.
- Currency is stored as integer cents to avoid floating-point arithmetic for balances.

## Requirements

- Paper 1.21.11
- Java 21
- Maven

## Build

Run from the project root:

```bash
mvn clean package
```

The plugin JAR will be at `target/money-smp-0.1.0.jar`.

Copy the JAR into your server's `plugins` folder and restart the server.

## Configuration

Edit `plugins/MoneySMP/config.yml`:

```yaml
starting-balance: 100.00
```

This value is only assigned to players who do not already have a saved balance. Changing it does not overwrite existing balances.

## Commands

| Command | Description |
|---|---|
| `/money` | View your balance |
| `/bal` | Alias |
| `/balance` | Alias |

Permission: `moneysmp.balance` (default: everyone).

## Development approach

Features will be added in small milestones. Each milestone should be built and tested on a Paper 1.21.11 server before the next feature is started.
