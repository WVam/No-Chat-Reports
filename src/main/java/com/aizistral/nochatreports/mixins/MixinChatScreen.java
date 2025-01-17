package com.aizistral.nochatreports.mixins;

import java.util.List;
import java.util.stream.Collectors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.aizistral.nochatreports.core.NoReportsConfig;
import com.aizistral.nochatreports.core.ServerSafetyState;
import com.aizistral.nochatreports.core.ServerSafetyLevel;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.OptionInstance.TooltipSupplierFactory;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen extends MixinScreen {
	private static final ResourceLocation CHAT_STATUS_ICONS = new ResourceLocation("nochatreports", "textures/gui/chat_status_icons.png");

	@Inject(method = "init", at = @At("HEAD"))
	private void onInit(CallbackInfo info) {
		if (!NoReportsConfig.showServerSafety())
			return;

		ServerSafetyLevel trust = this.minecraft.isLocalServer() ? ServerSafetyLevel.SECURE : ServerSafetyState.getCurrent();

		var button = new ImageButton(this.width - 23, this.height - 37, 20, 20, this.getXOffset(trust),
				0, 20, CHAT_STATUS_ICONS, 64, 64, btn -> {}, (btn, poseStack, i, j) ->
				this.renderTooltipNoGap(poseStack, this.minecraft.font.split(trust.getTooltip(), 250), i, j),
				Component.translatable("gui.socialInteractions.report"));
		button.active = false;
		button.visible = true;
		this.addRenderableOnly(button);
	}

	private int getXOffset(ServerSafetyLevel level) {
		return switch (level) {
		case SECURE -> 21;
		case UNINTRUSIVE, UNKNOWN -> 42;
		case INSECURE -> 0;
		};
	}

}
