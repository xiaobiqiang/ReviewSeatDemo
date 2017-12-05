package com.qq1312952829.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.qq1312952829.exe.ReviewExe;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * ����ʱ��ʹ��displayUI,��Ȼ�½�һ���߳�Ҳ���ԣ���������ͨ��ǰ�ߡ�
 *
 */
public class ConditionUI extends MouseAdapter implements Runnable{
	
	public void displayUI() {
		SwingUtilities.invokeLater(this);
	}
	
	@Override
	public void run() {
		initComponent();
	}
	
	private void initComponent() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		} finally { //���ǵ���ʹ�����쳣Ҳ���Լ���ִ�У���˷���finally��
			initFrame();
			initPanel();
			initIdAndPwd();
			initCombobox();
			initNumLabelAndBox();
			initBtn();
			addAll();
			frame.setVisible(true);
		}
	}
			
	private void initFrame() {
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		frame = new JFrame("��Ϣѧ��ͼ�����λԤԼ��������V1.0");
		frame.setLayout(null);
		frame.setResizable(false);
		frame.setBounds(d.width/2 - 250, d.height/2 - 400, 500, 510);
		frame.setAutoRequestFocus(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
		
	private void initPanel() {
		panel = new JPanel() {
			private static final long serialVersionUID = 1000000000000000L;
			@Override
			protected void paintComponent(Graphics g) {
				BufferedImage img = null;
				try {
					img = ImageIO.read(new File("resources/panel3.bmp"));
					g.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);
				} catch (IOException e) {
					e.printStackTrace();
				}
				super.paintComponent(g);
			}
		};
		panel.setLayout(null);
		panel.setBounds(0, 0, frame.getWidth(), frame.getHeight());
		panel.setOpaque(false);//�������͸��
	}
		
	private void initIdAndPwd() {
		idLabel = new JLabel("ѧ    ��");
		idLabel.setFont(new Font("����", Font.PLAIN, 18));
		idLabel.setBounds(60, 25, 90, 30);
		
		pwdLabel = new JLabel("��    ��");
		pwdLabel.setFont(new Font("����", Font.PLAIN, 18));
		pwdLabel.setBounds(60, 85, 90, 30);
		
		idText = new JTextField(100);
		idText.setBounds(160, 25, 220, 30);
		idText.setFont(new Font("Courier New", Font.PLAIN, 18));
		
		pwdText = new JPasswordField(100);
		pwdText.setEchoChar('*');
		pwdText.setBounds(160, 85, 220, 30);
		pwdText.setFont(new Font("Courier New", Font.PLAIN, 18));
	}
		
	private void initCombobox() {
		roomLabel = new JLabel("��    ��");
		roomLabel.setFont(new Font("����", Font.PLAIN, 18));
		roomLabel.setBounds(60, 145, 90, 30);
		
		tsLabel = new JLabel("��ʼʱ��");
		tsLabel.setFont(new Font("����", Font.PLAIN, 18));
		tsLabel.setBounds(60, 205, 90, 30);
		
		teLabel = new JLabel("����ʱ��");
		teLabel.setFont(new Font("����", Font.PLAIN, 18));
		teLabel.setBounds(60, 265, 90, 30);
		
		roomBox = new JComboBox<String>();
		roomBox.setFont(new Font("����", Font.PLAIN, 18));
		roomBox.setBounds(160, 145, 220, 30);
		roomBox.setAutoscrolls(true);
		roomBox.addItem("���޷���");
		roomBox.addItem("һ¥3C���Ϳռ�");
		roomBox.addItem("һ¥����ѧϰ������");
		roomBox.addItem("��¥����Ȼ��ѧͼ�������");
		roomBox.addItem("��¥����Ȼ��ѧͼ�������");
		roomBox.addItem("��¥������ѧͼ�������");
		roomBox.addItem("��¥��ͼ��������");
		roomBox.addItem("��¥������ѧͼ�������");
		roomBox.addItem("��¥��ͼ��������");
		roomBox.addItem("��¥����ѧϰ��");
		roomBox.addItem("3C����-������Դ��������20̨��");
		roomBox.addItem("3C����-˫�����ԣ�20̨��");
		roomBox.addItem("����ѧϰ-MAC���ԣ�12̨��");
		roomBox.addItem("����ѧϰ-�����棨42̨��");	
		//ÿ�θı�ѡ��ķ���ʱ����ˢ��numBox,Ҳ��������������λ��
		roomBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				//�������ÿ�θı�Item��ִ�����Σ���Ϊһ����ΪDeSelected,һ����ΪSelected��
				if(e.getStateChange() == ItemEvent.SELECTED)
					updateNumBox(roomBox.getSelectedIndex());
			}
		});
		
		tsBox = new JComboBox<String>();
		tsBox.setFont(new Font("����", Font.PLAIN, 18));
		tsBox.setBounds(160, 205, 220, 30);
		tsBox.setAutoscrolls(true);
		tsBox.addItem("����ʱ��");
		addTimeItems(tsBox);
		
		teBox = new JComboBox<String>();
		teBox.setFont(new Font("����", Font.PLAIN, 18));
		teBox.setBounds(160, 265, 220, 30);
		teBox.setAutoscrolls(true);
		teBox.addItem("����ʱ��");
		addTimeItems(teBox);
	}
		
	private void addTimeItems(JComboBox<String> tBox) {
		for(int hour=0; hour<24; hour++) {
			String strHour = hour < 10 ? "0"+hour : ""+hour;
			for(int numKe=0; numKe<4; numKe++) {
				String strMin = numKe == 0 ? "00" : ""+(numKe*15);
				tBox.addItem(strHour + ":" + strMin);
			}
		}
	}
		
	private void updateNumBox(int selectedIndex) {
		int numSeats = 
				selectedIndex == 0 ? 
						1 : selectedIndex == 1 ?
								110 : selectedIndex == 2 ?
										64 : selectedIndex == 3 ?
												92 : selectedIndex == 4 ?
														92 : selectedIndex == 5 ?
																88 : selectedIndex == 6 ?
																		88 : selectedIndex == 7 ?
																				84 : selectedIndex == 8 ?
																						80 : selectedIndex == 9 ?
																								188 : selectedIndex == 10 ?
																										20 : selectedIndex == 11 ?
																												20 : selectedIndex == 12 ?
																														12 : selectedIndex == 13 ?
																																42 : 0
																																;
		int already = numBox.getItemCount();

		if(already < numSeats) {
			for(int i=already+1; i<=numSeats; i++)
				numBox.addItem(""+i);
		} else {
			for(int i=numSeats; i<already; i++)
				numBox.removeItemAt(numSeats);   //ɾ��һ��������ǰ��ʱindexҲ����Ӧ�ļ�1��������������
		}
																																
		numBox.repaint();																
	}
		
	private void initNumLabelAndBox() {
		numLabel = new JLabel("�� λ ��");
		numLabel.setFont(new Font("����", Font.PLAIN, 18));
		numLabel.setBounds(60, 325, 90, 30);
		
		numBox = new JComboBox<String>();
		numBox.setFont(new Font("����", Font.PLAIN, 18));
		numBox.setBounds(160, 325, 220, 30);
		numBox.setAutoscrolls(true);
	}
		
	private void initBtn() {
		searchBtn = new JButton("��ѯ");
		searchBtn.setFont(new Font("����", Font.BOLD, 20));
		searchBtn.setBounds(50, 410, 150, 40);
		searchBtn.setFocusPainted(false);
		searchBtn.addMouseListener(this);
		
		numSearchBtn = new JButton("���Ų�ѯ");
		numSearchBtn.setFont(new Font("����", Font.BOLD, 20));
		numSearchBtn.setBounds(300, 410, 150, 40);
		numSearchBtn.setFocusPainted(false);
		numSearchBtn.addMouseListener(this);
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1) {
			String id = idText.getText();
			String pwd = new String(pwdText.getPassword());
			int roomIndex = roomBox.getSelectedIndex();
			int tsIndex = tsBox.getSelectedIndex();
			int teIndex = teBox.getSelectedIndex();
			
			if(((JButton)e.getSource()).getText().equals("��ѯ")) {
				exeApp = new ReviewExe(id, pwd, roomIndex, tsIndex, teIndex);
				searchBtn.setEnabled(false);
			}else if(((JButton)e.getSource()).getText().equals("���Ų�ѯ")) {
				int seatIndex = numBox.getSelectedIndex();
				exeApp = new ReviewExe(id, pwd, roomIndex, seatIndex, tsIndex, teIndex);
				numSearchBtn.setEnabled(false);
			}
			
			exeApp.start();
		}
	}

	private void addAll() {
		panel.add(idLabel);
		panel.add(pwdLabel);
		
		panel.add(idText);
		panel.add(pwdText);
		
		panel.add(roomLabel);
		panel.add(tsLabel);
		panel.add(teLabel);
		
		panel.add(roomBox);
		panel.add(tsBox);
		panel.add(teBox);
		
		panel.add(numLabel);
		panel.add(numBox);
		
		panel.add(searchBtn);
		panel.add(numSearchBtn);
		
		frame.getContentPane().add(panel);
	}

	public static void main(String[] args) throws Exception {
		new ConditionUI().displayUI();
	}
	
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;
	private JFrame frame = null;
	private JPanel panel = null;
	
	private JLabel idLabel = null;
	private JLabel pwdLabel = null;
	private JLabel numLabel = null;
	
	private JTextField idText = null; 
	private JPasswordField pwdText = null; 
	
	private JLabel roomLabel = null;
	private JLabel tsLabel = null;
	private JLabel teLabel = null;
	
	private JComboBox<String> roomBox = null;
	private JComboBox<String> tsBox = null;
	private JComboBox<String> teBox = null;
	private JComboBox<String> numBox = null;
	
	private JButton searchBtn = null;
	private JButton numSearchBtn = null;
	
	private ReviewExe exeApp = null;
}
