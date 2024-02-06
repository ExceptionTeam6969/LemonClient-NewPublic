package dev.lemonclient.mixininterface;

@FunctionalInterface
public interface IEpic<T, E> {
    E get(T t);
}
