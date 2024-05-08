import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.*;

public class Recommendation {

    static HashMap<String, String> bNamesHashMap = new HashMap<>(); //key --> id; value --> Businesses names
    static HashMap<String, Business> businessHashMap = new HashMap<>(); //key --> id; value --> business
    static HashMap<String, Business> nameTobusinessHashMap = new HashMap<>(); //key --> name; value --> business
    static HashMap<String, String> BNames = new HashMap<>();//key --> name; Value --> id
    static HT wordFrequencyTable = new HT(); //key --> word; value --> count
    static PHT businessPHT; //key --> business name; value --> business ID file name
    static HashMap<Business, String> locaHM = new HashMap<>(); //key --> Business; Value --> Location
    static HashMap<String, String> nameToLocationHM = new HashMap<>(); //key --> name; Value --> Location


    static Map<Business, List<Business>> closestNeighborsMap = new HashMap<>(); //key --> Business; Value --> List of closest businesses

    //For GUI
    public static HashMap<String, Business> getNameToBusiness(){
        for(String name : BNames.keySet()){
            String id = BNames.get(name);
            Business b = businessHashMap.get(id);
            nameTobusinessHashMap.put(name, b);
        }
        return nameTobusinessHashMap;
    }

    // ---=== TDIDF ===---
    public static HashMap<String, Business> TFIDF(String input) throws IOException, ClassNotFoundException {
        Gson gson = new Gson();
        BufferedReader reader;
        JsonObject[] businessData = new JsonObject[150345], businessReview = new JsonObject[6990280];


        // ---=== READ AND RELATE BUSINESS, BUSINESS NAME, AND BUSINESS ID ===---
        int counter = 0;
        try {
            reader = new BufferedReader(new FileReader("yelp_dataset/yelp_academic_dataset_business.json"));
            String documentLine;
            while ((documentLine = reader.readLine()) != null) {
                JsonObject business = gson.fromJson(documentLine, JsonObject.class);
                String id = String.valueOf(business.get("business_id")).substring(1, 23);
                String name = String.join(" ", String.valueOf(business.get("name")).split("[^a-zA-Z0-9'&-]+")).substring(1);
                String longitude = String.valueOf(business.get("longitude"));
                String latitude = String.valueOf(business.get("latitude"));
                String loca = latitude + ", " + longitude;

                Business b = new Business(id);
                b.setName(name);
                if (counter % 100 == 0) {
                    locaHM.put(b, loca);
                    b.setName(name);

//                    nameToLocationHM.put(name, loca);
                }
                b.setLatitude(Double.parseDouble(latitude));
                b.setLongitude(Double.parseDouble(longitude));
                businessHashMap.put(id, b);
                bNamesHashMap.put(id, name);
                if (!BNames.containsKey(name)) { //no duplicates
                    BNames.put(name, id);
                }
                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


//        //map business id to business. Why? when I want a business I just have to look through the hashMap for the id as the key
        for (Business business : businessHashMap.values()) {
            String id = String.valueOf(business.getBusinessID());
            businessHashMap.put(id, new Business(id));
        }


        //---=== READ AND RELATE BUSINESS, BUSINESS NAME, AND REVIEW ===---
        int numOfReviews = 0;
        try {
            reader = new BufferedReader(new FileReader("yelp_dataset/yelp_academic_dataset_review.json"));
            while (numOfReviews < businessData.length) {
                String documentLine = reader.readLine();
                businessReview[numOfReviews] = gson.fromJson(documentLine, JsonObject.class);
                numOfReviews++;
            }
        } catch (IOException e) {
            e.printStackTrace();
            ;
        }

        for (JsonObject jsonObject : businessReview) {
            if (jsonObject != null) {
                String id = String.valueOf(jsonObject.get("business_id")).substring(1, 23);

                String review = String.join(" ", String.valueOf(jsonObject.get("text")).split("[^a-zA-Z0-9'&]+"));
                businessHashMap.put(id, new Business(id));
                businessHashMap.get(id).setName(bNamesHashMap.get(id));
                businessHashMap.get(id).setReview(review);
            }
        }
        System.out.println("A)  READ AND RELATE BUSINESS, BUSINESS ID, and business names ---> DONE");

        // ---=== FILL WORD FREQUENCY TABLE ===---
        for (Business business : businessHashMap.values()) {
            if (business.getReview() != null) {
                Set<String> words = new HashSet<>(List.of(business.getReview().split("[^a-zA-Z0-9'&]+"))); //Add the words in the review to a set to avoid dublicates
                for (String term : words) {
                    if (!term.equals("")) {
                        if (wordFrequencyTable.contains(term)) { //if the word is found in the set --> increase count by 1
                            wordFrequencyTable.setValue(term, (wordFrequencyTable.value(term) + 1.0));
                        } else {
                            wordFrequencyTable.add(term, 1.0);
                        }
                    }
                }
            }
        }
        System.out.println("B)  FILL WORD FREQUENCY TABLE ---> DONE");


        // ---=== TF Try 2 ===---
        for (Business business : businessHashMap.values()) {
            HT wordInReviewFrequency = new HT(); //key --> word; value --> count
//            Get the frequency of a word in the reviews of each business
            if (business.getReview() != null) {
                Set<String> words = new HashSet<>(); //Add the words in the review to a set to avoid dublicates
                for (String oneWord : business.getReview().split("[^a-zA-Z0-9'&]+")) {
                    if (oneWord != null && (!oneWord.equals(""))) {
                        if (wordInReviewFrequency.contains(oneWord)) {
                            wordInReviewFrequency.setValue(oneWord, (wordInReviewFrequency.value(oneWord) + 1.0));

                        } else {
                            wordInReviewFrequency.add(oneWord, wordInReviewFrequency.value(oneWord));
                            wordInReviewFrequency.setValue(oneWord, 1.0);
                        }
                    }
                }
            }

//            Calculate TF
            HT TF = new HT();
            for (String term : wordInReviewFrequency.getKeySet()) {
                double countOfOneTerm = wordInReviewFrequency.value(term);
                double totalTermsInReview = wordInReviewFrequency.size;
                TF.add(term, (countOfOneTerm / totalTermsInReview));
                business.setTF(TF);
            }
        }
        System.out.println("C)  TF Try 2 ---> DONE");


        // ---=== IDF ===---
        //Done to the document that holds the reviews
        HT IDF = new HT(); //key --> business name; value --> idf of business
        //log10( (total number of reviews(i) )/(number of documents with the word in it) )
        for (String s : wordFrequencyTable.getKeySet()) {
            String name = s;
            Double count = wordFrequencyTable.value(s);
            double idf = Math.log10(numOfReviews / (count + 1));
            IDF.add(name, idf);
        }
        System.out.println("D)  IDF ---> DONE");


        // ---=== TF*IDF ===---
        for (Business business : businessHashMap.values()) {
            for (String termInReview : business.getTF().getKeySet()) {
                double tf = business.getTF().value(termInReview);
                double idf = IDF.value(termInReview);
                double tfidf = tf * idf;
                business.addTFIDF(termInReview, tfidf);
            }
        }
        System.out.println("E)  TF*IDF ---> DONE");


//        System.out.println(input);
        String inputID = BNames.get(input);
//        System.out.println(inputID);
        return cosineVector(inputID);
    }


    // ---=== COSINE VECTOR ===---
    public static HashMap<String, Business> cosineVector(String inputID) throws IOException, ClassNotFoundException {
        //Cosinevector = (a * b)/( ( squart(A^2) )( squart(B^2) )
        serializeBusinesses();
        Business userBusiness = businessHashMap.get(inputID);
        HashMap<String, Double> similarityResults = new HashMap<>(); //Stores key-->id; value -->cosine vector result.
        HashMap<String, Business> nameToResult = new HashMap<>(); //key --> name; value --> similar business to the given

        //calculate cosine vector
        for (Business business : businessHashMap.values()) {
            double dotProductResult = 0.0;
            double userBusinessMagn = 0.0;
            double compareBusinessesMagn = 0.0;
            //Dot product ---> (a * b)
            for (String term : userBusiness.getTFIDF().getKeySet()) {
                double userBusinessTFIDF = userBusiness.getTFIDF().value(term);
                double compareBusinessesTFIDF = business.getTFIDF().value(term);
                double product = userBusinessTFIDF * compareBusinessesTFIDF;
                dotProductResult = dotProductResult + product;
                //magnitudes
                userBusinessMagn = userBusinessMagn + (Math.pow(userBusinessTFIDF, 2));
                compareBusinessesMagn = compareBusinessesMagn + (Math.pow(compareBusinessesTFIDF, 2));
            }
            userBusinessMagn = Math.sqrt(userBusinessMagn);
            compareBusinessesMagn = Math.sqrt(compareBusinessesMagn);
            double csvResult = dotProductResult / ((userBusinessMagn * compareBusinessesMagn) + 0.0001);
            similarityResults.put(business.getBusinessID(), csvResult);
            business.setCosineSimilarity(csvResult);
            business.setName(bNamesHashMap.get(business.getBusinessID()));
        }
        List<Map.Entry<String, Double>> sortedResults = new ArrayList<>(similarityResults.entrySet());
        sortedResults.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));


        // Get top similar businesses
        int mostSimilar = 3; // Change this value to get more top businesses
        for (int i = 0; i < Math.min(mostSimilar, sortedResults.size()); i++) {
            Map.Entry<String, Double> reslt = sortedResults.get(i);
            String name = bNamesHashMap.get(businessHashMap.get(reslt.getKey()).getBusinessID());
            nameToResult.put(businessHashMap.get(reslt.getKey()).getBusinessID(), businessHashMap.get(BNames.get(name)));
        }
        makeClusters();
        return nameToResult;

    }


    // ---=== Serialization ===---
    public static void serializeBusinesses() {
        Business b;
        File checkFile;
        for (Business business : businessHashMap.values()) {
            b = business;
            String filename = "Serialized businesses/" + business.getBusinessID();
            FileOutputStream fileOut = null;
            ObjectOutputStream objOut = null;
            //Actual serialization
            try {
                File directory = new File("Serialized businesses");
                if (!directory.exists()) {
                    directory.mkdir();
                    businessPHT = new PHT(false);
                } else {
                    businessPHT = new PHT(true);
                }

                checkFile = new File(filename);
                if (!checkFile.exists()) {

                    businessPHT.put(b.getName(), filename);
                    fileOut = new FileOutputStream(filename);
                    objOut = new ObjectOutputStream(fileOut);
                    objOut.writeObject(b);

                    objOut.close();
                    fileOut.close();
                }
            } catch (IOException | ClassNotFoundException ex) {
//                System.out.println("IOException caught");
            }
        }
//        System.out.println("Businesses Serialized :)");

    }

    //---=== Clusters ===---
    public static void makeClusters() {
        ArrayList<Double> listOfCentroids = centroids(7);        // Initialize K cluster centroids randomly
        assignCentroidToClusters(listOfCentroids);        // Assign each data point to the nearest cluster centroid


        int numOfIterations = 100;
        for (int i = 0; i < numOfIterations; i++) {
            updateCentroidsAssignments(listOfCentroids);
            assignCentroidToClusters(listOfCentroids);
        }
    }

    private static ArrayList<Double> centroids(int numOfCentroids) {
        ArrayList<Double> centroids = new ArrayList<>();
        Random r = new Random();
        for (int i = 0; i < numOfCentroids; i++) {
            centroids.add(r.nextDouble());
        }
        return centroids;
    }

    private static void assignCentroidToClusters(ArrayList<Double> listOfCentroids) {
        // Iterate through data points and assign them to the nearest cluster centroid
        for (Business business : businessHashMap.values()) {
            double minDistance = Double.MAX_VALUE;
            int nearestCluster = -1;
            for (int i = 0; i < listOfCentroids.size(); i++) {
                double distance = Math.abs(business.getCosineSimilarity() - listOfCentroids.get(i));
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestCluster = i;
                }
            }
            business.setCluster(nearestCluster);
        }
    }

    private static void updateCentroidsAssignments(ArrayList<Double> listOfCentroids) {
        // Update centroids based on the mean of data points assigned to each cluster
        for (int i = 0; i < listOfCentroids.size(); i++) {
            double sum = 0;
            int count = 0;
            for (Business business : businessHashMap.values()) {
                if (business.getCluster() == i) {
                    sum += business.getCosineSimilarity();
                    count++;
                }
            }
            if (count != 0) {
                listOfCentroids.set(i, sum / count);
            }
        }
    }


    // ---=== Geographical Neighbors ===---
    // Method to calculate distance between two points using Haversine formula
    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;

        return distance;
    }

    // Method to find the four closest neighbors for each business
    public static Map<Business, List<Business>> findClosestNeighbors() {
        closestNeighborsMap = new HashMap<>();

        for (Business b1 : locaHM.keySet()) {
            List<Business> closestNeighbors = new ArrayList<>();

            // To avoid all businesses from the same location, select every 100th business
            for (Business b2 : locaHM.keySet()) {
                if (!b1.equals(b2)) {
                    double distance = calculateDistance(b1.getLatitude(), b1.getLongitude(),
                            b2.getLatitude(), b2.getLongitude());

                    if (closestNeighbors.size() < 4) {
                        closestNeighbors.add(b2);
                    } else {    // Add b2 to closestNeighbors if it's closer than any of the existing neighbors
                        for (int i = 0; i < 4; i++) {
                            if (distance < calculateDistance(b1.getLatitude(), b1.getLongitude(),
                                    closestNeighbors.get(i).getLatitude(), closestNeighbors.get(i).getLongitude())) {
                                closestNeighbors.set(i, b2);
                                break;
                            }
                        }
                    }
                }
            }

            // -+-TEST-+-
            closestNeighborsMap.put(b1, closestNeighbors);
//                b1.setClosestNeighbors(closestNeighborsMap.get(b1)); //save the neighbors in each business
//                System.out.println("Closest geographical businesses to: " + b1.getName() + "at: " + b1.getLatitude() + ", " + b1.getLongitude());
//                System.out.println("    -" + b1.getClosestNeighbors().get(0));
//                System.out.println("    -" + b1.getClosestNeighbors().get(1));
//                System.out.println("    -" + b1.getClosestNeighbors().get(2));
//                System.out.println("    -" + b1.getClosestNeighbors().get(3));

        }


        return closestNeighborsMap;
    }


    // ---=== Disjoint Sets ===---
    public static void processDisjointSets(Map<Business, List<Business>> closestNeighborsMap) {

        // Initialize Disjoint Set
        DisjointSet disjointSet = new DisjointSet();

        // Make sets for all businesses in locaHM
        for (Business business : locaHM.keySet()) {
            disjointSet.makeSet(business);
        }

        // Find disjoint sets from geographical neighbors
        for (Map.Entry<Business, List<Business>> entry : closestNeighborsMap.entrySet()) {
            Business root = disjointSet.find(entry.getKey());
            for (Business neighbor : entry.getValue()) {
                disjointSet.union(root, neighbor);
            }
        }

        // Count Disjoint Sets
        Set<Business> roots = new HashSet<>();
        for (Business business : locaHM.keySet()) {
            roots.add(disjointSet.find(business));
        }
//        for (Business b : roots){
//            System.out.println(b);
//            for (Business b2 : disjointSet.getSetMembers(b)){
//                System.out.println("        " + b2);
//            }
//        }
        int numberOfDisjointSets = roots.size();
        System.out.println("Number of disjoint sets: " + numberOfDisjointSets);

        // Persistently Store Disjoint Sets
        try {
            DisjointSet.serialize(disjointSet, "disjoint_sets.ser");
            System.out.println("Disjoint sets stored successfully.");
        } catch (IOException e) {
            System.err.println("Error while storing disjoint sets: " + e.getMessage());
        }
    }

    public static Map<Business, List<Business>> buildAdjacencyList(Map<Business, List<Business>> closestNeighborsMap) {
        Map<Business, List<Business>> adjacencyList = new HashMap<>();
        // Assuming closestNeighborsMap contains each business and a list of their closest neighbors
        for (Map.Entry<Business, List<Business>> entry : closestNeighborsMap.entrySet()) {
            List<Business> neighbors = entry.getValue();
            adjacencyList.put(entry.getKey(), neighbors);
        }
        return adjacencyList;
    }



    public static String findPath(Business b1, Business b2){
        String next = " ---> ";
        String path = b1.getName() + next;



        DisjointSet disjointSet = new DisjointSet();

        // Make sets for all businesses in locaHM
        for (Business business : locaHM.keySet()) {
            disjointSet.makeSet(business);
        }

        // Find disjoint sets from geographical neighbors
        for (Map.Entry<Business, List<Business>> entry : closestNeighborsMap.entrySet()) {
            Business root = disjointSet.find(entry.getKey());
            for (Business neighbor : entry.getValue()) {
                disjointSet.union(root, neighbor);
            }
        }

        Set<Business> roots = new HashSet<>();
        for (Business business : locaHM.keySet()) {
            roots.add(disjointSet.find(business));
        }

//        Business b1Parent = disjointSet.getParent(b1);
//        Business b2Parent = disjointSet.getParent(b2);


        for (Business b : roots){ //goes through the roots

            Set<Business> setOfMembers = disjointSet.getSetMembers(b);
            Set<String> setOfMembersNames = new HashSet<>();

            for( Business business : setOfMembers){
                String name = business.getName();
                setOfMembersNames.add(name);
            }

//            Iterator<String> iterator = setOfMembersNames.iterator();

            //if the parent root of b1 and b2 are the same business (b)
            if ( (setOfMembersNames.contains(b2.getName())) && setOfMembersNames.contains(b1.getName()) ){ //you're in the set of the wanted businesses
//                for (Business bu : setOfMembers) { //goes through the businesses that are members of that root
                    for(String name : setOfMembersNames) {
                        //make your path
                        System.out.println("        " + name);
                    }
//                }
            }
        }

        path = path + b2.getName() + next;
        path = path.substring(0, (path.length() - 5)); //get rid of extra pointing arrow
        return path;
    }






    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String input = "Westshore Pizza";
        TFIDF(input);

        // ---=== test storing latitude and longitude
//        for(Business b : locaHM.keySet()){
//            System.out.println(b.getBusinessID() + " , " + b.getLatitude() + " , " + b.getLongitude());
//        }


        // ---=== Test Geographical Neighbors ===---
        closestNeighborsMap = findClosestNeighbors(); //NEEDED for the neighbors to be saved. If you dont have it the findClosestNeighbors is never called

        for (Map.Entry<Business, List<Business>> entry : closestNeighborsMap.entrySet()) {

            Business business = entry.getKey();
            List<Business> closestNeighbors = entry.getValue();

            business.setClosestNeighbors(closestNeighbors);
            System.out.println("Closest geographical businesses to: " + business.getName() + "at: " + business.getLatitude() + ", " + business.getLongitude());
            for (Business b : business.getClosestNeighbors()) {
                System.out.println("  - " + b.getName() + "at: " + b.getLatitude() + ", " + b.getLongitude());
            }
            System.out.println();
        }

        System.out.println(closestNeighborsMap.size());


        // ---=== Test Disjoint Sets ===---
        processDisjointSets(closestNeighborsMap);


        String business1Name = "D S Vespers Sports Pub and Eatery";
        String business2Name = "Mojo Tapas";

        HashMap<String, Business> nameToBusinessHM = getNameToBusiness();
        Business business1 = nameToBusinessHM.get(business1Name);
        Business business2 = nameToBusinessHM.get(business2Name);
//        System.out.println(business1);
//        System.out.println(business2);

        System.out.println(findPath(business1,business2));
    }


}