package Classes;

import java.util.*;

public class Graph {
    // Member variables of this class
    int[] dist;
    private Set<Integer> settled;
    private PriorityQueue<Node> pq;
    private int[] predecessors;  // To store the path

    // Number of vertices
    private int vertices;
    List<List<Node>> adj;

    // Constructor of this class
    public Graph(int V) {
        // This keyword refers to current object itself
        this.vertices = V;
        dist = new int[V];
        settled = new HashSet<Integer>();
        pq = new PriorityQueue<Node>(V, new Node());
        predecessors = new int[V];
        Arrays.fill(predecessors, -1);
    }

    // Dijkstra's Algorithm
    public void dijkstra(List<List<Node>> adj, int src) {
        this.adj = adj;

        for (int i = 0; i < vertices; i++)
            dist[i] = Integer.MAX_VALUE;

        pq.add(new Node(src, 0));
        dist[src] = 0;

        while (settled.size() != vertices) {
            if (pq.isEmpty())
                return;

            int u = pq.remove().node;

            if (settled.contains(u))
                continue;
            settled.add(u);

            e_Neighbours(u);
        }
    }

    // Method to process all the neighbours of the passed node
    private void e_Neighbours(int u) {
        int edgeDistance;
        int newDistance;

        // All the neighbors of v
        for (int i = 0; i < adj.get(u).size(); i++) {
            Node v = adj.get(u).get(i);

            // If current node hasn't already been processed
            if (!settled.contains(v.node)) {
                edgeDistance = v.cost;
                newDistance = dist[u] + edgeDistance;

                // If new distance is cheaper in cost
                if (newDistance < dist[v.node]) {
                    dist[v.node] = newDistance;
                    predecessors[v.node] = u;
                    pq.add(new Node(v.node, dist[v.node]));
                }
            }
        }
    }

    // Method to find the shortest path between two nodes
    public List<Integer> findShortestPath(int start, int end) {
        dijkstra(adj, start);  // Run Dijkstra from start node
        List<Integer> path = new ArrayList<>();

        // Trace path back from end to start using predecessors
        for (int at = end; at != -1; at = predecessors[at]) {
            path.add(at);
        }
        Collections.reverse(path);  // Reverse to get the path from start to end

        if (path.get(0) != start) {  // Check if a path exists
            return Collections.emptyList();  // Return an empty list if no path exists
        }
        return path;  // Return the path
    }

    // Main driver method
    public static void main(String arg[]) {
        int V = 5;
        int source = 0;

        List<List<Node>> adj = new ArrayList<>();

        // Initialize list for every node
        for (int i = 0; i < V; i++) {
            List<Node> item = new ArrayList<>();
            adj.add(item);
        }

        // Inputs for the GFG(dpq) graph
        adj.get(0).add(new Node(1, 9));
        adj.get(0).add(new Node(2, 6));
        adj.get(0).add(new Node(3, 5));
        adj.get(0).add(new Node(4, 3));

        adj.get(2).add(new Node(1, 2));
        adj.get(2).add(new Node(3, 4));

        // Calculating the single source shortest path
        Graph dpq = new Graph(V);
        dpq.dijkstra(adj, source);

        // Printing the shortest path to all the nodes from the source node
        System.out.println("The shortest path from node :");
        List<Integer> path = dpq.findShortestPath(0, 3); // Find shortest path from node 0 to node 3
        System.out.println("Shortest path from 0 to 3: " + path);
    }
}
