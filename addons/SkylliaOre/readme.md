# SkylliaOre Generator Plugin

This plugin manages custom block generators for SkyBlock islands, with support for Oraxen.

## Prerequisites

- [Skyllia](https://github.com/Euphillya/Skyllia)
- [Oraxen](https://oraxen.com/) (optional)
- [Nexo](https://docs.nexomc.com) (optinal)
- [CraftEngine](https://github.com/Xiao-MoMi/craft-engine) (optinal)

## Installation

1. Clone the Skyllia repository and follow the instructions to install Skyllia.
2. (Optional) Ensure Oraxen/Nexo or CraftEngine is installed and configured on your server if you want to use custom
   blocks.
3. Place the `SkylliaOre.jar` file in your server's `plugins` directory.
4. Start your server to generate the default configuration files.

## Configuration

The configuration file `config.yml` should be located in the `plugins/SkylliaOre/` directory. Here is an example
configuration for a generator:

```yml
generator_default: test

generators:
  - name: "test"
    replace_block:
      - cobblestone
      - stone
    world:
      - sky-overworld
    block_chance:
      - block: cobblestone
        chance: 70.0
      - block: oraxen:ore
        chance: 10.0
      - block: nexo:ore
        chance: 10.0
      - block: "craftengine:ore:bcop2"
        chance: 10.0
```

### Configuration Explanation

- **generator_default**: The default generator to be used if no specific generator is found in the database.
- **generators**: A list of generator configurations.
    - **name**: The name of the generator.
    - **replace_block**: The list of blocks to be replaced by the generator.
    - **world**: The list of worlds where the generator is active.
    - **block_chance**: The list of blocks to generate with their respective chances.

### Detailed Example

```yml
generator_default: test

generators:
  - name: "test"
    replace_block:
      - cobblestone
      - stone
    world:
      - sky-overworld
    block_chance:
      - block: cobblestone
        chance: 70.0
      - block: oraxen:ore
        chance: 10.0
      - block: nexo:ore
        chance: 10.0
      - block: "craftengine:ore:bcop2"
        chance: 10.0
```

In this example, the default generator is set to "test". The generator named "test" will replace `cobblestone` and
`stone` blocks in the `sky-overworld` world. It has an 80% chance to generate `cobblestone`, a 10% chance to generate
`oraxen:ore`, a 10% chance to generate `nexo:ore` and 10% chance to generate `ore:bcop2`.

## Usage

### Commands

- `/skylliaadmin generator <player> <generator>`: Change the generator for a specific player.
- `/skylliaadmin generator reload`: Reload the `config.yml` from disk and clear the generator caches. The new generator
  definitions take effect immediately, without restarting the server.

### Note on Generator Change

When changing the generator, there is a delay of approximately 30 seconds before the update takes effect due to caching.

### Permissions

- `skylliaore.use`: Permission to use the `/skylliaadmin generator` command.
- `skylliaore.reload`: Permission to use the `/skylliaadmin generator reload` command.

## Support

For support, please join our [Discord server](https://discord.gg/uUJQEB7XNN).

## Contributing

Contributions are welcome! Please read the [contribution guidelines](../../CONTRIBUTING.md) before submitting a pull
request.

## License

SkylliaInfo is licensed under the [MIT License](../../LICENSE).