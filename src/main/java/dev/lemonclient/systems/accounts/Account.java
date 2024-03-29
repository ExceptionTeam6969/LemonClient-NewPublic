package dev.lemonclient.systems.accounts;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import dev.lemonclient.mixin.IMinecraftClient;
import dev.lemonclient.mixin.IPlayerSkinProvider;
import dev.lemonclient.utils.misc.ISerializable;
import dev.lemonclient.utils.misc.NbtException;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.report.AbuseReportContext;
import net.minecraft.client.report.ReporterEnvironment;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.util.ProfileKeys;
import net.minecraft.client.util.Session;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.encryption.SignatureVerifier;

import static dev.lemonclient.LemonClient.mc;

public abstract class Account<T extends Account<?>> implements ISerializable<T> {
    protected AccountType type;
    protected String name;

    protected final AccountCache cache;

    protected Account(AccountType type, String name) {
        this.type = type;
        this.name = name;
        this.cache = new AccountCache();
    }

    public abstract boolean fetchInfo();

    public boolean login() {
        YggdrasilAuthenticationService authenticationService = new YggdrasilAuthenticationService(((IMinecraftClient) mc).getProxy());
        applyLoginEnvironment(authenticationService, authenticationService.createMinecraftSessionService());

        return true;
    }

    public String getUsername() {
        if (cache.username.isEmpty()) return name;
        return cache.username;
    }

    public AccountType getType() {
        return type;
    }

    public AccountCache getCache() {
        return cache;
    }

    public static void setSession(Session session) {
        IMinecraftClient mca = (IMinecraftClient) mc;
        mca.setSession(session);
        mc.getSessionProperties().clear();
        UserApiService apiService;
        try {
            apiService = mca.getAuthenticationService().createUserApiService(session.getAccessToken());
        } catch (AuthenticationException e) {
            apiService = UserApiService.OFFLINE;
        }
        mca.setUserApiService(apiService);
        mca.setSocialInteractionsManager(new SocialInteractionsManager(mc, apiService));
        mca.setProfileKeys(ProfileKeys.create(apiService, session, mc.runDirectory.toPath()));
        mca.setAbuseReportContext(AbuseReportContext.create(ReporterEnvironment.ofIntegratedServer(), apiService));
    }

    public static void applyLoginEnvironment(YggdrasilAuthenticationService authService, MinecraftSessionService sessService) {
        IMinecraftClient mca = (IMinecraftClient) mc;
        mca.setAuthenticationService(authService);
        SignatureVerifier.create(authService.getServicesKeySet(), ServicesKeyType.PROFILE_KEY);
        mca.setSessionService(sessService);
        mca.setSkinProvider(new PlayerSkinProvider(mc.getTextureManager(), ((IPlayerSkinProvider) mc.getSkinProvider()).getSkinCache(), sessService));
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putString("type", type.name());
        tag.putString("name", name);
        tag.put("cache", cache.toTag());

        return tag;
    }

    @Override
    public T fromTag(NbtCompound tag) {
        if (!tag.contains("name") || !tag.contains("cache")) throw new NbtException();

        name = tag.getString("name");
        cache.fromTag(tag.getCompound("cache"));

        return (T) this;
    }
}
