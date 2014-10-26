package com.cmcm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
/**
 * 读取配置文件
 * @author zhangrujing
 *
 */
public enum AppContext {

	INSTANCE;
	
	private volatile Properties configuration = new Properties();
	
	public void init(){
		InputStream is = this.getClass().getResourceAsStream("/app.properties");
        if (is != null) {
            try {
                this.configuration.clear();
                this.configuration.load(is);
            } catch (IOException e) {
            } finally {
                try {
                    is.close();
                } catch (Throwable t) {}
            }
        }
	}
	
	public String getConfigValue(String key) {
		  return this.configuration.getProperty(key);
	}
	
	public boolean isTestRunMod(){
		String runMod = this.configuration.getProperty("run_mod");
		return (runMod != null && "test".equals(runMod));
	}
	
}
