package com.abcew.camera.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by laputan on 16/10/31.
 */
public abstract class CallSet<E> implements Iterable<E> {
    protected final Set<E> hashSet;
    protected final HashMap<String, HashSet<E>> pollStore;

    public CallSet() {
        pollStore = new HashMap<>();
        hashSet = Collections.synchronizedSet(new HashSet<E>());
    }

    protected boolean singleCallIt(String name, E e) {
        HashSet<E> map = pollStore.get(name);
        if (map == null) {
            map = new HashSet<>();
            pollStore.put(name, map);
        }
        return map.add(e); //Return true if not exist,
    }

    public synchronized HashSet<E> getSet() {
        synchronized (hashSet) {
            return new HashSet<>(hashSet);
        }
    }

    public synchronized Iterator<E> iterator() {
        return getSet().iterator();
    }

    public synchronized void add(E e) {
        synchronized (hashSet) {
            hashSet.add(e);
        }
    }

    public synchronized void remove(E e) {
        synchronized (hashSet) {
            hashSet.remove(e);
        }
    }

    public synchronized void clear() {
        synchronized (hashSet) {
            hashSet.clear();
        }
    }

    public boolean isEmpty() {
        return hashSet.size() < 1;
    }
}