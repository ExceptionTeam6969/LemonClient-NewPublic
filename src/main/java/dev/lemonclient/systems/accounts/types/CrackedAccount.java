package dev.lemonclient.systems.accounts.types;

import dev.lemonclient.systems.accounts.Account;
import dev.lemonclient.systems.accounts.AccountType;
import net.minecraft.client.util.Session;
import net.minecraft.util.Uuids;

import java.util.Optional;

public class CrackedAccount extends Account<CrackedAccount> {
    public CrackedAccount(String name) {
        super(AccountType.Cracked, name);
    }

    @Override
    public boolean fetchInfo() {
        cache.username = name;
        return true;
    }

    @Override
    public boolean login() {
        super.login();

        cache.loadHead();
        setSession(new Session(name, Uuids.getOfflinePlayerUuid(name).toString(), "", Optional.empty(), Optional.empty(), Session.AccountType.MOJANG));
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CrackedAccount)) return false;
        return ((CrackedAccount) o).getUsername().equals(this.getUsername());
    }
}
