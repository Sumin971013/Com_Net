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
	
	private static class ARPHeader {
		// Hardware Type
		byte[] Hardware_type = new byte[2];
		
		// Protocol Type
		byte[] Protocol_type = new byte[2];
		
		// Length of hardware Address
		byte Hardware_Adlen;
		
		// Length of protocol Address
		byte Protocol_Adlen;
		
		// Opcode (ARP Request)
		byte[] Opcode = new byte[2];
		
		// Sender's hardware Address
		byte[] src_hardware_Address = new byte[2];
		
		// Sender's protocol Address
		byte[] src_protocol_Address = new byte[4];
		
		// Target's hardware Address
		byte[] dst_hardware_Address = new byte[2];
		
		// Target's protocol Address
		byte[] dst_protocol_Address = new byte[4];
	}
	
	public boolean Send(byte[] input, int length) {
		// ARP Layer Send
		
		return false;
	}
	
	public boolean Receive(byte[] input) {
		// ARP Layer Receive
		
		return false;
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
