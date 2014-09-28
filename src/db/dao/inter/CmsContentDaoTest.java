package db.dao.inter;


import java.io.Reader;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import db.dao.dbmodel.CmsContent;
import db.dao.dbmodel.NewsClassStat;

public class CmsContentDaoTest {

	private SqlSessionFactory sessionFactory;
	@Before
	public void setUp() throws Exception {
		Reader cmUnAccReader = Resources.getResourceAsReader("Configuration_api.xml");
		sessionFactory = new SqlSessionFactoryBuilder().build(cmUnAccReader);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	
	@Test
	public void testGet(){
		SqlSession cmUnAccSession = sessionFactory.openSession();
		CmsContentDao cmsContentDao = cmUnAccSession.getMapper(CmsContentDao.class);
		List<NewsClassStat> list = cmsContentDao.getCountByCid("20140902", "20140901");
		System.out.println(list.size());
	}
	@Test
	public void testGetAll(){
		SqlSession cmUnAccSession = sessionFactory.openSession();
		CmsContentDao cmsContentDao = cmUnAccSession.getMapper(CmsContentDao.class);
		List<CmsContent> list = cmsContentDao.getContentAll("20140902", "20140901");
		System.out.println(list.size());
	}

}
