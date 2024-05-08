import java.util.ArrayList;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Business implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private double latitude;
    private double longitude;
    private List<Business> closestNeighbors;
    private String businessID;
    private int cluster;
    private String review;
    private double cosineSimilarity;
    private HT termFrequency;
    private HT TF;
    private HT TFIDF;
    private ArrayList<Double> clusterDistance;






    //constructor
    public Business ( String businessID){
//        this.name = name;
        this.businessID = businessID;
        this.termFrequency = new HT();
        this.TF = new HT();
        this.TFIDF = new HT();
        this.clusterDistance = new ArrayList<>();
    }


    //getters
    public String getName(){return name;}
    public HT getTFIDF() {return TFIDF;}
    public HT getTermFrequency(){return termFrequency;}
    public String getReview(){return review;}
    public String getBusinessID(){return businessID;}
    public HT getTF() { return  this.TF;}
    public int getCluster(){ return this.cluster;}
    public double getCosineSimilarity() {return cosineSimilarity;}
    public ArrayList<Double> getClusterDistance() {return clusterDistance;}
    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public List<Business> getClosestNeighbors() {
        return closestNeighbors;
    }

    //Setter
    public void setTermFrequency(HT termFrequency) {
        this.termFrequency = termFrequency;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setBusinessID(String businessID){
        this.businessID = businessID;
    }
    public void setReview(String review){
        this.review = review;
    }
    public  void  setTF (HT TF) { this.TF = TF;}
    public void setCluster(int cluster){this.cluster = cluster;}
    public void setTFIDF(HT TFIDF) {this.TFIDF = TFIDF;}
    public void setCosineSimilarity(double cosineSimilarity) {this.cosineSimilarity = cosineSimilarity;}
    public void setClusterDistance(ArrayList<Double> clusterDistance) {
        this.clusterDistance = clusterDistance;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setClosestNeighbors(List<Business> closestNeighbors) {
        this.closestNeighbors = closestNeighbors;
    }

    //other methods
    public void addTFIDF(String term, double termTFIDF){
        this.TFIDF.add(term, termTFIDF);
    }
    public void addClusterDistanceArraylist(double clusterDistance) {this.clusterDistance.add(clusterDistance);}

    @Override
    public String toString() {
        return "Business{" +
                "name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    public boolean busineesEquals(Business b){
        return this.name.equals(b.getName());
    }
}