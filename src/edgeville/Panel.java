package edgeville;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import edgeville.model.entity.Player;

/**
 * 
 * @author jack
 *
 */

public class Panel implements ActionListener, KeyListener {

	private JTextArea namePanel;
	private JTextArea idPanel;
	private JTextArea searchPanel;

	public Panel(Player player) {
		JFrame.setDefaultLookAndFeelDecorated(true);
		JFrame localJFrame = new JFrame("SabsabiOnline - Item Searcher Build #2");
		localJFrame.setDefaultCloseOperation(2);
		localJFrame.getContentPane().setLayout(new BorderLayout());
		
		this.namePanel = new JTextArea();
		this.namePanel.setEditable(false);
		this.idPanel = new JTextArea();
		this.idPanel.setEditable(false);
		
		JPanel localJPanel1 = new JPanel(new FlowLayout());
		localJPanel1.add(this.namePanel);
		localJPanel1.add(this.idPanel);
		JScrollPane localJScrollPane = new JScrollPane(localJPanel1, 22, 31);
		localJScrollPane.setPreferredSize(new Dimension(280, 503));
		String str = "\n";

		JButton localJButton = new JButton("Search");
		localJButton.addActionListener(this);
		this.searchPanel = new JTextArea();
		this.searchPanel.addKeyListener(this);
		this.searchPanel.setLineWrap(false);
		this.searchPanel.setRows(1);
		this.searchPanel.setColumns(9);
		JPanel localJPanel2 = new JPanel(new FlowLayout());
		localJPanel2.add(this.searchPanel);
		localJPanel2.add(localJButton);
		localJFrame.getContentPane().add(localJScrollPane, "Center");
		localJFrame.getContentPane().add(localJPanel2, "South");
		localJFrame.pack();
		localJFrame.setVisible(true);
		this.searchPanel.requestFocus();
	}

	private void executeSearch() {

	}

	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == 10) {
			executeSearch();
		}
	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == 10) {
			this.searchPanel.setText(this.searchPanel.getText().replace("\n", ""));
		}
	}

	public void actionPerformed(ActionEvent e) {
		executeSearch();
	}

}