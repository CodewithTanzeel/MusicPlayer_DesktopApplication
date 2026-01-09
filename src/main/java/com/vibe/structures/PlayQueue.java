package com.vibe.structures;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

// FR-4: ‘Play Next’ Queue (Priority Queue / FIFO)
// Enhanced to support flexible reordering and removal for UI management.
public class PlayQueue<T> {
    private LinkedList<T> list = new LinkedList<>();

    public void enqueue(T item) {
        list.addLast(item);
    }
    
    public void addFirst(T item) {
        list.addFirst(item);
    }

    public T dequeue() {
        if (isEmpty()) return null;
        return list.removeFirst();
    }
    
    public T remove(int index) {
        if (index < 0 || index >= list.size()) return null;
        return list.remove(index);
    }
    
    public boolean remove(T item) {
        return list.remove(item);
    }

    public void move(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex >= list.size() || toIndex < 0 || toIndex >= list.size()) {
            return;
        }
        T item = list.remove(fromIndex);
        list.add(toIndex, item);
    }

    public T peek() {
        return list.peekFirst();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }
    
    public void clear() {
        list.clear();
    }
    
    public int size() {
        return list.size();
    }
    
    public List<T> getAll() {
        return new java.util.ArrayList<>(list);
    }
}
