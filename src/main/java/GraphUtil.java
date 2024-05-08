import java.util.*;

public class GraphUtil {
    private Map<Business, List<Business>> adjacencyList; // Business connections graph
    private DisjointSet disjointSet; // Disjoint set instance

    public GraphUtil(Map<Business, List<Business>> adjacencyList, DisjointSet disjointSet) {
        this.adjacencyList = adjacencyList;
        this.disjointSet = disjointSet;
    }

    // Method to find path between two businesses using BFS within the same disjoint set
    public List<Business> findPath(Business start, Business end) {
        if (!disjointSet.isSameComponent(start, end)) {
            return Collections.emptyList(); // No path if businesses are not in the same component
        }

        if (!adjacencyList.containsKey(start) || !adjacencyList.containsKey(end)) {
            return Collections.emptyList(); // No path if one of the businesses isn't in the graph
        }

        Queue<Business> queue = new LinkedList<>();
        Map<Business, Business> predecessors = new HashMap<>();
        Set<Business> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);
        predecessors.put(start, null); // Start business has no predecessor

        // Perform BFS from start business within the same component
        while (!queue.isEmpty()) {
            Business current = queue.poll();
            if (current.equals(end)) {
                break; // Stop when end is found
            }

            // Go through all adjacent businesses within the same component
            for (Business neighbor : adjacencyList.get(current)) {
                if (!visited.contains(neighbor) && disjointSet.isSameComponent(current, neighbor)) {
                    visited.add(neighbor);
                    predecessors.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        return reconstructPath(predecessors, end);
    }

    // Method to reconstruct the path from start to end using predecessors
    private List<Business> reconstructPath(Map<Business, Business> predecessors, Business end) {
        List<Business> path = new ArrayList<>();
        for (Business at = end; at != null; at = predecessors.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        return path; // Return the path from start to end
    }
}
