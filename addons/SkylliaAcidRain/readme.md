# SkylliaAcidRain

**SkylliaAcidRain** is an addon for the [Skyllia](https://github.com/Euphyllia/Skyllia) skyblock plugin. It adds an *
*acid rain/water** mechanic: any living entity exposed to rain or water on a skyblock island takes periodic damage and
receives debuff effects.

---

## Requirements

| Dependency    | Version |
|---------------|---------|
| Paper / Folia | 1.21.x  |
| Java          | 21+     |
| Skyllia       | Latest  |

---

## Features

- Damages entities (players and/or mobs) when they are in **water** or **rain** within a skyblock world
- Configurable **damage amount** and **damage interval**
- Optional **potion effects**: Poison, Slowness, Nausea (each independently toggleable)
- Optional **particle** and **sound** effects on hit
- Option to restrict the mechanic to **players only** (ignoring mobs)
- Fires a custom **`EntityDamageAcidEvent`** — other addons can listen to or cancel it
- **Folia compatible** — uses entity-local schedulers
- **Hot-reload** supported via the Skyllia config registry

---

## How It Works

### Entity Tracking

When an entity enters a skyblock world, it is registered and a **repeating task** is scheduled on its entity scheduler (
Folia-safe). When the entity leaves the world, the task is cancelled and the entity is untracked.

### Acid Tick

On each tick interval, the following logic runs:

1. Skip if the entity is **dead**, or if it is a player in **Creative / Spectator** mode.
2. Check if the entity is in **water** or **rain**. If neither, skip.
3. Fire the cancellable `EntityDamageAcidEvent`.
4. Apply **damage**.
5. Apply enabled **potion effects** (Poison, Slowness, Nausea).
6. Spawn **particles** (splash + dripping water) if enabled.
7. Play a **sound** if enabled.
8. Send an **action bar message** to the player indicating water or rain contact (uses Skyllia's language system).

### Configuration Reload

When the config file is reloaded (via Skyllia's reload mechanism), all running tasks are **cancelled and restarted**
with the new values.

---

## Configuration

The config file is generated automatically at `plugins/SkylliaAcidRain/config/config.toml` on first run.

```toml
[acid]
damage = 2.0
damage-interval-tick = 20   # 20 ticks = 1 second
particles = true
only-player = false

[acid.sound]
enabled = true
name = "BLOCK_LAVA_AMBIENT"

[acid.effects.poison]
enabled = true
duration = 60
amplifier = 1

[acid.effects.slowness]
enabled = true
duration = 40
amplifier = 0

[acid.effects.nausea]
enabled = true
duration = 80
amplifier = 0
```

### Config Reference

| Key                               | Type    | Default              | Description                                             |
|-----------------------------------|---------|----------------------|---------------------------------------------------------|
| `acid.damage`                     | double  | `2.0`                | Damage dealt per tick (half-hearts)                     |
| `acid.damage-interval-tick`       | long    | `20`                 | Ticks between each damage application                   |
| `acid.particles`                  | boolean | `true`               | Show splash/dripping water particles                    |
| `acid.sound.enabled`              | boolean | `true`               | Play a sound on damage                                  |
| `acid.sound.name`                 | string  | `BLOCK_LAVA_AMBIENT` | Sound key (Bukkit NamespacedKey format)                 |
| `acid.only-player`                | boolean | `false`              | If `true`, only players are affected (mobs are ignored) |
| `acid.effects.poison.enabled`     | boolean | `true`               | Apply Poison effect                                     |
| `acid.effects.poison.duration`    | int     | `60`                 | Duration in ticks                                       |
| `acid.effects.poison.amplifier`   | int     | `1`                  | Effect level (0 = level I)                              |
| `acid.effects.slowness.enabled`   | boolean | `true`               | Apply Slowness effect                                   |
| `acid.effects.slowness.duration`  | int     | `40`                 | Duration in ticks                                       |
| `acid.effects.slowness.amplifier` | int     | `0`                  | Effect level                                            |
| `acid.effects.nausea.enabled`     | boolean | `true`               | Apply Nausea effect                                     |
| `acid.effects.nausea.duration`    | int     | `80`                 | Duration in ticks                                       |
| `acid.effects.nausea.amplifier`   | int     | `0`                  | Effect level                                            |

---

## Developer API

The addon fires a **`EntityDamageAcidEvent`** before applying damage. You can listen to it from another addon to:

- **Cancel** the damage entirely
- **Modify** the damage amount
- Know whether the contact was **water** or **rain**

```java
@EventHandler
public void onAcidDamage(EntityDamageAcidEvent event) {
    if (event.getEntity() instanceof Player player) {
        // Double the damage for players in water
        if (event.isInWater()) {
            event.setDamage(event.getDamage() * 2);
        }
        // Cancel damage for a specific island
        // event.setCancelled(true);
    }
}
```

---

## Installation

1. Make sure **Skyllia** is installed and enabled.
2. Drop the `SkylliaAcidRain.jar` into your `plugins/` folder.
3. Start or reload the server — the config is generated automatically.
4. Edit `plugins/SkylliaAcidRain/config/config.toml` to your liking.
5. Use Skyllia's reload command to apply changes without restarting.

---
