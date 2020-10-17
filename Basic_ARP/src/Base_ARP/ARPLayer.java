package Base_ARP;

import java.net.InetAddress;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import java.util.Hashtable;
import java.util.Map.Entry;

public class ARPLayer implements BaseLayer{
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	
	_ARP_HEADER m_sHeader;
	
	// ARP Cache Table & Proxy Table
	public Hashtable<String, _ARPCache_Entry> _ARPCache_Table = new Hashtable<>();
	public Hashtable<String, _Proxy_Entry> _Proxy_Table = new Hashtable<>();
	
	// Device's Info
	public _IP_ADDR myIpAddress = new _IP_ADDR();
	public _ETHERNET_ADDR myMacAddress = new _ETHERNET_ADDR();
	public String targetIpInput = new String();
	public String gratMacInput = new String();
	
	// ARP Cache Entry
	// Ip 주소는 Table에서 Key로 가지고 있으므로 Mac Address와 Status, lifeTime만 보유
	public static class _ARPCache_Entry {
		byte[] addr;
		String status;
		int lifeTime;
		
		public _ARPCache_Entry(byte[] addr, String status, int lifeTime) {  // boolena status -> string status 수정 
			this.addr = addr;
			this.status = status;
			this.lifeTime = lifeTime;
		}
	}
	
	// Proxy Entry
	// Ip주소는 Table에서 Key로 가지고 있으므로 Mac Address와 hostName만 보유
	public static class _Proxy_Entry {
		String hostName;
		byte[] addr;
		
		public _Proxy_Entry(byte[] addr, String hostName) {
			this.hostName = hostName;
			this.addr = addr;
		}
	}
	
	private void ResetHeader() {
		m_sHeader = new _ARP_HEADER();
	}
	
	public ARPLayer(String pName) {
		// super(pName);
		pLayerName = pName;
		ResetHeader();
	}
	
	private class _IP_ADDR {
		private byte[] addr = new byte[4];
		
		public _IP_ADDR() {
			this.addr[0] = (byte) 0x00;
			this.addr[1] = (byte) 0x00;
			this.addr[2] = (byte) 0x00;
			this.addr[3] = (byte) 0x00;
		}
	}
	
	
	private class _ETHERNET_ADDR {
		private byte[] addr = new byte[6];
		
		public _ETHERNET_ADDR() {
			this.addr[0] = (byte) 0x00;
			this.addr[1] = (byte) 0x00;
			this.addr[2] = (byte) 0x00;
			this.addr[3] = (byte) 0x00;
			this.addr[4] = (byte) 0x00;
			this.addr[5] = (byte) 0x00;
		}
	}
		
	private class _ARP_HEADER {
		byte[] macType;								// Hardware Type
		byte[] ipType;								// Protocol Type
		byte macAddrLen;							// Length of hardware Address
		byte ipAddrLen;								// Length of protocol Address
		byte[] opcode;								// Opcode (ARP Request)
		_ETHERNET_ADDR srcMac;						// Sender's hardware Address
		_IP_ADDR srcIp;								// Sender's protocol Address
		_ETHERNET_ADDR dstMac;						// Target's hardware Address
		_IP_ADDR dstIp;								// Target's protocol Address
		
		public _ARP_HEADER() {						// 28 Bytes
			this.macType = new byte[2];				// 2 Bytes / 0 ~ 1
			this.ipType = new byte[2];				// 2 Bytes / 2 ~ 3
			this.macAddrLen = (byte) 0x00;			// 1 Byte  / 4
			this.ipAddrLen = (byte) 0x00;			// 1 Byte  / 5
			this.opcode = new byte[2];				// 2 Bytes / 6 ~ 7 
			this.srcMac = new _ETHERNET_ADDR();		// 6 Bytes / 8 ~ 13 
			this.srcIp = new _IP_ADDR();			// 4 Bytes / 14 ~ 17
			this.dstMac = new _ETHERNET_ADDR();		// 6 Bytes / 18 ~ 23
			this.dstIp = new _IP_ADDR();			// 4 Bytes / 24 ~ 27
			
		}
	}
	
	private byte[] ObjToByte(_ARP_HEADER Header, byte[] input, int length) {
		byte[] buf = new byte[28 + length];
		
		System.arraycopy(Header.macType, 0, buf, 0, 2);
		System.arraycopy(Header.ipType, 0, buf, 2, 2);
		buf[4] = Header.macAddrLen;
		buf[5] = Header.ipAddrLen;
		System.arraycopy(Header.opcode, 0, buf, 6, 2);
		System.arraycopy(Header.srcMac.addr, 0, buf, 8, 6);
		System.arraycopy(Header.srcIp.addr, 0, buf, 14, 4);
		System.arraycopy(Header.dstMac.addr, 0, buf, 18, 6);
		System.arraycopy(Header.dstIp.addr, 0, buf, 24, 4);
		
		System.arraycopy(input, 0, buf, 28, length);
				
		return buf;
	}
	
	public boolean Send(byte[] input, int length) {
		
		// 먼저 자신이 가지고있는 ARP Cache인지 확인
		if(containsARP(targetIpInput)) {
			_ARPCache_Entry tempEntry = _ARPCache_Table.get(targetIpInput);
			
			if(tempEntry.status.equals("Incomplete")) {	
				// Incomplete 상태라 Request 보내야하는경우
			}
			else {	
				// ARP Request 보낼 필요 없는경우
			}
		}
		else { // 처음 전송인 경우 
			_ARPCache_Table.put(targetIpInput, new _ARPCache_Entry(new byte[6], "Incomplete", 100));
			setSrcHeader(); // 자신의 맥주소 아이피 주소 세팅 하는 것 
			System.out.println(targetIpInput);
			setDstIp(ipToByte(targetIpInput));
			setDstMac(new byte[6]);
			
			setDefaultHeader((byte) 0x01); // hardware type, proto type, protocol length , opcode까지 처음 동작일때 헤더 셋팅  
			byte[] _ARP_FRAME = ObjToByte(m_sHeader, input, length);
			this.GetUnderLayer().Send(_ARP_FRAME, _ARP_FRAME.length);
		}
		
		return false;
	}
	
	public void setDefaultHeader(byte opcode) {
		this.m_sHeader.macType[1] = (byte) 0x01;
		this.m_sHeader.ipType[0] = (byte) 0x80;
		this.m_sHeader.ipType[1] = (byte) 0x00;
		this.m_sHeader.ipAddrLen = (byte) 0x04;
		this.m_sHeader.macAddrLen = (byte) 0x06;
		this.m_sHeader.opcode[1] = opcode;
	}
	
	public void setSrcHeader() {
		try {
			setSrcIp();
			setSrcMac();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
	}
	
	// ARPCache Table이 ip에 해당되는 Element를 가지고있는지 검사
	public boolean containsARP(String ip) {
		if(_ARPCache_Table.containsKey(ip)) 
			return true;
		else
			return false;
	}
	
	// Proxy Table이 ip input에 해당되는 Element를 가지고있는지 검사
	public boolean containsProxy(String ip) {
		if(_Proxy_Table.containsKey(ip))
			return true;
		else
			return false;
	}
	
	// m_sHeader의 mac Address를 본인의 Mac으로 채우는 함수
	public void setSrcMac() throws UnknownHostException, SocketException {
		byte[] srcMac = getLocalMacAddress();
		for(int i=0; i<srcMac.length; i ++) {
			this.m_sHeader.srcMac.addr[i]=srcMac[i];
		}
	}
	
	// m_sHeader의 ip Address를 본인의 IP로 채우는 함수
	public void setSrcIp() throws UnknownHostException {
		byte[] srcIP = getLocalIPAddress();
		for(int i=0; i<srcIP.length; i++) {
			this.m_sHeader.srcIp.addr[i]=srcIP[i];
		}
	}
	
	// m_sHeader의 dst ip를 input 값으로 채우는 함수
	public void setDstIp(byte[] input) {
		 for(int idx = 0; idx < 4; idx++) {
			 this.m_sHeader.dstIp.addr[idx] = input[idx];
		 }
	}
	
	// m_sHeader의 dst Mac를 input 값으로 채우는 함수
	public void setDstMac(byte[] input) {
		 for(int idx = 0; idx < 6; idx++) {
			 this.m_sHeader.dstMac.addr[idx] = input[idx];
		 }
	}
	
	
	public void setARPHeaderBeforeSend() {  // 
//		this.m_sHeader.macType=byte4To2(intToByte(1));
		this.m_sHeader.ipType[0]=(byte)0x00;
		this.m_sHeader.ipType[1]=(byte)0x00;
		this.m_sHeader.macAddrLen = 6;
		this.m_sHeader.ipAddrLen = 4;
		this.m_sHeader.opcode[1] = 1;
	}
	
	public boolean Receive(byte[] input) {
		// ARP Layer Receive
		byte[] opcode = new byte[2];
		
		
		return false;
	}
	
	// Swaping 함수
	// src와 dst의 Mac, Ip Address Swap
	private byte[] Swaping(byte[] input) {
		byte[] srcIp = new byte[6];
		byte[] srcMac = new byte[4];
		
		byte[] dstIp = new byte[6];
		byte[] dstMac = new byte[4];
		
		// 현재 Mac Address 저장
		for (int idx = 0; idx < 4; idx++) {
			srcMac[idx] = input[8 + idx];
			dstMac[idx] = input[18 + idx];
		}
		
		// 현재 IP Address 저장
		for (int idx = 0; idx < 6; idx++) {
			srcIp[idx] = input[14 + idx];
			dstIp[idx] = input[24 + idx];
		}
		
		// Swap된 Mac Address 입력
		for (int idx = 0; idx < 4; idx++) {
			input[8 + idx] = dstMac[idx];
			input[18 + idx] = srcMac[idx];
		}
		
		// Swap된 Ip Address 입력
		for (int idx = 0; idx < 6; idx++) {
			input[14 + idx] = dstIp[idx];
			input[24 + idx] = srcIp[idx];
		}
		
		return input;
	}
	
	// isItMyARP 함수
	// 수신한 ARP Message가 본인이 보낸 ARP Request/Reply인지 확인한다
	private boolean isItMyARP(byte[] input) {
		
		return true;
	}
	
	// isItMyProxy 함수
	// 수신한 ARP Message의 mac주소가 자신이 보유하고 있는 Proxy의 Mac인지 검사하는 함수
	private boolean isItMyProxy(byte[] input) {
		
		return false;
	}
	
	// 
	
	
	// Local Mac Address 가져오는 함수
	 public byte[] getLocalMacAddress() throws UnknownHostException, SocketException {
		 	String result = "";
			InetAddress ip;

			ip = InetAddress.getLocalHost();
			NetworkInterface network = NetworkInterface.getByInetAddress(ip);
			byte[] mac = network.getHardwareAddress();
			
			return mac;
	 }
	 
	 // Local IP Address 가져오는 함수
	 public byte[] getLocalIPAddress() throws UnknownHostException {
		 InetAddress local = InetAddress.getLocalHost();
		 
		 byte[] ip = local.getAddress();
		 
		 return ip;
	 }

	 
	 // String Type IP 주소를 Byte로 변환
	 public byte[] ipToByte(String ip) {
		 String[] ipBuf = ip.split("[.]");
		 byte[] buf = new byte[4];
		 
		 for(int idx = 0; idx < 4; idx++) {
			 buf[idx] = (byte) Integer.parseInt(ipBuf[idx]);
		 }
		 
		 return buf;
	 }
	 
	 // String Type Mac 주소를 Byte로 변환
	 public byte[] macToByte(String mac) {
		 String[] macBuf = mac.split("-");
		 byte[] buf = new byte[6];
		 
		 for(int idx = 0; idx < 6; idx++) {
			 buf[idx] = (byte) Integer.parseInt(macBuf[idx]);
		 }
		 
		 return buf;
	 }
	 
	// byte 배열로 된 mac 주소를 String으로 변환하는 함수
	public String macToString(byte[] mac) {
		String macString = "";
			
		// addr에 가지고 있는 byte 배열의 byte를 가져와 두 자리의
		// mac Address String으로 변환하여 String에 더함

		for (byte b : mac) {
			macString += String.format("%02X:", b);
		}
			
		// 마지막에 붙은 ":"는 제거하여 return
		return macString.substring(0, macString.length() - 1);
	}
		
	public String ipToString(byte[] ip) {
		String ipAddress = "";
			
		// addr에 가지고있는 byte를 가져와 Integer로 변환 후 .을 더함
		for (byte b : ip) {
			ipAddress += Integer.toString(b & 0xFF) + ".";
		}
			
		// 마지막에 붙은 "."은 제거하여 return
		return ipAddress.substring(0, ipAddress.length() - 1);
	}


	 
	
	// BaseLayer Function
	
	@Override
	public String GetLayerName() {
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() {
		if (p_UnderLayer == null)
			return null;
		return p_UnderLayer;
	}

	@Override
	public BaseLayer GetUpperLayer(int nindex) {
		if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
			return null;
		return p_aUpperLayer.get(nindex);
	}

	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		if (pUnderLayer == null)
			return;
		p_UnderLayer = pUnderLayer;
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
		// nUpperLayerCount++;
	}

	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);

	}

}
