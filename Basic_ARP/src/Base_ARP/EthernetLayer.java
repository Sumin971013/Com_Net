package Base_ARP;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class EthernetLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	
	private final static byte[] enetType_ARP=byte4To2(intToByte(0x0806));
	private final static byte[] broadCastAddr = {(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF };	
	
	_ETHERNET_HEADER m_sHeader = new _ETHERNET_HEADER(); // ethernet header생성자   

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

	private class _ETHERNET_HEADER {
		_ETHERNET_ADDR enet_dstaddr;
		_ETHERNET_ADDR enet_srcaddr;
		byte[] enet_type;
		byte[] enet_data;

		public _ETHERNET_HEADER() {
			this.enet_dstaddr = new _ETHERNET_ADDR();
			this.enet_srcaddr = new _ETHERNET_ADDR();
			this.enet_type = new byte[2];
			this.enet_data = null;
		}
	}

	public EthernetLayer(String pName) {
		// super(pName);
		pLayerName = pName;
		ResetHeader();
	}
	
	private void ResetHeader() {
		m_sHeader = new _ETHERNET_HEADER();
	}
	
	
	
	public byte[] getLocalMacAddress() throws UnknownHostException, SocketException {
	 	String result = "";
		InetAddress ip;

		ip = InetAddress.getLocalHost();
		NetworkInterface network = NetworkInterface.getByInetAddress(ip);
		byte[] mac = network.getHardwareAddress();
		
		return mac;
 
	}
	
 
	
	
	
	public boolean needToBroadCast(byte [] input) { // broadcasting 용 즉 dst Mac add =???  일때
		
		for(int i=0; i<6; i++) {
			
			if(input[i+18]==(byte)0x00) {
				continue;
			}
			
			else {
				return false;
				
			}
		}
		
		return true;
		
	}


	public boolean Send(byte[] input, int length) {
		
		byte[] bytes;
		m_sHeader.enet_data = input;
		
		byte [] my_dstAddress = new byte[6]; // sender  맥주소 복사하기 용 
		
		
		try {
			byte[] srcMac=getLocalMacAddress(); // 내 맥주소 가져오기   
			
			
			if(input[6] == 0x00 && input[7] == 0x01) {
				// Opcode 0x0001
				// ARP Request Message => Protocol Type 0x0806
				m_sHeader.enet_type[0] = (byte) 0x08;
				m_sHeader.enet_type[1] = (byte) 0x06;
				
				SetEthernetSrcAdd(srcMac); // 성진 구현한  getLocalMacAddress 로 맥주소 가져와서 헤더에 셋팅  
				SetEthernetDstAdd(broadCastAddr); // 브로드 캐스트 주소는 static 으로 선언한 0xffffffff ... 
				
				
				
			}
			else if (input[6] == 0x00 && input[7] == 0x02) {
				// Opcode 0x0002
				// ARP Reply Message => Protocol Type 0x0806
				m_sHeader.enet_type[0] = (byte) 0x08;
				m_sHeader.enet_type[1] = (byte) 0x06;
				System.arraycopy(input,8,my_dstAddress,0,6); // my_dstaddress에 input 즉 위에서 내려온 데이터의 8바이트부터 즉 전송자의 맥주소 카피해서 목적지로 설정 
				
				SetEthernetSrcAdd(srcMac);
				SetEthernetDstAdd(my_dstAddress);
				

				
			}
			else {
				// Opcode 0x0000
				// Normal Data Message => Protocol Type 0x0800
				m_sHeader.enet_type[0] = (byte) 0x08;
				m_sHeader.enet_type[1] = (byte) 0x00;
				
				System.arraycopy(input,8,my_dstAddress,0,6); // 이경우도 위에랑 동일 하다 
				SetEthernetSrcAdd(srcMac);
				SetEthernetDstAdd(my_dstAddress);
				
				
			}
			
			
			bytes = ObjToByte(m_sHeader, input, input.length);
			
			if(this.GetUnderLayer().Send(bytes, bytes.length))
				
				return true;
			
			
			else
				return false;
			
		} 
		
		
		catch (UnknownHostException | SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		
		}
		
		
		
		return false;
		
		
	}
	
	
	public void setEthernetHeader(byte [] input) throws SocketException{
		byte [] my_dstADD=new byte[6];
		byte [] my_srcADD=new byte[6];
		byte [] my_enetType = new byte[2];
		
		
		if(needToBroadCast(input)) {
			System.arraycopy(broadCastAddr, 0, my_dstADD, 0,6);  // 브로드 캐스팅 해야하면 사전에 final로 설정한  0xfffffff로 dstadd 설정
			
			
		}
		else {
			System.arraycopy(input, 18, my_dstADD, 0, 6); // 브로드 캐스팅이 아니면 그냥 헤더는 위에서 받은 데이터의 송신 수신자 주소 확인해서 그걸로 ㅓㄹ정 
		
		}
		
		System.arraycopy(enetType_ARP, 0, my_enetType, 0, 2);
		
		
		
		SetEthernetDstAdd(my_dstADD);
		SetEthernetSrcAdd(my_srcADD);
		
		
		
	}
	
	
	public void SetEthernetDstAdd(byte[] input){
		for(int i=0; i<6; i++) {
			m_sHeader.enet_dstaddr.addr[i]=input[i];
		}
		

	}
	
	public void SetEthernetSrcAdd(byte[] input) {
		for(int i=0; i<6; i++) {
			m_sHeader.enet_srcaddr.addr[i]=input[i];
		}
		
	}
	
    
	public boolean isItMyPacket_send(byte[] input) { // 내가 보낸 패킷 input의 src  주소와 m_sHeader의 src 주소가 같으면 true   즉 내가 보낸것 
		
		for(int i=0; i<6; i++) {
			if(m_sHeader.enet_srcaddr.addr[i] == input[6+i]) {
				continue;
			}
			
			else {
				return false;
			}
			
		}
		return true;
		
	}
	
	public boolean isItMineToReceive(byte [] input) { // input에 있는 목적지 주소가 내 맥주소랑 같은지 확인  
		for(int i=0; i<6; i++) {
			if(m_sHeader.enet_srcaddr.addr[i]==input[i]) {
				continue;
			}
			else {
				return false;
			}
			
		}
		return true;
		
	}
	
	private boolean isBroadcast(byte[] input) {// input에 있는 주소가 브로드 캐스팅 주소인지 확인 
		for(int i = 0; i< 6; i++)
			
			if (input[i] != (byte) 0xff)
				return false;
		
		
		return true;
	}
	
	
	
	public byte[] RemoveEthernetHeader(byte[] input, int length) { // ethernet header 제거 함수  
		byte[] cpyInput = new byte[length - 14];
		
		System.arraycopy(input, 14, cpyInput, 0, length - 14);
		
		input = cpyInput;
		return input;
	}
	
	
	public boolean Receive(byte[] input) {
		byte[] bytes;
		boolean MyPacket,Mine,BroadCast;
		byte[] data;
		
		MyPacket=isItMyPacket_send(input); // 내가 보낸건지 확인 하는 과정  
		
		if(MyPacket==true) { // 내가 보낸거면 안 받는다 
			
			return false; 
			
		}
		
		// 내가 보낸게 아니라면 받긴 해야한다 
		
		else {
			BroadCast=isBroadcast(input);
			
			
			if(BroadCast==false) { // input으로 온 dst 주소가 특정 주소이다  
				Mine=isItMineToReceive(input); // input으로 들어온 dst주소가 내 맥주소랑 일치하는지?  
				
				if(Mine==false) { // 내가 받을 패킷이 아니라면 받지 않는다   
					return false;
				}
				
			}
		}
		
	
		
		if(input[12] == 0x08 && input[13] == 0x06) {
			// Protocol Type 0x0806
			// ARP Message ( Request or Reply ) 인 경우
			
			Mine=isItMineToReceive(input);
			BroadCast=isBroadcast(input);
			
			if(Mine == true || BroadCast==true) {
				data=RemoveEthernetHeader(input,input.length);
				this.GetUnderLayer().Receive(data);
				
				return true;
				
			}
			
			else {
				return false;
			}
				
			
		}
		
		else if (input[12] == 0x08 && input[13] == 0x00) {
			// Protocol Type 0x0800
			// IPv4 Message
			
			
			
		}
		return true;
	}
	
	public byte[] ObjToByte(_ETHERNET_HEADER Header, byte[] input, int length) {
		byte[] buf = new byte[length + 14];
		
		System.arraycopy(Header.enet_dstaddr, 0, buf, 0, 6);
		System.arraycopy(Header.enet_srcaddr, 0, buf, 6, 6);
		System.arraycopy(Header.enet_type, 0, buf, 12, 2);

		System.arraycopy(input, 0, buf, 14, length);

		return buf;
	}
	
	
	
	public static byte[] intToByte(int value) {
		byte[] byteArray = new byte[4];
		byteArray[0] = (byte) (value >> 24);
		byteArray[1] = (byte) (value >> 16);
		byteArray[2] = (byte) (value >> 8);
		byteArray[3] = (byte) (value);
		return byteArray;
	}

	public static byte[] byte4To2(byte[] fourByte) {
		byte[] byteArray = new byte[2];
		byteArray[0] = fourByte[2];
		byteArray[1] = fourByte[3];
		return byteArray;
	}
	

	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		if(pUnderLayer == null)
			return;
		this.p_UnderLayer = pUnderLayer;
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		if(pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
	}

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
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);
	}
}
