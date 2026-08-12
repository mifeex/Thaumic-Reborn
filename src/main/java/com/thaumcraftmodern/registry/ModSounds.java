package com.thaumcraftmodern.registry;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ThaumcraftModern.MOD_ID);

    public static final RegistryObject<SoundEvent> CAMERA_TICKS = sound("cameraticks");
    public static final RegistryObject<SoundEvent> CAMERA_CLACK = sound("cameraclack");
    public static final RegistryObject<SoundEvent> PAGE = sound("page");
    public static final RegistryObject<SoundEvent> KEY = sound("key");
    public static final RegistryObject<SoundEvent> LEARN = sound("learn");
    public static final RegistryObject<SoundEvent> WRITE = sound("write");
    public static final RegistryObject<SoundEvent> ERASE = sound("erase");
    public static final RegistryObject<SoundEvent> WHISPERS = sound("whispers");
    public static final RegistryObject<SoundEvent> HEARTBEAT = sound("heartbeat");
    public static final RegistryObject<SoundEvent> UPGRADE = sound("upgrade");
    public static final RegistryObject<SoundEvent> CRYSTAL = sound("crystal");
    public static final RegistryObject<SoundEvent> BUBBLE = sound("bubble");
    public static final RegistryObject<SoundEvent> SPILL = sound("spill");
    public static final RegistryObject<SoundEvent> SQUEEK = sound("squeek");
    public static final RegistryObject<SoundEvent> CREAK = sound("creak");
    public static final RegistryObject<SoundEvent> TOOL = sound("tool");
    public static final RegistryObject<SoundEvent> PUMP = sound("pump");
    public static final RegistryObject<SoundEvent> HH_OFF = sound("hhoff");
    public static final RegistryObject<SoundEvent> HH_ON = sound("hhon");
    public static final RegistryObject<SoundEvent> WAND = sound("wand");
    public static final RegistryObject<SoundEvent> WAND_FAIL = sound("wandfail");
    public static final RegistryObject<SoundEvent> WIND = sound("wind");
    public static final RegistryObject<SoundEvent> SWING = sound("swing");
    public static final RegistryObject<SoundEvent> JAR = sound("jar");
    public static final RegistryObject<SoundEvent> ZAP = sound("zap");
    public static final RegistryObject<SoundEvent> WISP_LIVE = sound("wisplive");
    public static final RegistryObject<SoundEvent> WISP_DEAD = sound("wispdead");
    public static final RegistryObject<SoundEvent> EG_IDLE = sound("egidle");
    public static final RegistryObject<SoundEvent> EG_ATTACK =
            sound("egattack");
    public static final RegistryObject<SoundEvent> EG_DEATH =
            sound("egdeath");
    public static final RegistryObject<SoundEvent> EG_SCREECH =
            sound("egscreech");
    public static final RegistryObject<SoundEvent> JACOBS =
            sound("jacobs");
    public static final RegistryObject<SoundEvent> SHOCK =
            sound("shock");
    public static final RegistryObject<SoundEvent> CULTIST_CHANT =
            sound("chant");
    public static final RegistryObject<SoundEvent> GORE = sound("gore");
    public static final RegistryObject<SoundEvent> ROOTS = sound("roots");
    public static final RegistryObject<SoundEvent> SWARM = sound("swarm");
    public static final RegistryObject<SoundEvent> SWARM_ATTACK =
            sound("swarmattack");
    public static final RegistryObject<SoundEvent> FLY = sound("fly");
    public static final RegistryObject<SoundEvent> PECH_IDLE =
            sound("pech_idle");
    public static final RegistryObject<SoundEvent> PECH_HIT =
            sound("pech_hit");
    public static final RegistryObject<SoundEvent> PECH_DEATH =
            sound("pech_death");
    public static final RegistryObject<SoundEvent> PECH_CHARGE =
            sound("pech_charge");
    public static final RegistryObject<SoundEvent> PECH_TRADE =
            sound("pech_trade");
    public static final RegistryObject<SoundEvent> PECH_DICE =
            sound("pech_dice");
    public static final RegistryObject<SoundEvent> CRAB_TALK =
            sound("crabtalk");
    public static final RegistryObject<SoundEvent> CRAB_DEATH =
            sound("crabdeath");
    public static final RegistryObject<SoundEvent> CRAB_CLAW =
            sound("crabclaw");
    public static final RegistryObject<SoundEvent> WITNESS_IDLE =
            sound("witness_idle");
    public static final RegistryObject<SoundEvent> WITNESS_ALERT =
            sound("witness_alert");
    public static final RegistryObject<SoundEvent> WITNESS_ATTACK =
            sound("witness_attack");
    public static final RegistryObject<SoundEvent> WITNESS_HURT =
            sound("witness_hurt");
    public static final RegistryObject<SoundEvent> WITNESS_DEATH =
            sound("witness_death");
    /*
     * Kept registered for save compatibility with 1.4.1. Research Table
     * gameplay deliberately does not use these events.
     */
    public static final RegistryObject<SoundEvent> CRAFT_SUCCESS = sound("craft_success");
    public static final RegistryObject<SoundEvent> CRAFT_FAIL = sound("craft_fail");
    public static final RegistryObject<SoundEvent> INFUSER = sound("infuser");
    public static final RegistryObject<SoundEvent> INFUSER_START =
            sound("infuser_start");
    public static final RegistryObject<SoundEvent> RUNIC_SHIELD_EFFECT =
            sound("runic_shield_effect");
    public static final RegistryObject<SoundEvent> RUNIC_SHIELD_CHARGE =
            sound("runic_shield_charge");

    private ModSounds() {
    }

    private static RegistryObject<SoundEvent> sound(String name) {
        ResourceLocation id = new ResourceLocation(ThaumcraftModern.MOD_ID, name);
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }
}
