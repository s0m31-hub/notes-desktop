package org.nwolfhub;

import org.nwolfhub.easycli.Defaults;
import org.nwolfhub.easycli.EasyCLI;
import org.nwolfhub.easycli.model.FlexableValue;
import org.nwolfhub.easycli.model.InputTask;
import org.nwolfhub.easycli.model.Template;
import org.nwolfhub.easycli.model.Variable;
import org.nwolfhub.utils.Configurator;
import org.nwolfhub.utils.Utils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private JPanel notesWindow;
    private JList notesList;
    private JLabel webStatus;
    private JPanel content;
    private JButton switchWebState;
    private JTextField noteName;
    private JTextPane noteText;
    private JButton newNote;
    private static Main main;

    public static boolean edited = false;
    public static String prevNote = "";

    public static EasyCLI cli;
    public static Configurator configurator;

    public static void main(String[] args) throws IOException {
        cli = new EasyCLI();
        FlexableValue dateValue = new FlexableValue() {
            @Override
            public String call() {
                return new SimpleDateFormat("(dd) hh:mm:ss").format(((Date) meta).getTime());
            }
        };
        dateValue.setMeta(new Date());
        Template loggingTemplate = new Template().setName("logger").setPrefix("[{time}] ").addVariable(new Variable("{time}", dateValue)).setPostfix("\n");
        cli.addTemplate(loggingTemplate);
        File configDir = new File(System.getProperty("user.home") + "/.config/");
        if (!configDir.isDirectory()) {
            cli.print("Users config folder is missing! Creating new one. Are you a windows user? >:(");
            configDir.mkdirs();
        }
        configDir = new File(System.getProperty("user.home") + "/.config/nwolfhubNotes/");
        if (!configDir.exists()) {
            cli.print("Creating notes config dir");
            configDir.mkdirs();
        }
        File config = new File(System.getProperty("user.home") + "/.config/nwolfhubNotes/config.cfg");
        if (!config.exists()) {
            cli.print("Creating config file at " + config.getAbsolutePath());
            config.createNewFile();
            String baseContent = "notes_location=" + System.getProperty("user.home") + "/.notes\n" +
                    "server=https://notes.nwolfhub.org";
            try (FileOutputStream outputStream = new FileOutputStream(config)) {
                outputStream.write(baseContent.getBytes(StandardCharsets.UTF_8));
            }
        }
        cli.print("Reading config file");
        configurator = new Configurator(config);
        cli.print("Creating main window");
        main = new Main();
    }

    public Main() {
        JFrame frame = new JFrame();
        frame.setTitle("Nwolfhub notes");
        frame.setContentPane(notesWindow);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 550);
        frame.setVisible(true);
        cli.print("Reading notes");
        File notesDir = new File(configurator.getValue("notes_location"));
        if (!notesDir.exists()) {
            cli.print("Creating notes directory");
            notesDir.mkdirs();
        }
        HashMap<String, Note> notes = new HashMap<>();
        for (File noteFile : Objects.requireNonNull(notesDir.listFiles())) {
            if (noteFile.getName().matches(".*\\.note$")) {
                try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(noteFile))) {
                    Note note = (Note) in.readObject();
                    notes.put(note.getName(), note);
                } catch (IOException e) {
                    cli.print("Failed to read note " + noteFile.getAbsolutePath());
                } catch (ClassNotFoundException e) {
                    cli.print("Note file is corrupted: " + noteFile.getAbsolutePath());
                }
            }
        }
        cli.print("Finished reading notes");
        for (Note note : notes.values()) {
            JLabel noteLabel = new JLabel(note.getName());
            noteLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (edited) {

                            notes.put(prevNote, new Note(prevNote, noteText.getText(), false));
                        } else {
                            if (notes.get(noteLabel.getText()).isOnline) {
                                // TODO: 17.07.2023 online
                            } else {
                                prevNote = note.getName();
                                noteName.setEnabled(true);
                                noteName.setText(note.getName());
                                noteText.setEnabled(true);
                                noteText.setText(note.getContent());
                            }
                        }
                    }
                }
            });
            notesList.add(noteLabel);
        }
    }

    private void initNameTextField(JTextField field) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {

            }

            @Override
            public void removeUpdate(DocumentEvent e) {

            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                edited = true;
            }
        });
    }

    public void saveNote(String name, String text, boolean online) {
        if (!online) {
            File noteFile = new File(configurator.getValue("notes_location") + "/" + name + ".note");
            if (!noteFile.exists()) {
                try {
                    noteFile.createNewFile();
                    try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(noteFile))) {
                        outputStream.writeObject(new Note(name, text, false));
                    }
                    if (edited) {
                        File prevNoteFile = new File(configurator.getValue("notes_location") + "/" + prevNote + ".note");
                        if (prevNoteFile.exists()) {
                            prevNoteFile.delete();
                            cli.print("Removed note with previous name");
                        }
                    }
                    edited = false;
                    cli.print("Saved note");
                } catch (IOException e) {
                    cli.print("Failed to save note! " + Arrays.stream(e.getStackTrace()).collect(Collectors.toList()));
                    JOptionPane.showConfirmDialog(main.content, "Could not save note!");
                }
            }
        }
    }

    private void rebuildNotesList(HashMap<String, Note> notes) {
        for (Note note : notes.values()) {
            JLabel noteLabel = new JLabel(note.getName());
            noteLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (edited) {

                            notes.put(prevNote, new Note(prevNote, noteText.getText(), false));
                        } else {
                            if (notes.get(noteLabel.getText()).isOnline) {
                                // TODO: 17.07.2023 online
                            } else {
                                prevNote = note.getName();
                                noteName.setEnabled(true);
                                noteName.setText(note.getName());
                                noteText.setEnabled(true);
                                noteText.setText(note.getContent());
                            }
                        }
                    }
                }
            });
            notesList.add(noteLabel);
        }
    }












    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        notesWindow = new JPanel();
        notesWindow.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 5, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        notesWindow.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 2, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        notesList = new JList();
        notesList.setEnabled(true);
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        notesList.setModel(defaultListModel1);
        panel1.add(notesList, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        notesWindow.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_SOUTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        content = new JPanel();
        content.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        notesWindow.add(content, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        noteName = new JTextField();
        noteName.setEnabled(false);
        content.add(noteName, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        noteText = new JTextPane();
        noteText.setEnabled(false);
        noteText.setText("");
        content.add(noteText, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 570), null, 0, false));
        newNote = new JButton();
        newNote.setText("New note");
        notesWindow.add(newNote, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        switchWebState = new JButton();
        switchWebState.setText("Login");
        notesWindow.add(switchWebState, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        webStatus = new JLabel();
        webStatus.setText("OFFLINE");
        notesWindow.add(webStatus, new com.intellij.uiDesigner.core.GridConstraints(0, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return notesWindow;
    }
}