package com.thaumcraftmodern.registry;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.world.menu.ArcaneWorkbenchMenu;
import com.thaumcraftmodern.world.menu.ArcaneSpaMenu;
import com.thaumcraftmodern.world.menu.ArcaneBoreMenu;
import com.thaumcraftmodern.world.menu.AlchemicalFurnaceMenu;
import com.thaumcraftmodern.world.menu.DeconstructionTableMenu;
import com.thaumcraftmodern.world.menu.ResearchTableMenu;
import com.thaumcraftmodern.world.menu.PechMenu;
import com.thaumcraftmodern.world.menu.ThaumatoriumMenu;
import com.thaumcraftmodern.world.menu.HandMirrorMenu;
import com.thaumcraftmodern.world.menu.FocalManipulatorMenu;
import com.thaumcraftmodern.world.menu.GolemMenu;
import com.thaumcraftmodern.world.menu.TravelingTrunkMenu;
import com.thaumcraftmodern.world.menu.FocusPouchMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, ThaumcraftModern.MOD_ID);

    public static final RegistryObject<MenuType<ResearchTableMenu>> RESEARCH_TABLE =
            MENUS.register("research_table", () -> IForgeMenuType.create(ResearchTableMenu::fromNetwork));
    public static final RegistryObject<MenuType<ArcaneWorkbenchMenu>> ARCANE_WORKBENCH =
            MENUS.register(
                    "arcane_workbench",
                    () -> IForgeMenuType.create(ArcaneWorkbenchMenu::fromNetwork)
            );
    public static final RegistryObject<MenuType<ArcaneSpaMenu>> ARCANE_SPA =
            MENUS.register("arcane_spa",
                    () -> IForgeMenuType.create(ArcaneSpaMenu::fromNetwork));
    public static final RegistryObject<MenuType<ArcaneBoreMenu>> ARCANE_BORE =
            MENUS.register("arcane_bore",
                    () -> IForgeMenuType.create(ArcaneBoreMenu::fromNetwork));
    public static final RegistryObject<MenuType<DeconstructionTableMenu>>
            DECONSTRUCTION_TABLE = MENUS.register(
                    "deconstruction_table",
                    () -> IForgeMenuType.create(
                            DeconstructionTableMenu::fromNetwork
                    )
            );
    public static final RegistryObject<MenuType<PechMenu>> PECH =
            MENUS.register(
                    "pech",
                    () -> IForgeMenuType.create(PechMenu::fromNetwork)
            );
    public static final RegistryObject<MenuType<AlchemicalFurnaceMenu>>
            ALCHEMICAL_FURNACE = MENUS.register(
                    "alchemical_furnace",
                    () -> IForgeMenuType.create(
                            AlchemicalFurnaceMenu::fromNetwork
                    )
            );
    public static final RegistryObject<MenuType<ThaumatoriumMenu>> THAUMATORIUM =
            MENUS.register("thaumatorium",
                    () -> IForgeMenuType.create(ThaumatoriumMenu::fromNetwork));
    public static final RegistryObject<MenuType<HandMirrorMenu>> HAND_MIRROR =
            MENUS.register("hand_mirror",
                    () -> IForgeMenuType.create(HandMirrorMenu::fromNetwork));
    public static final RegistryObject<MenuType<FocalManipulatorMenu>> FOCAL_MANIPULATOR =
            MENUS.register("focal_manipulator",
                    () -> IForgeMenuType.create(FocalManipulatorMenu::fromNetwork));
    public static final RegistryObject<MenuType<GolemMenu>> GOLEM =
            MENUS.register("golem", () -> IForgeMenuType.create(GolemMenu::fromNetwork));
    public static final RegistryObject<MenuType<TravelingTrunkMenu>> TRAVELING_TRUNK =
            MENUS.register("traveling_trunk", () -> IForgeMenuType.create(TravelingTrunkMenu::fromNetwork));
    public static final RegistryObject<MenuType<FocusPouchMenu>> FOCUS_POUCH =
            MENUS.register("focus_pouch", () -> IForgeMenuType.create(FocusPouchMenu::fromNetwork));

    private ModMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
