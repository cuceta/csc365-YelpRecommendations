import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.*;

public class Recommendation {

    static HashMap<String, String> bNamesHashMap = new HashMap<>(); //key --> id; value --> Businesses names
    static HashMap<String, Business> businessHashMap = new HashMap<String, Business>(); //key --> id; value -->business
    static HashMap<String, String> BNames = new HashMap<>();//key --> name; Value --> id
    static HT wordFrequencyTable = new HT(); //key --> word; value --> count
    static PHT businessPHT; //key --> business name; value --> business ID file name
    static HashMap<Business, String> locaHM = new HashMap<>(); //key --> Business; Value --> Location


    // ---=== TDIDF ===---
    public static HashMap<String, Business> TFIDF(String input) throws IOException, ClassNotFoundException {
        Gson gson = new Gson();
        BufferedReader reader;
        JsonObject[] businessData = new JsonObject[150345], businessReview = new JsonObject[6990280];


        // ---=== READ AND RELATE BUSINESS, BUSINESS NAME, AND BUSINESS ID ===---
        int numofBusinesses = 0;
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
                locaHM.put(b, loca);
                b.setLatitude(Double.parseDouble(latitude));
                b.setLongitude(Double.parseDouble(longitude));
                businessHashMap.put(id, b);
                bNamesHashMap.put(id, name);
                if (!BNames.containsKey(name)) { //no duplicates
                    BNames.put(name, id);
                }
                numofBusinesses++;
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
    public static HashMap<String, Business> cosineVector(String inputID) throws IOException, ClassNotFoundException { //Cosinevector = (a * b)/( ( squart(A^2) )( squart(B^2) )
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
//        System.out.println("Top " + mostSimilar + " similar businesses to " + userBusiness.getName() + ":");
        for (int i = 0; i < Math.min(mostSimilar, sortedResults.size()); i++) {
            Map.Entry<String, Double> reslt = sortedResults.get(i);
            String name = bNamesHashMap.get(businessHashMap.get(reslt.getKey()).getBusinessID());
//            double similarityResult = reslt.getValue();
            nameToResult.put(businessHashMap.get(reslt.getKey()).getBusinessID(), businessHashMap.get(BNames.get(name)));
//            System.out.println(name + ": " + similarityResult + ";       Cluster: " + businessHashMap.get(reslt.getKey()).getCluster());
//            System.out.println(businessHashMap.get(businessHashMap.get(reslt.getKey()).getBusinessID()).getReview());
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
        Map<Business, List<Business>> closestNeighborsMap = new HashMap<>();
        int counter = 0;

        // Assuming 'businessHashMap' contains all businesses
        for (Business b1 : locaHM.keySet()) {
            List<Business> closestNeighbors = new ArrayList<>();

            // To avoid clustering, select every 100th business
            if (counter % 100 == 0) {
                for (Business b2 : locaHM.keySet()) {
                    if (!b1.equals(b2)) {
                        double distance = calculateDistance(b1.getLatitude(), b1.getLongitude(),
                                b2.getLatitude(), b2.getLongitude());

                        // Add b2 to closestNeighbors if it's closer than any of the existing neighbors
                        if (closestNeighbors.size() < 4) {
                            closestNeighbors.add(b2);
                        } else {
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

                closestNeighborsMap.put(b1, closestNeighbors);
            }

            counter++;
        }

        return closestNeighborsMap;
    }


    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String input = "Westshore Pizza";
        TFIDF(input);
//        int clusterCount = 0;
//        for (Business b : businessHashMap.values()){
//            System.out.println(b.toString());
//            if (b.getCluster() == 0){
//                clusterCount++;
//            }
//            System.out.println(b);
//        }
//        for(Business b : locaHM.keySet()){
//            System.out.println(b.getBusinessID() + " , " + b.getLatitude() + " , " + b.getLongitude());
//        }
//        System.out.println(businessHashMap.size());
//        System.out.println(clusterCount);

        Map<Business, List<Business>> closestNeighborsMap = findClosestNeighbors();

        for (Map.Entry<Business, List<Business>> entry : closestNeighborsMap.entrySet()) {
            Business business = entry.getKey();
            List<Business> closestNeighbors = entry.getValue();

            System.out.println("Closest geographical businesses to: " + business.getName() + "at: " + business.getLatitude() + ", " + business.getLongitude());
            for (Business neighbor : closestNeighbors) {
                System.out.println("  - " + neighbor.getName() + "at: " + business.getLatitude() + ", " + business.getLongitude());
            }
            System.out.println();
        }
    }


}