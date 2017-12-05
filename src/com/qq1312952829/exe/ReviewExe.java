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
	//����firefox��ִ���ļ���λ�ã����ǰ�װ��Ĭ��λ�þͲ������á�
	//�°治��ֱ�Ӵ�firefox����Ҫ����geckodriver.exe������
	static {
		//��root path�ļ�·�����½�config/config.properties�����ļ�����ȡ���á�
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
				//����opencv��
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
	 * ��ѯ��ʽ��
	 * 0����Χ����
	 * 1������λ����
	 */
	private int searchType = 0;
	
	/**
	 * ���Ƿ�Χ���ҵĹ��캯��
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
	 * ������λ���ҵĹ��캯��
	 * Ҫע��ľ���this��super���캯��Ҫ������ǰ���ԭ�򣺸�������
	����this���캯����������棬��ôthis���캯������searchType�ͻḲ��������Ҫ������ֵ��
	�����Ƿ�����ǰ�棬���ǿ����ں�������������Ҫ�����ԡ�
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
				Thread.sleep(200);  //���߿��Խ���CPU��ռ����
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}while(current < ts);
	}
	
	private void startReviewSeat() {
		//�ȵ�ʱ��
		this.ensureToClock();
		//������
		if(!this.openBrowserByUrl(this.libUrl))
			return;
		//��½ͼ���
		this.loginLibrary(this.userName, this.pwd);
		//��ʼԤ��
		this.reviewStart();
	}

	private boolean openBrowserByUrl(String url) {
		boolean state = true;
		try {
			this.firefoxDriver = new FirefoxDriver();
			//������url
			firefoxDriver.get(url);
			//�������
			firefoxDriver.manage().window().maximize();
		} catch(org.openqa.selenium.WebDriverException e) {
			state = false;
			if(null != this.firefoxDriver)
				this.firefoxDriver.close();
		}
		
		return state;
	}
	
	private void loginLibrary(String userName, String password) {
		//�ҵ�userName�����ı���Դ���뷢��name = "username"
		WebElement userNameText = this.firefoxDriver.findElement(By.name("username"));
		//�ҵ�password�ı������:Դ���뷢��id = "bor_verification"
		WebElement pwdText = this.firefoxDriver.findElement(By.name("password"));
		//�ҵ���֤�������
		WebElement captchaText = this.firefoxDriver.findElement(By.id("captcha"));
		//�ҵ���½��ť:�˽�xpath,���tr,divʲô�Ĳ��д�1��ʼ������λ������tr[5]
		WebElement loginBtn = this.firefoxDriver.findElement(By.className("btn1"));
		
		//����userName
		userNameText.sendKeys(userName);
		//����password
		pwdText.sendKeys(password);
		//��ȡ��֤��
		String captcha = this.findCAPTCHA();
		//������֤��
		captchaText.sendKeys(captcha);
		//�����½
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
				min = Double.MAX_VALUE;  //��������һ��
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
		//��½ʧ��ʱ(�����˺��������Ҳ��������֤�����ʧ��),ִ���������������쳣
		//�����쳣����û��˵����½�ɹ���
		boolean state= true;
		try {
			//ѡ������
			this.firefoxDriver.findElement(By.id("display_onDate")).click();
			//ȡ�¸����ڣ���ʽ�������add���ӻ��Զ���֤��ȷ�ԣ�(�Զ��ı�ɺ��ʵ���ݺ��·�)
			Calendar nextDate = Calendar.getInstance();
			nextDate.add(Calendar.DAY_OF_MONTH, 0);
			Date next = nextDate.getTime();
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String dst = format.format(next);
			this.firefoxDriver.findElement(By.xpath("//p[@id='options_onDate']/a[@value='" + dst + "']")).click();
			//ѡ������
			this.firefoxDriver.findElement(By.id("display_building")).click();
			this.firefoxDriver.findElement(By.xpath("//p[@id='options_building']/a[@value='1']")).click();
			//ѡ�����䣬=0�ǲ��޷��䣬����ʹ��Ĭ��
			if(roomIndex > 0) {
				this.firefoxDriver.findElement(By.id("display_room")).click();
				WebElement roomEle = this.firefoxDriver.findElement(By.xpath("//div[@id='room_select']/p/a[@value='" + (roomIndex+3) + "']"));
				//Ҫ�ڿɼ���Χ�ڲ��ܵ��ѡ��
				((JavascriptExecutor)this.firefoxDriver).executeScript("arguments[0].scrollIntoView();", roomEle); 
				roomEle.click();
			}
			
			//ѡ����ʼʱ��,�����±��1��ʼ
			this.firefoxDriver.findElement(By.id("display_startMin")).click();
			WebElement tsEle = this.firefoxDriver.findElement(By.xpath("//p[@id='options_startMin']/a[" + (tsIndex+1) + "]"));
			((JavascriptExecutor)this.firefoxDriver).executeScript("arguments[0].scrollIntoView();", tsEle); 
			tsEle.click();
			
			//ѡ������ʱ��
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
	 * ���ڿ�����λĬ�Ϸ��ص�һ��WebElement
	 * �����ڷ���null��
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
		//�ҵ���ѯ��ť
		this.firefoxDriver.findElement(By.cssSelector("input.searchBtn.fl")).click();
	}
	
	private void findSeatByNumSearch() {
		//ѡ����λ���,className�пո�ȸ��Ϸ��Ų���ֱ���ã���cssSelector
		this.firefoxDriver.findElement(By.cssSelector("input.seatBtn.fl")).click();
		//�ҵ���������:1,2,3,4,5,6,7,8,9,0
		List<WebElement> numList = this.firefoxDriver.findElements(By.className("seatLink"));
		//�õ����ֵ�ÿλ������
		int[] hitNums = intCvtIntArrayByBit(this.seatIndex);
		//�Ӹ�λ����λһ�ε��
		for(int i=hitNums.length-1; i>=0; i--) {
			int k = hitNums[i];
			if(hitNums[i] == 0)
				k = 10;
			numList.get(k-1).click();
		}
		//ȷ��
		this.firefoxDriver.findElement(By.id("searchBySeatID")).click();
	}
	
	//��ֳɵ�λ��ǰ����λ�ں�
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
		//���壬ʹ�������
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
		//ѡ��ʼʱ��,������ʾ��ʱ�����԰�СʱΪ��С�̶ȵģ�
		//��˻����tsIndex���任���任����һ��:30��:00
		if(tsIndex > 0) {
			tsIndex = tsIndex%2 == 0 ? tsIndex+1 : tsIndex;
			this.firefoxDriver.findElement(By.xpath("//div[@id='startTime']/dl/ul/li/a[@time='" + ((tsIndex-1)*15) + "']")).click();
		}
		else if(tsIndex == 0) {
			List<WebElement> tsEles = this.firefoxDriver.findElements(By.xpath("//div[@id='startTime']/dl/ul/li"));
			tsEles.get(0).click();
		}
		
		//ѡ����ʱ��,����ʱ��������00��15��ʼ
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
		
		//�õ���֤��
		String captcha = this.findCAPTCHA();
		//�ҵ���֤�������,������֤��
		this.firefoxDriver.findElement(By.xpath("//input[@id='captchaValue']")).sendKeys(captcha);
		//�ҵ�ȷ��ԤԼ��ť�����
		this.firefoxDriver.findElement(By.id("reserveBtn")).click();
		
		return state;
	}
}
