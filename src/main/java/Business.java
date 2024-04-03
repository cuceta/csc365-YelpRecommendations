import java.util.ArrayList;
import java.io.Serializable;
public class Business implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
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


    //to string
    @Override
    public String toString() {
        return "Business{" +
                "name='" + name + '\'' +
                ", businessID='" + businessID + '\'' +
                ", cluster=" + cluster +
                '}';
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

    public void addClusterDistanceArraylist(double clusterDistance) {this.clusterDistance.add(clusterDistance);}

    //other methods
    public void addTFIDF(String term, double termTFIDF){
        this.TFIDF.add(term, termTFIDF);
    }

}
