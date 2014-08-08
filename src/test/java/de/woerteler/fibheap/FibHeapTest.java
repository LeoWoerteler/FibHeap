package de.woerteler.fibheap;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import de.woerteler.fibheap.FibHeap.FibNode;

public class FibHeapTest {
  private static final Comparator<Integer> INT_COMP =
      new Comparator<Integer>() {
        @Override
        public int compare(final Integer o1, final Integer o2) {
          return o1 < o2 ? -1 : o1 == o2 ? 0 : 1;
        }
      };

  @Test
  public void sortTest() {
    final FibHeap<String, Integer> heap = new FibHeap<>(INT_COMP);

    final List<Integer> rand = new ArrayList<>(1000);
    for(int i = 0; i < 1000; i++) {
      rand.add(i);
    }
    Collections.shuffle(rand);

    for(final int i : rand) {
      heap.insert("v" + i, i);
    }

    for(int i = 0; i < 1000; i++) {
      assertFalse(heap.isEmpty());
      assertEquals("v" + i, heap.extractMin());
    }
    assertTrue(heap.isEmpty());
  }

  @Test
  public void cascadeRoot() {
    final FibHeap<String, Integer> heap = new FibHeap<>(INT_COMP);
    final List<FibNode<String, Integer>> nodes = new ArrayList<>();
    for(int i = 0; i <= 4; i++) {
      nodes.add(heap.insert("v" + i, i));
    }

    assertEquals("v0", heap.extractMin());
    final FibNode<String, Integer> v2 = nodes.get(2);
    assertTrue(v2.isValid());
    v2.decreaseKey(0);
    assertEquals("v2", heap.extractMin());
    assertFalse(v2.isValid());
    assertEquals("v2", v2.getValue());
    nodes.get(3).decreaseKey(0);
    assertEquals("v3", heap.extractMin());
    assertEquals("v1", heap.extractMin());
    assertEquals("v4", heap.extractMin());
    assertTrue(heap.isEmpty());
  }

  @Test
  public void cascadingCut() {
    final FibHeap<String, Integer> heap = new FibHeap<>(INT_COMP);
    final List<FibNode<String, Integer>> nodes = new ArrayList<>();
    for(int i = 0; i < 9; i++) {
      nodes.add(heap.insert("v" + i, i));
    }

    assertEquals("v0", heap.extractMin());
    nodes.get(2).decreaseKey(0);
    assertEquals("v2", heap.extractMin());
    nodes.get(6).decreaseKey(0);
    assertEquals("v6", heap.extractMin());
    nodes.get(8).decreaseKey(0);
    assertEquals("v8", heap.extractMin());

    nodes.get(7).decreaseKey(0);

    for(int i : new int[] {7, 1, 3, 4, 5}) {
      assertEquals("v" + i, heap.extractMin());
    }
    assertTrue(heap.isEmpty());
  }

  @Test
  public void deleteNotFirst() {
    final FibHeap<String, Integer> heap = new FibHeap<>(INT_COMP);
    final List<FibNode<String, Integer>> nodes = new ArrayList<>();
    for(int i = 0; i < 9; i++) {
      nodes.add(heap.insert("v" + i, i));
    }

    assertEquals("v0", heap.extractMin());
    nodes.get(7).decreaseKey(0);

    for(int i : new int[] {7, 1, 2, 3, 4, 5, 6, 8}) {
      assertEquals("v" + i, heap.extractMin());
    }
    assertTrue(heap.isEmpty());
  }

  @Test
  public void decreaseMid() {
    final FibHeap<String, Integer> heap = new FibHeap<>(INT_COMP);
    @SuppressWarnings({"unchecked"})
    final FibNode<String, Integer>[] v = new FibNode[]{
        heap.insert("v0", 0),
        heap.insert("v1", 2),
        heap.insert("v2", 4),
        heap.insert("v3", 6)
    };

    v[2].decreaseKey(1);
    assertEquals("v0", heap.extractMin());
    System.out.println(heap);
    v[3].decreaseKey(5);
    for(int i : new int[] { 2, 1, 3 }) {
      assertEquals("v" + i, heap.extractMin());
    }
    assertTrue(heap.isEmpty());
  }

  @Test
  public void errorTest() {
    final FibHeap<String, Integer> heap = new FibHeap<>(INT_COMP);
    final FibNode<String, Integer> v0 = heap.insert("v0", 0);
    try {
      v0.decreaseKey(1);
      fail();
    } catch(final IllegalArgumentException e) {
      // expected
    }

    assertEquals("v0", heap.extractMin());
    assertFalse(v0.isValid());
    try {
      v0.decreaseKey(-1);
      fail();
    } catch(final IllegalArgumentException e) {
      // expected
    }
  }
}
