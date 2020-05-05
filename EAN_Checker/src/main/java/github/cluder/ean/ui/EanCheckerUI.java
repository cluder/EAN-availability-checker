package github.cluder.ean.ui;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import github.cluder.ean.checker.EANChecker;
import github.cluder.ean.checker.Result;
import github.cluder.ean.provider.AbstractProvider;
import github.cluder.ean.provider.ProductProviders;

public class EanCheckerUI extends JFrame {
	private static Logger log = LoggerFactory.getLogger(EanCheckerUI.class);
	private static final long serialVersionUID = 1L;

	String lastRefreshTxt = "Letzte Aktualisierung: ";
	JLabel lblLastCheckTxt;
	JTable tabResult;
	JTextField txtTableSearch;
	ResultTableModel tableDataModel;
	TableRowSorter<ResultTableModel> tableRowSorter;
	JCheckBox checkBoxOnlyAvailable;

	EANChecker checker = new EANChecker();

	public EanCheckerUI() {
		setTitle("EAN/ISBN Checker");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		final int width = 1000;
		setBounds(100, 100, width, 600);

		createMenubar();

		// add main panel to frame
		// -------------------------
		// top area
		JPanel mainPanel = new JPanel();
		getContentPane().add(mainPanel);
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		lblLastCheckTxt = new JLabel(lastRefreshTxt);
		int gridY = 0;
		c.gridy = gridY;
		c.gridx = gridY;
		c.weightx = 0;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		c.insets.left = 10;
		mainPanel.add(lblLastCheckTxt, c);

		gridY++;
		checkBoxOnlyAvailable = new JCheckBox("nicht verfügbare ausblenden");
		c.gridy = gridY;
		c.gridx = 0;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		mainPanel.add(checkBoxOnlyAvailable, c);
		checkBoxOnlyAvailable.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				tableRowSorter.sort();
			}
		});

		gridY++;
		c.gridy = gridY;
		c.gridwidth = 1;
		c.ipadx = 10;
		c.weightx = 2;
		c.gridx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		txtTableSearch = new JTextField();
		txtTableSearch.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				tableRowSorter.sort();
			}
		});
		mainPanel.add(txtTableSearch, c);

		// ---------------------------
		// button area
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(Box.createHorizontalGlue());

		{
			JButton btn = new JButton("Verfügbarkeit prüfen");
			buttonPanel.add(btn);

			btn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					checker.readEans();
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					final List<Result> checkResults = checker.checkAllEans();
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					tableDataModel.setTableData(checkResults);
					tableDataModel.fireTableDataChanged();

					SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

					lblLastCheckTxt.setText(lastRefreshTxt + sdf.format(new Date()));
				}
			});
		}
		{
			JButton btn = new JButton("EANs aus Datei laden");
			buttonPanel.add(btn);
			btn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					initTableValues();
				}
			});
		}
		buttonPanel.add(Box.createHorizontalGlue());

		// -----------------------------
		// table area
		tableDataModel = new ResultTableModel();
		tabResult = new JTable(tableDataModel);
		tabResult.setFont(new Font("monospaced", Font.PLAIN, 12));
		JScrollPane tableScrollPane = new JScrollPane(tabResult);
		tabResult.setFillsViewportHeight(true);
		int eanWidth = 115;
		tabResult.getColumnModel().getColumn(0).setPreferredWidth(eanWidth);
		tabResult.getColumnModel().getColumn(0).setMinWidth(eanWidth);
		tabResult.getColumnModel().getColumn(0).setMaxWidth(eanWidth);
		tabResult.getColumnModel().getColumn(1).setPreferredWidth(280);
		tabResult.getColumnModel().getColumn(2).setPreferredWidth(280);
		tabResult.getColumnModel().getColumn(3).setPreferredWidth(280);

		tableRowSorter = new TableRowSorter<>(this.tableDataModel);

		RowFilter<Object, Object> customRowFilter = new RowFilter<Object, Object>() {
			@Override
			public boolean include(Entry<? extends Object, ? extends Object> entry) {
				ResultTableModel model = (ResultTableModel) entry.getModel();
				return model.isVisible((int) entry.getIdentifier(), checkBoxOnlyAvailable.isSelected(),
						txtTableSearch.getText());
			}
		};

		tableRowSorter.setRowFilter(customRowFilter);
		tabResult.setRowSorter(tableRowSorter);

		JPopupMenu tableMenu = new JPopupMenu();
		tabResult.setComponentPopupMenu(tableMenu);
		for (AbstractProvider p : ProductProviders.getProviders()) {
			JMenuItem openUrlItem = new JMenuItem(p.getName() + " Link öffnen");
			tableMenu.add(openUrlItem);
			openUrlItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final int selectedRow = tabResult.getSelectedRow();
					if (selectedRow < 0) {
						return;
					}
					final Result row = tableDataModel.getRow(selectedRow);
					if (row != null && !row.ean.isEmpty()) {
						try {
							final Desktop desktop = Desktop.getDesktop();
							desktop.browse(new URI(p.getSearchUrl(row.ean)));
						} catch (IOException | URISyntaxException ex) {
							log.error("error opening url:" + ex.getMessage(), ex);
						}
					}
				}
			});
		}

		gridY++;
		c.gridx = 0;
		c.gridy = gridY;
		c.weightx = 3;
		c.weighty = 3;
		c.fill = GridBagConstraints.BOTH;
		mainPanel.add(tableScrollPane, c);

		gridY++;
		c.gridx = 0;
		c.gridy = gridY;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(buttonPanel, c);

		// load ean's from file
		initTableValues();
	}

	private void createMenubar() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		{
			JMenu mnuFile = new JMenu("Datei");
			menuBar.add(mnuFile);

			JMenuItem mntmExit = new JMenuItem("Beenden");
			mntmExit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			});
			mnuFile.add(mntmExit);
		}
	}

	private void initTableValues() {
		final List<String> eans = checker.readEans();
		tableDataModel.clear();
		eans.stream().forEach(ean -> {
			tableDataModel.getTableData().add(new Result(ean));
		});
		tableDataModel.fireTableDataChanged();
	}
}
