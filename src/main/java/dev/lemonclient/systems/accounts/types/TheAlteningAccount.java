package dev.lemonclient.systems.accounts.types;

import com.mojang.authlib.Agent;
import com.mojang.authlib.Environment;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import dev.lemonclient.LemonClient;
import dev.lemonclient.mixin.IMinecraftClient;
import dev.lemonclient.mixin.IYggdrasilMinecraftSessionService;
import dev.lemonclient.systems.accounts.Account;
import dev.lemonclient.systems.accounts.AccountType;
import net.minecraft.client.util.Session;

import java.util.Optional;

import static dev.lemonclient.LemonClient.mc;

public class TheAlteningAccount extends Account<TheAlteningAccount> {
    private static final Environment ENVIRONMENT = Environment.create("http://authserver.thealtening.com", "https://api.mojang.com", "http://sessionserver.thealtening.com", "https://api.minecraftservices.com", "The Altening");
    private static final YggdrasilAuthenticationService SERVICE = new YggdrasilAuthenticationService(((IMinecraftClient) mc).getProxy(), "", ENVIRONMENT);

    public TheAlteningAccount(String token) {
        super(AccountType.TheAltening, token);
    }

    @Override
    public boolean fetchInfo() {
        YggdrasilUserAuthentication auth = getAuth();

        try {
            auth.logIn();

            cache.username = auth.getSelectedProfile().getName();
            cache.uuid = auth.getSelectedProfile().getId().toString();

            return true;
        } catch (AuthenticationException e) {
            return false;
        }
    }

    @Override
    public boolean login() {
        applyLoginEnvironment(SERVICE, IYggdrasilMinecraftSessionService.createYggdrasilMinecraftSessionService(SERVICE, ENVIRONMENT));

        YggdrasilUserAuthentication auth = getAuth();

        try {
            auth.logIn();
            setSession(new Session(auth.getSelectedProfile().getName(), auth.getSelectedProfile().getId().toString(), auth.getAuthenticatedToken(), Optional.empty(), Optional.empty(), Session.AccountType.MOJANG));

            cache.username = auth.getSelectedProfile().getName();
            cache.loadHead();

            return true;
        } catch (AuthenticationException e) {
            LemonClient.LOG.error("Failed to login with TheAltening.");
            return false;
        }
    }

    private YggdrasilUserAuthentication getAuth() {
        YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) SERVICE.createUserAuthentication(Agent.MINECRAFT);

        auth.setUsername(name);
        auth.setPassword("Lemon Client");

        return auth;
    }
}
