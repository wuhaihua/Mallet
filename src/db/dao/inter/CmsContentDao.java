package db.dao.inter;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import db.dao.dbmodel.CmsContent;
import db.dao.dbmodel.NewsClassStat;

public interface CmsContentDao {

	public CmsContent get(int id);

	public List<CmsContent> getContentList(@Param("max") int max, @Param("min") int min, @Param("cid") int cid);

	public List<CmsContent> getContentAll(@Param("maxDate") String maxDate, @Param("minDate") String minDate);

	public List<NewsClassStat> getCountByCid(@Param("maxDate") String maxDate, @Param("minDate") String minDate);
	
	public List<CmsContent> getContentAllChinese(@Param("maxDate") String maxDate, @Param("minDate") String minDate);
	
	
}
