import java.util.HashMap;
import java.util.Hashtable;
import java.io.Serializable;
public class Business implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name, review, businessID;
    private HT termFrequency;
    private HT TF;
    private HT TFIDF;



    //constructor
    public Business ( String businessID){
//        this.name = name;
        this.businessID = businessID;
        termFrequency = new HT();
        TF = new HT();
        TFIDF = new HT();
    }

    //getters
    public String getName(){
        return name;
    }

    public HT getTFIDF() {
        return TFIDF;
    }

    public HT getTermFrequency(){
        return termFrequency;
    }
    public String getReview(){
        return review;
    }
    public String getBusinessID(){
        return businessID;
    }
    public HT getTF() { return  this.TF;}


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

    //othe methods
    public void addTFIDF(String term, double termTFIDF){
        this.TFIDF.add(term, termTFIDF);
    }
}
