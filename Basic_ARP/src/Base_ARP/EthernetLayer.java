package Base_ARP;

import java.io.ByteArrayOutputStream;
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
	
	_ETHERNET_HEADER m_sHeader = new _ETHERNET_HEADER();

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


	public boolean Send(byte[] input, int length) {
		byte[] bytes;
		m_sHeader.enet_data = input;
		
		if(input[6] == 0x00 && input[7] == 0x01) {
			// Opcode 0x0001
			// ARP Request Message => Protocol Type 0x0806
			m_sHeader.enet_type[0] = (byte) 0x08;
			m_sHeader.enet_type[1] = (byte) 0x06;
		}
		else if (input[6] == 0x00 && input[7] == 0x02) {
			// Opcode 0x0002
			// ARP Reply Message => Protocol Type 0x0806
			m_sHeader.enet_type[0] = (byte) 0x08;
			m_sHeader.enet_type[1] = (byte) 0x06;
		}
		else {
			// Opcode 0x0000
			// Normal Data Message => Protocol Type 0x0800
			m_sHeader.enet_type[0] = (byte) 0x08;
			m_sHeader.enet_type[1] = (byte) 0x00;
		}
		
		bytes = ObjToByte(m_sHeader, input, input.length);
		
		if(this.GetUnderLayer().Send(bytes, bytes.length))
			return true;
		else
			return false;
		
	}

	public boolean Receive(byte[] input) {
		byte[] bytes;
		
		if (input[12] == 0x08 && input[13] == 0x06) {
			// Protocol Type 0x0806
			// ARP Message ( Request or Reply ) 인 경우
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
