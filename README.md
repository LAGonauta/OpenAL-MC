# OpenAL-MC
Mod to use most OpenAL drivers with MC

# Features
- Reuse OpenAL buffer on streaming sources
- Remove need of AL_EXT_source_distance_model
- Allow LWJGL to work with OpenAL router
- Add driver quirk to LWJGL for Creative's hardware OpenAL
- Selection of device, frequency, and number of EFX auxiliary sends

# Using
- Install as a Fabric mod as usual
- Add `-Dorg.lwjgl.openal.libname="C:/Windows/System32/OpenAL32.dll"` (or equivalent) as Java argument

# Dependencies
- ModMenu
