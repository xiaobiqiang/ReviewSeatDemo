package com.qq1312952829.exe;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.w3c.dom.Document;

import com.qq1312952829.encode.ruokuai.RuoKuai;

public class ReviewExe extends Thread {
	//设置firefox可执行文件的位置，若是安装在默认位置就不用设置。
	//新版不能直接打开firefox，需要下载geckodriver.exe来设置
	static {
		//在root path文件路径下新建config/config.properties属性文件来读取配置。
		File f = new File("config/config.properties");
		FileInputStream fis = null;
		if(f.exists()) {
			try {
				fis = new FileInputStream(f);
				Properties prop = new Properties();
				prop.load(fis);
				String browserPath = prop.getProperty("firefox_path");
				String driverPath = prop.getProperty("gecko_driver_path");
				System.out.println(browserPath);
				System.setProperty(
									FirefoxDriver.SystemProperty.BROWSER_BINARY, 
									browserPath
									);
				System.setProperty(
						"webdriver.gecko.driver", 
						driverPath
						);
				//加载opencv库
				System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if(fis != null)
						fis.close();
				}catch(IOException e) {
				}
			}
		}
	}
	
	private WebDriver firefoxDriver;
	
	private final String libUrl = 
			"http://seat.lib.whu.edu.cn/login?targetUri=%2F";
	private final String userName;
	private final String pwd;
	private int roomIndex = 0;
	private int tsIndex = 0;
	private int teIndex = 0;
	private int seatIndex = 0;
	/**
	 * 查询方式。
	 * 0代表范围查找
	 * 1代表座位查找
	 */
	private int searchType = 0;
	
	/**
	 * 这是范围查找的构造函数
	 */
	public ReviewExe(String id, String pwd, int roomIndex, int tsIndex, int teIndex) {
		super();
		this.searchType = 0;
		
		this.userName = id;
		this.pwd = pwd;
		this.roomIndex = roomIndex;
		this.tsIndex = tsIndex;
		this.teIndex = teIndex;
	}
	/**
	 * 这是座位查找的构造函数
	 * 要注意的就是this，super构造函数要放在最前面的原因：覆盖属性
	想象：this构造函数放在最后面，那么this构造函数设置searchType就会覆盖我们想要的设置值。
	而若是放在最前面，我们可以在后面设置我们想要的属性。
	 */
	public ReviewExe(String id, String pwd, int roomIndex, int seatIndex, int tsIndex, int teIndex) {
		this(id, pwd, roomIndex, tsIndex, teIndex);
		
		this.searchType = 1;
		this.seatIndex = seatIndex;
	}
	
	@Override
	public void run() {
		startReviewSeat();
	}

	private void ensureToClock() {
		Calendar time = Calendar.getInstance(Locale.CHINESE);
		long ts = Long.parseLong(	time.get(Calendar.YEAR) + 
									"" +
									time.get(Calendar.MONTH) + 
									time.get(Calendar.DAY_OF_MONTH) +
									"223020"
									);
		long current = 0L;
		do {
			time = Calendar.getInstance(Locale.CHINESE);
			current = Long.parseLong(	time.get(Calendar.YEAR) + 
										"" +
										time.get(Calendar.MONTH) + 
										time.get(Calendar.DAY_OF_MONTH) +
										time.get(Calendar.HOUR_OF_DAY) +
										time.get(Calendar.MINUTE) +
										time.get(Calendar.SECOND)
										);
			try {
				Thread.sleep(200);  //休眠可以降低CPU的占用率
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}while(current < ts);
	}
	
	private void startReviewSeat() {
		//等到时间
		this.ensureToClock();
		//打开链接
		if(!this.openBrowserByUrl(this.libUrl))
			return;
		//登陆图书馆
		this.loginLibrary(this.userName, this.pwd);
		//开始预定
		this.reviewStart();
	}

	private boolean openBrowserByUrl(String url) {
		boolean state = true;
		try {
			this.firefoxDriver = new FirefoxDriver();
			//打开链接url
			firefoxDriver.get(url);
			//窗口最大化
			firefoxDriver.manage().window().maximize();
		} catch(org.openqa.selenium.WebDriverException e) {
			state = false;
			if(null != this.firefoxDriver)
				this.firefoxDriver.close();
		}
		
		return state;
	}
	
	private void loginLibrary(String userName, String password) {
		//找到userName输入文本框：源代码发现name = "username"
		WebElement userNameText = this.firefoxDriver.findElement(By.name("username"));
		//找到password文本输入框:源代码发现id = "bor_verification"
		WebElement pwdText = this.firefoxDriver.findElement(By.name("password"));
		//找到验证码输入框
		WebElement captchaText = this.firefoxDriver.findElement(By.id("captcha"));
		//找到登陆按钮:了解xpath,多个tr,div什么的并列从1开始数，定位，比如tr[5]
		WebElement loginBtn = this.firefoxDriver.findElement(By.className("btn1"));
		
		//输入userName
		userNameText.sendKeys(userName);
		//输入password
		pwdText.sendKeys(password);
		//获取验证码
		String captcha = this.findCAPTCHA();
		//输入验证码
		captchaText.sendKeys(captcha);
		//点击登陆
		loginBtn.click();
	}
	
	private String findCAPTCHA() {
		String captchaSavepath = "captcha/captcha.png";
		cutCaptchaImageToFile(captchaSavepath);
		String captcha = recognizeImageText(captchaSavepath);
		
		return captcha;
	}
	
	private void cutCaptchaImageToFile(String path) {
		byte[] scrshootBytes = ((FirefoxDriver)this.firefoxDriver).getScreenshotAs(OutputType.BYTES);
		
		ByteArrayInputStream bis = null;
		try {
			bis = new ByteArrayInputStream(scrshootBytes);
			BufferedImage scrshootImg = ImageIO.read(bis);
			
			WebElement captchaLabel = this.firefoxDriver.findElement(By.xpath("//img[@class='code']"));
			Rectangle labelLocation = captchaLabel.getRect();
			BufferedImage captchaImage = scrshootImg.getSubimage(
															labelLocation.x, 
															labelLocation.y, 
															labelLocation.width, 
															labelLocation.height
															);
			File dstFile= new File(path);
			if(!dstFile.exists()) {
				if(!dstFile.getParentFile().exists())
					dstFile.getParentFile().mkdirs();
				dstFile.createNewFile();
			}
			ImageIO.write(captchaImage, "png", dstFile);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(null != bis)
					bis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * path is the src image path, then convert the src image to binary one,
	 * so that we can pip it to RuoKuai website to recognize well.
	 * threshold is standard line than which gray image RGB is set to 255 when larger.
	 * return the path the binary image saved.
	 */
	private String binarySrcImg(String path, int threshold) {
		Mat src = Imgcodecs.imread(path);
		
		double[] temp = new double[src.channels()];
		double min;
		for(int i=0; i<src.cols(); i++) {
			for(int j=0; j<src.rows(); j++) {
				temp = src.get(j, i);
				min = Double.MAX_VALUE;  //别忘了这一句
				for(int k=0; k<temp.length; k++) {
					min = min < temp[k] ? min : temp[k];
					temp[k] = 255;
				}
				
				if(min > threshold)
					src.put(j, i, temp);
			}
		}
		
		String dstpath = new File(path).getParent() + "/binary.png";
		Imgcodecs.imwrite(dstpath, src);
		
		return dstpath;
	}
	
	private String recognizeImageText(String imgpath) {
		String binarypath = this.binarySrcImg(imgpath, 140);
		String xml = RuoKuai.createByPost(	"xiaobiqiang", 
											"qqqwwe1995.11.01", 
											"3050",
											"60", 
											"1", 
											"b40ffbee5c1cf4e38028c197eb2fc751", 
											binarypath);
		String out = null;
		DocumentBuilderFactory docbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder docBuilder = docbf.newDocumentBuilder();
			Document doc = docBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
			out = doc.getElementsByTagName("Result").item(0).getFirstChild().getNodeValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return out;
	}
	
	private void reviewStart() {
		if(!this.setCondition())
			return;
		
		if(this.searchType == 0)
			this.findSeatBySearch();
		else if(this.searchType == 1)
			this.findSeatByNumSearch();
		else
			return ;
		
		this.setSeatTime();
	}
	
	private boolean setCondition() {
		//登陆失败时(可能账号密码错误，也可能是验证码解码失败),执行这个函数会出现异常
		//捕获异常，若没有说明登陆成功了
		boolean state= true;
		try {
			//选定日期
			this.firefoxDriver.findElement(By.id("display_onDate")).click();
			//取下个日期，格式化输出，add增加会自动保证正确性，(自动改变成合适的年份和月份)
			Calendar nextDate = Calendar.getInstance();
			nextDate.add(Calendar.DAY_OF_MONTH, 0);
			Date next = nextDate.getTime();
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String dst = format.format(next);
			this.firefoxDriver.findElement(By.xpath("//p[@id='options_onDate']/a[@value='" + dst + "']")).click();
			//选定场馆
			this.firefoxDriver.findElement(By.id("display_building")).click();
			this.firefoxDriver.findElement(By.xpath("//p[@id='options_building']/a[@value='1']")).click();
			//选定房间，=0是不限房间，所以使用默认
			if(roomIndex > 0) {
				this.firefoxDriver.findElement(By.id("display_room")).click();
				WebElement roomEle = this.firefoxDriver.findElement(By.xpath("//div[@id='room_select']/p/a[@value='" + (roomIndex+3) + "']"));
				//要在可见范围内才能点击选定
				((JavascriptExecutor)this.firefoxDriver).executeScript("arguments[0].scrollIntoView();", roomEle); 
				roomEle.click();
			}
			
			//选定开始时间,并列下标从1开始
			this.firefoxDriver.findElement(By.id("display_startMin")).click();
			WebElement tsEle = this.firefoxDriver.findElement(By.xpath("//p[@id='options_startMin']/a[" + (tsIndex+1) + "]"));
			((JavascriptExecutor)this.firefoxDriver).executeScript("arguments[0].scrollIntoView();", tsEle); 
			tsEle.click();
			
			//选定结束时间
			this.firefoxDriver.findElement(By.id("display_endMin")).click();
			WebElement teEle = this.firefoxDriver.findElement(By.xpath("//p[@id='options_endMin']/a[" + (teIndex+1) + "]"));
			((JavascriptExecutor)this.firefoxDriver).executeScript("arguments[0].scrollIntoView();", teEle); 
			teEle.click();
		} catch(org.openqa.selenium.NoSuchElementException e) {
			state = false;
			if(null != this.firefoxDriver)
				this.firefoxDriver.close();
		}
		
		return state;
	}
	
	/**
	 * 存在空闲座位默认返回第一个WebElement
	 * 不存在返回null。
	 */
	private WebElement selectFreeSeat() {
		WebElement out = null;
		try {
			out= this.firefoxDriver.findElement(By.xpath("//li[@class='free']/dl[1]/dt"));
		} catch(NoSuchElementException e) {
		} 
		
		return out;
	}

	private void findSeatBySearch() {	
		//找到查询按钮
		this.firefoxDriver.findElement(By.cssSelector("input.searchBtn.fl")).click();
	}
	
	private void findSeatByNumSearch() {
		//选定座位序号,className有空格等复合符号不能直接用，用cssSelector
		this.firefoxDriver.findElement(By.cssSelector("input.seatBtn.fl")).click();
		//找到数字链表:1,2,3,4,5,6,7,8,9,0
		List<WebElement> numList = this.firefoxDriver.findElements(By.className("seatLink"));
		//得到数字的每位的数字
		int[] hitNums = intCvtIntArrayByBit(this.seatIndex);
		//从高位到地位一次点击
		for(int i=hitNums.length-1; i>=0; i--) {
			int k = hitNums[i];
			if(hitNums[i] == 0)
				k = 10;
			numList.get(k-1).click();
		}
		//确认
		this.firefoxDriver.findElement(By.id("searchBySeatID")).click();
	}
	
	//拆分成低位在前，高位在后
	private int[] intCvtIntArrayByBit(int count) {
		int temp[] = new int[3];
		
		int size = 0, t;
		do {
			count = (t = count) / 10;
			temp[size++] = t - count * 10;
		}while(count != 0);
		
		int[] dst = new int[size];
		System.arraycopy(temp, 0, dst, 0, size);
		
		return dst;
	}

	
	private boolean setSeatTime() {
		//缓冲，使搜索完成
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} 
		boolean state = true;
		WebElement freeEle = null;
		if(null == (freeEle = selectFreeSeat())) {
			state = false;
			this.firefoxDriver.close();
			return state;
		}
		
		freeEle.click();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		//选开始时间,由于显示的时间是以半小时为最小刻度的，
		//因此还需对tsIndex做变换，变换到后一个:30或:00
		if(tsIndex > 0) {
			tsIndex = tsIndex%2 == 0 ? tsIndex+1 : tsIndex;
			this.firefoxDriver.findElement(By.xpath("//div[@id='startTime']/dl/ul/li/a[@time='" + ((tsIndex-1)*15) + "']")).click();
		}
		else if(tsIndex == 0) {
			List<WebElement> tsEles = this.firefoxDriver.findElements(By.xpath("//div[@id='startTime']/dl/ul/li"));
			tsEles.get(0).click();
		}
		
		//选结束时间,结束时间起码以00：15开始
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(teIndex > 1) {
			teIndex = teIndex%2 == 0 ? teIndex-1 : teIndex;
			this.firefoxDriver.findElement(By.xpath("//div[@id='endTime']/dl/ul/li/a[@time='" + ((teIndex-1)*15) + "']")).click();
		}
		else if(teIndex == 0 || teIndex == 1) {
			List<WebElement> teEles = this.firefoxDriver.findElements(By.xpath("//div[@id='endTime']/dl/ul/li"));
			teEles.get(teEles.size()-1).click();
		}
		
		//得到验证码
		String captcha = this.findCAPTCHA();
		//找到验证码输入框,输入验证码
		this.firefoxDriver.findElement(By.xpath("//input[@id='captchaValue']")).sendKeys(captcha);
		//找到确认预约按钮并点击
		this.firefoxDriver.findElement(By.id("reserveBtn")).click();
		
		return state;
	}
}
