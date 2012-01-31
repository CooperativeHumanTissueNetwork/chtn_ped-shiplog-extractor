package nch.chtn.shiplog.extractor.persistence;

import java.util.List;
import java.util.Map;

import nch.chtn.shiplog.extractor.ShiplogExtractor;

public interface SpecimenInfoMapper {
	public List<Map<String, Object>> selectSpecimenInfo(ShiplogExtractor extractor);
	public List<Map<String, Object>> selectDistributionProjectInfo(ShiplogExtractor extractor);
	public List<Map<String, Object>> selectReceivingInvestigatorInfo(ShiplogExtractor extractor);
}
