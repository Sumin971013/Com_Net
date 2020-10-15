package Base_ARP;

import java.util.ArrayList;

import java.util.Hashtable;
import java.util.Map.Entry;

import static Base_ARP.EthernetLayer.byte4To2; //  함수 import
import static Base_ARP.EthernetLayer.intToByte; // 

public class ARPLayer implements BaseLayer{
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	
	_ARP_HEADER m_sHeader;
	
	// ARP Cache Table & Proxy Table
	private Hashtable<String, _ARPCache_Entry> _ARPCache_Table = new Hashtable<>();
	private Hashtable<String, _Proxy_Entry> _Proxy_Table = new Hashtable<>();
	
	// Device's Info
	public _IP_ADDR myIpAddress = new _IP_ADDR();
	public _ETHERNET_ADDR myMacAddress = new _ETHERNET_ADDR();
	
	// ARP Cache Entry
	// Ip 주소는 Table에서 Key로 가지고 있으므로 Mac Address와 Status, lifeTime만 보유
	public class _ARPCache_Entry {
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
	public class _Proxy_Entry {
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
		
		@Override
		public String toString() {
			String ipAddress = "";
			
			// addr에 가지고있는 byte를 가져와 Integer로 변환 후 .을 더함
			for (byte b : this.addr) {
				ipAddress += Integer.toString(b & 0xFF) + ".";
			}
			
			// 마지막에 붙은 "."은 제거하여 return
			return ipAddress.substring(0, ipAddress.length() - 1);
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
		
		@Override
		public String toString() {
			String macAddress = "";
			
			// addr에 가지고 있는 byte 배열의 byte를 가져와 두 자리의
			// mac Address String으로 변환하여 String에 더함
			for (byte b : this.addr) {
				macAddress += String.format("%02X:", b);
			}
			
			// 마지막에 붙은 ":"는 제거하여 return
			return macAddress.substring(0, macAddress.length() - 1);
				
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
		
		if(cameFromDlg(input)) { // 다이얼로그에서 바로왔다면  
			
			setARPHeaderBeforeSend();
			setSrcMac(myMacAddress.addr);
			setSrcIp(myIpAddress.addr);
			
			// dst ip는 gui에서 처리
			
			byte [] Arp_Header_added=ObjToByte(m_sHeader,input,length); // arp header  붙인 프레임 생성 
			
			String target_ip=getDstAddFromHeader(Arp_Header_added);
			
			_ARPCache_Entry cache_entry=new _ARPCache_Entry(new byte[6],"Incomplete",80); //incomplete로 변경 
			
			if(!_ARPCache_Table.contains(target_ip)) {  // 해당 ip  없다면 cache_table에 넣는다.    
				
				_ARPCache_Table.put(target_ip,cache_entry);
				
			}
			
			this.GetUnderLayer().Send(Arp_Header_added,Arp_Header_added.length);  //  아래 ethernet layer에 전송을 한다 			
			
		}
		
		else { //  비어있지 않은 byte  send reply
			
			for(int i=0; i<6; i++) {
				input[i+18]=  myMacAddress.addr[i];
				
				
			}
			
			input=Swaping(input);
			input[6]=(byte) 0x00;
			input[7]=(byte) 0x02; // opcode 2 reply로 해서 전송   
			
			this.GetUnderLayer().Send(input, input.length);
			
			
		}
		
		// ARP Layer Send
		
		return false;
	}
	
	public void setDstMac(byte [] dstMac) {
		
		for(int i=0; i<dstMac.length; i++) {
			this.m_sHeader.dstMac.addr[i]=dstMac[i];
			
		}
	}
	
	public void setDstIp(byte [] dstIp) {
		
		for (int i=0; i<dstIp.length; i++) {
			this.m_sHeader.dstIp.addr[i]=dstIp[i];
			
		}
	}
	
	
	public void setSrcMac(byte [] srcMac) {
		for(int i=0; i<srcMac.length; i ++) {
			this.m_sHeader.srcMac.addr[i]=srcMac[i];
			
		}
		
	}
	
	
	public void setSrcIp(byte [] srcIP) {
		for(int i=0; i<srcIP.length; i++) {
			this.m_sHeader.srcIp.addr[i]=srcIP[i];
		}
	}
	
	
	public void setARPHeaderBeforeSend() {  // 
		this.m_sHeader.macType=byte4To2(intToByte(1));
		this.m_sHeader.ipType[0]=(byte)0x00;
		this.m_sHeader.ipType[1]=(byte)0x00;
		this.m_sHeader.macAddrLen = 6;
		this.m_sHeader.ipAddrLen = 4;
		this.m_sHeader.opcode[1] = 1;
		
	}
	
	public boolean cameFromDlg(byte [] input) {  // 바로 applicatio 에서 온 비어있는 데이터
		for (int i = 0; i < input.length; i++) {
			if (input[i] == 0)
				continue;
			else
				return false;
		}
		return true;
		
	}
	
	
	// arp header 의ip주소를 string 으로 변환 ex) xxx.xxx.xxx.xxxx
	public String getDstAddFromHeader(byte [] input) {
		byte[] bytes = new byte[4];

		String dst_Addr = "";
		System.arraycopy(input, 24, bytes, 0, 4);
		for (byte b : bytes) {
			dst_Addr += Integer.toString(b & 0xFF) + ".";
		}
		return dst_Addr.substring(0, dst_Addr.length() - 1);
		
		
		
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
