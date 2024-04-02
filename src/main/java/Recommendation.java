import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.swing.text.html.HTMLWriter;
import java.io.*;
import java.util.*;

public class Recommendation {

    static HashMap<String, String> bNamesHashMap = new HashMap<>(); //key --> id; value --> Businesses names
    static HashMap<String, Business> businessHashMap = new HashMap<String, Business>(); //key --> id; value -->business
    static HashMap<String, String> BNames = new HashMap<>();//key --> name; Value --> id

    static HT wordFrequencyTable = new HT(); //key --> word; value --> count

    static PHT businessPHT; //key --> business name; value --> business ID file name



    // ---=== TDIDF ===---
    public static HashMap<String, Double> TFIDF (String input) {
        Gson gson = new Gson();
        BufferedReader reader;
        JsonObject[] businessData = new JsonObject[150345], businessReview = new JsonObject[150345];



        // ---=== READ AND RELATE BUSINESS, BUSINESS NAME, AND BUSINESS ID ===---
        int numofBusinesses = 0;
        try{
            reader = new BufferedReader(new FileReader("dataset/yelp_academic_dataset_business.json"));
            String documentLine;
            while ( (documentLine = reader.readLine()) != null ){
                JsonObject business = gson.fromJson(documentLine, JsonObject.class);
                String id = String.valueOf(business.get("business_id")).substring(1,23);
                String name = String.join(" ", String.valueOf(business.get("name")).split("[^a-zA-Z0-9'&-]+")).substring(1);
                businessHashMap.put(id, new Business(id));
                bNamesHashMap.put(id, name);
                if(!BNames.containsKey(name)) {
                    BNames.put(name, id);
                }
//                System.out.println(name + ": " + BNames.get(name));
                numofBusinesses++;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }


//        //map business id to business. Why? when I want a business I just have to look through the hashMap for the id as the key
        for (Business business : businessHashMap.values()){
            String id = String.valueOf(business.getBusinessID());
            businessHashMap.put(id, new Business(id));
        }



        //---=== READ AND RELATE BUSINESS, BUSINESS NAME, AND REVIEW ===---
        int numOfReviews = 0;
        try{
            reader = new BufferedReader(new FileReader("dataset/yelp_academic_dataset_review.json"));
            while (numOfReviews < businessData.length){
                String documentLine = reader.readLine();
                businessReview[numOfReviews] = gson.fromJson(documentLine, JsonObject.class);
                numOfReviews++;
            }
        }
        catch (IOException e) {
            e.printStackTrace();;
        }

        for (JsonObject jsonObject : businessReview) {
            if (jsonObject != null ){
                String id = String.valueOf(jsonObject.get("business_id")).substring(1, 23);

                String review = String.join(" ", String.valueOf(jsonObject.get("text")).split("[^a-zA-Z0-9'&]+"));
                businessHashMap.put(id, new Business(id));
                businessHashMap.get(id).setName(bNamesHashMap.get(id));
                businessHashMap.get(id).setReview(review);
            }
        }
        System.out.println("A)  READ AND RELATE BUSINESS, BUSINESS ID, and business names ---> DONE");





        // ---=== FILL WORD FREQUENCY TABLE ===---
        for (Business business : businessHashMap.values()){
            if(business.getReview() != null) {
                Set<String> words = new HashSet<>(List.of(business.getReview().split("[^a-zA-Z0-9'&]+"))); //Add the words in the review to a set to avoid dublicates
                for (String term : words ){
                    if(!term.equals("")) {
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
        for( Business business : businessHashMap.values()) {
            HT wordInReviewFrequency = new HT(); //key --> word; value --> count
//            Get the frequency of a word in the reviews of each business
            if(business.getReview() != null) {
                Set<String> words = new HashSet<>(); //Add the words in the review to a set to avoid dublicates
                for (String oneWord : business.getReview().split("[^a-zA-Z0-9'&]+")) {
                    if(oneWord != null && (!oneWord.equals(""))) {
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
            for (String term : wordInReviewFrequency.getKeySet()){
                double countOfOneTerm = wordInReviewFrequency.value(term);
                double totalTermsInReview = wordInReviewFrequency.size;
                TF.add(term, (countOfOneTerm/totalTermsInReview));
                business.setTF(TF);
            }
        }
        System.out.println("C)  TF Try 2 ---> DONE");



        // ---=== IDF ===---
        //Done to the document that holds the reviews
        HT IDF = new HT(); //key --> business name; value --> idf of business
        //log10( (total number of reviews(i) )/(number of documents with the word in it) )
        for (String s: wordFrequencyTable.getKeySet()){
            String name = s;
            Double count = wordFrequencyTable.value(s);
            double idf = Math.log10( numOfReviews/(count + 1));
            IDF.add(name, idf);
        }
        System.out.println("D)  IDF ---> DONE");



        // ---=== TF*IDF ===---
        for (Business business : businessHashMap.values()){
            for (String termInReview : business.getTF().getKeySet()){
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
    public static HashMap<String, Double>  cosineVector(String inputID){ //Cosinevector = (a * b)/( ( squart(A^2) )( squart(B^2) )
        Business userBusiness = businessHashMap.get(inputID);
        HashMap<String, Double> similarityResults = new HashMap<>();
        HashMap<String, Double> result = new HashMap<>(); //key --> name; value --> CSV result

        //calculate cosine vector
        for (Business business : businessHashMap.values()){
            double dotProductResult = 0.0;
            double userBusinessMagn = 0.0;
            double compareBusinessesMagn = 0.0;
            //Dot product ---> (a * b)
            for (String term : userBusiness.getTFIDF().getKeySet()){
                double userBusinessTFIDF = userBusiness.getTFIDF().value(term);
                double compareBusinessesTFIDF = business.getTFIDF().value(term);
                double product = userBusinessTFIDF * compareBusinessesTFIDF;
                dotProductResult = dotProductResult + product;
                //magnitudes
                userBusinessMagn = userBusinessMagn + (Math.pow(userBusinessTFIDF,2));
                compareBusinessesMagn = compareBusinessesMagn + (Math.pow(compareBusinessesTFIDF,2));
            }
            userBusinessMagn = Math.sqrt(userBusinessMagn);
            compareBusinessesMagn = Math.sqrt(compareBusinessesMagn);
            double csvResult = dotProductResult/((userBusinessMagn * compareBusinessesMagn)+0.0001);
            similarityResults.put(business.getBusinessID(), csvResult);
        }
        List<Map.Entry<String, Double>> sortedResults = new ArrayList<>(similarityResults.entrySet());
        sortedResults.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));


        // Get top similar businesses
        int mostSimilar = 3; // Change this value to get more top businesses
        System.out.println("Top " + mostSimilar + " similar businesses to " + userBusiness.getName() + ":");
        for (int i = 0; i < Math.min(mostSimilar, sortedResults.size()); i++) {
            Map.Entry<String, Double> reslt = sortedResults.get(i);
            String similarBusinessName = businessHashMap.get(reslt.getKey()).getBusinessID();
            String name = bNamesHashMap.get(businessHashMap.get(reslt.getKey()).getBusinessID());
            double similarityResult = reslt.getValue();
            result.put(name, similarityResult);
            System.out.println(name + ": " + similarityResult);
            System.out.println(businessHashMap.get(businessHashMap.get(reslt.getKey()).getBusinessID()).getReview());
        }
        System.out.println();
        serializeBusinesses();
        return result;
    }


    // ---=== Serialization ===---
    public static void serializeBusinesses (){
        Business b;
        File checkFile;
        for (Business business : businessHashMap.values()){
            b = business;
            String filename = "Serialized businesses/" + business.getBusinessID();
            FileOutputStream fileOut = null;
            ObjectOutputStream objOut = null;
            //Actual serialization
            try{
                File directory = new File("Serialized businesses");
                if (!directory.exists()) {
                    directory.mkdir();
                    businessPHT = new PHT(false);
                } else{
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
        System.out.println("Businesses Serialized :)");

    }


    public static void main(String[] args) {
        String input = "Starbucks";
        TFIDF(input);
//        System.out.println(bNamesHashMap.size());
    }


}
