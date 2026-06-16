# Electrical Age: Re-Wired — Unofficial Build

> ### ⚡ Unofficial community build `2.0.0-unofficial-a`
> This is an **unofficial** continuation build of [Electrical Age: Re-Wired](https://github.com/brambora69123/electrical-age-rewired)
> (the 1.12.2 port by **brambora69123**, which itself ports the original [Electrical Age](https://github.com/Electrical-Age/ElectricalAge)).
> The upstream port stalled at roughly 90%; this build picks up the loose ends so the mod is actually playable end‑to‑end.
>
> **This build was completed with [Claude](https://claude.com/claude-code) (Anthropic), working with [@crunkazcanbe](https://github.com/crunkazcanbe).**
> It is **not** affiliated with or endorsed by the original Electrical Age team or brambora69123 — all original authors retain full credit (see below). Same name, same parts; just finished and labeled *unofficial build a*.
>
> Released for the community to play and **report bugs** so they can be fixed. Use at your own risk; back up your worlds.

## What this unofficial build fixes

- **Missing crafting recipes added** — the upstream port shipped with almost no recipes registered (nearly nothing was craftable in survival). ~220+ recipes are now wired up, including generators, turbines, shafts, and the portable tools.
- **Missing/broken textures fixed** — windmill, ferromagnetic cores, ore scanner, brush, portable drill/axe, transformer, downlink, and more were ported from the original 1.7.10 mod or recreated to match.
- **The big "magenta squares" bug fixed** — the electrical tools, portable items (flashlights, batteries, capacitors, mining drill/axe, X‑Ray scanner) and fuel burners were never registered for rendering and showed as missing‑texture blocks. They now render correctly.
- **Item names fixed** — items that showed raw translation keys (e.g. `tile.50v_generator`) now display their proper names.
- **Crash on world close fixed** — the game no longer hangs on "Shutting down internal server" (an unhandled error during eln's shutdown).
- Internal placeholder items (ghost, flubber, node containers) hidden from the item list.
- **JEI / HadEnoughItems integration** — eln machines (Macerator, Compressor, Plate Machine, Magnetizer) now show their processing recipes in the item viewer; right-click a machine to see what it makes.

---

Electrical Age is a Minecraft Mod offering the ability to perform large-scale in-game electrical simulations.

## How to get started

**Minecraft 1.12.2 only. Forge or CleanroomMC needed (works on both).**
<br>
We recommend [CleanroomMC](https://cleanroommc.com/) over Forge — it includes many improvements, allows newer Java (21–25), and uses LWJGL3 (better input handling, performance, etc.).

### Dependencies

**Required:**
- [CoFH Core](https://www.curseforge.com/minecraft/mc-mods/cofh-core) — required for energy integration

**Optional:**
- [Hwyla/WAILA](https://www.curseforge.com/minecraft/mc-mods/hwyla) — tooltip information
- [ComputerCraft](https://www.curseforge.com/minecraft/mc-mods/computercraft) — computer integration
- [OpenComputers](https://www.curseforge.com/minecraft/mc-mods/opencomputers) — computer integration

1. Download the latest release from the [Releases page](https://github.com/crunkazcanbe/electrical-age-rewired/releases).
2. Install CoFH Core (and any optional mods you want).
3. Launch with your favorite launcher. PrismLauncher highly recommended.

### Building from source

You need Git and a JDK. This build's Gradle (RetroFuturaGradle) requires **JDK 25**.

```sh
git clone https://github.com/crunkazcanbe/electrical-age-rewired.git
cd electrical-age-rewired
JAVA_HOME=/path/to/jdk-25 ./gradlew build
# output: build/libs/eln-2.0.0-unofficial-a.jar
```

## ABOUT

Here are some highlighted features:

**A better simulation** — electrical simulation with resistive and capacitive effects; behavior similar to real-life objects.

**Multiple electrical machines and components** — furnaces, solar panels, wind turbines, batteries, capacitors, ...

**Break the cube** — cables, sensors, actuators, alarms, etc. can be placed on each face (outer and inner) of a cube, greatly reducing the space consumed by electrical installations.

**Night-lighting revisited** — lamps, switches, captors, ...

**Small and big electrical consumers** — from lamps and electrical furnaces to miners and transporters...

**Incredible tools** — X-Ray scanner, flashlight, portable mining drill...

**Interoperability** — old redstone circuits can be exploited with electrical ↔ redstone converters.

**Game lifetime/complexity extended** — a substantial list of new raw materials and items...

## CURRENT STATE

Electrical Age: Re-Wired is still **pre-alpha**. Use at your own risk and make map backups regularly. This unofficial build aims to make the existing content actually usable; expect rough edges and please file issues.

## CREDITS

This unofficial `2.0.0-unofficial-a` build was completed with **Claude (Anthropic)** working with **[@crunkazcanbe](https://github.com/crunkazcanbe)**.

**Re-Wired 1.12.2 port:**
- **brambora69123** (mod porting)

**Original Electrical Age team:**
- **Dolu1990** (code guru, concepts, some 3D models)
- **Svein Ove Aas, aka. Baughn** (code, some 3D models, concepts)
- **cm0x4D** (sound engineer, code and 3D models/texturing, concepts)
- **lambdaShade** (3D models/texturing/graphics, concepts, some sounds and code)
- **metc** (website/wiki webmaster)

**Contributors (code/models):** bloxgate, DrummerMC, ltouroumov, meelock, Sukasa, DrummingFish, lolmegaxde1.
**Translations:** bomdia (it_IT), KLsz/aneBlack/Ahtsm (zh_CN), dcbrwn (ru_RU), XxCoolGamesxX (es_ES).

Full upstream contributor list: <https://github.com/brambora69123/electrical-age-rewired/graphs/contributors>

## LICENSE

The source code of this mod is licensed under the **LGPL v3.0**. See <http://www.gnu.org/copyleft/lesser.html>.

Most graphics and all 3D models are licensed under the **Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License** (<http://creativecommons.org/licenses/by-nc-sa/3.0/>), attributed to the Electrical Age team, with these exceptions:

- `src/main/resources/assets/eln/textures/blocks/2x3solarpanel.png` — designed by [Luis Prado](https://thenounproject.com/Luis/).
- `src/main/resources/assets/eln/textures/blocks/scanner.png` — designed by [Creative Stall](https://thenounproject.com/creativestall/).
- `src/main/resources/assets/eln/textures/items/` — designed by Guillermo Guso from the Noun Project.

Some graphics are public domain:
- `src/main/resources/assets/eln/textures/blocks/smallsolarpanel.png`
- `src/main/resources/assets/eln/textures/blocks/smallrotatingsolarpanel.png`
- `src/main/resources/assets/eln/textures/blocks/2x3rotatingsolarpanel.png`
