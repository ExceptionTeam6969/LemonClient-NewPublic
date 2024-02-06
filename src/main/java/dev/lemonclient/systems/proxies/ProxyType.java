package dev.lemonclient.systems.proxies;

import org.jetbrains.annotations.Nullable;

public enum ProxyType {
    Socks4,
    Socks5;

    @Nullable
    public static ProxyType parse(String group) {
        for (ProxyType type : values()) {
            if (type.name().equalsIgnoreCase(group)) {
                return type;
            }
        }
        return null;
    }
}
