import java.util.*;

public class Graph {
    private Map<Business, Map<Business, Double>> adjacencyMap;

    public Graph(String businessesDirectory) {
        adjacencyMap = new HashMap<>();
        // Load businesses from the directory and create the graph
        loadBusinesses(businessesDirectory);
        // Calculate TFIDF weights for edges
        calculateTFIDFWeights();
    }

    private void loadBusinesses(String directory) {
        // Load businesses from the directory and add them to the graph
        Map<String, Business> businesses = Recommendation.getNameToBusiness();
        for (Business business : businesses.values()) {
            adjacencyMap.put(business, new HashMap<>());
        }
    }

    private void calculateTFIDFWeights() {
        // Calculate TFIDF weights for edges based on cosine similarity between businesses
        for (Business business1 : adjacencyMap.keySet()) {
            for (Business business2 : adjacencyMap.keySet()) {
                if (!business1.equals(business2)) {
                    double similarity = calculateCosineSimilarity(business1, business2);
                    adjacencyMap.get(business1).put(business2, similarity);
                }
            }
        }
    }

    private double calculateCosineSimilarity(Business business1, Business business2) {
        HT tfidf1 = business1.getTFIDF();
        HT tfidf2 = business2.getTFIDF();

        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;

        // Calculate dot product and magnitudes
        for (String term : tfidf1.getKeySet()) {
            double value1 = tfidf1.value(term);
            double value2 = tfidf2.value(term);
            dotProduct += value1 * value2;
            magnitude1 += Math.pow(value1, 2);
        }

        for (String term : tfidf2.getKeySet()) {
            magnitude2 += Math.pow(tfidf2.value(term), 2);
        }

        // Calculate cosine similarity
        if (magnitude1 == 0 || magnitude2 == 0) {
            return 0.0; // Handle division by zero
        }
        return dotProduct / (Math.sqrt(magnitude1) * Math.sqrt(magnitude2));
    }

    public List<Business> findShortestPath(Business start, Business end) {
        // Find the shortest path between two businesses using Dijkstra's algorithm
        Map<Business, Double> distance = new HashMap<>();
        Map<Business, Business> previous = new HashMap<>();
        PriorityQueue<Business> queue = new PriorityQueue<>(Comparator.comparingDouble(distance::get));

        // Initialization
        for (Business business : adjacencyMap.keySet()) {
            distance.put(business, Double.MAX_VALUE);
            previous.put(business, null);
        }
        distance.put(start, 0.0);
        queue.add(start);

        // Dijkstra's algorithm
        while (!queue.isEmpty()) {
            Business current = queue.poll();
            if (current.equals(end)) {
                break;
            }
            for (Business neighbor : adjacencyMap.get(current).keySet()) {
                double alt = distance.get(current) + adjacencyMap.get(current).get(neighbor);
                if (alt < distance.get(neighbor)) {
                    distance.put(neighbor, alt);
                    previous.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        // Reconstruct the shortest path
        List<Business> path = new ArrayList<>();
        Business current = end;
        while (current != null) {
            path.add(current);
            current = previous.get(current);
        }
        Collections.reverse(path);
        return path;
    }

}
