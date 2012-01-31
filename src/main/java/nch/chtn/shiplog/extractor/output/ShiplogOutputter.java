package nch.chtn.shiplog.extractor.output;

import java.util.List;
import java.util.Map;

public interface ShiplogOutputter {
	public void write(
			List<Map<String, Object>> specimens, 
			List<Map<String, Object>> projects, 
			List<Map<String, Object>> investigators
	) throws Exception;
}
