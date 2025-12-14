package com.vibe.structures;

import java.util.LinkedList;

// FR-4: ‘Play Next’ Queue (Priority Queue / FIFO)
// While the prompt mentions Priority Queue, "Play Next" is often a simple FIFO queue 
// where added songs play immediately after current. 
// A strict PriorityQueue sorts by 'priority'. 
// We will implement as a FIFO Queue for simplicity unless 'priority' field is needed.
// This wraps a standard structure but exposes Queue operations.
public class PlayQueue<T> {
    private LinkedList<T> list = new LinkedList<>();

    public void enqueue(T item) {
        list.addLast(item);
    }

    public T dequeue() {
        if (isEmpty()) return null;
        return list.removeFirst();
    }

    public T peek() {
        return list.peekFirst();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }
    
    public java.util.List<T> getAll() {
        return new java.util.ArrayList<>(list);
    }
}
