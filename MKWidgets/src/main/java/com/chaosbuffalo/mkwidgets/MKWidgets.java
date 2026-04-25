package com.chaosbuffalo.mkwidgets;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * MKWidgets is a retained-mode UI framework built on top of Minecraft's screen layer. Instead of treating
 * a screen as a loose collection of independently managed controls, it models UI as a composable widget tree
 * where parent-child relationships drive rendering, hover detection, focus, and input dispatch.
 * <p>
 * Layout is handled by dedicated widget containers such as
 * {@link com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout}, which apply composable constraints for
 * margins, centering, stacking, and sizing relative to the available layout space. At the screen level,
 * {@link com.chaosbuffalo.mkwidgets.client.gui.screens.MKScreen} coordinates top-level widget trees, modal
 * layers, focus traversal, drag state, and screen state transitions.
 * <p>
 * The library predates vanilla Minecraft's newer widget model, but it solves a similar class of UI problems
 * with stronger emphasis on composition: widgets own children, layouts are widgets, and input and rendering
 * both flow through the same tree structure.
 */
@Mod("mkwidgets")
public class MKWidgets {
    /**
     * Shared module logger for diagnostics emitted by MKWidgets screens, layouts, and widgets.
     */
    public static final Logger LOGGER = LogManager.getLogger();

    /**
     * Mod identifier used for resource locations and mod registration.
     */
    public static final String MODID = "mkwidgets";

    /**
     * Constructs the MKWidgets mod entry point.
     *
     * @param modEventBus the NeoForge mod event bus for lifecycle registration
     * @param modContainer the active mod container for this module
     */
    public MKWidgets(IEventBus modEventBus, ModContainer modContainer) {
    }
}
