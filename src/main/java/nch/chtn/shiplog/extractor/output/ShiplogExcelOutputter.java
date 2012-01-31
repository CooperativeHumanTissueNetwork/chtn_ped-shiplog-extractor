package nch.chtn.shiplog.extractor.output;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import com.google.common.io.Files;

@Log @Getter @Setter
public class ShiplogExcelOutputter implements ShiplogOutputter {
	private String filename;
	private File file;
	
	// Java Excel API
	private WritableWorkbook currentWorkbook;
	private WritableSheet currentWorksheet;
	private int currentRowIndex;
	private int currentColumnIndex;
	private int nextWorksheetIndex = 0;
	private WritableFont fontArialBold12 = new WritableFont(WritableFont.ARIAL, 12, WritableFont.BOLD);
	private WritableCellFormat headerFormat = new WritableCellFormat(fontArialBold12);  // headerFormat.setAlignment(Alignment.CENTRE);
	private WritableFont fontArial10 = new WritableFont(WritableFont.ARIAL, 10);
	private WritableCellFormat bodyFormat = new WritableCellFormat(fontArial10);

	private void createDirectoryStructureIfNecessary() throws Exception {
		file = new File(filename);
		Files.createParentDirs(file);
	}
	
	public void write(
			List<Map<String, Object>> specimens, 
			List<Map<String, Object>> projects, 
			List<Map<String, Object>> investigators
	) throws Exception {
		log.info(MessageFormat.format("Outputting shiplog data to Excel file: {0}", filename));
		log.info(MessageFormat.format("Outputing {0} specimen(s), {1} project(s), {2} investigator(s)...",
				specimens.size(), projects.size(), investigators.size()));
		
		createWorkbook();
		addWorksheetForData("Specimens", new String[] { 
				"ShipDateTime", "SpecPatientUSI", "SpecimenID", "DistProjectName",
				"ShipReceiverType", "ShipInvestigatorID", "ShipInvestigatorName",
				"ShipInvestigatorTissueQuestID", "ShipTrackingNumber", "ShipToName",
				"ShipToAddress", "ShipToCity", "ShipToState", "SpecType", "SpecStatisticalGroup",
				"SpecUnitValue", "SpecUnitOfMeasure", "SpecSite", "SpecPathReviewDx", "SpecCurrentStatus"				
			}, 
			specimens);
		addWorksheetForData("Projects", new String[] { 
				"DistProjectName", "DistCount", "MinShipDateTime", "MaxShipDateTime", "TQ REQ#"
			}, 
			projects);
		addWorksheetForData("Investigators", new String[] { 
				"ShipInvestigatorName", "ShipToName", "ShipInvestigatorTissueQuestID",
				"DistCount", "MinShipDateTime", "MaxShipDateTime", "TQ INV#"
			}, 
			investigators);
		saveWorkbook();
	}

	private WritableWorkbook createWorkbook() throws Exception {
		createDirectoryStructureIfNecessary();
		currentWorkbook = Workbook.createWorkbook(file);
		return currentWorkbook;
	}
	
	private void saveWorkbook() throws IOException, WriteException {
		currentWorkbook.write();
		currentWorkbook.close();
		log.info(MessageFormat.format("Wrote Excel workbook filename = {0}, size = {1}", filename, file.length()));
	}

	private void addWorksheetForData(String worksheetName, String[] fieldNamesInOrder,
			List<Map<String, Object>> rows) throws WriteException,
			RowsExceededException {
		addNewWorksheet(worksheetName);
		addHeadings(fieldNamesInOrder);
		addRows(fieldNamesInOrder, rows);
	}

	private void addRows(String[] fieldNamesInOrder,
			List<Map<String, Object>> rows) throws WriteException,
			RowsExceededException {
		for(Map<String,Object> row : rows) {
			for(String fieldName : fieldNamesInOrder) {
				Object fieldValue = row.get(fieldName);
				writeBodyCellAndPositionToNextCell(fieldValue);
			}
			positionToNextRow();
		}
	}

	private void writeHeaderCellAndPositionToNextCell(Object fieldValue)
			throws WriteException, RowsExceededException {
		Label l = new Label(currentColumnIndex, currentRowIndex, fieldValue.toString(), headerFormat);
		currentWorksheet.addCell(l);
		positionToNextColumn();
	}

	private void writeBodyCellAndPositionToNextCell(Object fieldValue)
			throws WriteException, RowsExceededException {
		if(fieldValue instanceof Number) {
			jxl.write.Number n = new jxl.write.Number(currentColumnIndex, currentRowIndex, ((Number) fieldValue).doubleValue());
			currentWorksheet.addCell(n);
		} else {
			Label l = new Label(currentColumnIndex, currentRowIndex, fieldValue == null ? null : fieldValue.toString(), bodyFormat);
			currentWorksheet.addCell(l);
		}
		
		positionToNextColumn();
	}
	
	private void positionToNextRow() {
		currentRowIndex++; 
		currentColumnIndex = 0;
	}

	private void addHeadings(String[] specFieldsInOrder) throws WriteException,
			RowsExceededException {
		for(String fieldName : specFieldsInOrder) {
			writeHeaderCellAndPositionToNextCell(fieldName);
		}
		positionToNextRow();
	}

	private void positionToNextColumn() {
		currentColumnIndex++;
	}

	private void addNewWorksheet(String name) {
		currentWorksheet = currentWorkbook.createSheet(name, nextWorksheetIndex++);
		currentRowIndex = 0;
		currentColumnIndex = 0;
	}

}
