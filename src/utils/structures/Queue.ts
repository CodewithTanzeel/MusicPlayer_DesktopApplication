import { DoublyLinkedList } from './DoublyLinkedList';

// FR-4: ‘Play Next’ Queue (FIFO)
// Implemented using the DoublyLinkedList for O(1) operations.
export class Queue<T> {
  private list = new DoublyLinkedList<T>();

  enqueue(item: T) {
    this.list.append(item);
  }

  dequeue(): T | undefined {
    if (!this.list.head) return undefined;
    const value = this.list.head.value;
    this.list.remove(this.list.head);
    return value;
  }

  peek(): T | undefined {
    return this.list.head?.value;
  }

  isEmpty(): boolean {
    return this.list.length === 0;
  }

  toArray(): T[] {
    return this.list.toArray();
  }
}
