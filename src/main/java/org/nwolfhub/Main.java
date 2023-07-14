package org.nwolfhub;

import javax.swing.*;

public class Main {
    private JPanel notesWindow;
    private JList notesList;
    private JLabel webStatus;
    private JPanel content;
    private JButton switchWebState;
    private JTextField noteName;
    private JTextPane noteText;
    private JButton newNote;

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        JFrame frame = new JFrame();
        frame.setTitle("Nwolfhub notes");
        frame.setContentPane(notesWindow);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 550);
        frame.setVisible(true);
    }
}