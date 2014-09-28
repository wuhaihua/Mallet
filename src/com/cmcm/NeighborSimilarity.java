package com.cmcm;

public class NeighborSimilarity {

	public String Article_ID;
	public Double Similarity;
	
	public NeighborSimilarity(String Article_ID, Double Similarity) {
		this.Article_ID = Article_ID;
		this.Similarity = Similarity;
	}
	
	
	public Double Get_Similarity() {
		return Similarity;
	}

	public String Get_ID() {
		// TODO Auto-generated method stub
		return Article_ID;
	}
}
