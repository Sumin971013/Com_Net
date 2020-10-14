package Base_ARP;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class TCPLayer implements BaseLayer{
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	
	private class _TCP_ADDR {
		private byte[] addr = new byte[2];
		
		public _TCP_ADDR() {
			this.addr[0] = (byte) 0x00;
			this.addr[1] = (byte) 0x00;
		}
	}
	
	private class _TCP_HEADER {
		_TCP_ADDR tcp_srcport;						// Source Port
		_TCP_ADDR tcp_dstport;						// Destination Port
		byte[] tcp_seq;								// Sequence number
		byte[] tcp_ack;								// Acknowledged Sequence
		byte tcp_offset;							// No use
		byte tcp_flag;								// Control flag
		byte[] tcp_window;							// No use
		byte[] tcp_cksum;							// Check sum
		byte[] tcp_urgptr;							// No use
		byte[] padding;								// Padding
		byte[] tcp_data;							// Additional Data Part
		
		public _TCP_HEADER() {						// Total 24 Bytes
			this.tcp_srcport = new _TCP_ADDR();		// 2 Bytes	/ 0~1
			this.tcp_dstport = new _TCP_ADDR();		// 2 Bytes	/ 2~3
			this.tcp_seq = new byte[4];				// 4 Bytes	/ 4~7
			this.tcp_ack = new byte[4];				// 4 Bytes	/ 8~11
			this.tcp_offset = (byte) 0x00;			// 1 Byte	/ 12
			this.tcp_flag = (byte) 0x00;			// 1 Byte	/ 13
			this.tcp_window = new byte[2];			// 2 Bytes	/ 14~15
			this.tcp_cksum = new byte[2];			// 2 Bytes	/ 16~17
			this.tcp_urgptr = new byte[2];			// 2 Bytes	/ 18~19
			this.padding = new byte[4];				// 4 Bytes	/ 20~23
			this.tcp_data = null;
		}
	}

	public TCPLayer(String pName) {
		// super(pName);
		pLayerName = pName;
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
