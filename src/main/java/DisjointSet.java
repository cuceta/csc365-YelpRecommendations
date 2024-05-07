import java.io.*;
import java.util.*;

public class DisjointSet {
    private Map<Business, Business> parent;
    private Map<Business, Integer> rank;

    public DisjointSet() {
        parent = new HashMap<>();
        rank = new HashMap<>();
    }

    // Create a new set with a single element
    public void makeSet(Business business) {
        parent.put(business, business);
        rank.put(business, 0);
    }

    // Find the root of the set to which the element belongs
    public Business find(Business business) {
        if (business != parent.get(business)) {
            parent.put(business, find(parent.get(business))); // Path compression
        }
        return parent.get(business);
    }

    // Merge two sets into one
    public void union(Business x, Business y) {
        Business xRoot = find(x);
        Business yRoot = find(y);

        if (xRoot == yRoot) return;

        if (rank.get(xRoot) < rank.get(yRoot)) {
            parent.put(xRoot, yRoot);
        } else if (rank.get(xRoot) > rank.get(yRoot)) {
            parent.put(yRoot, xRoot);
        } else {
            parent.put(yRoot, xRoot);
            rank.put(xRoot, rank.get(xRoot) + 1);
        }
    }

    // Count the number of disjoint sets
    public int countDisjointSets() {
        Set<Business> roots = new HashSet<>();
        for (Business business : parent.keySet()) {
            roots.add(find(business));
        }
        return roots.size();
    }

    // Persistently store disjoint sets to a file
    public void storeDisjointSetsToFile(String filename) throws IOException {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(filename))) {
            outputStream.writeObject(parent);
        }
    }

    // Load disjoint sets from a file
    public void loadDisjointSetsFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(filename))) {
            parent = (Map<Business, Business>) inputStream.readObject();
        }
    }
}
