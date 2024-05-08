import java.io.*;
import java.util.*;

public class DisjointSet implements Serializable {
    private static final long serialVersionUID = 1L; // Add serialization ID
    private Map<Business, Business> parent;
    private Map<Business, Integer> rank;

    public DisjointSet() {
        parent = new HashMap<>();
        rank = new HashMap<>();
    }

    public void makeSet(Business business) {
        parent.put(business, business);
        rank.put(business, 0);
    }

    public Business find(Business business) {
        if (business != parent.get(business)) {
            parent.put(business, find(parent.get(business)));
        }
        return parent.get(business);
    }

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

    public Set<Business> getAllRoots() {
        Set<Business> roots = new HashSet<>();
        for (Business business : parent.keySet()) {
            roots.add(find(business));
        }
        return roots;
    }

    public Set<Business> getSetMembers(Business root) {
        Set<Business> members = new HashSet<>();
        for (Business business : parent.keySet()) {
            if (find(business).equals(root)) {
                members.add(business);
            }
        }
        return members;
    }

    public static void serialize(DisjointSet set, String filename) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(set);
        }
    }

    public static DisjointSet deserialize(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            return (DisjointSet) in.readObject();
        }
    }
}
