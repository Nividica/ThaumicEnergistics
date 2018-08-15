# ThaumicEnergistics
*Because the digital age could use some magic!*

---

### Please note that this mod is currently in development for 1.12.2.
The following versions are required for this mod:
- Thaumcraft 6.1.BETA21
- Applied Energistics 2 rv6-alpha-3

About
---
The aim of this mod is to serve as a bridge between Thaumcraft and Applied Energistics. The primary focus is essentia management, both in storage, transportation, and application.

Thanks go out to AlgorithmX2 for Applied Energistics, Azanor for Thaumcraft, M3gaFr3ak for ExtraCells, and the Forge team.

Localization's thanks to Mrkwtkr, alvin137, puyo061, Wuestengecko, TheVizzy, Adaptivity, Joccob, & Keridos.
Texture thanks to CyanideX.
Special thanks to MKoanga, Keridos & Aquahatsche.

Built for MC 1.7.10

Code Analysis
---
[![](https://sonarcloud.io/images/project_badges/sonarcloud-white.svg)](https://sonarcloud.io/dashboard?id=thaumicenergistics%3AThaumicEnergistics)

![](https://sonarcloud.io/api/project_badges/measure?project=thaumicenergistics%3AThaumicEnergistics&metric=sqale_rating) ![](https://sonarcloud.io/api/project_badges/measure?project=thaumicenergistics%3AThaumicEnergistics&metric=reliability_rating) ![](https://sonarcloud.io/api/project_badges/measure?project=thaumicenergistics%3AThaumicEnergistics&metric=security_rating)

![](https://sonarcloud.io/api/project_badges/measure?project=thaumicenergistics%3AThaumicEnergistics&metric=ncloc) ![](https://sonarcloud.io/api/project_badges/measure?project=thaumicenergistics%3AThaumicEnergistics&metric=bugs) ![](https://sonarcloud.io/api/project_badges/measure?project=thaumicenergistics%3AThaumicEnergistics&metric=vulnerabilities) ![](https://sonarcloud.io/api/project_badges/measure?project=thaumicenergistics%3AThaumicEnergistics&metric=code_smells)

How To Compile
---

1. Get the 1.7.10 versions of [AE2 API](http://ae-mod.info/Downloads/), [CoFHCore API](http://minecraft.curseforge.com/mc-mods/69162-cofhcore/files), and [Thaumcraft API](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/1292130-thaumcraft-4-2-3-5-updated-2015-2-17) [Computer Craft](http://minecraft.curseforge.com/projects/computercraft/files/2269339), [Mekanism](http://aidancbrady.com/mekanism/download/), and [RotaryCraft](https://sites.google.com/site/reikasminecraft/rotarycraft) put it in a new folder "libs/"
2. Run ./gradlew setupDecompWorkspace build
3. Your build shall be in build/libs/

How To Debug
----
1. Do step 1 of compile
2. Add "-Dfml.coreMods.load=thaumicenergistics.fml.ThECore", without quotes, to your run & debug configuration.
In Eclipse, Menu: Run > Run|Debug Configurations > Arguments Tab > VM Arguments.

