package ${package}.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * 类名：CodeCounter.java<br>
 * <p>
 * 功能：统计项目代码量 工具类
 * </p>
 *
 * @Author:<a href="mailto:wxcmyx@163.com">lilong</a> <br/>
 * @Date:2013-3-15<br/>
 * @Time:下午5:12:11 <br/>
 * @Version:1.1 <br/>
 */
public class CodeCounter {
	private static final  Logger logger = LoggerFactory.getLogger(CodeCounter.class);
	private static Pattern PATTERN_ST = Pattern.compile("/\\*");
	private static Pattern PATTERN_END =Pattern.compile("\\*/");
	static long commentLine = 0;
	static long whiteLine = 0;
	static long normalLine = 0;
	static long totalLine = 0;
	static boolean comment = false;

	public static void main(String[] args) {
		File file = new File("./src"); // 在这里输入需要统计的文件夹路径
		getChild(file);
		System.out.println("有效代码行数: " + normalLine);
		System.out.println("注释行数: " + commentLine);
		System.out.println("空白行数: " + whiteLine);
		System.out.println("总代码行数: " + totalLine);
	}

	private static void getChild(File child) { // 遍历子目录
		if (child.getName().matches(".*\\.java$")) { // 只查询java文件
			BufferedReader br = null;
			try {
				 br =  new BufferedReader(new FileReader(child));
				String line = "";
				while ((line = br.readLine()) != null) {
					parse(line);
				}
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				logger.error(e1.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
			}finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
						logger.error(e.getMessage());
					}
				}

			}
		}
		if (child.listFiles() != null) {
			for (File f : child.listFiles()) {
				getChild(f);
			}
		}
	}

	private static void parse(String line) {
		line = line.trim();
		totalLine++;
		if (line.length() == 0) {
			whiteLine++;
		} else if (comment) {
			commentLine++;
			if (line.endsWith("*/")) {
				comment = false;
			} else if (line.matches(".*\\*/.+")) {
				normalLine++;
				comment = false;
			}
		} else if (line.startsWith("//")) {
			commentLine++;
		} else if (line.matches(".+//.*")) {
			commentLine++;
			normalLine++;
		} else if (line.startsWith("/*") && line.matches(".+\\*/.+")) {
			commentLine++;
			normalLine++;
			if (findPair(line)) {
				comment = false;
			} else {
				comment = true;
			}
		} else if (line.startsWith("/*") && !line.endsWith("*/")) {
			commentLine++;
			comment = true;
		} else if (line.startsWith("/*") && line.endsWith("*/")) {
			commentLine++;
			comment = false;
		} else if (line.matches(".+/\\*.*") && !line.endsWith("*/")) {
			commentLine++;
			normalLine++;
			if (findPair(line)) {
				comment = false;
			} else {
				comment = true;
			}
		} else if (line.matches(".+/\\*.*") && line.endsWith("*/")) {
			commentLine++;
			normalLine++;
			comment = false;
		} else {
			normalLine++;
		}
	}

	private static boolean findPair(String line) { // 查找一行中/*与*/是否成对出现
		int count1 = 0;
		int count2 = 0;
		Matcher m = PATTERN_ST.matcher(line);
		while (m.find()) {
			count1++;
		}
		m = PATTERN_END.matcher(line);
		while (m.find()) {
			count2++;
		}
		return (count1 == count2);
	}

}
