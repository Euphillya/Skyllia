# SkylliaBackup

**SkylliaBackup** is an addon for the [Skyllia](https://github.com/Euphyllia/Skyllia) skyblock plugin. It allows
automatic and on-demand backups of skyblock islands: each backup packages the relevant Minecraft region files (`.mca`)
into a ZIP archive, with optional rotation and HTTP upload support.

---

## Requirements

| Dependency    | Version |
|---------------|---------|
| Paper / Folia | 1.20.6+ |
| Java          | 21+     |
| Skyllia       | Latest  |

---

## Features

- Back up any island's region files into a structured **ZIP archive**
- Players can trigger a backup of their own island via `/is backup`
- Admins can back up a specific player's island or **all islands at once** via `/isadmin backup`
- Configurable **cooldown** between player-triggered backups
- Automatic **backup rotation** — keeps only the N most recent backups per island
- Optional **HTTP upload** of backup ZIPs to an external endpoint (with Bearer token support)
- **Folia compatible** — all async work runs on Folia's async scheduler
- Config is registered in Skyllia's config registry and supports **hot-reload**

---

## How It Works

### Backup Process

When a backup is triggered (by a player or an admin), the following steps occur:

1. The island's registered Skyllia worlds are retrieved.
2. Region files (`.mca`) covering the island's footprint are collected from each world's `region/` folder.
3. A ZIP archive is created with the following internal structure:
   ```
   <islandId>/
     backup-info.txt          ← Island ID, timestamp, file count
     <worldName>/region/r.X.Z.mca
     ...
   ```
4. The ZIP is saved under `<backupFolder>/<islandId>/backup_<owner>_<trigger>_<timestamp>.zip`.
5. Old backups beyond the configured maximum are **deleted** (oldest first).
6. If an upload URL is configured, the ZIP is **uploaded asynchronously** via multipart HTTP POST.

### Backup Rotation

Old backups are automatically pruned when the number of ZIPs in an island's folder exceeds `max-per-island`. The oldest
files (by last-modified date) are deleted first.

### Upload

If `upload.url` is set, each backup ZIP is uploaded to that endpoint as a `multipart/form-data` POST with two fields:

| Field       | Content                          |
|-------------|----------------------------------|
| `island_id` | The island's UUID                |
| `file`      | The ZIP file (`application/zip`) |

An `Authorization: Bearer <token>` header is added when `upload.token` is set.

---

## Commands

### Player Command

```
/is backup
```

Creates a backup of the player's own island. Subject to cooldown.

| Permission              | Description               |
|-------------------------|---------------------------|
| `skyllia.island.backup` | Allows using `/is backup` |

### Admin Command

```
/isadmin backup <player|all>
```

- `<player>` — backs up the specified player's island immediately.
- `all` — backs up every active island on the server.

| Permission                       | Description                    |
|----------------------------------|--------------------------------|
| `skyllia.admins.commands.backup` | Allows using `/isadmin backup` |

---

## Configuration

The config file is generated automatically at `plugins/SkylliaBackup/config.toml` on first run.

```toml
[backup]
# Directory where backup ZIPs are stored (absolute or relative to server root)
# Example for a NAS mount: "/mnt/nas/minecraft-backups"
folder = "plugins/SkylliaBackup/backups"

# Maximum number of backups kept per island (0 = unlimited)
max-per-island = 5

# Allow players to create a backup of their own island via /is backup
allow-player-backup = true

# Cooldown in seconds between two player-triggered backups (0 = disabled)
# 3600 = 1 hour
player-cooldown-seconds = 3600

[upload]
# HTTP endpoint to upload backup ZIPs to (leave empty to disable)
# Example: "http://my-server:3000/upload"
url = ""

# Bearer token sent in the Authorization header (leave empty if none)
token = ""
```

### Config Reference

| Key                              | Type    | Default                           | Description                                            |
|----------------------------------|---------|-----------------------------------|--------------------------------------------------------|
| `backup.folder`                  | string  | `"plugins/SkylliaBackup/backups"` | Directory where ZIP archives are stored                |
| `backup.max-per-island`          | int     | `5`                               | Max backups kept per island (`0` = unlimited)          |
| `backup.allow-player-backup`     | boolean | `true`                            | Whether players can trigger their own backup           |
| `backup.player-cooldown-seconds` | long    | `3600`                            | Cooldown in seconds between player backups (`0` = off) |
| `upload.url`                     | string  | `""`                              | HTTP endpoint to upload ZIPs to (empty = disabled)     |
| `upload.token`                   | string  | `""`                              | Bearer token for the upload endpoint                   |

---

## Installation

1. Make sure **Skyllia** is installed and enabled.
2. Drop the `SkylliaBackup.jar` into your `plugins/` folder.
3. Start or reload the server — the config is generated automatically.
4. Edit `plugins/SkylliaBackup/config.toml` to your liking.
5. Use Skyllia's reload command to apply config changes without restarting.

---

## Language Keys

The following keys must be present in your Skyllia language file:

| Key                                | Description                                                        |
|------------------------------------|--------------------------------------------------------------------|
| `addons.backup.player.player-only` | Shown when a non-player runs `/is backup`                          |
| `addons.backup.player.disabled`    | Shown when player backups are disabled                             |
| `addons.backup.player.cooldown`    | Cooldown message (placeholders: `%minutes%`, `%seconds%`)          |
| `addons.backup.player.started`     | Shown when the backup starts                                       |
| `addons.backup.player.success`     | Shown on success (placeholder: `%file%`)                           |
| `addons.backup.player.failed`      | Shown on failure                                                   |
| `addons.backup.player.uploading`   | Shown when upload is in progress                                   |
| `addons.backup.admin.usage`        | Usage hint for `/isadmin backup`                                   |
| `addons.backup.admin.all-started`  | Shown when a full backup is starting                               |
| `addons.backup.admin.all-done`     | Shown when full backup finishes (placeholder: `%count%`)           |
| `addons.backup.admin.started`      | Shown when a single-island backup starts (placeholder: `%player%`) |
| `addons.backup.admin.success`      | Shown on success (placeholders: `%player%`, `%file%`)              |
| `addons.backup.admin.failed`       | Shown on failure (placeholder: `%player%`)                         |