package de.teamlapen.vampirism.proxy;

import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.api.entity.convertible.BiteableRegistry;
import de.teamlapen.vampirism.client.core.ModBlocksRender;
import de.teamlapen.vampirism.client.core.ModEntitiesRender;
import de.teamlapen.vampirism.client.core.ModItemsRender;
import de.teamlapen.vampirism.client.core.ModKeys;
import de.teamlapen.vampirism.client.gui.VampirismHUDOverlay;
import de.teamlapen.vampirism.client.render.LayerVampireEntity;
import de.teamlapen.vampirism.client.render.LayerVampirePlayerHead;
import de.teamlapen.vampirism.client.render.RenderHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLStateEvent;

import java.util.Map;

/**
 * Clientside Proxy
 */
public class ClientProxy extends CommonProxy {
    private final static String TAG = "ClientProxy";

    @Override
    public void onInitStep(Step step, FMLStateEvent event) {
        super.onInitStep(step, event);
        ModBlocksRender.onInitStep(step, event);
        ModItemsRender.onInitStep(step, event);
        ModEntitiesRender.onInitStep(step, event);
        ModKeys.onInitStep(step, event);
        if (step == Step.INIT) {
            registerSubscriptions();
        } else if (step == Step.POST_INIT) {
            registerVampireEntityOverlays();
        }
    }

    private void registerSubscriptions() {
        MinecraftForge.EVENT_BUS.register(new VampirismHUDOverlay(Minecraft.getMinecraft()));
        MinecraftForge.EVENT_BUS.register(new RenderHandler(Minecraft.getMinecraft()));
    }

    private void registerVampirePlayerHead(RenderManager manager) {
        for (RenderPlayer renderPlayer : manager.getSkinMap().values()) {
            renderPlayer.addLayer(new LayerVampirePlayerHead(renderPlayer));
        }
    }

    private void registerVampireEntityOverlays() {
        RenderManager manager = Minecraft.getMinecraft().getRenderManager();
        registerVampirePlayerHead(manager);
        for (Map.Entry<Class<? extends EntityCreature>, String> entry : BiteableRegistry.getConvertibleOverlay().entrySet()) {
            registerVampireEntityOverlay(manager, entry.getKey(), new ResourceLocation(entry.getValue()));
        }
    }

    private void registerVampireEntityOverlay(RenderManager manager, Class<? extends EntityCreature> clazz, ResourceLocation loc) {
        Render render = manager.getEntityClassRenderObject(clazz);
        if (render == null) {
            VampirismMod.log.e(TAG, "Did not find renderer for %s", clazz);
            return;
        }
        if (!(render instanceof RendererLivingEntity)) {
            VampirismMod.log.e(TAG, "Renderer (%s) for %s does not extend RenderLivingEntity", clazz, render);
            return;
        }
        RendererLivingEntity rendererLiving = (RendererLivingEntity) render;
        rendererLiving.addLayer(new LayerVampireEntity(rendererLiving, loc));
    }


    @Override
    public boolean isPlayerThePlayer(EntityPlayer player) {
        return Minecraft.getMinecraft().thePlayer.equals(player);
    }

    @Override
    public boolean isClientPlayerNull() {
        return Minecraft.getMinecraft().thePlayer==null;
    }
}
