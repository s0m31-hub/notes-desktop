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
                return  new SimpleDateFormat("(dd) hh:mm:ss").format (((Date) meta).getTime());
            }
        };
        dateValue.setMeta(new Date());
        Template loggingTemplate = new Template().setName("logger").setPrefix("[{time}] ").addVariable(new Variable("{time}", dateValue)).setPostfix("\n");
        cli.addTemplate(loggingTemplate);
        File configDir = new File(System.getProperty("user.home") + "/.config/");
        if(!configDir.isDirectory()) {
            cli.print("Users config folder is missing! Creating new one. Are you a windows user? >:(");
            configDir.mkdirs();
        }
        configDir = new File(System.getProperty("user.home") + "/.config/nwolfhubNotes/");
        if(!configDir.exists()) {
            cli.print("Creating notes config dir");
            configDir.mkdirs();
        }
        File config = new File(System.getProperty("user.home") + "/.config/nwolfhubNotes/config.cfg");
        if(!config.exists()) {
            cli.print("Creating config file at " + config.getAbsolutePath());
            config.createNewFile();
            String baseContent = "notes_location=" + System.getProperty("user.home") + "/.notes\n" +
                    "server=https://notes.nwolfhub.org";
            try(FileOutputStream outputStream = new FileOutputStream(config)) {
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
        if(!notesDir.exists()) {
            cli.print("Creating notes directory");
            notesDir.mkdirs();
        }
        HashMap<String, Note> notes = new HashMap<>();
        for(File noteFile: Objects.requireNonNull(notesDir.listFiles())) {
            if(noteFile.getName().matches(".*\\.note$")) {
                try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(noteFile))) {
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
        for(Note note:notes.values()) {
            JLabel noteLabel = new JLabel(note.getName());
            noteLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if(e.getButton()==MouseEvent.BUTTON1) {
                        if(edited) {

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
        if(!online) {
            File noteFile = new File(configurator.getValue("notes_location") + "/" + name + ".note");
            if(!noteFile.exists()) {
                try {
                    noteFile.createNewFile();
                    try(ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(noteFile))) {
                        outputStream.writeObject(new Note(name, text, false));
                    }
                    if(edited) {
                        File prevNoteFile = new File(configurator.getValue("notes_location") + "/" + prevNote + ".note");
                        if(prevNoteFile.exists()) {
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
        for(Note note:notes.values()) {
            JLabel noteLabel = new JLabel(note.getName());
            noteLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if(e.getButton()==MouseEvent.BUTTON1) {
                        if(edited) {

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
}