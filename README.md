# OpenAL-MC
Mod to use most OpenAL drivers with MC

# Features
- Reuse OpenAL buffer on streaming sources
- Remove need of AL_EXT_source_distance_model
- Allow LWJGL to work with OpenAL router
- Add driver quirk to LWJGL for Creative's hardware OpenAL
- Allow using all available sources for static sources, priorizing streaming sources
- Selection of device, frequency, and number of EFX auxiliary sends

# Dependencies
- GrossFabricHacks (to allow one to mixin into LWJGL)
- ModMenu
