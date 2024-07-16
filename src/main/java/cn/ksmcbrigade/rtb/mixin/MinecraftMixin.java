package cn.ksmcbrigade.rtb.mixin;

import cn.ksmcbrigade.rtb.RandomTitleReborn;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.server.integrated.IntegratedServer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.text.SimpleDateFormat;

@Mixin(MinecraftClient.class)
public abstract class MinecraftMixin {
    @Unique
    private final String randomTitleReborn$randomTitle = RandomTitleReborn.getTitle();

    @Shadow
    public abstract boolean isConnectedToRealms();

    @Shadow @Nullable public abstract ClientPlayNetworkHandler getNetworkHandler();

    @Shadow @Nullable private IntegratedServer server;

    @Shadow @Nullable public abstract ServerInfo getCurrentServerEntry();

    @Shadow
    static MinecraftClient instance;

    @Inject(method = "getWindowTitle", at = @At("HEAD"), cancellable = true)
    private void getWindowTitle(CallbackInfoReturnable<String> ci) {
        if(instance==null) return;
        try {
            String title = RandomTitleReborn.Get("format");

            StringBuilder stringBuilder = new StringBuilder(SharedConstants.getGameVersion().getName());
            stringBuilder.append(" ");

            ClientPlayNetworkHandler clientPlayNetworkHandler = this.getNetworkHandler();

            if (clientPlayNetworkHandler != null && clientPlayNetworkHandler.getConnection().isOpen()) {
                stringBuilder.append(" - ");
                if (this.server != null && !this.server.isRemote()) {
                    stringBuilder.append(I18n.translate("title.singleplayer"));
                } else if (this.isConnectedToRealms()) {
                    stringBuilder.append(I18n.translate("title.multiplayer.realms"));
                } else if (this.server == null && (this.getCurrentServerEntry() == null || !this.getCurrentServerEntry().isLocal())) {
                    stringBuilder.append(I18n.translate("title.multiplayer.other"));
                } else {
                    stringBuilder.append(I18n.translate("title.multiplayer.lan"));
                }

            }
            String date = new SimpleDateFormat(RandomTitleReborn.Get("dateformat")).format((System.currentTimeMillis()));

            title = title.replace("%date%", date);
            title = title.replace("%title%", randomTitleReborn$randomTitle);
            title = title.replace("%version%", stringBuilder.toString());
            title = title.replace("%mod%", String.valueOf(FabricLoader.getInstance().getAllMods().size()));

            ci.setReturnValue(title);
            ci.cancel();
        }
        catch (Exception e){
            RandomTitleReborn.LOGGER.error("Failed in set the window title {}",e.getMessage(),e);
        }
    }
}
