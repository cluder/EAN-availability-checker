package github.cluder.ean;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class ResultTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private List<Result> tableData = new ArrayList<>();
	String[] columnNames = { "EAN / ISBN", "Produkt", "ausverkauft", "Preis", "URL" };

	public void setTableData(List<Result> tableData) {
		this.tableData = tableData;
	}

	public List<Result> getTableData() {
		return tableData;
	}

	public void clear() {
		tableData.clear();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return tableData.size();
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return tableData.get(rowIndex).ean;
		case 1:
			return tableData.get(rowIndex).productName;
		case 2:
			return tableData.get(rowIndex).outOfStock;
		case 3:
			return tableData.get(rowIndex).price;
		case 4:
			return tableData.get(rowIndex).url;
		case 5:
			return tableData.get(rowIndex).ean;
		}
		return ":-(";
	}

	public Result getRow(int i) {
		if (tableData.size() >= i) {
			return tableData.get(i);
		}
		return null;
	}
}
