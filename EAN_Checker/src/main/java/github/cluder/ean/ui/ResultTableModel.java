package github.cluder.ean.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import github.cluder.ean.checker.ProviderResult;
import github.cluder.ean.checker.Result;
import github.cluder.ean.provider.AbstractProvider;
import github.cluder.ean.provider.ProductProviders;

public class ResultTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private List<Result> tableData = new ArrayList<>();

	List<String> columnNames = new ArrayList<>();

	public ResultTableModel() {
		columnNames.add("EAN / ISBN");
		for (AbstractProvider p : ProductProviders.getProviders()) {
			columnNames.add(p.getName());
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		final Result result = tableData.get(rowIndex);

		switch (columnIndex) {
		case 0:
			return result.ean;
		case 1:
		case 2:
		case 3:
			final String providerName = columnNames.get(columnIndex);
			final ProviderResult providerResult = result.providerResults.get(providerName);
			if (providerResult == null) {
				return "";
			}
			return providerResult.getDisplayString();
		default:
			break;
		}
		return "";
	}

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
		return columnNames.size();
	}

	@Override
	public int getRowCount() {
		return tableData.size();
	}

	@Override
	public String getColumnName(int column) {
		return columnNames.get(column);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	public Result getRow(int i) {
		if (tableData.size() >= i) {
			return tableData.get(i);
		}
		return null;
	}

	public boolean isVisible(int identifier, boolean checkAvailability, String filterString) {
		String filStringLower = filterString.toLowerCase();

		if (checkAvailability) {
			Result result = tableData.get(identifier);
			Collection<ProviderResult> values = result.providerResults.values();
			if (values.isEmpty()) {
				return true;
			}
			boolean allUnAvailable = values.stream().allMatch(p -> p.available == false);
			if (allUnAvailable) {
				return false;
			}
		}

		if (!filStringLower.isEmpty()) {
			boolean anyStringMatch = false;
			for (int i = 0; i < getColumnCount(); i++) {
				anyStringMatch |= ((String) getValueAt(identifier, i)).toLowerCase().contains(filStringLower);
			}
			return anyStringMatch;
		}

		return true;
	}

	public void addTableData(List<Result> chunks) {
		tableData.addAll(chunks);
	}

	public void updateData(Result data) {
		for (Result r : tableData) {
			if (r.ean.equals(data.ean)) {
				r.copyFrom(data);
			}
		}
	}

	public void updateData(List<Result> chunks) {
		for (Result r : chunks) {
			updateData(r);
		}
	}
}
