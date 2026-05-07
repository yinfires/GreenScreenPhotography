# GreenScreenPhotography

GreenScreenPhotography is a NeoForge mod for Minecraft content creation, built to provide a clean and controllable green-screen shooting workflow. It focuses on building the scene, locking the camera, and exporting usable footage for compositing, video editing, and showcase work.

## Features

- Green screen block: build a bright chroma background quickly for keying and compositing.
- Free camera: frame shots from an independent camera without moving the player body.
- World freeze: hold local motion steady while you capture a clean pose or scene.
- Transparent screenshot export: export a transparent PNG and matching matte image in one step.
- Creative tab: keep the mod's content grouped in its own creative-mode category for quick access.

## Controls

Default bindings, all of which can be changed in Minecraft key settings:

- `N`: toggle free camera
- `P`: freeze or restore the camera world
- `H`: export transparent screenshot

Transparent screenshots are written to the `green_screen_photography` folder under the current game version directory. Each export produces a transparent PNG and a matching matte image.

## Compositing Tips

When building a capture area, let the green-screen blocks cover the area behind the subject and the edge regions around it. Avoid large areas of saturated green on the subject itself. A good workflow is to freeze the world, refine the angle with free camera, and then export the transparent screenshot for use in an image editor or video editor.

## Compatibility

- Minecraft: 1.21.1
- NeoForge: 21.1.228 or newer in the 21.1.x line
- Java: 21

## Development

Common development commands:

```powershell
.\gradlew.bat runClient
.\gradlew.bat runServer
.\gradlew.bat build
```

Build outputs are written to `build/libs/`.
