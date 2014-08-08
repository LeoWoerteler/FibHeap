package de.woerteler.fibheap;

import static org.junit.Assert.*;

import java.util.*;
import java.util.Map.Entry;

import org.junit.*;

import de.woerteler.fibheap.FibHeap.FibNode;

/**
 * Tests for the {@link FibHeap fibonacci heap} that use it to implement Dijkstra's algorithm.
 * @author Leo Woerteler
 */
public class DijkstraTest {
  /** A comparator for {@code double}s. */
  private static final Comparator<Double> DBL_CMP =
      new Comparator<Double>() {
        @Override
        public int compare(final Double o1, final Double o2) {
          return o1.compareTo(o2);
        }
      };

  /** A vertex in a graph. */
  private static class Vertex {
    /** Vertex ID. */
    private final int id;
    /** Outgoing edges. */
    private final List<Edge> edges = new ArrayList<>();

    /**
     * Constructor.
     * @param id node ID
     */
    private Vertex(final int id) {
      this.id = id;
    }

    @Override
    public String toString() {
      return "v" + (this.id + 1);
    }
  }

  /** A directed edge in a graph. */
  private static class Edge {
    /** Target vertex. */
    private final Vertex target;
    /** Edge weight. */
    private final double weight;

    /**
     * Constructor.
     * @param target target vertex
     * @param weight edge weight
     */
    private Edge(final Vertex target, final double weight) {
      this.target = target;
      this.weight = weight;
    }
  }

  /** Data needed during the execution of the algorithm. */
  private static class VertexData {
    /** Current shortest distance to the vertex. */
    double distance;
    /** List of predecessors. */
    final List<Vertex> predecessors = new ArrayList<>();
    /** Associated fibonacci heap node. */
    private FibNode<Vertex, Double> fibNode;
    /** Flag for marking a vertex as already processed. */
    private boolean closed = false;

    /**
     * Constructor.
     * @param fibNode node in the fibonacci heap.
     */
    private VertexData(final FibNode<Vertex, Double> fibNode) {
      this.fibNode = fibNode;
      this.distance = fibNode.getPriority();
    }
  }

  private static Map<Vertex, VertexData> dijkstra(final Vertex start) {
    final Map<Vertex, VertexData> dists = new HashMap<>();

    final FibHeap<Vertex, Double> heap = new FibHeap<>(DBL_CMP);
    dists.put(start, new VertexData(heap.insert(start, 0.0)));

    while(!heap.isEmpty()) {
      final Vertex s = heap.extractMin();
      final VertexData data = dists.get(s);
      data.fibNode = null;
      data.closed = true;

      for(final Edge e : s.edges) {
        final Vertex t = e.target;
        final double newDist = data.distance + e.weight;
        VertexData dataT = dists.get(t);
        if(dataT == null) {
          dataT = new VertexData(heap.insert(t, newDist));
          dataT.predecessors.add(s);
          dists.put(t, dataT);
        } else if(!dataT.closed && newDist < dataT.distance) {
          dataT.distance = newDist;
          dataT.fibNode.decreaseKey(newDist);
          dataT.predecessors.clear();
          dataT.predecessors.add(s);
        }
      }
    }
    return dists;
  }

  /** Checks the correctness of Dijkstra's algorithm on a small example from Wikipedia.  */
  @Test
  public void wikipedia() {
    final Vertex[] nodes = {
        new Vertex(0), new Vertex(1), new Vertex(2),
        new Vertex(3), new Vertex(4), new Vertex(5)
    };

    final int[][] edges = {
        { 1, 2,  7 },
        { 1, 3,  9 },
        { 1, 6, 14 },
        { 2, 3, 10 },
        { 2, 4, 15 },
        { 3, 4, 11 },
        { 3, 6,  2 },
        { 4, 5,  6 },
        { 5, 6,  9 }
    };

    for(final int[] e : edges) {
      final Vertex s = nodes[e[0] - 1], t = nodes[e[1] - 1];
      s.edges.add(new Edge(t, e[2]));
      t.edges.add(new Edge(s, e[2]));
    }

    final int[][] dists = {
        {  0,  7,  9, 20, 20, 11 },
        {  7,  0, 10, 15, 21, 12 },
        {  9, 10,  0, 11, 11,  2 },
        { 20, 15, 11,  0,  6, 13 },
        { 20, 21, 11,  6,  0,  9 },
        { 11, 12,  2, 13,  9,  0 }
    };

    for(final Vertex v : nodes) {
      final Map<Vertex, VertexData> res = dijkstra(v);
      assertEquals(6, res.size());
      for(final Entry<Vertex, VertexData> e : res.entrySet()) {
        assertEquals(dists[v.id][e.getKey().id], Math.round(e.getValue().distance));
      }
    }
  }
}
