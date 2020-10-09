package Base_ARP;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;

public class ARPLayer implements BaseLayer{
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	
	// ARP Cache Table
	private Hashtable<String, Entry> ARP_CacheTable = new Hashtable<>();
	

	public ARPLayer(String pName) {
		// super(pName);
		pLayerName = pName;
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
				macAddress += String.format("%02X", b);
				macAddress += ":";
			}
			
			// 마지막에 붙은 ":"는 제거하여 return
			return macAddress.substring(0, macAddress.length() - 1);
				
		}
	}
	
	private class _ARP_HEADER {
		// Hardware Type
		byte[] macType = new byte[2];
		
		// Protocol Type
		byte[] ipType = new byte[2];
		
		// Length of hardware Address
		byte macAddrLen;
		
		// Length of protocol Address
		byte ipAddrLen;
		
		// Opcode (ARP Request)
		byte[] opcode = new byte[2];
		
		// Sender's hardware Address
		_ETHERNET_ADDR srcMac;
		
		// Sender's protocol Address
		_IP_ADDR srcIp;
		
		// Target's hardware Address
		_ETHERNET_ADDR dstMac;
		
		// Target's protocol Address
		_IP_ADDR dstIp;
		
		public _ARP_HEADER() {						// 28 Bytes
			this.macType = new byte[2];				// 2 Bytes / 0 ~ 1
			this.ipType = new byte[2];				// 2 Bytes / 2 ~ 3
			this.macAddrLen = (byte) 0x00;			// 1 Byte / 4
			this.ipAddrLen = (byte) 0x00;			// 1 Byte / 5
			this.opcode = new byte[2];				// 2 Bytes / 6 ~ 7
			this.srcMac = new _ETHERNET_ADDR();		// 6 Bytes / 8 ~ 13
			this.srcIp = new _IP_ADDR();			// 4 Bytes / 14 ~ 17
			this.dstMac = new _ETHERNET_ADDR();		// 6 Bytes / 18 ~ 23
			this.dstIp = new _IP_ADDR();			// 4 Bytes / 24 ~ 27
			
		}
	}
	
	public boolean Send(byte[] input, int length) {
		// ARP Layer Send
		
		return false;
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
