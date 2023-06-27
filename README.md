# Dataified Paintings

Vanilla compliant, datapack-driven custom paintings.  

Unlike other data-driven mods, this one does not implement its own variant system, but fully re-use the vanilla one. This was made with the intent to be compatible with [Invariable Paintings](https://modrinth.com/mod/invariable-paintings), but neither is required to work with the other.

Multiplayer is not supported at the moment.

### Word of caution
The mod is sufficiently stable for long play sessions, but touches a lot of delicate mechanics. I'm releasing it as an **Alpha** until I'm confident I have ironed out all the fatal edge cases.

Most of the heavy lifting is done during datapack reloads. They happen when using the `/reload` command, but also when joining a world, or creating a new one. If something ever goes wrong, it will probably be at one of these moments.  
Joining worlds should be safe enough, but manually reloading in an already loaded world is a lot more sensitive.


## Features

### Pack format
New variants are defined in separate json files, which simply contains a `width` and `height` properties, measured in block. The variant id is automatically derived from the file's basename and namespace.
```json
{
	"width":  3,
	"height": 2
}
```
Variant files are located in `/data/<namespace>/paintings/`. Textures and title/author are provided the same way as for the vanilla paintings.   Don't forget to add your paintings to the "placeable" tag, otherwise they will only be obtainable through commands.

In principle, textures and language files would need to be provided separately in a resource pack. But you can use [Embedded Assets](https://modrinth.com/mod/embedded_assets) to merge the resource pack into the datapack.  
(The official mod page has yet to port it to a compatible version of minecraft, but I have an up-to-date fork here [on github](https://github.com/Estecka/mc-Embedded-Assets/releases), waiting to be merged.)

The combined pack structure would look something like this:
```
./
├─ pack.mcmeta
├─ assets
│ └─ <namespace>
│     ├─ lang
│     │  └─ en_us.json
│     └─ textures
│        └─ painting
│           ├─ <title1>.png
│           └─ <title2>.png
└─ data
   ├─ minecraft
   │  └─ tags
   │     └─ painting_variant
   │        └─ placeable.json
   └─ <namespace>
      └─ paintings
         ├─ <title1>.json
         └─ <title2>.json
```

### Missing Variants
In vanilla minecraft, a painting bearing an unknown variant id is reversed to the "Kebab" variant.

With Dataified Paintings, a placeholder 1x1 variant will be created, and the paintings will never loose its variant id. This gives you the leisure to make mistakes while developping datapacks, so a broken one will not result in your already existing paintings to transformed.  
This applies to both placed paintings, and painting items.

Be mindful however that increasing an existing variant's size may still result in already placed paintings to be dropped from their wall, should they no longer fit.
