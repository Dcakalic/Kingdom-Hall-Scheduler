package main.java.com.kingdomhallscheduler;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class KingdomHallSchedulerApp {


    private JFrame frame;

    private List<Person> peopleList = new ArrayList<>();
    private HashMap<String, String[]> schedule = new HashMap<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new main.java.com.kingdomhallscheduler.KingdomHallSchedulerApp().createAndShowGUI());
    }

    public void createAndShowGUI() {
        frame = new JFrame("St.Peters Kingdom hall Assignment Schedule");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800,600);

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel schedulePanel = createSchedulePanel();
        tabbedPane.addTab("Schedule", schedulePanel);

        JPanel peoplePanel = createPeoplePanel();
        tabbedPane.addTab("People", peoplePanel);

        frame.add(tabbedPane);
        frame.setVisible(true);
    }

    private JPanel createSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel scheduleLabel = new JLabel("Week 10/21 - 10/27", SwingConstants.CENTER);
        panel.add(scheduleLabel, BorderLayout.NORTH);

        JTextArea scheduleArea = new JTextArea();
        panel.add(new JScrollPane(scheduleArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton autoAssignButton = new JButton("Auto Assign");
        autoAssignButton.addActionListener(e -> autoAssign(scheduleArea));
        buttonPanel.add(autoAssignButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPeoplePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        //List of people
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> peopleListDisplay = new JList<>(listModel);
        peopleListDisplay.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(peopleListDisplay), BorderLayout.CENTER);

        //Buttons for adding, editing, and deleting people

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

    private void addPerson(DefaultListModel<String> listModel) {
        JTextField nameField = new JTextField();
        JCheckBox[] skillCheckBoxes = createSkillCheckboxes();
        JTextArea notesField = new JTextArea();

        int result = JOptionPane.showConfirmDialog(null, createPersonInputPanel(nameField, skillCheckBoxes, notesField),
                "Add Person", JOptionPane.OK_CANCEL_OPTION);
        if(result == JOptionPane.OK_OPTION) {

            String name = nameField.getText();
            String[] skills = getCheckedSkills(skillCheckBoxes);
            String notes = notesField.getText();
            peopleList.add(new  Person(name, skills, notes));
            listModel.addElement(name);
        }
    }

    private void editPerson(String selectedPerson, DefaultListModel<String> listModel) {
        if (selectedPerson == null) {
            JOptionPane.showMessageDialog(frame, "Select a person to edit");
            return;
        }

        Person person = findPersonByName(selectedPerson);;
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

    private void autoAssign(JTextArea scheduleArea) {
        scheduleArea.setText("");  //clear previous schedule
        for (Person person : peopleList) {
            if (person.isTrainedIn("Left Microphone")) {
                scheduleArea.append(person.getName() + " - Left Microphone\n");
            }
            if (person.isTrainedIn("Right Microphone")) {
                scheduleArea.append(person.getName() + "- Right Microphone\n");
            }
            if (person.isTrainedIn("A/V")) {
                scheduleArea.append(person.getName() + "- A/V\n");
            }
            if (person.isTrainedIn("Sound")) {
                scheduleArea.append(person.getName() + "- Sound\n");
            }
            if (person.isTrainedIn("Stage")) {
                scheduleArea.append(person.getName() + "- Stage\n");
            }
            if (person.isTrainedIn("Zoom")) {
                scheduleArea.append(person.getName() + "- Zoom\n");
            }
            if (person.isTrainedIn("Lobby Attendant")) {
                scheduleArea.append(person.getName() + "- Lobby Attendant\n");
            }
            if (person.isTrainedIn("Hall Attendant")) {
                scheduleArea.append(person.getName() + "- Hall Attendant\n");
            }
        }
    }

    private JPanel createPersonInputPanel(JTextField nameField, JCheckBox[] skillCheckboxes, JTextArea notesField) {
        JPanel panel = new JPanel(new GridLayout(10,1));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Trained on:"));
        for (JCheckBox box : skillCheckboxes) panel.add(box);
        panel.add(new JLabel("Notes:"));
        panel.add(new JScrollPane(notesField));
        return panel;
    }

    private JCheckBox[] createSkillCheckboxes() {
        String[] skills = {"Left Microphone", "Right Microphone", "A/V", "Sound", "Stage", "Zoom", "Hall Attendant", "Lobby Attendant"};
        JCheckBox[] boxes = new JCheckBox[skills.length];
        for (int i = 0; i < skills.length; i++) {
            boxes[i] = new JCheckBox(skills[i]);
        }
        return boxes;
    }

    private String[] getCheckedSkills(JCheckBox[] boxes) {
        List<String> skills = new ArrayList<>();
        for (JCheckBox box : boxes) {
            if (box.isSelected()) skills.add(box.getText());
        }
        return skills.toArray(new String[0]);
    }

    private void setCheckedSkills(JCheckBox[] boxes, String[] skills) {
        for (JCheckBox box : boxes) {
            for (String skill : skills) {
                if (box.getText().equals(skill)) {
                    box.setSelected(true);
                }
            }
        }
    }

    private Person findPersonByName(String name) {
        for (Person person : peopleList) {
            if (person.getName().equals(name)) return person;
        }
        return null;
    }



}
File iconFile = new File("C:/Users/Dylkh/Desktop/KingdomhallScheduler/src/main/resources/icon/java_icon_schedule.png");
