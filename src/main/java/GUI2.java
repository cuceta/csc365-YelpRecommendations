
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
    private GraphUtil graphUtil;


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

        this.disjointSet = new DisjointSet();

        this.graphUtil = initializeGraphUtil();

        setUpGUI();
        setActions();
        loadDisjointSets();
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
        findPathButton.addActionListener(e -> {
            try {
                findShortestPath();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        });
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
            Set<String> membersName = new HashSet<>();
            JTextArea setDisplay = new JTextArea(2, 20);
            setDisplay.setText("Set Root: " + root.getName() + " includes: \n");

            for(Business business : members){
                String name = business.getName();
                membersName.add(name);
            }

            for (String businessName : membersName) {
                setDisplay.append(businessName + ", ");
            }
            setDisplay.setEditable(false);
            setsPanel.add(setDisplay);
        }
        setsPanel.revalidate();
        setsPanel.repaint();
    }


    public GraphUtil initializeGraphUtil() {
        Map<Business, List<Business>> adjacencyList = Recommendation.buildAdjacencyList(Recommendation.findClosestNeighbors());
        GraphUtil graphUtil = new GraphUtil(adjacencyList, disjointSet);
        return graphUtil;
    }

    private void findShortestPath() throws IOException, ClassNotFoundException {
        String business1Name = inputBusiness1.getText();
        String business2Name = inputBusiness2.getText();

        HashMap<String, Business> tfidf1 = Recommendation.TFIDF(business1Name);
        HashMap<String, Business> tfidf2 = Recommendation.TFIDF(business2Name);

        Map<Business, List<Business>> closestNeighborsMap = Recommendation.findClosestNeighbors();
        Recommendation.processDisjointSets(closestNeighborsMap);



        HashMap<String, Business> nameToBusinessHM = Recommendation.getNameToBusiness();
        Business business1 = nameToBusinessHM.get(business1Name);
        Business business2 = nameToBusinessHM.get(business2Name);

        if (business1 == null || business2 == null) {
            JOptionPane.showMessageDialog(frame, "One or both business names are invalid.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

//        List<Business> path = graphUtil.findPath(business1, business2);
        String message;
////        if (path.isEmpty()) {
////            message = "No path exists between " + business1Name + " and " + business2Name;
////        } else {
//            StringBuilder pathStringBuilder = new StringBuilder("Path: ");
//            for (Business business : path) {
//                pathStringBuilder.append(business.getName()).append(" -> ");
//            }
//            pathStringBuilder.delete(pathStringBuilder.length() - 4, pathStringBuilder.length());
//            message = pathStringBuilder.toString();
////        }
        message = Recommendation.findPath(business1,business2);

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
