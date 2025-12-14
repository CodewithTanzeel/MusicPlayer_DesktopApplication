package com.vibe.structures;

public class DoublyLinkedList<T> {
    
    public static class Node<T> {
        public T value;
        public Node<T> next;
        public Node<T> prev;

        public Node(T value) {
            this.value = value;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public void add(T value) {
        Node<T> newNode = new Node<>(value);
        if (head == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        size++;
    }

    // FR-2: Playlist navigation logic usually requires iterating
    public Node<T> getHead() {
        return head;
    }

    public Node<T> getTail() {
        return tail;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }
    
    public int size() {
        return size;
    }
}
