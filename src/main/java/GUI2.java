import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class GUI2 {
    private JFrame frame;
    private JButton closeButton, findPathButton;
    private JPanel container, top, results, setsPanel;
    private JLabel info;
    private JTextField inputBusiness1, inputBusiness2;

    private DisjointSet disjointSet;

    public GUI2() {
        frame = new JFrame("YELP Business Recommendations");
        container = new JPanel();
        results = new JPanel(new GridLayout(1, 2));
        setsPanel = new JPanel();
        top = new JPanel(new FlowLayout());

        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        closeButton = new JButton("Close");
        findPathButton = new JButton("Find Path");

        info = new JLabel("Enter business IDs for pathfinding:");
        inputBusiness1 = new JTextField(15);
        inputBusiness2 = new JTextField(15);

        setUpGUI();
        setActions();
        loadDisjointSets(); // Load and display sets at the start
    }

    private void setUpGUI() {
        frame.setSize(1500, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        top.add(info);
        top.add(inputBusiness1);
        top.add(inputBusiness2);
        top.add(findPathButton);
        top.add(closeButton);

        container.add(top);
        container.add(results);
        container.add(new JScrollPane(setsPanel)); // Ensure setsPanel is scrollable

        frame.add(container);
        frame.setVisible(true);
    }

    private void setActions() {
        closeButton.addActionListener(e -> frame.setVisible(false));
        findPathButton.addActionListener(e -> findShortestPath());
    }

    private void loadDisjointSets() {
        try {
            disjointSet = DisjointSet.deserialize("disjoint_sets.ser");
            displayDisjointSets();
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Failed to load disjoint sets: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void displayDisjointSets() {
        setsPanel.setLayout(new BoxLayout(setsPanel, BoxLayout.Y_AXIS)); // Use vertical layout
        Set<Business> roots = disjointSet.getAllRoots();
        setsPanel.removeAll();
        for (Business root : roots) {
            Set<Business> members = disjointSet.getSetMembers(root);
            JTextArea setDisplay = new JTextArea(2, 20);
            setDisplay.setText("Set Root: " + root.getName() + " includes: \n");
            for (Business member : members) {
                setDisplay.append(member.getName() + ", ");
            }
            setDisplay.setEditable(false);
            setsPanel.add(setDisplay);
        }
        setsPanel.revalidate();
        setsPanel.repaint();
    }

    private void findShortestPath() {
        String business1Name = inputBusiness1.getText().trim();
        String business2Name = inputBusiness2.getText().trim();

        HashMap<String, Business> businessMap = Recommendation.getNameToBusiness();

        // Assuming businessMap maps business names to Business objects
        Business business1 = businessMap.get(business1Name);
        Business business2 = businessMap.get(business2Name);

        if (business1 == null || business2 == null) {
            JOptionPane.showMessageDialog(frame, "One or both business names are invalid.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Assuming disjointSet is an instance of DisjointSet and is properly initialized
        Business root1 = disjointSet.find(business1);
        Business root2 = disjointSet.find(business2);

        // Check if both businesses share the same root in the disjoint set
        boolean isConnected = root1.equals(root2);

        // Display the result
        String message = isConnected ? "A path exists between " + business1Name + " and " + business2Name
                : "No path exists between " + business1Name + " and " + business2Name;

        JLabel pathLabel = new JLabel(message);
        results.removeAll();
        results.add(pathLabel);
        results.revalidate();
        results.repaint();
    }


    public static void main(String[] args) {
        new GUI2();
    }
}
