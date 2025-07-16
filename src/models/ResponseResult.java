package models;

public class ResponseResult {
    public double relevance_score;
    public boolean is_relevant;
    public String confidence;     // optional: for MiniLM response
    public String reasoning;      // optional: for Mistral response
}
