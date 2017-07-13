/*
 * Code to trim swagger generated java POJOs
 * 
 * -aditya.oli@infosys.com
 * 
 * */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SwaggerTrimmer {
	private JFrame mainFrame;
	private JPanel mainPanel;
	private JLabel fileCountLabel;
	private JLabel enterPkgLabel;
	private JLabel sampleLabel;
	private JTextField enterPkgValue;
	private JButton trimButton;
	
	public SwaggerTrimmer(int fileCount){
		prepareGUI(fileCount);
	}
	
	private void prepareGUI(int fileCount){
		mainFrame = new JFrame("Swagger Trimmer");
		mainFrame.setSize(420,250);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((screenSize.getWidth() - mainFrame.getWidth()) / 2);
	    int y = (int) ((screenSize.getHeight() - mainFrame.getHeight()) / 2);
		mainFrame.setLocation(x, y);
	    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	    mainPanel = new JPanel();
	    mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 15));
	    
	    fileCountLabel = new JLabel(fileCount + " files found!");
	    fileCountLabel.setFont(new Font("Courier New", Font.BOLD, 25));
	    fileCountLabel.setForeground(Color.BLUE);
	    
	    enterPkgLabel = new JLabel("Enter the Package name:");
	    enterPkgLabel.setFont(new Font("Courier New", Font.BOLD, 15));
	    sampleLabel = new JLabel("example: com.vf.uk.dal.entity.checkout");
	    sampleLabel.setFont(new Font("Courier New", Font.BOLD, 13));
	    sampleLabel.setForeground(Color.red);
	    enterPkgValue = new JTextField(30);
	    enterPkgValue.setFont(new Font("Arial", Font.PLAIN, 15));
	    trimButton = new JButton("OK");

	    mainPanel.add(fileCountLabel);
	    mainPanel.add(enterPkgLabel);
	    mainPanel.add(sampleLabel);
	    mainPanel.add(enterPkgValue);
	    mainPanel.add(trimButton);
		
	    mainFrame.add(mainPanel);
	    mainFrame.setResizable(false);
	    mainFrame.setVisible(true);
	    
	    trimButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String packageName = enterPkgValue.getText().trim();
				int status;
				if(packageName != null && !packageName.isEmpty() && packageName.matches("^[a-z][a-z_]*(\\.[a-z_]+)+[a-z_]$")){
					status = performTrim(packageName);
					if(status == -1)
						JOptionPane.showMessageDialog(new JFrame(), "Oops!... Something went wrong..", "Error", JOptionPane.ERROR_MESSAGE);
					else if(status == 0)
						JOptionPane.showMessageDialog(new JFrame(), "No Java files found !", "Info", JOptionPane.WARNING_MESSAGE);
					else{
						JOptionPane.showMessageDialog(new JFrame(), status + " files modified !", "Info", JOptionPane.INFORMATION_MESSAGE);
						mainFrame.dispose();
					}
				}
				else
					JOptionPane.showMessageDialog(new JFrame(), "Invalid Package name !", "Error",JOptionPane.ERROR_MESSAGE);
				
			}
		});
	}
	
	private int performTrim(String packageName){
 		String path = System.getProperty("user.dir") + "\\";
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		BufferedReader bufferedReader = null;
		FileReader fileReader = null;
		int filesModified = 0;
		
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() && listOfFiles[i].getName().contains(".java")) {
				System.out.println("File : " + listOfFiles[i].getName());
				try {
					Writer output;
					String sCurrentLine;
					bufferedReader = new BufferedReader(new FileReader(path + listOfFiles[i].getName()));

					File tempfile = new File(path + "temporaryFile.java");
					if (tempfile.createNewFile()) {
						System.out.println("Temporary file created.");
					} else {
						return -1;
					}

					output = new BufferedWriter(new FileWriter(path + "temporaryFile.java"));

					while ((sCurrentLine = bufferedReader.readLine()) != null) {
						// System.out.println(sCurrentLine);
						if(sCurrentLine.contains("package "))
						{
							output.write("package "+packageName+";");
							output.write("\r\n");
						}
						else if (   sCurrentLine.contains("ApiModelProperty")
								|| sCurrentLine.contains("com.fasterxml.jackson.annotation.JsonCreator")
								|| sCurrentLine.contains("io.swagger.annotations.ApiModel")
								|| sCurrentLine.contains("io.swagger.annotations.ApiModelProperty")
								|| sCurrentLine.contains("javax.validation.constraints.")
								|| sCurrentLine.contains("javax.annotation.Generated")
							    || sCurrentLine.contains("ApiModel")
								|| sCurrentLine.contains("io.swagger")
								|| sCurrentLine.contains("JsonProperty")
								|| sCurrentLine.contains("javax.annotation.Generated")
								|| sCurrentLine.contains("SerializedName")
								|| sCurrentLine.contains("*")
							   )
						{
								
								// Do Nothing
						}
						else {
							output.write(sCurrentLine);
							output.write("\r\n");
						}
					}
					output.close();
					InputStream inStream = new FileInputStream(path + "temporaryFile.java");
					OutputStream outStream = new FileOutputStream(path + listOfFiles[i].getName());

					byte[] buffer = new byte[1024];

					int length;
					while ((length = inStream.read(buffer)) > 0) {
						outStream.write(buffer, 0, length);
					}
					inStream.close();
					outStream.close();
					tempfile.delete();

				} catch (Exception e) {
					return -1;
				} finally {
					try {
						if (bufferedReader != null)
							bufferedReader.close();

						if (fileReader != null)
							fileReader.close();

					} catch (Exception ex) {
						return -1;
					}
				}
				filesModified ++;
			}
		}
		return filesModified;
	}
	
	public static void main(String[] args) {
		new SwaggerTrimmer(new File(System.getProperty("user.dir")).listFiles().length - 1);
	}
}
