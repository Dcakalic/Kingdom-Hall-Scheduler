package main.java.com.kingdomhallscheduler;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;


public class KingdomHallSchedulerApp {
    private JFrame frame;
    private List<Person> peopleList = new ArrayList<>();
    private HashMap<String, String[]> schedule = new HashMap<>();
    private JTable table;
    private LocalDate currentDate = LocalDate.now();
    private File currentFile = null;
    private JFileChooser fileChooser = new JFileChooser();
    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> peopleListDisplay = new JList<>(listModel);
    private HashMap<String, String[][]> monthlySchedules = new HashMap<>();



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new KingdomHallSchedulerApp().createAndShowGUI());
    }

    public void createAndShowGUI() {
        // Initialize the frame
        frame = new JFrame("Kingdom Hall Assignment Schedule");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Load and set the icon
        // Assuming your icon file is located in the same package as this class
        String iconPath = "java_icon_schedule.png"; // Update this to your actual icon path
        URL iconFile = getClass().getResource(iconPath);
        if (iconFile != null) {
            System.out.println("Icon found at: " + iconFile);
            ImageIcon icon = new ImageIcon(iconFile);
            frame.setIconImage(icon.getImage());  // Set the icon for the JFrame
        } else {
            System.err.println("Icon not found.");
        }


        // Create the tabbed pane and panels
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel schedulePanel = createSchedulePanel();
        tabbedPane.addTab("Schedule", schedulePanel);

        JPanel peoplePanel = createPeoplePanel();
        tabbedPane.addTab("People", peoplePanel);

        // Create and set the menu bar
        JMenuBar menuBar = createMenuBar();
        frame.setJMenuBar(menuBar);

        // Add the tabbed pane to the frame
        frame.add(tabbedPane);

        // Make the frame visible
        frame.setVisible(true);
    }


    private JPanel createSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Month selection controls
        JPanel monthPanel = new JPanel();
        JButton prevMonthButton = new JButton("Previous Month");
        JButton nextMonthButton = new JButton("Next Month");
        JLabel monthLabel = new JLabel(currentDate.getMonth().toString() + " " + currentDate.getYear(), SwingConstants.CENTER);
        prevMonthButton.addActionListener(e -> {
            saveCurrentMonthSchedule();  // Save current month's schedule before switching
            currentDate = currentDate.minusMonths(1);
            monthLabel.setText(currentDate.getMonth().toString() + " " + currentDate.getYear());
            updateScheduleTable();
        });

        nextMonthButton.addActionListener(e -> {
            saveCurrentMonthSchedule();  // Save current month's schedule before switching
            currentDate = currentDate.plusMonths(1);
            monthLabel.setText(currentDate.getMonth().toString() + " " + currentDate.getYear());
            updateScheduleTable();
        });
        monthPanel.add(prevMonthButton);
        monthPanel.add(monthLabel);
        monthPanel.add(nextMonthButton);
        panel.add(monthPanel, BorderLayout.NORTH);

        // Define the table headers (assignments)
        String[] columnNames = {"Week", "Left Microphone", "Right Microphone", "A/V", "Sound", "Stage", "Zoom", "Lobby Attendant", "Hall Attendant"};

        // Create an empty table
        Object[][] data = new Object[5][columnNames.length];  // 5 rows for weeks

        table = new JTable(data, columnNames);
        table.setCellSelectionEnabled(true);
        table.setPreferredScrollableViewportSize(new Dimension(700, 200));
        table.setFillsViewportHeight(true);

        // Add table inside a scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Add the "Auto Assign" button
        JPanel buttonPanel = new JPanel();
        JButton autoAssignButton = new JButton("Auto Assign");
        autoAssignButton.addActionListener(e -> autoAssign());
        buttonPanel.add(autoAssignButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPeoplePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Initialize listModel and peopleListDisplay if not already done
        if (listModel == null) {
            listModel = new DefaultListModel<>();
        }
        if (peopleListDisplay == null) {
            peopleListDisplay = new JList<>(listModel);
        }

        peopleListDisplay.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(peopleListDisplay), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Person");
        addButton.addActionListener(e -> addPerson(listModel));
        JButton editButton = new JButton("Edit Person");
        editButton.addActionListener(e -> editPerson(peopleListDisplay.getSelectedValue(), listModel));
        JButton deleteButton = new JButton("Delete Person");
        deleteButton.addActionListener(e -> deletePerson(peopleListDisplay.getSelectedValue(), listModel));

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }


    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> saveFile());
        fileMenu.add(saveItem);

        JMenuItem saveAsItem = new JMenuItem("Save As");
        saveAsItem.addActionListener(e -> saveFileAs());
        fileMenu.add(saveAsItem);

        JMenuItem loadItem = new JMenuItem("Load");
        loadItem.addActionListener(e -> loadFile());
        fileMenu.add(loadItem);

        JMenuItem exportCsvItem = new JMenuItem("Export to CSV");
        exportCsvItem.addActionListener(e -> exportToCSV());
        fileMenu.add(exportCsvItem);

        menuBar.add(fileMenu);

        return menuBar;
    }


    private void saveFile() {
        if (currentFile != null) {
            saveToFile(currentFile);
        } else {
            saveFileAs();  // If no file exists, trigger Save As
        }
    }

    // Save As functionality
    private void saveFileAs() {
        // Set the default file type to .sched
        fileChooser.setDialogTitle("Save As");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Schedule files", "sched"));
        int userSelection = fileChooser.showSaveDialog(frame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().endsWith(".sched")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".sched");
            }
            currentFile = fileToSave;  // Update the current file
            saveToFile(fileToSave);
        }
    }

    // Load a schedule file
    private void loadFile() {
        fileChooser.setDialogTitle("Load Schedule");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Schedule files", "sched"));
        int userSelection = fileChooser.showOpenDialog(frame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToLoad = fileChooser.getSelectedFile();
            loadFromFile(fileToLoad);
        }
    }

    // Save the schedule data to a file
    private void saveToFile(File file) {
        // Update schedule map with table data (people assigned to each role)
        for (int row = 0; row < table.getRowCount(); row++) {
            String[] assignments = new String[table.getColumnCount() - 1]; // Store assignments for one week
            for (int col = 1; col < table.getColumnCount(); col++) {
                Object cellValue = table.getValueAt(row, col);
                assignments[col - 1] = (cellValue != null) ? cellValue.toString() : "";  // Store name of assigned person or empty
            }
            String weekRange = (String) table.getValueAt(row, 0);  // Week range as the key
            schedule.put(weekRange, assignments);  // Store assignments for this week in the schedule map
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(peopleList);  // Save the people list
            oos.writeObject(schedule);    // Save the schedule (with people assignments)

            JOptionPane.showMessageDialog(frame, "Schedule and people saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error saving data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    // Load the schedule data from a file
    private void loadFromFile(File file) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            peopleList = (List<Person>) ois.readObject();  // Load the people list
            schedule = (HashMap<String, String[]>) ois.readObject();  // Load the schedule

            // Debugging: Print out the loaded data
            System.out.println("Loaded peopleList: " + peopleList);
            System.out.println("Loaded schedule: " + schedule);

            // Show success message
            JOptionPane.showMessageDialog(frame, "Schedule and people loaded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            // Update UI components
            updatePeopleListDisplay();  // Update people list display
            updateScheduleTable();  // Update schedule table

        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(frame, "Error loading data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveCurrentMonthSchedule() {
        String monthKey = getCurrentMonthKey();  // Get the key for the current month

        // Create a 2D array to hold the schedule for this month
        String[][] currentSchedule = new String[table.getRowCount()][table.getColumnCount() - 1];  // Exclude the week range column

        // Populate the 2D array with the data from the table (skip the first column)
        for (int row = 0; row < table.getRowCount(); row++) {
            for (int col = 1; col < table.getColumnCount(); col++) {  // Skip the week range column
                Object cellValue = table.getValueAt(row, col);
                currentSchedule[row][col - 1] = (cellValue != null) ? cellValue.toString() : "";  // Store the assignment or empty string
            }
        }

        // Store the current schedule in the monthlySchedules map
        monthlySchedules.put(monthKey, currentSchedule);
    }


    // Load the schedule for the current month
    private void loadMonthSchedule() {
        String monthKey = getCurrentMonthKey();
        if (monthlySchedules.containsKey(monthKey)) {
            String[][] savedSchedule = monthlySchedules.get(monthKey);
            for (int row = 0; row < table.getRowCount(); row++) {
                for (int col = 0; col < table.getColumnCount(); col++) {
                    table.setValueAt(savedSchedule[row][col], row, col);
                }
            }
        } else {
            clearSchedule();  // No schedule for this month, clear the table
        }
    }

    // Get the current month as a string key (e.g., "2024-10")
    private String getCurrentMonthKey() {
        return currentDate.getYear() + "-" + String.format("%02d", currentDate.getMonthValue());
    }

    // Switch to a different month, saving and loading schedules as needed
    private void switchMonth(LocalDate newDate) {
        saveCurrentMonthSchedule();  // Save the current month's schedule
        currentDate = newDate;  // Update to the new month
        loadMonthSchedule();  // Load the new month's schedule
    }


    private void updatePeopleListDisplay() {
        listModel.clear();  // Clear the current list

        // Populate the list model with the loaded peopleList
        for (Person person : peopleList) {
            listModel.addElement(person.getName());
        }

        // Ensure the UI is updated
        peopleListDisplay.repaint();
    }


    private void updateScheduleTable() {
        String monthKey = getCurrentMonthKey(); // Get the key for the current month

        // Calculate the first Monday of the current month
        LocalDate firstMonday = currentDate.with(TemporalAdjusters.firstInMonth(java.time.DayOfWeek.MONDAY));

        // Set the week range (first column) for the schedule
        for (int week = 0; week < 5; week++) {
            LocalDate startDate = firstMonday.plusWeeks(week);  // Monday
            LocalDate endDate = startDate.plusDays(6);          // Sunday

            // Format dates as MM/DD - MM/DD
            String weekRange = String.format("%s - %s", formatDate(startDate), formatDate(endDate));
            table.setValueAt(weekRange, week, 0);  // Set the week range in the first column
        }

        // Now, check if the saved schedule exists for the current month
        if (monthlySchedules.containsKey(monthKey)) {
            // Load the saved schedule for the current month
            String[][] savedSchedule = monthlySchedules.get(monthKey);
            for (int row = 0; row < table.getRowCount(); row++) {
                for (int col = 1; col < table.getColumnCount(); col++) { // Skip the first column (week range)
                    table.setValueAt(savedSchedule[row][col - 1], row, col); // Load the assignment data
                }
            }
        } else {
            // No schedule found for the current month, clear the rest of the table (not the first column)
            clearSchedule();
        }
    }

    private void exportToCSV() {
        // Set up the file chooser for saving CSV files
        fileChooser.setDialogTitle("Export Schedule as CSV");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV files", "csv"));
        int userSelection = fileChooser.showSaveDialog(frame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(fileToSave))) {
                // Write the column headers (role titles)
                for (int col = 0; col < table.getColumnCount(); col++) {
                    writer.print(table.getColumnName(col));
                    if (col < table.getColumnCount() - 1) {
                        writer.print(",");  // Comma between columns
                    }
                }
                writer.println();

                // Write each row of the table (schedule data)
                for (int row = 0; row < table.getRowCount(); row++) {
                    for (int col = 0; col < table.getColumnCount(); col++) {
                        Object cellValue = table.getValueAt(row, col);
                        writer.print(cellValue != null ? cellValue.toString() : "");
                        if (col < table.getColumnCount() - 1) {
                            writer.print(",");  // Comma between columns
                        }
                    }
                    writer.println();  // New line after each row
                }

                JOptionPane.showMessageDialog(frame, "Schedule successfully exported to CSV!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Error exporting to CSV: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private String formatDate(LocalDate date) {
        return String.format("%02d/%02d", date.getMonthValue(), date.getDayOfMonth());
    }

    private void autoAssign() {
        // Shuffle the people list to randomize the assignments
        List<Person> shuffledPeople = new ArrayList<>(peopleList);
        Collections.shuffle(shuffledPeople);  // Randomly shuffle the people list

        // Clear the current schedule (overwrite everything)
        clearSchedule();

        // Loop over the rows (weeks) in the schedule and assign people to roles
        for (int row = 0; row < table.getRowCount(); row++) {
            for (int col = 1; col < table.getColumnCount(); col++) {  // Skip first column (week range)
                // Try to find a valid person for this role (consider any requirements like avoiding repeats)
                for (Person person : shuffledPeople) {
                    if (isValidAssignment(person, row, col)) {  // Check if the assignment is valid (e.g., no consecutive assignments)
                        table.setValueAt(person.getName(), row, col);  // Assign person to role
                        shuffledPeople.remove(person);  // Remove person from shuffled list to avoid reassigning
                        break;  // Move to the next role after assigning someone
                    }
                }

                // If we run out of people, reshuffle and assign again (handling cases where people can be assigned more than once)
                if (shuffledPeople.isEmpty()) {
                    shuffledPeople = new ArrayList<>(peopleList);
                    Collections.shuffle(shuffledPeople);  // Re-shuffle the list for fairness
                }
            }
        }
    }

    private void clearSchedule() {
        // Loop through the table and clear all non-week-range cells (columns 1 to n)
        for (int row = 0; row < table.getRowCount(); row++) {
            for (int col = 1; col < table.getColumnCount(); col++) {
                table.setValueAt("", row, col);  // Clear cell
            }
        }
    }

    private boolean isValidAssignment(Person person, int row, int col) {
        // Example of a check to prevent assigning the same person to the same role in consecutive weeks
        if (row > 0) {  // Check if there's a previous row (week)
            String previousAssignment = (String) table.getValueAt(row - 1, col);
            if (previousAssignment != null && previousAssignment.equals(person.getName())) {
                return false;  // Don't assign the same person to the same role as in the previous week
            }
        }

        // Add more rules as necessary (e.g., specific person restrictions, etc.)
        return true;
    }

    // AddPerson, EditPerson, DeletePerson, etc. would remain the same as in your original implementation.

    private void addPerson(DefaultListModel<String> listModel) {
        JTextField nameField = new JTextField();
        JCheckBox[] skillCheckBoxes = createSkillCheckboxes();
        JTextArea notesField = new JTextArea();

        int result = JOptionPane.showConfirmDialog(null, createPersonInputPanel(nameField, skillCheckBoxes, notesField),
                "Add Person", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {

            String name = nameField.getText();
            String[] skills = getCheckedSkills(skillCheckBoxes);
            String notes = notesField.getText();
            peopleList.add(new Person(name, skills, notes));
            listModel.addElement(name);
        }
    }

    private void editPerson(String selectedPerson, DefaultListModel<String> listModel) {
        if (selectedPerson == null) {
            JOptionPane.showMessageDialog(frame, "Select a person to edit");
            return;
        }

        Person person = findPersonByName(selectedPerson);
        ;
        if (person == null) return;

        JTextField nameField = new JTextField(person.getName());
        JCheckBox[] skillCheckBoxes = createSkillCheckboxes();
        setCheckedSkills(skillCheckBoxes, person.getSkills());
        JTextArea notesField = new JTextArea(person.getNotes());

        int result = JOptionPane.showConfirmDialog(null, createPersonInputPanel(nameField, skillCheckBoxes, notesField),
                "Edit Person", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {

            person.setName(nameField.getText());
            person.setSkills(getCheckedSkills(skillCheckBoxes));
            person.setNotes(notesField.getText());
            listModel.set(listModel.indexOf(selectedPerson), person.getName());
        }
    }

    private void deletePerson(String selectedPerson, DefaultListModel<String> listModel) {
        if (selectedPerson != null) {
            peopleList.removeIf(person -> person.getName().equals(selectedPerson));
            listModel.removeElement(selectedPerson);
        }
    }

        private JPanel createPersonInputPanel (JTextField nameField, JCheckBox[]skillCheckboxes, JTextArea notesField){
            JPanel panel = new JPanel(new GridLayout(10, 1));
            panel.add(new JLabel("Name:"));
            panel.add(nameField);
            panel.add(new JLabel("Trained on:"));
            for (JCheckBox box : skillCheckboxes) panel.add(box);
            panel.add(new JLabel("Notes:"));
            panel.add(new JScrollPane(notesField));
            return panel;
        }

        private JCheckBox[] createSkillCheckboxes () {
            String[] skills = {"Left Microphone", "Right Microphone", "A/V", "Sound", "Stage", "Zoom", "Hall Attendant", "Lobby Attendant"};
            JCheckBox[] boxes = new JCheckBox[skills.length];
            for (int i = 0; i < skills.length; i++) {
                boxes[i] = new JCheckBox(skills[i]);
            }
            return boxes;
        }

        private String[] getCheckedSkills (JCheckBox[]boxes){
            List<String> skills = new ArrayList<>();
            for (JCheckBox box : boxes) {
                if (box.isSelected()) skills.add(box.getText());
            }
            return skills.toArray(new String[0]);
        }

        private void setCheckedSkills (JCheckBox[]boxes, String[]skills){
            for (JCheckBox box : boxes) {
                for (String skill : skills) {
                    if (box.getText().equals(skill)) {
                        box.setSelected(true);
                    }
                }
            }
        }

        private Person findPersonByName (String name){
            for (Person person : peopleList) {
                if (person.getName().equals(name)) return person;
            }
            return null;
        }


    }


