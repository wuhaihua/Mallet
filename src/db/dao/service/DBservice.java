package db.dao.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import db.dao.dbmodel.CmsContent;
import db.dao.dbmodel.NewsClassStat;
import db.dao.dbmodel.ClientCategoryStat;
import db.dao.inter.CmsContentDao;

public class DBservice {

	private SqlSession sqlSessionApi;
	private SqlSessionFactory sqlSessionFactoryApi;

	public DBservice(String LanguagePick) {
		Reader readerApi;
		try {
			if ( LanguagePick.equals("eng")) {
				readerApi = Resources.getResourceAsReader("Configuration_api_eng.xml");
				sqlSessionFactoryApi = new SqlSessionFactoryBuilder().build(readerApi);
			} else if ( LanguagePick.equals("chn")) {
				readerApi = Resources.getResourceAsReader("Configuration_api_chn.xml");
				sqlSessionFactoryApi = new SqlSessionFactoryBuilder().build(readerApi);
				
			}			

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public SqlSession getSqlSessionApi() {
		if (sqlSessionApi != null)
			return sqlSessionApi;
		else
			return this.sqlSessionFactoryApi.openSession();
	}

	public void getDBcontent(int id) {

		SqlSession sqlSession = this.getSqlSessionApi();
		CmsContentDao cmsContentDao = sqlSession.getMapper(CmsContentDao.class);

		CmsContent cmsContent = cmsContentDao.get(id);

		System.out.println("id:" + cmsContent.getId());

	}

	public List<CmsContent> getContent(String max_Time,String min_Time, String LanguagePick) {

		SqlSession cmUnAccSession = this.getSqlSessionApi();
		CmsContentDao cmsContentDao = cmUnAccSession.getMapper(CmsContentDao.class);

		List<CmsContent> cmsContent = null;
		
		if ( LanguagePick.equals("eng")) {
			cmsContent = cmsContentDao.getContentAll(max_Time,min_Time);
		} else if ( LanguagePick.equals("chn")) {
			cmsContent = cmsContentDao.getContentAllChinese(max_Time,min_Time);	//Go Chinese branch		
		}

		System.out.println("size:" + cmsContent.size());
		cmUnAccSession.close();

		return cmsContent;

	}
	
	public List<CmsContent> getContent(int id) {

		SqlSession cmUnAccSession = this.getSqlSessionApi();
		CmsContentDao cmsContentDao = cmUnAccSession.getMapper(CmsContentDao.class);

		List<CmsContent> cmsContent = null;
		
		cmsContent = cmsContentDao.getContentByid(id);	//Go Chinese branch		
		

		System.out.println("size:" + cmsContent.size());
		cmUnAccSession.close();

		return cmsContent;

	}

	public List<NewsClassStat> DBgetCountGroupCid(String max_editdata,String min_editdata) {
		try {
//		Reader cmUnAccReader = Resources.getResourceAsReader("Configuration_api.xml");
//		
//		SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(cmUnAccReader);
//		DBservice db = new DBservice();
		SqlSession cmUnAccSession = this.getSqlSessionApi();
//		SqlSession cmUnAccSession = sessionFactory.openSession();
		CmsContentDao cmsContentDao = cmUnAccSession.getMapper(CmsContentDao.class);
		List<NewsClassStat> list = cmsContentDao.getCountByCid(max_editdata, min_editdata);
		return list;
		
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		return null;
	}
	
	
	public List<ClientCategoryStat> DBgetCountClientGroupCid(String max_editdata,String min_editdata) {
		try {
//		Reader cmUnAccReader = Resources.getResourceAsReader("Configuration_api.xml");
//		
//		SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(cmUnAccReader);
//		DBservice db = new DBservice();
		SqlSession cmUnAccSession = this.getSqlSessionApi();
//		SqlSession cmUnAccSession = sessionFactory.openSession();
		CmsContentDao cmsContentDao = cmUnAccSession.getMapper(CmsContentDao.class);
		List<ClientCategoryStat> list = cmsContentDao.getCountByLBClassID(max_editdata, min_editdata);
		return list;
		
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		return null;
	}
	
	// 把cmsContent内容写入文件
	public boolean writeInFile(String dir, List<CmsContent> cmsContent, String LanguagePick) {
		if (dir.equals("") || cmsContent == null || cmsContent.size() == 0)
			return false;

		// BufferedWriter存在map中，
//		Map<Integer, BufferedWriter> map = new HashMap<Integer, BufferedWriter>();

		try {
			int num = cmsContent.size();

			for (int i = 0; i < num; i++) {
				CmsContent con = cmsContent.get(i);
				int client_cid = con.getLBClassID();

				
				
				 int id = con.getId();
				 
				if ( id == 141327 ) {
				 //if ( id == 113712 ) {
					System.out.println("article ID: " + id + " Found ");
				}

				
				
				// 拼装文件夹路径
				String foldPath = dir + "//" + client_cid;
				File f = new File(foldPath);
//				System.out.println("f.exists():" + f.exists());
				if (!f.exists()) {
					System.out.println("文件不存在，创建文件");
					f.mkdirs();
				}

				long editDate = getEditDate(con.getEditDate()) / 1000;
				
				//ArticleID, PR, EditTime, LayoutScore, CopyNum, PicNum, ArticleLength 

				String filePath = foldPath + "//" + con.getId() + "_" + con.getPR() + "_" + editDate + "_"+con.getImportantLevel()+"_"+con.getCopyNumber()+"_"+con.countPicNum()+"_"+con.getContentLength()+".txt";
				//String filePath = foldPath + "//" + con.getId() + "_" + StringFilter(con.getTitle()) +".txt";
						
//				filePath = foldPath + "//" + con.getId() + "_" + con.getPR() + "_" + editDate + "_"+ StringFilter(con.getTitle()) + ".txt";
				System.out.println("filePath:" + filePath);
				FileWriter fw  = null;
				try{
					fw = new FileWriter(filePath);
				}catch (FileNotFoundException e) {
					e.printStackTrace();
					continue;
				}
				BufferedWriter bw = new BufferedWriter(fw);
				
				if (LanguagePick.equals("eng")) {
					bw.write(con.getTitle() + " " + con.getSimTitle() + " " + con.getContentText());
					//In English version, duplicate titles and put into main text 
				} else if (LanguagePick.equals("chn")) {
					
					bw.write(con.getContentText());	// In Chinese version, Similar works is done in data base (along with word split)
					//bw.write(con.getContentNoHTML());	// In Chinese version, Similar works is done in data base (along with word split)
				}

				bw.flush();
				bw.close();
				// map.put(cid, bw_new);

				// }else{

				// bw.write(con.getContent());
				// }

			}

//			Iterator<Integer> it = map.keySet().iterator();
//			while (it.hasNext()) {
//				BufferedWriter bw = map.get(it.next());
//
//			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	
	public static String StringFilter(String str) throws PatternSyntaxException {
		// 只允许字母和数字
		// String regEx = "[^a-zA-Z0-9]";
		// 清除掉所有特殊字符
		String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		return m.replaceAll("").trim().replaceAll(" ", "").replaceAll("\"", "").replaceAll("\\-", "");
	}
	
	public Map<String, Integer> getCountByCid() {
		return null;
	}

	// editDate:2014-08-22 23:26:57
	public Long getEditDate(String editDate) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date d = sdf.parse(editDate);
			return d.getTime();

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0L;
	}

	public static void main(String[] args) {
		DBservice db = new DBservice("eng");
//		List<CmsContent> cmsContent = db.getContent();
//		db.writeInFile("db", cmsContent);
		System.out.println("db.currentTime:"+db.getCurrentTime("yyyyMMddHHmmss",-3));

	}
	
	//amount是偏移量，天数 Calendar.DAY_OF_MONTH
	public String getCurrentTime(String format,int amount){
		Date d = new Date(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.DAY_OF_MONTH, amount);
		return sdf.format(c.getTime());	
	}
	
	public String getCurrentTime(String format){
		return getCurrentTime(format,0);
	}

}