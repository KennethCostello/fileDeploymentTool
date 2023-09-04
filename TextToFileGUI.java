import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.FileInputStream;

public class TextToFileGUI extends JFrame {

    private JTextField textField1;
    private JTextField textField2;

    private JButton printButton1;
    private JButton printButton2;

    private List<String> pcs;
    private JCheckBox[] checkBoxes;
    private JButton writeButton;

    private JTextArea textArea1;
    private JTextArea textArea2;
    private JTextArea textArea3;
    private JTextArea textArea4;
    private JTextArea textArea5;

    private static String PC_INPUT_FILE_PATH;
    private static String PC_OUTPUT_FILE_PATH;
    private static String DEST_PATHWAY_FILE_PATH;
    private static String FOLD_PATHWAY_FILE_PATH;
    private static String SCRIPT_PATH;

    public TextToFileGUI() {
        Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream("setup.config")) {
            properties.load(fileInputStream);

            // Read the pathways from the configuration file
            PC_INPUT_FILE_PATH = properties.getProperty("Computer_List_Path");
            DEST_PATHWAY_FILE_PATH = properties.getProperty("Destination_Pathway_Path");
            FOLD_PATHWAY_FILE_PATH = properties.getProperty("Folder_Path_Path");
            PC_OUTPUT_FILE_PATH = properties.getProperty("Write_Computer_List_Path");
            SCRIPT_PATH = properties.getProperty("Script_Path");

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set up the GUI
        setTitle("File Deployment Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(800, 400));

        // Create a JTabbedPane to hold multiple tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        add(tabbedPane);

        // Create the "Write to File" tab
        JPanel printTab = new JPanel();
        printTab.setLayout(new BoxLayout(printTab, BoxLayout.Y_AXIS));
        tabbedPane.addTab("Write to File", printTab);
        printTab.setLayout(new GridLayout(0, 1));

        // Add title and text field for Destination Pathway
        JLabel label1 = new JLabel("Pathway:");
        label1.setAlignmentX(Component.LEFT_ALIGNMENT);
        printTab.add(label1);

        textField1 = new JTextField();
        JScrollPane scroll4Pane1 = new JScrollPane(textField1);
        printTab.add(scroll4Pane1);

        // Button to update DestinationPathway.txt
        printButton1 = new JButton("Update DestinationPathway.txt");
        printButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = textField1.getText();
                try {
                    FileWriter writer = new FileWriter(DEST_PATHWAY_FILE_PATH);
                    writer.write(text);
                    writer.close();
                    System.out.println("Successfully wrote to the file.");
                } catch (IOException e1) {
                    System.out.println("An error occurred while writing to the file: " + e1.getMessage());
                }
                textField1.setText("");
            }
        });
        printTab.add(printButton1);

        textArea4 = new JTextArea(20, 40);
        textArea4.setEditable(false);
        textArea4.setVisible(false);
        printTab.add(textArea4);

        // Add title and text field for Folder Path
        JLabel label2 = new JLabel("Folder:");
        label2.setAlignmentX(Component.LEFT_ALIGNMENT);
        printTab.add(label2);

        textField2 = new JTextField(20);
        JScrollPane scroll3Pane1 = new JScrollPane(textField2);
        printTab.add(scroll3Pane1);

        // Button to update FolderList.txt
        printButton2 = new JButton("Update FolderList.txt");
        printButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = textField2.getText();
                try {
                    FileWriter writer = new FileWriter(FOLD_PATHWAY_FILE_PATH);
                    writer.write(text);
                    writer.close();
                    System.out.println("Successfully wrote to the file.");
                } catch (IOException e1) {
                    System.out.println("An error occurred while writing to the file: " + e1.getMessage());
                }
                textField2.setText("");
            }
        });
        printTab.add(printButton2);

        textArea5 = new JTextArea(30, 40);
        textArea5.setEditable(false);
        textArea5.setVisible(false);
        printTab.add(textArea5);

        // Create the "PC Selection" tab
        JPanel pcSelectionTab = new JPanel();
        pcSelectionTab.setLayout(new BorderLayout());
        tabbedPane.addTab("PC Selection", pcSelectionTab);

        JPanel pcPanel = new JPanel(new GridLayout(0, 1));
        JScrollPane scrollPane = new JScrollPane(pcPanel);
        pcSelectionTab.add(scrollPane, BorderLayout.CENTER);

        writeButton = new JButton("Write to Selected PCs");
        pcSelectionTab.add(writeButton, BorderLayout.SOUTH);

        writeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                writeSelectedPCs();
            }
        });

        File inputFile = new File(PC_INPUT_FILE_PATH);
        pcs = readPCsFromFile(inputFile);

        if (!pcs.isEmpty()) {
            checkBoxes = new JCheckBox[pcs.size()];

            JCheckBox selectAllCheckBox = new JCheckBox("Select All");
            selectAllCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    boolean selectAll = selectAllCheckBox.isSelected();

                    // Set the state of checkboxes without "--" in their label
                    for (JCheckBox checkBox : checkBoxes) {
                        String label = checkBox.getText();
                        if (!label.contains("--")) {
                            checkBox.setSelected(selectAll);
                        }
                    }
                }
            });

            pcPanel.add(selectAllCheckBox);

            for (int i = 0; i < pcs.size(); i++) {
                String pc = pcs.get(i);

                JCheckBox checkBox = new JCheckBox(pc);
                checkBoxes[i] = checkBox;
                pcPanel.add(checkBox);

                // Disable checkboxes with "--" in their label
                if (pc.contains("--")) {
                    checkBox.setSelected(false);
                    checkBox.setEnabled(false);
                }
            }
        } else {
            pcPanel.add(new JLabel("No PCs found in the input file."));
            writeButton.setEnabled(false);
        }

        // Create the "Send to PCs" tab
        JPanel pShellTab = new JPanel();
        tabbedPane.addTab("Send to PCs", pShellTab);
        pShellTab.setLayout(new GridLayout(0, 1));

        // Add title and text area to display file content for Destination Path
        JLabel label3 = new JLabel("Destination Path:");
        label3.setAlignmentX(Component.LEFT_ALIGNMENT);
        pShellTab.add(label3);

        textArea1 = new JTextArea(2, 40);
        textArea1.setEditable(false);
        JScrollPane scrollPane1 = new JScrollPane(textArea1);
        pShellTab.add(scrollPane1);

        JButton showContentButton1 = new JButton("Show Content of DestinationPathway.txt");
        showContentButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    FileReader fileReader = new FileReader(DEST_PATHWAY_FILE_PATH);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);

                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        content.append(line).append("\n");
                    }

                    bufferedReader.close();

                    textArea1.setText(content.toString());
                } catch (IOException ex) {
                    System.out.println("An error occurred while reading the file: " + ex.getMessage());
                }
            }
        });
        pShellTab.add(showContentButton1);

        // Add title and text area to display file content for Folder Path
        JLabel label4 = new JLabel("Folder Path:");
        pShellTab.add(label4);

        textArea2 = new JTextArea(2, 40);
        textArea2.setEditable(false);
        JScrollPane scroll2Pane1 = new JScrollPane(textArea2);
        pShellTab.add(scroll2Pane1);

        JButton showContentButton2 = new JButton("Show Content of FolderPathway.txt");
        showContentButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    FileReader fileReader = new FileReader(FOLD_PATHWAY_FILE_PATH);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);

                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        content.append(line).append("\n");
                    }

                    bufferedReader.close();

                    textArea2.setText(content.toString());
                } catch (IOException ex) {
                    System.out.println("An error occurred while reading the file: " + ex.getMessage());
                }
            }
        });
        pShellTab.add(showContentButton2);

        textArea3 = new JTextArea(2, 40);
        textArea3.setEditable(false);
        textArea3.setVisible(false);
        pShellTab.add(textArea3);

        JLabel label5 = new JLabel("Run Script");
        pShellTab.add(label5);

        JButton exebutton = new JButton("EXECUTE SCRIPT");
        exebutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Replace the scriptPath with the actual path to your PowerShell script
                    String scriptPath = SCRIPT_PATH;
                    ProcessBuilder processBuilder = new ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Bypass", "-File", scriptPath);
                    Process process = processBuilder.start();
                    process.waitFor();

                    int exitCode = process.exitValue();
                    if (exitCode == 0) {
                        System.out.println("Script executed successfully.");
                    } else {
                        System.out.println("Script execution failed with exit code: " + exitCode);
                    }
                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });
        exebutton.setSize(50, 50);
        pShellTab.add(exebutton);

        pack();
        setVisible(true);
    }

    private List<String> readPCsFromFile(File inputFile) {
        List<String> pcs = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] pcTokens = line.split(" ");

                for (String pc : pcTokens) {
                    pcs.add(pc);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return pcs;
    }

    private void writeSelectedPCs() {
        File outputFile = new File(PC_OUTPUT_FILE_PATH);

        List<String> selectedPCs = new ArrayList<>();

        for (int i = 0; i < pcs.size(); i++) {
            if (checkBoxes[i].isSelected()) {
                selectedPCs.add(pcs.get(i));
            }
        }

        writePCsToFile(selectedPCs, outputFile);

        JOptionPane.showMessageDialog(this, "Selected PCs have been written to the output file.");
    }

    private void writePCsToFile(List<String> pcs, File outputFile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for (String pc : pcs) {
                writer.write(pc);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TextToFileGUI();
            }
        });
    }
}
