drop view vw_CHTN_Specimens_Shipped;
create view vw_CHTN_Specimens_Shipped as
with SpecimenShipmentInfo as (
	select
		specship.SpecimenID,
		case
			when track.InvestigatorID is not null then 'INVESTIGATOR'
			when track.ReviewerID is not null then 'REVIEWER'
			when track.StatisticalGroupInstIdentifier is not null then 'INSTITUTION'
			else 'UNKNOWN'
		end as ShipReceiverType,
		specship.DateTimeShipped as ShipDateTime,
		coalesce(track.InvestigatorID, '') as ShipInvestigatorID,
		coalesce(track.InvestigatorName, '') as ShipInvestigatorName,
		track.ShipmentNo as ShipTrackingNumber,
		track.Name as ShipToName,
		track.Address as ShipToAddress,
		track.City as ShipToCity,
		track.State as ShipToState
	from tblSpecimenShipment specship
		inner join tblShipmentTracking track on specship.ShipmentTrackingID = track.ShipmentTrackingID
	where 
		Notes <> 'Shipped to MGL' and 
		Direction = 'OUT'
), DistributionProjectInfo as (
	select
		dp.SpecimenID,
		dbo.ToList(distinct dp.DistributionProjectName) as DistProjectName
	from 
		tblSpecimenDistributionProject dp
	group by 
		dp.SpecimenID
), SpecimenDetails as (
	select
		sr.SpecimenID,
		sr.Type as SpecType,
		sr.StatisticalGroup as SpecStatisticalGroup,
		sr.UnitValue as SpecUnitValue,
		sr.UnitType as SpecUnitOfMeasure,
		sr.SiteType as SpecSiteType,
		sr.SpecimenStatus as SpecCurrentStatus,
		pat.HBSPatientID as SpecPatientUSI
	from
		tblSpecimenRepository sr
			inner join tblPatient pat on sr.PatientID = pat.PatientID
			left outer join tblSpecimenType spectype on sr.SpecimenTypeID = spectype.SpecimenTypeID
			left outer join tblSpecimenCategory speccat on speccat.SpecimenCategoryID = spectype.SpecimenCategoryID
), SpecimenPathReviewSummary as (
	select
		SpecimenID,
		dbo.ToList(distinct PrimarySite) as PathRevPrimarySite_List,
		dbo.ToList(distinct Diagnosis) as PathRevDiagnosis_List,
		dbo.ToList(distinct ReviewDxOther) as PathRevDiagnosisOther_List
	from tblPathReview
	where SpecimenID is not null
	group by SpecimenID
)
select
	ShipDateTime,
	coalesce(s.SpecPatientUSI, '') as SpecPatientUSI,
	ssi.SpecimenID,
	DistProjectName,
	ShipReceiverType,
	ShipInvestigatorID,
	ShipInvestigatorName,
	inv.TqInvestID as ShipInvestigatorTissueQuestID,
	ShipTrackingNumber,
	ShipToName,
	ShipToAddress,
	ShipToCity,
	ShipToState,
	SpecType,
	SpecStatisticalGroup,
	SpecUnitValue,
	SpecUnitOfMeasure,
	coalesce(s.SpecSiteType, path.PathRevPrimarySite_List, '') as SpecSite,
	case 
		when PathRevDiagnosis_List = '---- OTHER -----' then PathRevDiagnosisOther_List
		else coalesce(PathRevDiagnosis_List, '')
	end as SpecPathReviewDx,
	SpecCurrentStatus
from
	SpecimenShipmentInfo ssi
	inner join SpecimenDetails s on s.SpecimenID = ssi.SpecimenID
	left outer join DistributionProjectInfo dpi on ssi.SpecimenID = dpi.SpecimenID
	left outer join SpecimenPathReviewSummary path on ssi.SpecimenID = path.SpecimenID
	left outer join [CHTNIQ].[dbo].[Investigator] inv on inv.TqInvestID = ShipInvestigatorID
where
	s.SpecStatisticalGroup IN ('COG', 'Delinked') and
	ssi.ShipReceiverType <> 'REVIEWER' and
	dpi.DistProjectName is not null;

select *
from vw_CHTN_Specimens_Shipped
order by
	ShipDateTime,
	SpecPatientUSI;