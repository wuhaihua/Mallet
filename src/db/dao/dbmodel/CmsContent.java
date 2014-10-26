package db.dao.dbmodel;

public class CmsContent{
/*
	private int id;
	private int cid;
	private String title;
	private String simTitle;
	private String contentText;
	private String poster;
	private String source;
	private String pic1;
	private String pic2;
	private String pic3;
	private String postDate;
	private String editDate;
	private String keyWord;
	private int isPass;
	private int operate;
	private int isReview;
	private int isDel;
	private String sourceUrl;
	private String originalTime;
	private int classID;
	private int LBClassID;
	private int siteID;
	private int siteTypeID;
	private int Poolid;
	private int PR;
	private int PRExt;
	private int reviewCount;
	private int importantLevel;
	private String contentNoHTML;
	*/
	
	private int id;
	private int cid;
	private String title;
	private String simTitle;
	private String content;
	private String contentText;
	private String contentNoHTML;
	private long ContentLength;
	private String poster;
	private String source;
	private String pic1;
	private String pic2;
	private String pic3;
	private String postDate;
	private String editDate;
	private String keyWord;
	private int isPass;
	private int operate;
	private int isReview;
	private int isDel;
	private String sourceUrl;
	private String originalTime;
	private int classID;
	private int LBClassID;
	private int siteID;
	private int siteTypeID;
	private String TagsID;
	private String Tags;
	private int Poolid;
	private int IsShouDong;
	private int PR;
	private int PRExt;
	private int importantLevel;
	private int ContentRuleID; 
	private int CopyNumber;
	
/*
	public int getReviewCount() {
		return reviewCount;
	}
	public void setReviewCount(int reviewCount) {
		this.reviewCount = reviewCount;
	}
	*/
	public int getId() {
		return id;
	}	
	public void setId(int id) {
		this.id = id;
	}
	public int getCid() {
		return cid;
	}
	public void setCid(int cid) {
		this.cid = cid;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public String getSimTitle() {
		return simTitle;
	}
	public void setSimTitle(String simTitle) {
		this.simTitle = simTitle;
	}
	public String getPoster() {
		return poster;
	}
	public void setPoster(String poster) {
		this.poster = poster;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getPic1() {
		return pic1;
	}
	public void setPic1(String pic1) {
		this.pic1 = pic1;
	}
	public String getPic2() {
		return pic2;
	}
	public void setPic2(String pic2) {
		this.pic2 = pic2;
	}
	public String getPic3() {
		return pic3;
	}
	public void setPic3(String pic3) {
		this.pic3 = pic3;
	}
	
	public int countPicNum() {
		int count = 0;
		
		if( !pic1.isEmpty() ) {
			count++;
		}
		
		if( !pic2.isEmpty() ) {
			count++;
		}
		
		if( !pic3.isEmpty() ) {
			count++;
		}
		
		return count;
	}
	
	public long getContentLength() {
		return ContentLength;
	}
	
	public int getCopyNumber() {
		return CopyNumber;
	}
	
	public String getPostDate() {
		return postDate;
	}
	public void setPostDate(String postDate) {
		this.postDate = postDate;
	}
	public String getEditDate() {
		return editDate;
	}
	public void setEditDate(String editDate) {
		this.editDate = editDate;
	}
	public String getKeyWord() {
		return keyWord;
	}
	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}
	public int getIsPass() {
		return isPass;
	}
	public void setIsPass(int isPass) {
		this.isPass = isPass;
	}
	public int getOperate() {
		return operate;
	}
	public void setOperate(int operate) {
		this.operate = operate;
	}
	public int getIsReview() {
		return isReview;
	}
	public void setIsReview(int isReview) {
		this.isReview = isReview;
	}
	public int getIsDel() {
		return isDel;
	}
	public void setIsDel(int isDel) {
		this.isDel = isDel;
	}
	public String getSourceUrl() {
		return sourceUrl;
	}
	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}
	public String getOriginalTime() {
		return originalTime;
	}
	public void setOriginalTime(String originalTime) {
		this.originalTime = originalTime;
	}
	public int getClassID() {
		return classID;
	}	
	public void setClassID(int classID) {
		this.classID = classID;
	}
	public int getLBClassID() {
		return LBClassID;
	}	
	public void setLBClassID(int classID) {
		this.LBClassID = classID;
	}
	public int getSiteID() {
		return siteID;
	}
	public void setSiteID(int siteID) {
		this.siteID = siteID;
	}
	public int getSiteTypeID() {
		return siteTypeID;
	}
	public void setSiteTypeID(int siteTypeID) {
		this.siteTypeID = siteTypeID;
	}
	public int getPR() {
		return PR;
	}
	
	public int getRevisedPR() {
		
		int revised_PR = PR + PRExt;
		if ( revised_PR > 10 ) {
			revised_PR = 10;
		} else if (revised_PR < 1) {
			revised_PR = 1;			
		}
		return revised_PR;		
	}
	
	public void setPR(int pR) {
		this.PR = pR;
	}
	
	public int getPRExt() {
		return PRExt;
	}
	
	public void setPRExt(int pRExt) {
		this.PR = pRExt;
	}
	
	
	public String getContentText() {
		return contentText;
	}
	public String getContentNoHTML() {
		return contentNoHTML;
	}
	
	public void setContentNoHTML(String contentNoHTML) {
		this.contentNoHTML = contentNoHTML;
	}
	
	public void setContentText(String contentText) {
		this.contentText = contentText;
	}
	public int getImportantLevel() {
		return importantLevel;
	}
	public void setImportantLevel(int importantLevel) {
		this.importantLevel = importantLevel;
	}

	
	
}
