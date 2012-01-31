package nch.chtn.shiplog.extractor;

import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import nch.chtn.shiplog.extractor.output.ShiplogOutputter;
import nch.chtn.shiplog.extractor.persistence.SpecimenInfoMapper;

import org.apache.ibatis.session.SqlSession;

@Getter @Setter @Log
public class StarsShiplogExtractor implements ShiplogExtractor {

	// Dependencies
	private SqlSession sqlSession;
	private Date startDateTime;
	private Date endDateTime;
	private ShiplogOutputter outputter;
	// Query results for
	private List<Map<String,Object>> specimens;
	private List<Map<String,Object>> projects;
	private List<Map<String,Object>> investigators;
	
	public void Run() throws Exception {
		log.info("Beginning execution of STARS shiplog extractor");
		
		SpecimenInfoMapper specimenInfoMapper = (SpecimenInfoMapper) sqlSession.getMapper(SpecimenInfoMapper.class);
		specimens = specimenInfoMapper.selectSpecimenInfo(this);
		projects = specimenInfoMapper.selectDistributionProjectInfo(this);
		investigators = specimenInfoMapper.selectReceivingInvestigatorInfo(this);
		outputter.write(specimens, projects, investigators);

		log.info("Completed execution of STARS shiplog extractor");
	}
}
