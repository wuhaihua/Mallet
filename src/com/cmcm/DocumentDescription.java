package com.cmcm;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import com.cmcm.NeighborSimilarity;

public class DocumentDescription {
	private String Article_ID;
	private int Cluster_ID;	
	private String path;
	private int Layout_Score;
	private int time;
	private int PageRank_Score;
	private int Cluster_Number;
	
	private List<NeighborSimilarity> Neighbor_Similarity;
	
	
	public DocumentDescription(String Article_ID, int Cluster_ID, String path, int Layout_Score, int time, int PR, int Cluster_Number) {
		this.Article_ID = Article_ID;
		this.Cluster_ID = Cluster_ID;
		this.path = path;
		this.Layout_Score = Layout_Score;
		this.time = time;
		this.PageRank_Score = PR;
		this.Cluster_Number = Cluster_Number;	
		
		Neighbor_Similarity = new ArrayList<NeighborSimilarity>();
		
		
	};
	
	public void Set_Article_ID(String nID) {
		Article_ID = nID;
	};
	
	public String Get_Article_ID() {
		return Article_ID;
	};
	
	public int Get_Cluster_ID() {
		return Cluster_ID;
	};
	
	public String Get_path() {
		return path;
	};
	
	public int Get_Layout_Score() {
		return Layout_Score;
	};
	
	public int Get_time() {
		return time;
	};
	
	public int Get_PageRank() {
		
		return PageRank_Score;
	}
	

	
	public void Add_Neigbor_Similarity(String article_ID, Double similarity) {
		
		Neighbor_Similarity.add(new NeighborSimilarity(article_ID, similarity));		
		
	}
	
	public int Get_Neighbor_Size() {
		return Neighbor_Similarity.size();
	}
	/*
	public NeighborSimilarity GetNeighbor(int index) {
		return Neighbor_Similarity.get(index);
	}
	*/
	/*
	public String GetNeighborID(int index) {
		return Neighbor_Similarity.get(index).Get_ID();
	}
	
	public Double GetSimilarity(int index) {
		return Neighbor_Similarity.get(index).Get_Similarity();
	}	
	*/

	public NeighborSimilarity GetNeighbor(int j) {
		// TODO Auto-generated method stub
		return Neighbor_Similarity.get(j);
	}

	public int Get_Cluster_Number() {
		// TODO Auto-generated method stub
		return Cluster_Number;
	}
	
	
	
	

}
