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
  /** A vertex in a graph. */
  private static final class Vertex {
    /** Vertex ID. */
    private final int id;
    /** Vertex name. */
    private final String name;
    /** Outgoing edges. */
    private final List<Edge> edges = new ArrayList<>();

    /**
     * Constructor.
     * @param id node ID
     * @param name vertex name
     */
    private Vertex(final int id, final String name) {
      this.id = id;
      this.name = name;
    }

    @Override
    public String toString() {
      return this.name;
    }

    /**
     * Adds a new undirected edge between this vertex and the given one.
     * @param other other vertex
     * @param weight edge weight
     */
    void undirEdgeTo(final Vertex other, final double weight) {
      this.edges.add(new Edge(other, weight));
      other.edges.add(new Edge(this, weight));
    }
  }

  /** A directed edge in a graph. */
  private static final class Edge {
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
  private static final class VertexData {
    /** Current shortest distance to the vertex. */
    double distance;
    /** List of predecessors. */
    final List<Vertex> predecessors = new ArrayList<>();
    /** Associated fibonacci heap node. */
    private FibNode<Vertex, Double> fibNode;
    /** Flag for marking a vertex as already processed. */
    private boolean closed;

    /**
     * Constructor.
     * @param fibNode node in the fibonacci heap.
     */
    private VertexData(final FibNode<Vertex, Double> fibNode) {
      this.fibNode = fibNode;
      this.distance = fibNode.getKey();
    }

    @Override
    public String toString() {
      return "VertexData{distance=" + this.distance + ", predecessors=" + this.predecessors + "}";
    }
  }

  /**
   * Implementation of Dijkstra's single-source-shortest-paths algorithm.
   * @param start start vertex
   * @return map from vertices to distance and predecessors
   */
  private static Map<Vertex, VertexData> dijkstra(final Vertex start) {
    final Map<Vertex, VertexData> dists = new HashMap<>();

    final FibHeap<Vertex, Double> heap = FibHeap.newHeap(
      new Comparator<Double>() {
        @Override
        public int compare(final Double o1, final Double o2) {
          return o1.compareTo(o2);
        }
      }
    );

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
          dists.put(t, dataT);
        } else if(!dataT.closed && newDist < dataT.distance) {
          dataT.distance = newDist;
          dataT.fibNode.decreaseKey(newDist);
          dataT.predecessors.clear();
        }

        if(dataT.distance == newDist) {
          dataT.predecessors.add(s);
        }
      }
    }
    return dists;
  }

  /**
   * Implementation of Floyd & Warshall's all-pairs-shortest-paths algorithm.
   * @param vs the graph
   * @return distance matrix
   */
  private static double[][] floydWarshall(final Vertex[] vs) {
    final double[][] dists = new double[vs.length][vs.length];
    for(int i = 0; i < vs.length; i++) {
      Arrays.fill(dists[i], Double.POSITIVE_INFINITY);
      dists[i][i] = 0;
      for(final Edge e : vs[i].edges) {
        dists[i][e.target.id] = e.weight;
      }
    }

    for(int v = 0; v < vs.length; v++) {
      for(int s = 0; s < vs.length; s++) {
        for(int t = 0; t < vs.length; t++) {
          dists[s][t] = Math.min(dists[s][t], dists[s][v] + dists[v][t]);
        }
      }
    }

    return dists;
  }

  /** Checks the correctness of Dijkstra's algorithm on a small example from Wikipedia.  */
  @Test
  public void wikipedia() {
    final Vertex[] nodes = {
      new Vertex(0, "v1"), new Vertex(1, "v2"), new Vertex(2, "v3"),
      new Vertex(3, "v4"), new Vertex(4, "v5"), new Vertex(5, "v6")
    };

    final int[][] edges = {
      { 1, 2,  7 }, { 1, 3,  9 }, { 1, 6, 14 },
      { 2, 3, 10 }, { 2, 4, 15 },
      { 3, 4, 11 }, { 3, 6,  2 },
      { 4, 5,  6 },
      { 5, 6,  9 }
    };

    for(final int[] e : edges) {
      nodes[e[0] - 1].undirEdgeTo(nodes[e[1] - 1], e[2]);
    }

    final double[][] dists = floydWarshall(nodes);

    for(final Vertex v : nodes) {
      final Map<Vertex, VertexData> res = dijkstra(v);
      assertEquals(6, res.size());
      for(final Entry<Vertex, VertexData> e : res.entrySet()) {
        final Vertex w = e.getKey();
        final VertexData data = e.getValue();
        assertEquals(dists[v.id][w.id], data.distance, 0);
        assertEquals(v.id == w.id ? 0 : v.id == 1 && w.id == 4 || v.id == 4 && w.id == 1 ? 2 : 1,
            data.predecessors.size());
      }
    }
  }

  /** An example graph consisting of cities in southern Germany. */
  @Test
  public void southernGermany() {
    final Vertex frankfurt = new Vertex(0, "Frankfurt");
    final Vertex mannheim  = new Vertex(1, "Mannheim");
    final Vertex wuerzburg = new Vertex(2, "Würzburg");
    final Vertex stuttgart = new Vertex(3, "Stuttgart");
    final Vertex kassel    = new Vertex(4, "Kassel");
    final Vertex karlsruhe = new Vertex(5, "Karlsruhe");
    final Vertex erfurt    = new Vertex(6, "Erfurt");
    final Vertex nuernberg = new Vertex(7, "Nürnberg");
    final Vertex augsburg  = new Vertex(8, "Augsburg");
    final Vertex muenchen  = new Vertex(9, "München");

    final Vertex[] vertices = {
      frankfurt, mannheim, wuerzburg, stuttgart, kassel,
      karlsruhe, erfurt,   nuernberg, augsburg,  muenchen
    };

    frankfurt.undirEdgeTo(mannheim, 85);
    frankfurt.undirEdgeTo(wuerzburg, 217);
    frankfurt.undirEdgeTo(kassel, 85);
    mannheim.undirEdgeTo(karlsruhe, 80);
    wuerzburg.undirEdgeTo(erfurt, 186);
    wuerzburg.undirEdgeTo(nuernberg, 103);
    stuttgart.undirEdgeTo(nuernberg, 183);
    kassel.undirEdgeTo(muenchen, 502);
    karlsruhe.undirEdgeTo(augsburg, 250);
    nuernberg.undirEdgeTo(muenchen, 167);
    augsburg.undirEdgeTo(muenchen, 84);

    final double[][] dist = floydWarshall(vertices);

    for(final Vertex v : vertices) {
      final Map<Vertex, VertexData> res = dijkstra(v);
      for(final Entry<Vertex, VertexData> e : res.entrySet()) {
        final Vertex w = e.getKey();
        final VertexData data = e.getValue();
        assertEquals(dist[v.id][w.id], data.distance, 0);
      }
    }
  }
}
