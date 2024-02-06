package dev.lemonclient.systems.accounts;

import dev.lemonclient.systems.System;
import dev.lemonclient.systems.Systems;
import dev.lemonclient.systems.accounts.types.CrackedAccount;
import dev.lemonclient.systems.accounts.types.EasyMCAccount;
import dev.lemonclient.systems.accounts.types.MicrosoftAccount;
import dev.lemonclient.systems.accounts.types.TheAlteningAccount;
import dev.lemonclient.utils.misc.NbtException;
import dev.lemonclient.utils.misc.NbtUtils;
import dev.lemonclient.utils.network.Executor;
import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Accounts extends System<Accounts> implements Iterable<Account<?>> {
    private List<Account<?>> accounts = new ArrayList<>();

    public Accounts() {
        super("accounts");
    }

    public static Accounts get() {
        return Systems.get(Accounts.class);
    }

    public void add(Account<?> account) {
        accounts.add(account);
        save();
    }

    public boolean exists(Account<?> account) {
        return accounts.contains(account);
    }

    public void remove(Account<?> account) {
        if (accounts.remove(account)) {
            save();
        }
    }

    public int size() {
        return accounts.size();
    }

    @Override
    public Iterator<Account<?>> iterator() {
        return accounts.iterator();
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.put("accounts", NbtUtils.listToTag(accounts));

        return tag;
    }

    @Override
    public Accounts fromTag(NbtCompound tag) {
        Executor.execute(() -> accounts = NbtUtils.listFromTag(tag.getList("accounts", 10), tag1 -> {
            NbtCompound t = (NbtCompound) tag1;
            if (!t.contains("type")) return null;

            AccountType type = AccountType.valueOf(t.getString("type"));

            try {
                Account<?> account = switch (type) {
                    case Cracked -> new CrackedAccount(null).fromTag(t);
                    case Microsoft -> new MicrosoftAccount(null).fromTag(t);
                    case TheAltening -> new TheAlteningAccount(null).fromTag(t);
                    case EasyMC -> new EasyMCAccount(null).fromTag(t);
                };

                if (account.fetchInfo()) return account;
            } catch (NbtException e) {
                return null;
            }

            return null;
        }));

        return this;
    }
}
