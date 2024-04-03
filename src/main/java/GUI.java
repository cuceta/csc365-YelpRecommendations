//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.*;
//import java.io.IOException;
//import java.util.HashMap;
//
//public class GUI {
//    private JFrame frame;
//    private JButton submitButton;
//    private JButton closeButton;
//    private JPanel container;
//    private JPanel top;
//    private JPanel results;
//    private JLabel info;
//    private JTextField input;
//
//    private String storedInput;
//
//    // Place Holder
//    private JPanel recOne;
//    private JPanel recTwo;
//    private JLabel text1;
//    private JLabel text2;
//
//    public GUI() {
//        frame = new JFrame();
//        container = new JPanel();
//        results = new JPanel(new GridLayout(1,2));
//        results.setBackground(Color.BLACK);
//        recOne = new JPanel(new FlowLayout());
//        recTwo = new JPanel(new FlowLayout());
//        top = new JPanel(new FlowLayout());
//
//        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
//        text1 = new JLabel("Recommendation\n");
//        text2 = new JLabel("Recommendation\n");
//
//        submitButton = new JButton("Submit");
//        closeButton = new JButton("Close");
//        info = new JLabel("Enter the business ID of businesses you love and we'll give you some recommendations!");
//        input = new JTextField(24);
//
//        setUpGUI();
//        setCloseButton();
//        storeString();
//    }
//
//    public void setUpGUI() {
//        frame.setSize(1400,200);
//        frame.setTitle("YELP Business Recommendations");
//        frame.setBackground(Color.MAGENTA);
//        top.add(info);
//        top.add(input);
//        top.add(submitButton);
//        top.add(closeButton);
//
//
//
//        recOne.add(text1);
//        recTwo.add(text2);
//        results.add(recOne);
//        results.add(recTwo);
//
//        container.add(top);
//        container.add(results);
//        frame.add(container);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setVisible(true);
//    }
//
//    public void storeString() {
//        ActionListener submitRequest = new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                storedInput = input.getText();
////                System.out.println(storedInput);
//
//                HashMap<String, Business> results = null;
//                try {
//                    results = Recommendation.TFIDF(storedInput);
//                } catch (IOException ex) {
//                    throw new RuntimeException(ex);
//                } catch (ClassNotFoundException ex) {
//                    throw new RuntimeException(ex);
//                }
////                System.out.println(Recommendation.TFIDF(storedInput).size());
//
//                updateRecs(results);
////                System.out.println(results.getFirst());
//            }
//        };
//        submitButton.addActionListener(submitRequest);
//    }
//
//    private void updateRecs(HashMap<String, Business> recommendations) {
//        System.out.println(recommendations.keySet());
//
//        JLabel rec1 = new JLabel();
//        JLabel rec2 = new JLabel();
//        int i = 0;
//        for (String id : recommendations.keySet()) {
//            if (recommendations.get(id).getCosineSimilarity() <= .9) {
//                if (i == 0) {
//                    rec1.setText("[" + recommendations.get(id).getName() + ": Similarity --> " + recommendations.get(id).getCosineSimilarity()+ "]\n");
//                } else if (i == 1) {
//                    rec2.setText("[" + recommendations.get(id).getName() + ": Similarity --> " + recommendations.get(id).getCosineSimilarity() + "]\n");
//                    break; // We only need top two recommendations
//                }
//                i++;
//            }
//
//        }
//
//        // Add recommendations to the panels
//        recOne.add(rec1);
//        recTwo.add(rec2);
//
//        frame.revalidate();
//        frame.repaint();
//    }
//
//    public void setCloseButton() {
//        ActionListener closeGUI = new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                frame.setVisible(false);
//            }
//        };
//        closeButton.addActionListener(closeGUI);
//    }
//
//    public static void main(String[] args) {
//        GUI gui = new GUI();
//    }
//}