package de.woerteler.fibheap;
import java.util.*;

/**
 * A priority queue implemented as a fibonacci heap.
 * @author Leo Woerteler
 *
 * @param <P> priority type
 * @param <V> value type
 */
public final class FibHeap<V, P> {
  /** Key comparator. */
  private final Comparator<P> comp;
  /** Minimum node, {@code null} if the heap is empty. */
  private FibNode<V, P> min;

  /**
   * Constructor taking a comparator for the keys.
   * @param comp comparator
   */
  public FibHeap(final Comparator<P> comp) {
    this.comp = comp;
  }

  /**
   * Tests if this heap is empty.
   * @return {@code true} if the heap is empty, {@code false} otherwise
   */
  public boolean isEmpty() {
    return this.min == null;
  }

  /**
   * Inserts a new entry into this heap. <em>O(1)</em>
   * @param v value to insert
   * @param k key to insert
   * @return the inserted entry
   */
  public FibNode<V, P> insert(final V v, final P k) {
    final FibNode<V, P> nd = new FibNode<>(this, k, v);
    this.insertIntoRootList(nd);
    if(nd != this.min && this.comp.compare(nd.priority, this.min.priority) < 0) {
      this.min = nd;
    }
    return nd;
  }

  /**
   * Gets the entry currently at the top of this heap. <em>O(1)</em>
   * @return a minimal entry if the queue is non-empty, {@code null} otherwise
   */
  public FibNode<V, P> getMin() {
    return this.min;
  }

  /**
   * Extracts and returns the value with the smallest key from this heap.
   * <em>O(log n)*</em>
   * @return the value if the heap was not empty, {@code null} otherwise
   */
  public V extractMin() {
    final FibNode<V, P> mn = this.min;
    if(mn == null) {
      throw new IllegalStateException("empty heap");
    }

    // remove node from root list
    if(mn.right == mn) {
      this.min = null;
    } else {
      mn.left.right = mn.right;
      mn.right.left = mn.left;
      this.min = mn.right;
    }
    mn.left = mn.right = mn;

    // remove children
    final FibNode<V, P> fst = mn.firstChild;
    mn.firstChild = null;

    if(fst != null) {
      // add children to root list
      FibNode<V, P> curr = fst;
      do {
        final FibNode<V, P> next = curr.right;
        curr.parent = null;
        this.insertIntoRootList(curr);
        curr = next;
      } while(curr != fst);
    }

    // consolidate the root list
    if(!this.isEmpty()) {
      this.consolidate();
    }

    // invalidate and return the root entry
    mn.heap = null;
    return mn.value;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("FibHeap[");
    if(this.min != null) {
      sb.append('\n');
      FibNode<V, P> curr = this.min;
      do {
        toString(curr, sb, 1);
        curr = curr.right;
      } while(curr != this.min);
    }
    return sb.append(']').toString();
  }

  /**
   * Recursive helper for {@link #toString()}.
   * @param node current node
   * @param sb string builder
   * @param indent indentation level
   */
  private void toString(final FibNode<V, P> node, final StringBuilder sb, final int indent) {
    for(int i = 0; i < indent; i++) {
      sb.append("  ");
    }
    sb.append("Node").append(node.lost ? "'" : "").append('#').append(node.degree).append("[\n");
    for(int i = 0; i <= indent; i++) {
      sb.append("  ");
    }
    sb.append('(').append(node.priority).append(", ").append(node.value).append(")");
    if(node.firstChild == null) {
      sb.append("\n");
    } else {
      sb.append(",\n");
      FibNode<V, P> curr = node.firstChild;
      do {
        toString(curr, sb, indent + 1);
        curr = curr.right;
      } while(curr != node.firstChild);
    }
    for(int i = 0; i < indent; i++) {
      sb.append("  ");
    }
    sb.append("]\n");
  }

  /**
   * Inserts the given node into the root list. <em>O(1)</em>
   * @param nd node to insert
   */
  private void insertIntoRootList(final FibNode<V, P> nd) {
    final FibNode<V, P> mn = this.min;
    if(mn == null) {
      nd.left = nd.right = nd;
      this.min = nd;
    } else {
      nd.left = mn;
      nd.right = mn.right;
      mn.right.left = nd;
      mn.right = nd;
    }
  }

  /**
   * Consolidates the root list after a call to {@link #extractMin()}.<br/>
   * <em>O(r)</em> where <em>r</em> is the length of the root list
   */
  private void consolidate() {
    final ArrayList<FibNode<V, P>> degrees = new ArrayList<>();
    final FibNode<V, P> fst = this.min;
    this.min = null;

    // go through the root list and merge nodes with the same degree
    FibNode<V, P> curr = fst;
    do {
      final FibNode<V, P> next = curr.right;
      FibNode<V, P> other;
      int d = curr.degree;
      while(d < degrees.size() && (other = degrees.set(d, null)) != null) {
        // the smaller key goes on top
        if(comp.compare(curr.priority, other.priority) < 0) {
          curr.addChild(other);
        } else {
          other.addChild(curr);
          curr = other;
        }
        d++;
      }

      // insert the new node
      while(degrees.size() <= d) {
        degrees.add(null);
      }
      degrees.set(d, curr);

      curr = next;
    } while(curr != fst);

    // re-add all nodes to the root list and update the minimum
    FibNode<V, P> mn = null;
    for(final FibNode<V, P> nd : degrees) {
      if(nd != null) {
        this.insertIntoRootList(nd);
        if(mn == null || this.comp.compare(nd.priority, mn.priority) < 0) {
          mn = nd;
        }
      }
    }
    this.min = mn;
  }

  /**
   * A node in a {@link FibHeap}.
   * @author Leo Woerteler
   * @param <V> value type
   * @param <P> priority type
   */
  public static final class FibNode<V, P> {
    /** This node's heap. */
    private FibHeap<V, P> heap;

    /** Current key. */
    P priority;
    /** Value. */
    final V value;

    /** Parent pointer, {@code null} if the node is in the root list. */
    FibNode<V, P> parent;
    /** Pointer to some child, {@code null} iff {@link #degree} is {@code 0}. */
    FibNode<V, P> firstChild;
    /** Pointer to this node's left sibling (non-{@code null}, can be {@code this}). */
    FibNode<V, P> left = this;
    /** Pointer to this node's left sibling (non-{@code null}, can be {@code this}). */
    FibNode<V, P> right = this;

    /** Flag for nodes that already lost a child. */
    boolean lost;
    /** Number of children. */
    int degree;

    /**
     * Constructor.
     * @param heap heap of this node
     * @param priority priority
     * @param value value
     */
    FibNode(final FibHeap<V, P> heap, P priority, V value) {
      this.heap = heap;
      this.priority = priority;
      this.value = value;
    }

    /**
     * Getter for this node's current key.
     * @return the key currently associated with this node
     */
    public P getPriority() {
      return this.priority;
    }

    /**
     * Getter for this node's value.
     * @return the value associated with this node
     */
    public V getValue() {
      return this.value;
    }

    /**
     * Checks if this entry is still contained in its heap.
     * @return result of check
     */
    public boolean isValid() {
      return this.heap != null;
    }

    /**
     * Decreases this node's key in its heap.
     * @param newKey new, smaller key
     */
    public void decreaseKey(P newKey) {
      if(this.heap == null) {
        throw new IllegalArgumentException("node is not valid");
      }

      final Comparator<P> comp = this.heap.comp;
      if(comp.compare(newKey, this.priority) > 0) {
        throw new IllegalArgumentException("new key is greater than old one");
      }

      this.priority = newKey;

      FibNode<V, P> par = this.parent;
      if(par != null) {
        // node is not in the root list
        if(comp.compare(newKey, par.priority) < 0) {
          boolean cascade;
          FibNode<V, P> curr = this;
          do {
            // cascading cut
            cascade = curr.deleteFromParent();
            this.heap.insertIntoRootList(curr);
            curr = par;
            par = curr.parent;
          } while(cascade);
        }
      }

      // update min pointer
      if(comp.compare(newKey, this.heap.min.priority) < 0) {
        this.heap.min = this;
      }
    }

    @Override
    public String toString() {
      return "Node[" + this.priority + ", " + this.value + "]";
    }

    /**
     * Adds the given child to this node.
     * @param child child to add
     */
    void addChild(final FibNode<V, P> child) {
      child.parent = this;
      final FibNode<V, P> fst = this.firstChild;
      if(fst == null) {
        this.firstChild = child;
        child.left = child.right = child;
      } else {
        child.left = fst;
        child.right = fst.right;
        fst.right.left = child;
        fst.right = child;
      }
      this.degree++;
    }

    /**
     * Deletes this node from its parent if one exists.
     * @return {@code true} if a cascading cut has to be triggered, {@code false} otherwise
     */
    private boolean deleteFromParent() {
      final FibNode<V, P> par = this.parent;
      this.parent = null;
      if(this.right == this) {
        par.firstChild = null;
      } else {
        this.right.left = this.left;
        this.left.right = this.right;
        if(par.firstChild == this) {
          par.firstChild = this.right;
        }
      }
      par.degree--;
      if(par.parent != null) {
        final boolean cascade = par.lost;
        par.lost ^= true;
        return cascade;
      }
      return false;
    }
  }
}
