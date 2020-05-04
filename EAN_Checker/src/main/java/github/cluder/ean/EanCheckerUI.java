package github.cluder.ean;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EanCheckerUI extends JFrame {
	private static Logger log = LoggerFactory.getLogger(EanCheckerUI.class);
	private static final long serialVersionUID = 1L;

	JLabel lastCheckTxt;
	JTable tabResult;
	ResultTableModel tableDataModel;
	EANChecker checker = new EANChecker();
	String mediMopsLabel = "www.medimops.de";

	public EanCheckerUI() {
		setTitle("EAN/ISBN Checker");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		final int width = 800;
		setBounds(100, 100, width, 400);
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

		// add main panel to frame
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(10, 10));
		getContentPane().add(mainPanel, BorderLayout.CENTER);

		// top area
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		mainPanel.add(topPanel, BorderLayout.NORTH);

		JLabel medimopsLabel = new JLabel(mediMopsLabel);
		medimopsLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		topPanel.add(medimopsLabel);

		JPanel lastCheckPanel = new JPanel();
		lastCheckPanel.setLayout(new FlowLayout());
		lastCheckPanel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		topPanel.add(lastCheckPanel);

		JLabel lastCheckLabel = new JLabel("Letzter Check:");
		lastCheckTxt = new JLabel("-");
		lastCheckLabel.setLabelFor(lastCheckTxt);
		lastCheckPanel.add(lastCheckLabel);
		lastCheckPanel.add(lastCheckTxt);

		// button area
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.add(Box.createHorizontalGlue());

		{
			JButton btn = new JButton("Verf�gbarkeit pr�fen");
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

					lastCheckTxt.setText(sdf.format(new Date()));
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

		// table area
		tableDataModel = new ResultTableModel();
		tabResult = new JTable(tableDataModel);
		JScrollPane tableScrollPane = new JScrollPane(tabResult);
		tabResult.setFillsViewportHeight(true);
		tabResult.getColumnModel().getColumn(0).setPreferredWidth(100);
		tabResult.getColumnModel().getColumn(1).setPreferredWidth(200);
		tabResult.getColumnModel().getColumn(2).setPreferredWidth(150);
		tabResult.getColumnModel().getColumn(3).setPreferredWidth(50);
		tabResult.getColumnModel().getColumn(4).setPreferredWidth(230);

		JPopupMenu tableMenu = new JPopupMenu();
		JMenuItem openUrlItem = new JMenuItem("Ausgew�hlte URL �ffnen");
		tableMenu.add(openUrlItem);
		openUrlItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final int selectedRow = tabResult.getSelectedRow();
				if (selectedRow >= 0) {
					final Result row = tableDataModel.getRow(selectedRow);
					if (row != null && !row.ean.isEmpty()) {
						try {
							final Desktop desktop = Desktop.getDesktop();
							desktop.browse(new URI(EANCheckerMain.SEARCH_URL + row.ean));
						} catch (IOException | URISyntaxException ex) {
							log.error("error opening url:" + ex.getMessage(), ex);
						}
					}
				}
			}
		});

		tabResult.setComponentPopupMenu(tableMenu);

		mainPanel.add(tableScrollPane, BorderLayout.CENTER);

		initTableValues();
	}

	private void initTableValues() {
		final List<String> eans = checker.readEans();
		tableDataModel.clear();
		eans.stream().forEach(ean -> tableDataModel.getTableData().add(new Result(ean)));
		tableDataModel.fireTableDataChanged();
	}
}
