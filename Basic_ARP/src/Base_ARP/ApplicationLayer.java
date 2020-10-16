package Base_ARP;

import java.awt.Color;
import java.awt.Container;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.border.*;

import Base_ARP.ARPLayer._ARPCache_Entry;
import Base_ARP.ARPLayer._Proxy_Entry;;

public class ApplicationLayer extends JFrame implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	private static LayerManager m_LayerMgr = new LayerManager();	
	
	public static Hashtable<String, _ARPCache_Entry> _ARPCache_Table;
	public static Hashtable<String, _Proxy_Entry> _Proxy_Table;
	
	Container contentPane;	// 메인 ARP 인터페이스
	Container proxyAddPane; // Proxy Add 인터페이스
	
	JTextField TF_IPAddress;
	JTextField TF_HWAddress;
	JTextField TF_ProxyIPAddress;
	JTextField TF_ProxyMacAddress;
	
	
	JPanel Panel_IPAddress;
	JPanel Panel_HWAddress;
	JPanel Panel_ProxyAdd_IPAddress;
	JPanel Panel_ProxyAdd_HWAddress;
	JComboBox<String> ComboBox_Device;
	JFrame Frame_ProxyAddPopup;
	
	static DefaultListModel<String> ListModel_ARPCache;
	static DefaultListModel<String> ListModel_Proxy;
	
	JList<String> List_ARPCache;
	JList<String> List_Proxy;
	
	JLabel Label_IP;
	JLabel Label_HW;
	JLabel Label_ProxyAdd_Device;
	JLabel Label_ProxyAdd_IP;
	JLabel Label_ProxyAdd_Ethernet;
	
	JButton Btn_ItemDelete;
	JButton Btn_AllDelete;
	JButton Btn_ARPSend;
	JButton Btn_ProxyAdd;
	JButton Btn_ProxyDelete;
	JButton Btn_GratSend;
	JButton Btn_Exit;
	JButton Btn_Cancel;
	JButton Btn_ProxyAdd_Ok;
	JButton Btn_ProxyAdd_Cancel;
	
	
	public static void main(String[] args) throws UnknownHostException {		
		m_LayerMgr.AddLayer(new ApplicationLayer("GUI"));
		m_LayerMgr.AddLayer(new TCPLayer("TCP"));
		m_LayerMgr.AddLayer(new IPLayer("IP"));
		m_LayerMgr.AddLayer(new ARPLayer("ARP"));
		m_LayerMgr.AddLayer(new EthernetLayer("ETHERNET"));
		m_LayerMgr.AddLayer(new NILayer("NI"));
		
		m_LayerMgr.ConnectLayers(" NI ( *ETHERNET ( *ARP +IP ( -ARP *TCP ( *GUI ) ) ) )");
		
		// ARPLayer의 ARP&Proxy Table을 가져와 동기화시킨다
		_ARPCache_Table = ((ARPLayer) m_LayerMgr.GetLayer("ARP"))._ARPCache_Table;
		_Proxy_Table = ((ARPLayer) m_LayerMgr.GetLayer("ARP"))._Proxy_Table;
		
		// Thread에 updater를 넣어 시작
		Thread updaterThread = new Thread(updater, "updaterThread");
		updaterThread.start();
	}
	
	// CacheTable update를 돌리기 위한 Runnable
	static Runnable updater = () -> {
		while(true) {
			try {
				// 연산 부담을 줄이기 위해
				// 2초 sleep을 걸어주어 2초마다 updateGUI 함수 실행
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			updateGUI();
		}
	};
	
	public static void updateGUI() {
		Enumeration<String> arpKeys = _ARPCache_Table.keys();
		while(arpKeys.hasMoreElements()) {
			// arpKeys를 순회하면서
			// byte 형식으로 저장된 mac Address를 String으로 변환한 후
			// ip + mac + status 형식으로
			// ListModel_ARPCache에 이를 update 해야함
		}
	}
	
	// ARP Send를 누를 때 새로운 ARP Cache를 Table에 넣어주는 함수
	public void cacheAddToTable() {
		// Send 버튼을 누른 뒤
		// ARP CacheTable 탐색후 해당 key가 존재하지 않을 때
		// TP_IPAddress의 Text값을 가져와
		// "????" + status : uncomplete + lifeTime 으로
		// 새로운 Entry를 만들어
		// cacheTable에 이를 저장하는 기능
	}
	
	

	public ApplicationLayer(String pName) {
		pLayerName = pName;

		setTitle("TestARP");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(250, 250, 748, 380);
		contentPane = new JPanel();
		((JComponent) contentPane).setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel ARPPanel = new JPanel();
		ARPPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "ARP Cache",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		ARPPanel.setBounds(10, 5, 355, 276);
		contentPane.add(ARPPanel);
		ARPPanel.setLayout(null);

		JPanel chattingEditorPanel = new JPanel();
		chattingEditorPanel.setBounds(10, 15, 340, 180);
		ARPPanel.add(chattingEditorPanel);
		chattingEditorPanel.setLayout(null);

		ListModel_ARPCache = new DefaultListModel<String>();
		List_ARPCache = new JList<String>(ListModel_ARPCache);
		List_ARPCache.setBounds(0, 0, 340, 180);
		chattingEditorPanel.add(List_ARPCache);

		Btn_ItemDelete = new JButton("Item Delete");
		Btn_ItemDelete.setBounds(50, 205, 120, 25);
		Btn_ItemDelete.addActionListener(new btnClickEvent());

		ARPPanel.add(Btn_ItemDelete);

		Btn_AllDelete = new JButton("All Delete");
		Btn_AllDelete.setBounds(190, 205, 120, 25);
		Btn_AllDelete.addActionListener(new btnClickEvent());
		ARPPanel.add(Btn_AllDelete);

		Label_IP = new JLabel("IP주소");
		Label_IP.setBounds(20, 240, 80, 25);
		ARPPanel.add(Label_IP);

		Panel_IPAddress = new JPanel();
		Panel_IPAddress.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		Panel_IPAddress.setBounds(70, 240, 180, 25);
		ARPPanel.add(Panel_IPAddress);
		Panel_IPAddress.setLayout(null);

		TF_IPAddress = new JTextField();
		TF_IPAddress.setBounds(2, 2, 180, 25);// 249
		Panel_IPAddress.add(TF_IPAddress);
		TF_IPAddress.setHorizontalAlignment(SwingConstants.CENTER);
		TF_IPAddress.setColumns(10);

		Btn_ARPSend = new JButton("Send");
		Btn_ARPSend.setBounds(260, 240, 80, 25);
		Btn_ARPSend.addActionListener(new btnClickEvent());
		ARPPanel.add(Btn_ARPSend);

		JPanel settingPanel = new JPanel();
		settingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Proxy ARP Entry",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		settingPanel.setBounds(370, 5, 355, 215);
		contentPane.add(settingPanel);
		settingPanel.setLayout(null);

		JPanel proxyEditorPanel = new JPanel();
		proxyEditorPanel.setBounds(5, 20, 345, 150);
		settingPanel.add(proxyEditorPanel);
		proxyEditorPanel.setLayout(null);

		ListModel_Proxy = new DefaultListModel<String>();
		List_Proxy = new JList<String>(ListModel_Proxy);
		List_Proxy.setBounds(0, 0, 345, 150);
		proxyEditorPanel.add(List_Proxy);

		Btn_ProxyAdd = new JButton("Add");
		Btn_ProxyAdd.setBounds(50, 180, 120, 25);
		Btn_ProxyAdd.addActionListener(new btnClickEvent());
		settingPanel.add(Btn_ProxyAdd);

		Btn_ProxyDelete = new JButton("Delete");
		Btn_ProxyDelete.setBounds(190, 180, 120, 25);
		Btn_ProxyDelete.addActionListener(new btnClickEvent());
		settingPanel.add(Btn_ProxyDelete);

		JPanel GratuitousPanel = new JPanel();
		GratuitousPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Gratuitous ARP",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GratuitousPanel.setBounds(370, 220, 355, 60);
		contentPane.add(GratuitousPanel);
		GratuitousPanel.setLayout(null);

		Label_HW = new JLabel("H/W주소");
		Label_HW.setBounds(15, 23, 60, 25);
		GratuitousPanel.add(Label_HW);

		Panel_HWAddress = new JPanel();
		Panel_HWAddress.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		Panel_HWAddress.setBounds(70, 23, 180, 25);
		GratuitousPanel.add(Panel_HWAddress);
		Panel_HWAddress.setLayout(null);

		TF_HWAddress = new JTextField();
		TF_HWAddress.setBounds(0, 0, 180, 25);// 249
		Panel_HWAddress.add(TF_HWAddress);
		TF_HWAddress.setHorizontalAlignment(SwingConstants.CENTER);
		TF_HWAddress.setColumns(10);
	
		Btn_GratSend = new JButton("Send");
		Btn_GratSend.setBounds(260, 23, 80, 25);
		Btn_GratSend.addActionListener(new btnClickEvent());
		GratuitousPanel.add(Btn_GratSend);

		Btn_Exit = new JButton("종료");
		Btn_Exit.setBounds(263, 290, 100, 25);
		Btn_Exit.addActionListener(new btnClickEvent());
		contentPane.add(Btn_Exit);

		Btn_Cancel = new JButton("취소");
		Btn_Cancel.setBounds(370, 290, 100, 25);
		Btn_Cancel.addActionListener(new btnClickEvent());
		contentPane.add(Btn_Cancel);

		setVisible(true);
	}
	
	public void ProxyAddPopUP() {
		Frame_ProxyAddPopup = new JFrame("Proxy ARP Entry 추가");
		Frame_ProxyAddPopup.setBounds(200, 200, 300, 220);
		proxyAddPane = Frame_ProxyAddPopup.getContentPane();
		Frame_ProxyAddPopup.setLayout(null);
		Frame_ProxyAddPopup.setVisible(true);

		Label_ProxyAdd_Device = new JLabel("Device");
		Label_ProxyAdd_Device.setBounds(47, 10, 60, 25);
		proxyAddPane.add(Label_ProxyAdd_Device);

		String[] str = { "Host B", "Host C" };

		ComboBox_Device = new JComboBox<>(str);
		ComboBox_Device.setBounds(90, 10, 150, 25);
		ComboBox_Device.addActionListener(new btnClickEvent());
		((JLabel) ComboBox_Device.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER); // 텍스트를 가운데로 정렬하는 코드
		proxyAddPane.add(ComboBox_Device);

		Label_ProxyAdd_IP = new JLabel("IP주소");
		Label_ProxyAdd_IP.setBounds(47, 50, 60, 25);
		proxyAddPane.add(Label_ProxyAdd_IP);

		Panel_ProxyAdd_IPAddress = new JPanel();
		Panel_ProxyAdd_IPAddress.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		Panel_ProxyAdd_IPAddress.setBounds(90, 50, 150, 25);
		proxyAddPane.add(Panel_ProxyAdd_IPAddress);
		Panel_ProxyAdd_IPAddress.setLayout(null);

		TF_ProxyIPAddress = new JTextField();
		TF_ProxyIPAddress.setBounds(2, 2, 150, 25);
		Panel_ProxyAdd_IPAddress.add(TF_ProxyIPAddress);
		TF_ProxyIPAddress.setHorizontalAlignment(SwingConstants.CENTER);
		TF_ProxyIPAddress.setColumns(10);
		
		Label_ProxyAdd_Ethernet = new JLabel("Ethernet주소");
		Label_ProxyAdd_Ethernet.setBounds(10, 90, 80, 25);
		proxyAddPane.add(Label_ProxyAdd_Ethernet);

		Panel_ProxyAdd_HWAddress = new JPanel();
		Panel_ProxyAdd_HWAddress.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		Panel_ProxyAdd_HWAddress.setBounds(90, 90, 150, 25);
		proxyAddPane.add(Panel_ProxyAdd_HWAddress);
		Panel_ProxyAdd_HWAddress.setLayout(null);
		
		// Proxy Add Frame - Ethernet Address TextBox
		TF_ProxyMacAddress = new JTextField();
		TF_ProxyMacAddress.setBounds(2, 2, 150, 25);
		Panel_ProxyAdd_HWAddress.add(TF_ProxyMacAddress);
		TF_ProxyMacAddress.setHorizontalAlignment(SwingConstants.CENTER);
		TF_ProxyMacAddress.setColumns(10);
		
		// Proxy Add Frame - Ok Button
		Btn_ProxyAdd_Ok = new JButton("OK");
		Btn_ProxyAdd_Ok.setBounds(30, 130, 100, 25);
		Btn_ProxyAdd_Ok.addActionListener(new btnClickEvent());
		proxyAddPane.add(Btn_ProxyAdd_Ok);

		// Proxy Add Frame - Cancel Button
		Btn_ProxyAdd_Cancel = new JButton("Cancel");
		Btn_ProxyAdd_Cancel.setBounds(150, 130, 100, 25);
		Btn_ProxyAdd_Cancel.addActionListener(new btnClickEvent());
		proxyAddPane.add(Btn_ProxyAdd_Cancel);

	}
	
	class btnClickEvent implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == Btn_ItemDelete) {
				
			}
			if (e.getSource() == Btn_AllDelete) {
				
			}
			if (e.getSource() == Btn_ARPSend) {
				String ip_input = TF_IPAddress.getText();
				if(ipValidationCheck(ip_input)) {
					((TCPLayer) m_LayerMgr.GetLayer("TCP")).Send(null, 0);
				} else {
					System.out.println("유효하지 않은 IP 입력입니다 : " + ip_input);
				}
			}
			if (e.getSource() == Btn_ProxyAdd) {
				
			}
			if (e.getSource() == Btn_ProxyDelete) {
				
			}
			if (e.getSource() == Btn_GratSend) {
				
			}
			if (e.getSource() == Btn_Exit) {
				System.exit(0);
			}
			if (e.getSource() == Btn_Cancel) {
				System.exit(0);
				dispose();
			}
			if (e.getSource() == Btn_ProxyAdd_Ok) {
				
			}
			if (e.getSource() == Btn_ProxyAdd_Cancel) {
				
			}
		}
		
	}
	
	private void SetCombobox() {
		
	}
	
	
	public boolean Receive(byte[] input) {
		
		return false;
	}
	
	public String getIpAddressInput() {
		return TF_IPAddress.getText();
	}
	
	private boolean ipValidationCheck(String input) {
		String validIp = "^([1-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])"
				+ "(\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3}$";
		  
		  if (!Pattern.matches(validIp, input )) {
			  return false;
		  }
		  return true;
	}
	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		// TODO Auto-generated method stub
		if (pUnderLayer == null)
			return;
		this.p_UnderLayer = pUnderLayer;
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		// TODO Auto-generated method stub
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
		// nUpperLayerCount++;
	}

	@Override
	public String GetLayerName() {
		// TODO Auto-generated method stub
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() {
		// TODO Auto-generated method stub
		if (p_UnderLayer == null)
			return null;
		return p_UnderLayer;
	}

	@Override
	public BaseLayer GetUpperLayer(int nindex) {
		// TODO Auto-generated method stub
		if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
			return null;
		return p_aUpperLayer.get(nindex);
	}

	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);

	}
	
	 public static String getLocalMacAddress() {
		 	String result = "";
			InetAddress ip;

			try {
				ip = InetAddress.getLocalHost();
			   
				NetworkInterface network = NetworkInterface.getByInetAddress(ip);
				byte[] mac = network.getHardwareAddress();
			   
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < mac.length; i++) {
					sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
				}
					result = sb.toString();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (SocketException e){
				e.printStackTrace();
			}
			    
			return result;
	 }

}
